package io.github.stainlessstasis.skytrader.datagen.advancement;

import io.github.stainlessstasis.skytrader.ModConstants;
import io.github.stainlessstasis.skytrader.advancement.ModAdvancements;
import net.minecraft.advancements.*;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.criterion.ImpossibleTrigger;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.advancements.AdvancementSubProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.function.Consumer;

public class SnackRunAdvancementGenerator implements AdvancementSubProvider {
    @Override
    public void generate(HolderLookup.@NonNull Provider provider, @NonNull Consumer<AdvancementHolder> saver) {
        Advancement.Builder builder = Advancement.Builder.advancement();
        builder.parent(AdvancementSubProvider.createPlaceholder(ModAdvancements.WELCOME_ABOARD.toString()));

        builder.display(
                new ItemStackTemplate(Items.COOKIE),
                Component.translatable("advancements."+ ModConstants.MOD_ID+".snack_run.title"),
                Component.translatable("advancements."+ ModConstants.MOD_ID+".snack_run.description"),
                null,
                AdvancementType.GOAL,
                true,
                true,
                false
        );

        builder.rewards(
                AdvancementRewards.Builder.experience(25)
        );

        builder.addCriterion(
                "buy_sky_trader_snack",
                CriteriaTriggers.IMPOSSIBLE.createCriterion(new ImpossibleTrigger.TriggerInstance())
        );
        builder.requirements(AdvancementRequirements.allOf(List.of("buy_sky_trader_snack")));
        builder.save(saver, ModAdvancements.SNACK_RUN);
    }
}
