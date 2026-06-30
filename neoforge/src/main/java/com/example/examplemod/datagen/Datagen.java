package com.example.examplemod.datagen;

import com.example.examplemod.ModConstants;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

@EventBusSubscriber
public class Datagen {
    @SubscribeEvent
    static void gatherData(GatherDataEvent.Client event) {
        event.createProvider(output -> new SkyTraderEntityTagProvider(output, event.getLookupProvider(), ModConstants.MOD_ID));
    }
}
