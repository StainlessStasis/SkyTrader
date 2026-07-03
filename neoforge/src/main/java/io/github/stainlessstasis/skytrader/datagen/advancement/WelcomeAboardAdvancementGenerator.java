package io.github.stainlessstasis.skytrader.datagen.advancement;

import io.github.stainlessstasis.skytrader.ModConstants;
import io.github.stainlessstasis.skytrader.advancement.ModAdvancements;
import io.github.stainlessstasis.skytrader.item.ModItems;
import net.minecraft.advancements.*;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.criterion.ImpossibleTrigger;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.advancements.AdvancementSubProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStackTemplate;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.function.Consumer;

public class WelcomeAboardAdvancementGenerator implements AdvancementSubProvider {
    @Override
    public void generate(HolderLookup.@NonNull Provider provider, @NonNull Consumer<AdvancementHolder> saver) {
        Advancement.Builder builder = Advancement.Builder.advancement();
        builder.parent(AdvancementSubProvider.createPlaceholder("minecraft:adventure/root"));

        builder.display(
                new ItemStackTemplate(ModItems.SKYFARE_TICKET.get()),
                Component.translatable("advancements."+ ModConstants.MOD_ID+".welcome_aboard.title"),
                Component.translatable("advancements."+ ModConstants.MOD_ID+".welcome_aboard.description"),
                null,
                AdvancementType.TASK,
                true,
                true,
                false
        );

        builder.rewards(
                AdvancementRewards.Builder.experience(15)
        );

        builder.addCriterion(
                "board_sky_trader_ghast",
                CriteriaTriggers.IMPOSSIBLE.createCriterion(new ImpossibleTrigger.TriggerInstance())
        );
        builder.requirements(AdvancementRequirements.allOf(List.of("board_sky_trader_ghast")));
        builder.save(saver, ModAdvancements.WELCOME_ABOARD);
    }
}
