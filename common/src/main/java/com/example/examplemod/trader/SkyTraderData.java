package com.example.examplemod.trader;

import com.example.examplemod.ModConstants;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

public class SkyTraderData extends SavedData {
    public static final Codec<SkyTraderData> CODEC = RecordCodecBuilder.create(
            i -> i.group(
                            Codec.INT.optionalFieldOf("spawn_delay", SkyTraderSpawner.DEFAULT_SPAWN_DELAY).forGetter(data -> data.spawnDelay),
                            Codec.INT.optionalFieldOf("spawn_chance", SkyTraderSpawner.MIN_SPAWN_CHANCE).forGetter(data -> data.spawnChance)
                    )
                    .apply(i, SkyTraderData::new)
    );
    public static final SavedDataType<SkyTraderData> TYPE = new SavedDataType<>(
            ModConstants.id("sky_trader"), SkyTraderData::new, CODEC, DataFixTypes.SAVED_DATA_WANDERING_TRADER // TODO: this should be my own thing?
    );
    private int spawnDelay;
    private int spawnChance;

    public SkyTraderData() {
        this(SkyTraderSpawner.DEFAULT_SPAWN_DELAY, SkyTraderSpawner.MIN_SPAWN_CHANCE);
    }

    public SkyTraderData(int spawnDelay, int spawnChance) {
        this.spawnDelay = spawnDelay;
        this.spawnChance = spawnChance;
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
