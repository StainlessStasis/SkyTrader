package io.github.stainlessstasis.skytrader.trader;

import io.github.stainlessstasis.skytrader.ModConstants;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SkyTraderSpawnerData extends SavedData {

    public record PendingSpawn(UUID playerUUID, int ticksRemaining) {
        public static final Codec<PendingSpawn> CODEC = RecordCodecBuilder.create(
                i -> i.group(
                        UUIDUtil.STRING_CODEC.fieldOf("player").forGetter(PendingSpawn::playerUUID),
                        Codec.INT.fieldOf("ticks").forGetter(PendingSpawn::ticksRemaining)
                ).apply(i, PendingSpawn::new)
        );
    }

    public static final Codec<SkyTraderSpawnerData> CODEC = RecordCodecBuilder.create(
            i -> i.group(
                            Codec.INT.optionalFieldOf("spawn_delay", 1200).forGetter(data -> data.spawnDelay),
                            Codec.INT.optionalFieldOf("spawn_chance", 25).forGetter(data -> data.spawnChance),
                            PendingSpawn.CODEC.listOf().optionalFieldOf("pending_spawns", List.of()).forGetter(data -> data.pendingSpawns)
                    )
                    .apply(i, SkyTraderSpawnerData::new)
    );

    public static final SavedDataType<SkyTraderSpawnerData> TYPE = new SavedDataType<>(
            ModConstants.id("sky_trader"), SkyTraderSpawnerData::new, CODEC, DataFixTypes.SAVED_DATA_WANDERING_TRADER
    );

    private int spawnDelay;
    private int spawnChance;
    private final List<PendingSpawn> pendingSpawns;

    public SkyTraderSpawnerData() {
        this(1200, 25, new ArrayList<>());
    }

    public SkyTraderSpawnerData(int spawnDelay, int spawnChance, List<PendingSpawn> pendingSpawns) {
        this.spawnDelay = spawnDelay;
        this.spawnChance = spawnChance;
        this.pendingSpawns = new ArrayList<>(pendingSpawns);
    }

    public List<PendingSpawn> getPendingSpawns() {
        return this.pendingSpawns;
    }

    public void addPendingSpawn(UUID playerUUID, int delayTicks) {
        this.pendingSpawns.add(new PendingSpawn(playerUUID, delayTicks));
        this.setDirty(true);
    }

    public int spawnDelay() {
        return this.spawnDelay;
    }

    public void setSpawnDelay(int spawnDelay) {
        if (this.spawnDelay != spawnDelay) {
            this.spawnDelay = spawnDelay;
            this.setDirty(true);
        }
    }

    public int spawnChance() {
        return this.spawnChance;
    }

    public void setSpawnChance(int spawnChance) {
        if (this.spawnChance != spawnChance) {
            this.spawnChance = spawnChance;
            this.setDirty(true);
        }
    }
}