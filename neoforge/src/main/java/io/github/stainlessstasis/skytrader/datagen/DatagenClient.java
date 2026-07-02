package io.github.stainlessstasis.skytrader.datagen;

import io.github.stainlessstasis.skytrader.ModConstants;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

@EventBusSubscriber(value = Dist.CLIENT)
public class DatagenClient {
    @SubscribeEvent
    static void gatherData(GatherDataEvent.Client event) {
        event.createProvider(output -> new ModItemModelsProvider(output, ModConstants.MOD_ID));
    }
}
