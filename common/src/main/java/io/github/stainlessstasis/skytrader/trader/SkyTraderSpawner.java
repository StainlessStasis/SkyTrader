package io.github.stainlessstasis.skytrader.trader;

import java.util.Optional;

import io.github.stainlessstasis.ModGameRules;
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
    private static final int DEFAULT_TICK_DELAY = 1200;
    public static final int DEFAULT_SPAWN_DELAY = 24000;
    public static final int MIN_SPAWN_CHANCE = 25;
    private static final int MAX_SPAWN_CHANCE = 75;
    private static final int SPAWN_CHANCE_INCREASE = 25;
    private static final int SPAWN_ONE_IN_X_CHANCE = 1;
    private static final int NUMBER_OF_SPAWN_ATTEMPTS = 10;
    private static final int GHAST_HORIZONTAL_CLEARANCE = 2;
    private static final int GHAST_VERTICAL_CLEARANCE = 4;
    private static final int GHAST_UPWARD_SEARCH_LIMIT = 32;
    private static final int SPAWN_ALTITUDE = 60;
    private static final int SEARCH_RADIUS = 48;

    private final RandomSource random = RandomSource.create();
    private final SavedDataStorage savedDataStorage;
    private int tickDelay;
    private @Nullable SkyTraderData traderData;

    public SkyTraderSpawner(SavedDataStorage savedDataStorage) {
        this.savedDataStorage = savedDataStorage;
        this.tickDelay = DEFAULT_TICK_DELAY;
        this.traderData = null;
    }

    public static void forceSpawn(ServerLevel level) {
        ((ServerLevelAccessorMixin) level).getCustomSpawners().forEach(customSpawner -> {
            if (customSpawner instanceof SkyTraderSpawner spawner) {
                spawner.spawn(level, true);
            }
        });
    }

    @Override
    public void tick(ServerLevel level, boolean spawnEnemies) {
        if (level.getGameRules().get(ModGameRules.SPAWN_SKY_TRADERS.get())) {
            if (--this.tickDelay <= 0) {
                this.tickDelay = DEFAULT_TICK_DELAY;
                SkyTraderData data = this.getTraderData();
                int spawnDelay = data.spawnDelay() - DEFAULT_TICK_DELAY;
                data.setSpawnDelay(spawnDelay);
                if (spawnDelay <= 0) {
                    data.setSpawnDelay(DEFAULT_SPAWN_DELAY);
                    int chanceToSpawn = data.spawnChance();
                    int newSpawnChance = Mth.clamp(chanceToSpawn + SPAWN_CHANCE_INCREASE, MIN_SPAWN_CHANCE, MAX_SPAWN_CHANCE);
                    data.setSpawnChance(newSpawnChance);
                    if (this.random.nextInt(100) <= chanceToSpawn) {
                        if (this.spawn(level, false)) {
                            data.setSpawnChance(MIN_SPAWN_CHANCE);
                        }
                    }
                }
            }
        }
    }

    private SkyTraderData getTraderData() {
        if (this.traderData == null) {
            this.traderData = this.savedDataStorage.computeIfAbsent(SkyTraderData.TYPE);
        }
        return this.traderData;
    }

    private boolean spawn(ServerLevel level, boolean force) {
        Player player = level.getRandomPlayer();
        if (player == null) {
            return true;
        }

        if (!force && this.random.nextInt(SPAWN_ONE_IN_X_CHANCE) != 0) {
            return false;
        }

        BlockPos playerPos = player.blockPosition();
        PoiManager poiManager = level.getPoiManager();
        Optional<BlockPos> poiPos = poiManager.find(p -> p.is(PoiTypes.MEETING), p -> true, playerPos, SEARCH_RADIUS, PoiManager.Occupancy.ANY);
        BlockPos groundReference = poiPos.orElse(playerPos);

        if (level.getBiome(groundReference).is(BiomeTags.WITHOUT_WANDERING_TRADER_SPAWNS)) {
            return false;
        }

        BlockPos skySpawnPos = findClearSkySpawnPosition(level, groundReference, SEARCH_RADIUS);
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

        trader.setDespawnTicks(48000);

        return true;
    }

    private @Nullable BlockPos findClearSkySpawnPosition(LevelReader level, BlockPos groundReference, int radius) {
        for (int i = 0; i < NUMBER_OF_SPAWN_ATTEMPTS; i++) {
            int x = groundReference.getX() + this.random.nextInt(radius * 2) - radius;
            int z = groundReference.getZ() + this.random.nextInt(radius * 2) - radius;
            int groundY = level.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z);
            int targetY = Math.min(groundY + SPAWN_ALTITUDE, level.getMaxY() - GHAST_VERTICAL_CLEARANCE - 1);
            BlockPos candidate = findClearGhastPositionFrom(level, x, targetY, z);
            if (candidate != null) {
                return candidate;
            }
        }
        return null;
    }

    private @Nullable BlockPos findClearGhastPositionFrom(LevelReader level, int x, int startY, int z) {
        int maxY = Math.min(startY + GHAST_UPWARD_SEARCH_LIMIT, level.getMaxY());
        for (int y = startY; y <= maxY; y++) {
            BlockPos candidate = new BlockPos(x, y, z);
            if (hasEnoughSpaceForGhast(level, candidate)) {
                return candidate;
            }
        }
        return null;
    }

    private boolean hasEnoughSpaceForGhast(BlockGetter level, BlockPos center) {
        for (BlockPos pos : BlockPos.betweenClosed(
                center.offset(-GHAST_HORIZONTAL_CLEARANCE, 0, -GHAST_HORIZONTAL_CLEARANCE),
                center.offset(GHAST_HORIZONTAL_CLEARANCE, GHAST_VERTICAL_CLEARANCE, GHAST_HORIZONTAL_CLEARANCE))) {
            if (!level.getBlockState(pos).getCollisionShape(level, pos).isEmpty()) {
                return false;
            }
        }
        return true;
    }
}