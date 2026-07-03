package io.github.stainlessstasis.skytrader.advancement;

import io.github.stainlessstasis.skytrader.ModConstants;
import io.github.stainlessstasis.skytrader.platform.Services;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.triggers.ImpossibleTrigger;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

import java.util.function.Supplier;

public class ModAdvancements {
    public static final Supplier<ImpossibleTrigger> MANUAL_TRIGGER = Services.REGISTRY.registerAdvancementTrigger("manual_trigger");
    public static final Identifier WELCOME_ABOARD = ModConstants.id("welcome_aboard");

    public static void grant(ServerPlayer player, Identifier advancementId) {
        AdvancementHolder holder = player.level().getServer().getAdvancements().get(advancementId);
        if (holder == null) {
            return;
        }

        var progress = player.getAdvancements().getOrStartProgress(holder);
        if (progress.isDone()) {
            return;
        }

        for (String criterion : progress.getRemainingCriteria()) {
            player.getAdvancements().award(holder, criterion);
        }
    }

    public static void init() {}
}
