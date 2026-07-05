package io.github.stainlessstasis.skytrader.datagen.advancement;

import io.github.stainlessstasis.skytrader.ModConstants;
import io.github.stainlessstasis.skytrader.advancement.ModAdvancements;
import net.minecraft.advancements.*;
import net.minecraft.advancements.criterion.ImpossibleTrigger;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.data.advancements.AdvancementSubProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.function.Consumer;

public class FirstClassCustomer implements AdvancementSubProvider {
    @Override
    public void generate(HolderLookup.@NonNull Provider provider, @NonNull Consumer<AdvancementHolder> saver) {
        Advancement.Builder builder = Advancement.Builder.advancement();
        builder.parent(AdvancementSubProvider.createPlaceholder(ModAdvancements.SNACK_RUN.toString()));

        DataComponentPatch glint = DataComponentPatch.builder()
                .set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true)
                .build();
        builder.display(
                new ItemStackTemplate(Items.COOKIE, 1, glint),
                Component.translatable("advancements."+ ModConstants.MOD_ID+".first_class_customer.title"),
                Component.translatable("advancements."+ ModConstants.MOD_ID+".first_class_customer.description"),
                null,
                AdvancementType.GOAL,
                true,
                true,
                false
        );

        builder.rewards(
                AdvancementRewards.Builder.experience(50)
        );

        builder.addCriterion(
                "buy_all_sky_trader_snack",
                CriteriaTriggers.IMPOSSIBLE.createCriterion(new ImpossibleTrigger.TriggerInstance())
        );
        builder.requirements(AdvancementRequirements.allOf(List.of("buy_all_sky_trader_snack")));
        builder.save(saver, ModAdvancements.FIRST_CLASS_CUSTOMER);
    }
}
