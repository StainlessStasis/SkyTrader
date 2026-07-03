package io.github.stainlessstasis.skytrader.datagen.advancement;

import io.github.stainlessstasis.skytrader.ModConstants;
import io.github.stainlessstasis.skytrader.advancement.ModAdvancements;
import net.minecraft.advancements.*;
import net.minecraft.advancements.triggers.ImpossibleTrigger;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.advancements.AdvancementSubProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.function.Consumer;

public class NoFlyListAdvancementGenerator implements AdvancementSubProvider {
    @Override
    public void generate(HolderLookup.@NonNull Provider provider, @NonNull Consumer<AdvancementHolder> saver) {
        Advancement.Builder builder = Advancement.Builder.advancement();
        builder.parent(AdvancementSubProvider.createPlaceholder(ModAdvancements.WELCOME_ABOARD.toString()));

        builder.display(
                new ItemStackTemplate(Items.BARRIER),
                Component.translatable("advancements."+ ModConstants.MOD_ID+".no_fly_list.title"),
                Component.translatable("advancements."+ ModConstants.MOD_ID+".no_fly_list.description"),
                null,
                AdvancementType.CHALLENGE,
                true,
                true,
                true
        );

        builder.rewards(
                AdvancementRewards.Builder.experience(100)
        );

        builder.addCriterion(
                "kicked_off_sky_trader_ghast",
                ModAdvancements.MANUAL_TRIGGER.get().createCriterion(new ImpossibleTrigger.TriggerInstance())
        );
        builder.requirements(AdvancementRequirements.allOf(List.of("kicked_off_sky_trader_ghast")));
        builder.save(saver, ModAdvancements.NO_FLY_LIST);
    }
}
