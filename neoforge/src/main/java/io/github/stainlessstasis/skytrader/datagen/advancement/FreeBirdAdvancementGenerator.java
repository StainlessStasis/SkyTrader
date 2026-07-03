package io.github.stainlessstasis.skytrader.datagen.advancement;

import io.github.stainlessstasis.skytrader.ModConstants;
import io.github.stainlessstasis.skytrader.advancement.ModAdvancements;
import net.minecraft.advancements.*;
import net.minecraft.advancements.triggers.CriteriaTriggers;
import net.minecraft.advancements.triggers.ImpossibleTrigger;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.advancements.AdvancementSubProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.function.Consumer;

public class FreeBirdAdvancementGenerator implements AdvancementSubProvider {
    @Override
    public void generate(HolderLookup.@NonNull Provider provider, @NonNull Consumer<AdvancementHolder> saver) {
        Advancement.Builder builder = Advancement.Builder.advancement();
        builder.parent(AdvancementSubProvider.createPlaceholder(ModAdvancements.WELCOME_ABOARD.toString()));

        builder.display(
                new ItemStackTemplate(Items.FEATHER),
                Component.translatable("advancements."+ ModConstants.MOD_ID+".free_bird.title"),
                Component.translatable("advancements."+ ModConstants.MOD_ID+".free_bird.description"),
                null,
                AdvancementType.GOAL,
                true,
                true,
                true
        );

        builder.rewards(
                AdvancementRewards.Builder.experience(50)
        );

        builder.addCriterion(
                "jump_off_sky_trader_ghast",
                CriteriaTriggers.IMPOSSIBLE.createCriterion(new ImpossibleTrigger.TriggerInstance())
        );
        builder.requirements(AdvancementRequirements.allOf(List.of("jump_off_sky_trader_ghast")));
        builder.save(saver, ModAdvancements.FREE_BIRD);
    }
}
