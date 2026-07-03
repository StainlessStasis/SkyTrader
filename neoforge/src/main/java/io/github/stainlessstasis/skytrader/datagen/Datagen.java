package io.github.stainlessstasis.skytrader.datagen;

import io.github.stainlessstasis.skytrader.ModConstants;
import io.github.stainlessstasis.skytrader.datagen.advancement.FreeBirdAdvancementGenerator;
import io.github.stainlessstasis.skytrader.datagen.advancement.NoFlyListAdvancementGenerator;
import io.github.stainlessstasis.skytrader.datagen.advancement.SnackRunAdvancementGenerator;
import io.github.stainlessstasis.skytrader.datagen.advancement.WelcomeAboardAdvancementGenerator;
import net.minecraft.data.advancements.AdvancementProvider;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.List;

@EventBusSubscriber
public class Datagen {
    @SubscribeEvent
    static void gatherData(GatherDataEvent.Client event) {
        var lookupProvider = event.getLookupProvider();
        event.createProvider(output -> new ModEntityTagProvider(output, lookupProvider, ModConstants.MOD_ID));
        event.createProvider(output -> new ModTradeProvider(output, lookupProvider));
        event.createProvider(output -> new AdvancementProvider(
                output, lookupProvider,
                List.of(
                        new WelcomeAboardAdvancementGenerator(),
                        new SnackRunAdvancementGenerator(),
                        new FreeBirdAdvancementGenerator(),
                        new NoFlyListAdvancementGenerator()
                ))
        );
    }
}
