package io.github.stainlessstasis.skytrader;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.maps.MapDecorationTypes;
import org.jetbrains.annotations.Nullable;

public class MapHelper {
    public static @Nullable BlockPos getMapDestination(ItemStack map) {
        var decorations = map.get(DataComponents.MAP_DECORATIONS);
        if (decorations == null) return null;

        for (var entry : decorations.decorations().entrySet()) {
            var decoration = entry.getValue();
            var type = decoration.type();
            if (type.is(MapDecorationTypes.DESERT_VILLAGE) ||
                    type.is(MapDecorationTypes.PLAINS_VILLAGE) ||
                    type.is(MapDecorationTypes.SAVANNA_VILLAGE) ||
                    type.is(MapDecorationTypes.SNOWY_VILLAGE) ||
                    type.is(MapDecorationTypes.TAIGA_VILLAGE) ||
                    type.is(MapDecorationTypes.JUNGLE_TEMPLE) ||
                    type.is(MapDecorationTypes.SWAMP_HUT) ||
                    type.is(MapDecorationTypes.WOODLAND_MANSION) ||
                    type.is(MapDecorationTypes.OCEAN_MONUMENT) ||
                    type.is(MapDecorationTypes.TRIAL_CHAMBERS))
            {
                return BlockPos.containing(decoration.x(), 64, decoration.z());
            }
        }
        return null;
    }
}
