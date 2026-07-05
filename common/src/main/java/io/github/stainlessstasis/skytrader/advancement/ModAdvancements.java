package io.github.stainlessstasis.skytrader.advancement;

import io.github.stainlessstasis.skytrader.ModConstants;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

public class ModAdvancements {
    public static final Identifier ROOT = ModConstants.id("root");
    public static final Identifier WELCOME_ABOARD = ModConstants.id("welcome_aboard");

    public static final Identifier SNACK_RUN = ModConstants.id("snack_run");
    public static final Identifier FIRST_CLASS_CUSTOMER = ModConstants.id("first_class_customer");

    public static final Identifier YOU_CALLED = ModConstants.id("you_called");
    public static final Identifier THERES_NO_SKY_HERE = ModConstants.id("theres_no_sky_here");
    public static final Identifier A_BIT_REDUNDANT = ModConstants.id("a_bit_redundant");

    public static final Identifier FREE_BIRD = ModConstants.id("free_bird");
    public static final Identifier NO_FLY_LIST = ModConstants.id("no_fly_list");
    public static final Identifier NOT_A_SEAT = ModConstants.id("not_a_seat");

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
