package io.github.stainlessstasis.skytrader.advancement;

import io.github.stainlessstasis.skytrader.ModConstants;
import io.github.stainlessstasis.skytrader.entity.SkyTraderGhast;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;

public class ModAdvancements {
    public static final Identifier ROOT = ModConstants.id("root");
    public static final Identifier WELCOME_ABOARD = ModConstants.id("welcome_aboard");
    public static final Identifier FREE_BIRD = ModConstants.id("free_bird");
    public static final Identifier NO_FLY_LIST = ModConstants.id("no_fly_list");
    public static final Identifier NOT_A_SEAT = ModConstants.id("not_a_seat");
    public static final Identifier STAR_TRAVELER = ModConstants.id("star_traveler");
    public static final Identifier NO_FLIGHT_DELAYS = ModConstants.id("no_flight_delays");
    public static final Identifier PERMISSION_DENIED = ModConstants.id("permission_denied");
    public static final Identifier LOCAL_COMMUTER = ModConstants.id("local_commuter");
    public static final Identifier TERRIBLE_DAY_FOR_RAIN = ModConstants.id("terrible_day_for_rain");
    public static final Identifier NO_REFUNDS = ModConstants.id("no_refunds");
    public static final Identifier TRANSATLANTIC_TRAVEL = ModConstants.id("transatlantic_travel");
    public static final Identifier CHANGING_CLIMATES = ModConstants.id("changing_climates");

    public static final Identifier SNACK_RUN = ModConstants.id("snack_run");
    public static final Identifier FIRST_CLASS_CUSTOMER = ModConstants.id("first_class_customer");

    public static final Identifier YOU_CALLED = ModConstants.id("you_called");
    public static final Identifier THERES_NO_SKY_HERE = ModConstants.id("theres_no_sky_here");
    public static final Identifier A_BIT_REDUNDANT = ModConstants.id("a_bit_redundant");

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

    public static void checkPermissionDeniedAdvancement(Entity _player, DamageSource damageSource) {
        if (!(_player instanceof ServerPlayer player)) return;
        if (player.isPassenger()
                && player.getVehicle() instanceof SkyTraderGhast ghast
                && ghast.getRideState().hasMovement()
                && damageSource.is(DamageTypes.LIGHTNING_BOLT))
        {
            grant(player, PERMISSION_DENIED);
        }
    }

    public static void init() {}
}
