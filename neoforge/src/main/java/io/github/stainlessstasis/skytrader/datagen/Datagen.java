package io.github.stainlessstasis.skytrader.datagen;

import io.github.stainlessstasis.skytrader.ModConstants;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

@EventBusSubscriber
public class Datagen {
    @SubscribeEvent
    static void gatherData(GatherDataEvent.Client event) {
        event.createProvider(output -> new ModEntityTagProvider(output, event.getLookupProvider(), ModConstants.MOD_ID));
        event.createProvider(output -> new ModTradeProvider(output, event.getLookupProvider()));
        event.createProvider(output -> new ModItemModelsProvider(output, ModConstants.MOD_ID));
    }
}
