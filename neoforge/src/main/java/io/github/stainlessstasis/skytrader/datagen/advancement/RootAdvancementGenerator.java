package io.github.stainlessstasis.skytrader.datagen.advancement;

import io.github.stainlessstasis.skytrader.ModConstants;
import io.github.stainlessstasis.skytrader.advancement.ModAdvancements;
import io.github.stainlessstasis.skytrader.item.ModItems;
import net.minecraft.advancements.*;
import net.minecraft.advancements.triggers.CriteriaTriggers;
import net.minecraft.advancements.triggers.ImpossibleTrigger;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.advancements.AdvancementSubProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStackTemplate;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.function.Consumer;

public class RootAdvancementGenerator implements AdvancementSubProvider {
    @Override
    public void generate(HolderLookup.@NonNull Provider provider, @NonNull Consumer<AdvancementHolder> saver) {
        Advancement.Builder builder = Advancement.Builder.advancement();

        builder.display(
                new ItemStackTemplate(ModItems.SKYFARE_TICKET.get()),
                Component.translatable("advancements."+ ModConstants.MOD_ID+".root.title"),
                Component.translatable("advancements."+ ModConstants.MOD_ID+".root.description"),
                Identifier.withDefaultNamespace("block/light_blue_concrete_powder"),
                AdvancementType.TASK,
                false,
                false,
                true
        );

        builder.rewards(
                AdvancementRewards.Builder.experience(0)
        );

        builder.addCriterion(
                "interact_with_sky_trader",
                CriteriaTriggers.IMPOSSIBLE.createCriterion(new ImpossibleTrigger.TriggerInstance())
        );
        builder.requirements(AdvancementRequirements.allOf(List.of("interact_with_sky_trader")));
        builder.save(saver, ModAdvancements.ROOT);
    }
}
