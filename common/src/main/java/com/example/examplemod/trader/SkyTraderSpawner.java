package com.example.examplemod.trader;

import java.util.Optional;

import com.example.examplemod.entity.ModEntities;
import com.example.examplemod.entity.SkyTrader;
import com.example.examplemod.entity.SkyTraderGhast;
import com.example.examplemod.mixin.ServerLevelAccessorMixin;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.SpawnPlacementType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.CustomSpawner;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.storage.SavedDataStorage;
import org.jspecify.annotations.Nullable;

public class SkyTraderSpawner implements CustomSpawner {
    private static final int DEFAULT_TICK_DELAY = 60;
    public static final int DEFAULT_SPAWN_DELAY = 61;
    public static final int MIN_SPAWN_CHANCE = 75;
    private static final int MAX_SPAWN_CHANCE = 75;
    private static final int SPAWN_CHANCE_INCREASE = 25;
    private static final int SPAWN_ONE_IN_X_CHANCE = 1;
    private static final int NUMBER_OF_SPAWN_ATTEMPTS = 10;
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
        ((ServerLevelAccessorMixin)level).getCustomSpawners().forEach(customSpawner -> {
            if (customSpawner instanceof SkyTraderSpawner spawner) {
                spawner.spawn(level);
                return;
            }
        });
    }

    @Override
    public void tick(ServerLevel level, boolean spawnEnemies) {
        if (level.getGameRules().get(GameRules.SPAWN_WANDERING_TRADERS)) {
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
                        if (this.spawn(level)) {
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

    private boolean spawn(ServerLevel level) {
        Player player = level.getRandomPlayer();
        if (player == null) {
            return true;
        }

        if (this.random.nextInt(SPAWN_ONE_IN_X_CHANCE) != 0) {
            return false;
        }

        BlockPos playerPos = player.blockPosition();
        int radius = 48;
        PoiManager poiManager = level.getPoiManager();
        Optional<BlockPos> poiPos = poiManager.find(p -> p.is(PoiTypes.MEETING), p -> true, playerPos, radius, PoiManager.Occupancy.ANY);
        BlockPos referencePos = poiPos.orElse(playerPos);
        BlockPos spawnPosition = this.findSpawnPositionNear(level, referencePos, radius);
        if (spawnPosition != null && this.hasEnoughSpace(level, spawnPosition)) {
            if (level.getBiome(spawnPosition).is(BiomeTags.WITHOUT_WANDERING_TRADER_SPAWNS)) {
                return false;
            }

            SkyTrader trader = ModEntities.SKY_TRADER.spawn(level, spawnPosition, EntitySpawnReason.EVENT);
            if (trader != null) {
                this.tryToSpawnGhastFor(level, trader, 8);

                trader.setDespawnDelay(48000);
                trader.setWanderTarget(referencePos);
                trader.setHomeTo(referencePos, 16);
                return true;
            }
        }

        return false;
    }

    private void tryToSpawnGhastFor(ServerLevel level, SkyTrader trader, int radius) {
        BlockPos spawnPosition = this.findSpawnPositionNear(level, trader.blockPosition(), radius);
        if (spawnPosition != null) {
            SkyTraderGhast ghast = ModEntities.SKY_TRADER_GHAST.spawn(level, spawnPosition, EntitySpawnReason.EVENT);
            if (ghast != null) {
                ghast.setOwner(trader);
                ghast.setLeashedTo(trader, true);
                ghast.equipItemIfPossible(level, new ItemStack(Items.HARNESS.white()));
            }
        }
    }

    private @Nullable BlockPos findSpawnPositionNear(LevelReader level, BlockPos referencePosition, int radius) {
        BlockPos spawnPosition = null;
        SpawnPlacementType wanderingTraderSpawnType = SpawnPlacements.getPlacementType(ModEntities.SKY_TRADER);

        for (int i = 0; i < NUMBER_OF_SPAWN_ATTEMPTS; i++) {
            int xPosition = referencePosition.getX() + this.random.nextInt(radius * 2) - radius;
            int zPosition = referencePosition.getZ() + this.random.nextInt(radius * 2) - radius;
            int yPosition = level.getHeight(SpawnPlacements.getHeightmapType(ModEntities.SKY_TRADER), xPosition, zPosition);
            BlockPos spawnPos = new BlockPos(xPosition, yPosition, zPosition);
            if (wanderingTraderSpawnType.isSpawnPositionOk(level, spawnPos, ModEntities.SKY_TRADER)) {
                spawnPosition = spawnPos;
                break;
            }
        }

        return spawnPosition;
    }

    private boolean hasEnoughSpace(BlockGetter level, BlockPos spawnPos) {
        for (BlockPos pos : BlockPos.betweenClosed(spawnPos, spawnPos.offset(1, 2, 1))) {
            if (!level.getBlockState(pos).getCollisionShape(level, pos).isEmpty()) {
                return false;
            }
        }

        return true;
    }
}
