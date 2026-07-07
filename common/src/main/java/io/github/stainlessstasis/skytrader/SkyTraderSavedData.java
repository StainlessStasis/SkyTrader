package io.github.stainlessstasis.skytrader;

import com.mojang.serialization.Codec;
import net.minecraft.core.UUIDUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class SkyTraderSavedData extends SavedData {
    public static final Codec<SkyTraderSavedData> CODEC = UUIDUtil.CODEC.listOf()
            .xmap(
                    list -> {
                        SkyTraderSavedData data = new SkyTraderSavedData();
                        data.formerTraderGhasts.addAll(list);
                        return data;
                    },
                    data -> List.copyOf(data.formerTraderGhasts)
            );

    public static final SavedDataType<SkyTraderSavedData> TYPE = new SavedDataType<>(
            ModConstants.id("sky_trader_data"),
            SkyTraderSavedData::new,
            CODEC,
            DataFixTypes.SAVED_DATA_WANDERING_TRADER
    );

    public final Set<UUID> formerTraderGhasts = new HashSet<>();

    public static SkyTraderSavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(TYPE);
    }
}
