package io.github.stainlessstasis.skytrader.datagen.advancement;

import io.github.stainlessstasis.skytrader.ModConstants;
import io.github.stainlessstasis.skytrader.advancement.ModAdvancements;
import io.github.stainlessstasis.skytrader.item.ModItems;
import net.minecraft.advancements.*;
import net.minecraft.advancements.triggers.CriteriaTriggers;
import net.minecraft.advancements.triggers.ImpossibleTrigger;
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

public class YouCalled implements AdvancementSubProvider {
    @Override
    public void generate(HolderLookup.@NonNull Provider provider, @NonNull Consumer<AdvancementHolder> saver) {
        Advancement.Builder builder = Advancement.Builder.advancement();
        builder.parent(AdvancementSubProvider.createPlaceholder(ModAdvancements.ROOT.toString()));

        builder.display(
                new ItemStackTemplate(ModItems.SKYFLARE.get()),
                Component.translatable("advancements."+ ModConstants.MOD_ID+".you_called.title"),
                Component.translatable("advancements."+ ModConstants.MOD_ID+".you_called.description"),
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
                "use_skyflare",
                CriteriaTriggers.IMPOSSIBLE.createCriterion(new ImpossibleTrigger.TriggerInstance())
        );
        builder.requirements(AdvancementRequirements.allOf(List.of("use_skyflare")));
        builder.save(saver, ModAdvancements.YOU_CALLED);
    }
}
