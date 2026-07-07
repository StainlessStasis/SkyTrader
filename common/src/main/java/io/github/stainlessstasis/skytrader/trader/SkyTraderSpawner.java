package io.github.stainlessstasis.skytrader.trader;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import io.github.stainlessstasis.skytrader.ModGameRules;
import io.github.stainlessstasis.skytrader.entity.ModEntities;
import io.github.stainlessstasis.skytrader.entity.SkyTrader;
import io.github.stainlessstasis.skytrader.entity.SkyTraderGhast;
import io.github.stainlessstasis.skytrader.mixin.ServerLevelAccessorMixin;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.CustomSpawner;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.storage.SavedDataStorage;
import org.jspecify.annotations.Nullable;

public class SkyTraderSpawner implements CustomSpawner {
    private final RandomSource random = RandomSource.create();
    private final SavedDataStorage savedDataStorage;
    private int tickDelay;
    private @Nullable SkyTraderSpawnerData traderData;

    public SkyTraderSpawner(SavedDataStorage savedDataStorage) {
        this.savedDataStorage = savedDataStorage;
        this.tickDelay = SkyTraderConfig.get().spawning.tickDelay;
        this.traderData = null;
    }

    public static void forceSpawn(@Nullable Player player, ServerLevel level) {
        ((ServerLevelAccessorMixin) level).getCustomSpawners().forEach(customSpawner -> {
            if (player != null && customSpawner instanceof SkyTraderSpawner spawner) {
                spawner.getTraderData().addPendingSpawn(player.getUUID(), 100);
            }
        });
    }

    @Override
    public void tick(ServerLevel level, boolean spawnEnemies) {
        var config = SkyTraderConfig.get().spawning;
        SkyTraderSpawnerData data = getTraderData();

        // pending spawns from Skyflare items
        List<SkyTraderSpawnerData.PendingSpawn> pending = data.getPendingSpawns();
        if (!pending.isEmpty()) {
            List<SkyTraderSpawnerData.PendingSpawn> updatedList = new ArrayList<>();

            for (SkyTraderSpawnerData.PendingSpawn spawn : pending) {
                int nextTicks = spawn.ticksRemaining() - 1;

                if (nextTicks <= 0) {
                    Player targetPlayer = level.getPlayerByUUID(spawn.playerUUID());
                    spawn(level, targetPlayer, true);
                } else {
                    updatedList.add(new SkyTraderSpawnerData.PendingSpawn(spawn.playerUUID(), nextTicks));
                }
            }

            pending.clear();
            pending.addAll(updatedList);
            data.setDirty();
        }

        // natural spawns
        if (level.getGameRules().get(ModGameRules.SPAWN_SKY_TRADERS.get())) {
            if (--this.tickDelay <= 0) {
                this.tickDelay = config.tickDelay;
                int spawnDelay = data.spawnDelay() - config.tickDelay;
                data.setSpawnDelay(spawnDelay);
                if (spawnDelay <= 0) {
                    data.setSpawnDelay(config.spawnDelayTicks);
                    int chanceToSpawn = data.spawnChance();
                    int newSpawnChance = Mth.clamp(chanceToSpawn + config.spawnChanceIncrease, config.minSpawnChance, config.maxSpawnChance);
                    data.setSpawnChance(newSpawnChance);
                    if (this.random.nextInt(100) <= chanceToSpawn) {
                        if (this.spawn(level, null, false)) {
                            data.setSpawnChance(config.minSpawnChance);
                        }
                    }
                }
            }
        }
    }

    private SkyTraderSpawnerData getTraderData() {
        if (this.traderData == null) {
            this.traderData = this.savedDataStorage.computeIfAbsent(SkyTraderSpawnerData.TYPE);
        }
        return this.traderData;
    }

    private boolean spawn(ServerLevel level, @Nullable Player player, boolean force) {
        var spawning = SkyTraderConfig.get().spawning;

        if (player == null) player = level.getRandomPlayer();
        if (player == null) {
            return true;
        }

        if (!force && this.random.nextInt(spawning.spawnOneInXChance) != 0) {
            return false;
        }

        BlockPos playerPos = player.blockPosition();
        PoiManager poiManager = level.getPoiManager();
        Optional<BlockPos> poiPos = poiManager.find(p -> p.is(PoiTypes.MEETING), p -> true, playerPos, spawning.searchRadius, PoiManager.Occupancy.ANY);
        BlockPos groundReference = poiPos.orElse(playerPos);

        if (level.getBiome(groundReference).is(BiomeTags.WITHOUT_WANDERING_TRADER_SPAWNS)) {
            return false;
        }

        BlockPos skySpawnPos = findClearSkySpawnPosition(level, groundReference, spawning.searchRadius);
        if (skySpawnPos == null) {
            return false;
        }

        SkyTrader trader = ModEntities.SKY_TRADER.spawn(level, skySpawnPos, EntitySpawnReason.EVENT);
        if (trader == null) {
            return false;
        }
        trader.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 100));

        SkyTraderGhast ghast = ModEntities.SKY_TRADER_GHAST.spawn(level, skySpawnPos, EntitySpawnReason.EVENT);
        if (ghast == null) {
            trader.discard();
            return false;
        }

        ghast.setOwner(trader);
        trader.setGhast(ghast);
        ghast.equipItemIfPossible(level, new ItemStack(Items.HARNESS.white()));
        ghast.setOwnerRiding();
        ghast.beginSpawnDescent(groundReference);

        trader.setDespawnTicks(spawning.despawnTicks);

        return true;
    }

    private @Nullable BlockPos findClearSkySpawnPosition(LevelReader level, BlockPos groundReference, int radius) {
        var spawning = SkyTraderConfig.get().spawning;
        for (int i = 0; i < spawning.numberOfSpawnAttempts; i++) {
            int x = groundReference.getX() + this.random.nextInt(radius * 2) - radius;
            int z = groundReference.getZ() + this.random.nextInt(radius * 2) - radius;
            int groundY = level.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z);
            int targetY = Math.min(groundY + spawning.spawnAltitude, level.getMaxY() - spawning.ghastVerticalClearance - 1);
            BlockPos candidate = findClearGhastPositionFrom(level, x, targetY, z);
            if (candidate != null) {
                return candidate;
            }
        }
        return null;
    }

    private @Nullable BlockPos findClearGhastPositionFrom(LevelReader level, int x, int startY, int z) {
        int maxY = Math.min(startY + SkyTraderConfig.get().spawning.ghastUpwardSearchLimit, level.getMaxY());
        for (int y = startY; y <= maxY; y++) {
            BlockPos candidate = new BlockPos(x, y, z);
            if (hasEnoughSpaceForGhast(level, candidate)) {
                return candidate;
            }
        }
        return null;
    }

    private boolean hasEnoughSpaceForGhast(BlockGetter level, BlockPos center) {
        int clearance = SkyTraderConfig.get().spawning.ghastHorizontalClearance;
        int vertical = SkyTraderConfig.get().spawning.ghastVerticalClearance;
        for (BlockPos pos : BlockPos.betweenClosed(
                center.offset(-clearance, 0, -clearance),
                center.offset(clearance, vertical, clearance))) {
            if (!level.getBlockState(pos).getCollisionShape(level, pos).isEmpty()) {
                return false;
            }
        }
        return true;
    }
}