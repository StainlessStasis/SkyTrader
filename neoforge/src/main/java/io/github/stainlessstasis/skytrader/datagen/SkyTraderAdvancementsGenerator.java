package io.github.stainlessstasis.skytrader.datagen;

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
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.function.Consumer;

public class SkyTraderAdvancementsGenerator implements AdvancementSubProvider {
    @Override
    public void generate(HolderLookup.@NonNull Provider provider, @NonNull Consumer<AdvancementHolder> saver) {
        DataComponentPatch glint = DataComponentPatch.builder()
                .set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true)
                .build();


        // ROOT
        Advancement.Builder rootBuilder = Advancement.Builder.advancement()
                .display(
                        new ItemStackTemplate(ModItems.SKYFARE_TICKET.get()),
                        Component.translatable("advancements." + ModConstants.MOD_ID + ".root.title"),
                        Component.translatable("advancements." + ModConstants.MOD_ID + ".root.description"),
                        Identifier.withDefaultNamespace("block/light_blue_concrete_powder"),
                        AdvancementType.TASK,
                        false,
                        false,
                        true
                )
                .rewards(AdvancementRewards.Builder.experience(0))
                .addCriterion("interact_with_sky_trader", CriteriaTriggers.IMPOSSIBLE.createCriterion(new ImpossibleTrigger.TriggerInstance()));

        rootBuilder.requirements(AdvancementRequirements.allOf(List.of("interact_with_sky_trader")));
        rootBuilder.save(saver, ModAdvancements.ROOT);


        // WELCOME ABOARD
        Advancement.Builder welcomeAboardBuilder = Advancement.Builder.advancement()
                .parent(AdvancementSubProvider.createPlaceholder(ModAdvancements.ROOT.toString()))
                .display(
                        new ItemStackTemplate(ModItems.SKYFARE_TICKET.get()),
                        Component.translatable("advancements." + ModConstants.MOD_ID + ".welcome_aboard.title"),
                        Component.translatable("advancements." + ModConstants.MOD_ID + ".welcome_aboard.description"),
                        null,
                        AdvancementType.TASK,
                        true,
                        true,
                        false
                )
                .rewards(AdvancementRewards.Builder.experience(15))
                .addCriterion("board_sky_trader_ghast", CriteriaTriggers.IMPOSSIBLE.createCriterion(new ImpossibleTrigger.TriggerInstance()));

        welcomeAboardBuilder.requirements(AdvancementRequirements.allOf(List.of("board_sky_trader_ghast")));
        welcomeAboardBuilder.save(saver, ModAdvancements.WELCOME_ABOARD);


        // FREE BIRD
        Advancement.Builder freeBirdBuilder = Advancement.Builder.advancement()
                .parent(AdvancementSubProvider.createPlaceholder(ModAdvancements.WELCOME_ABOARD.toString()))
                .display(
                        new ItemStackTemplate(Items.FEATHER),
                        Component.translatable("advancements." + ModConstants.MOD_ID + ".free_bird.title"),
                        Component.translatable("advancements." + ModConstants.MOD_ID + ".free_bird.description"),
                        null,
                        AdvancementType.GOAL,
                        true,
                        true,
                        false
                )
                .rewards(AdvancementRewards.Builder.experience(50))
                .addCriterion("jump_off_sky_trader_ghast", CriteriaTriggers.IMPOSSIBLE.createCriterion(new ImpossibleTrigger.TriggerInstance()));

        freeBirdBuilder.requirements(AdvancementRequirements.allOf(List.of("jump_off_sky_trader_ghast")));
        freeBirdBuilder.save(saver, ModAdvancements.FREE_BIRD);


        // NO FLY LIST
        Advancement.Builder noFlyListBuilder = Advancement.Builder.advancement()
                .parent(AdvancementSubProvider.createPlaceholder(ModAdvancements.WELCOME_ABOARD.toString()))
                .display(
                        new ItemStackTemplate(Items.BARRIER),
                        Component.translatable("advancements." + ModConstants.MOD_ID + ".no_fly_list.title"),
                        Component.translatable("advancements." + ModConstants.MOD_ID + ".no_fly_list.description"),
                        null,
                        AdvancementType.CHALLENGE,
                        true,
                        true,
                        true
                )
                .rewards(AdvancementRewards.Builder.experience(100))
                .addCriterion("kicked_off_sky_trader_ghast", CriteriaTriggers.IMPOSSIBLE.createCriterion(new ImpossibleTrigger.TriggerInstance()));

        noFlyListBuilder.requirements(AdvancementRequirements.allOf(List.of("kicked_off_sky_trader_ghast")));
        noFlyListBuilder.save(saver, ModAdvancements.NO_FLY_LIST);


        // THAT'S NOT A SEAT
        Advancement.Builder notASeatBuilder = Advancement.Builder.advancement()
                .parent(AdvancementSubProvider.createPlaceholder(ModAdvancements.WELCOME_ABOARD.toString()))
                .display(
                        new ItemStackTemplate(Items.SADDLE),
                        Component.translatable("advancements." + ModConstants.MOD_ID + ".not_a_seat.title"),
                        Component.translatable("advancements." + ModConstants.MOD_ID + ".not_a_seat.description"),
                        null,
                        AdvancementType.GOAL,
                        true,
                        true,
                        false
                )
                .rewards(AdvancementRewards.Builder.experience(15))
                .addCriterion("block_takeoff", CriteriaTriggers.IMPOSSIBLE.createCriterion(new ImpossibleTrigger.TriggerInstance()));

        notASeatBuilder.requirements(AdvancementRequirements.allOf(List.of("block_takeoff")));
        notASeatBuilder.save(saver, ModAdvancements.NOT_A_SEAT);


        // STAR TRAVELER
        Advancement.Builder starTravelerBuilder = Advancement.Builder.advancement()
                .parent(AdvancementSubProvider.createPlaceholder(ModAdvancements.WELCOME_ABOARD.toString()))
                .display(
                        new ItemStackTemplate(Items.NETHER_STAR),
                        Component.translatable("advancements." + ModConstants.MOD_ID + ".star_traveler.title"),
                        Component.translatable("advancements." + ModConstants.MOD_ID + ".star_traveler.description"),
                        null,
                        AdvancementType.CHALLENGE,
                        true,
                        true,
                        false
                )
                .rewards(AdvancementRewards.Builder.experience(100))
                .addCriterion("board_flight_really_high", CriteriaTriggers.IMPOSSIBLE.createCriterion(new ImpossibleTrigger.TriggerInstance()));

        starTravelerBuilder.requirements(AdvancementRequirements.allOf(List.of("board_flight_really_high")));
        starTravelerBuilder.save(saver, ModAdvancements.STAR_TRAVELER);


        // SNACK RUN
        Advancement.Builder snackRunBuilder = Advancement.Builder.advancement()
                .parent(AdvancementSubProvider.createPlaceholder(ModAdvancements.WELCOME_ABOARD.toString()))
                .display(
                        new ItemStackTemplate(Items.COOKIE),
                        Component.translatable("advancements." + ModConstants.MOD_ID + ".snack_run.title"),
                        Component.translatable("advancements." + ModConstants.MOD_ID + ".snack_run.description"),
                        null,
                        AdvancementType.GOAL,
                        true,
                        true,
                        false
                )
                .rewards(AdvancementRewards.Builder.experience(25))
                .addCriterion("buy_sky_trader_snack", CriteriaTriggers.IMPOSSIBLE.createCriterion(new ImpossibleTrigger.TriggerInstance()));

        snackRunBuilder.requirements(AdvancementRequirements.allOf(List.of("buy_sky_trader_snack")));
        snackRunBuilder.save(saver, ModAdvancements.SNACK_RUN);


        // FIRST CLASS CUSTOMER
        Advancement.Builder firstClassCustomerBuilder = Advancement.Builder.advancement()
                .parent(AdvancementSubProvider.createPlaceholder(ModAdvancements.SNACK_RUN.toString()))
                .display(
                        new ItemStackTemplate(Items.COOKIE, 1, glint),
                        Component.translatable("advancements." + ModConstants.MOD_ID + ".first_class_customer.title"),
                        Component.translatable("advancements." + ModConstants.MOD_ID + ".first_class_customer.description"),
                        null,
                        AdvancementType.GOAL,
                        true,
                        true,
                        false
                )
                .rewards(AdvancementRewards.Builder.experience(50))
                .addCriterion("buy_all_sky_trader_snack", CriteriaTriggers.IMPOSSIBLE.createCriterion(new ImpossibleTrigger.TriggerInstance()));

        firstClassCustomerBuilder.requirements(AdvancementRequirements.allOf(List.of("buy_all_sky_trader_snack")));
        firstClassCustomerBuilder.save(saver, ModAdvancements.FIRST_CLASS_CUSTOMER);


        // YOU CALLED
        Advancement.Builder youCalledBuilder = Advancement.Builder.advancement()
                .parent(AdvancementSubProvider.createPlaceholder(ModAdvancements.WELCOME_ABOARD.toString()))
                .display(
                        new ItemStackTemplate(ModItems.SKYFLARE.get()),
                        Component.translatable("advancements." + ModConstants.MOD_ID + ".you_called.title"),
                        Component.translatable("advancements." + ModConstants.MOD_ID + ".you_called.description"),
                        null,
                        AdvancementType.TASK,
                        true,
                        true,
                        false
                )
                .rewards(AdvancementRewards.Builder.experience(15))
                .addCriterion("use_skyflare", CriteriaTriggers.IMPOSSIBLE.createCriterion(new ImpossibleTrigger.TriggerInstance()));

        youCalledBuilder.requirements(AdvancementRequirements.allOf(List.of("use_skyflare")));
        youCalledBuilder.save(saver, ModAdvancements.YOU_CALLED);


        // A BIT REDUNDANT, DON'T YOU THINK
        Advancement.Builder redundantBuilder = Advancement.Builder.advancement()
                .parent(AdvancementSubProvider.createPlaceholder(ModAdvancements.YOU_CALLED.toString()))
                .display(
                        new ItemStackTemplate(Items.ELYTRA),
                        Component.translatable("advancements." + ModConstants.MOD_ID + ".a_bit_redundant.title"),
                        Component.translatable("advancements." + ModConstants.MOD_ID + ".a_bit_redundant.description"),
                        null,
                        AdvancementType.GOAL,
                        true,
                        true,
                        false
                )
                .rewards(AdvancementRewards.Builder.experience(25))
                .addCriterion("use_skyflare_with_elytra", CriteriaTriggers.IMPOSSIBLE.createCriterion(new ImpossibleTrigger.TriggerInstance()));

        redundantBuilder.requirements(AdvancementRequirements.allOf(List.of("use_skyflare_with_elytra")));
        redundantBuilder.save(saver, ModAdvancements.A_BIT_REDUNDANT);


        // THERE'S NO SKY HERE
        Advancement.Builder noSkyBuilder = Advancement.Builder.advancement()
                .parent(AdvancementSubProvider.createPlaceholder(ModAdvancements.YOU_CALLED.toString()))
                .display(
                        new ItemStackTemplate(ModItems.SKYFLARE.get(), 1, glint),
                        Component.translatable("advancements." + ModConstants.MOD_ID + ".theres_no_sky_here.title"),
                        Component.translatable("advancements." + ModConstants.MOD_ID + ".theres_no_sky_here.description"),
                        null,
                        AdvancementType.GOAL,
                        true,
                        true,
                        true
                )
                .rewards(AdvancementRewards.Builder.experience(25))
                .addCriterion("use_skyflare_not_in_overworld", CriteriaTriggers.IMPOSSIBLE.createCriterion(new ImpossibleTrigger.TriggerInstance()));

        noSkyBuilder.requirements(AdvancementRequirements.allOf(List.of("use_skyflare_not_in_overworld")));
        noSkyBuilder.save(saver, ModAdvancements.THERES_NO_SKY_HERE);

        // NO FLIGHT DELAYS HERE
        Advancement.Builder noFlightDelaysBuilder = Advancement.Builder.advancement()
                .parent(AdvancementSubProvider.createPlaceholder(ModAdvancements.WELCOME_ABOARD.toString()))
                .display(
                        new ItemStackTemplate(Items.LIGHTNING_ROD.waxed().unaffected()),
                        Component.translatable("advancements." + ModConstants.MOD_ID + ".no_flight_delays.title"),
                        Component.translatable("advancements." + ModConstants.MOD_ID + ".no_flight_delays.description"),
                        null,
                        AdvancementType.GOAL,
                        true,
                        true,
                        false
                )
                .rewards(AdvancementRewards.Builder.experience(25))
                .addCriterion("fly_through_thunderstorm", CriteriaTriggers.IMPOSSIBLE.createCriterion(new ImpossibleTrigger.TriggerInstance()));

        noFlightDelaysBuilder.requirements(AdvancementRequirements.allOf(List.of("fly_through_thunderstorm")));
        noFlightDelaysBuilder.save(saver, ModAdvancements.NO_FLIGHT_DELAYS);


        // PERMISSION DENIED
        Advancement.Builder permissionDeniedBuilder = Advancement.Builder.advancement()
                .parent(AdvancementSubProvider.createPlaceholder(ModAdvancements.NO_FLIGHT_DELAYS.toString()))
                .display(
                        new ItemStackTemplate(Items.LIGHTNING_ROD.waxed().oxidized()),
                        Component.translatable("advancements." + ModConstants.MOD_ID + ".permission_denied.title"),
                        Component.translatable("advancements." + ModConstants.MOD_ID + ".permission_denied.description"),
                        null,
                        AdvancementType.GOAL,
                        true,
                        true,
                        true
                )
                .rewards(AdvancementRewards.Builder.experience(25))
                .addCriterion("struck_by_lightning_during_flight", CriteriaTriggers.IMPOSSIBLE.createCriterion(new ImpossibleTrigger.TriggerInstance()));

        permissionDeniedBuilder.requirements(AdvancementRequirements.allOf(List.of("struck_by_lightning_during_flight")));
        permissionDeniedBuilder.save(saver, ModAdvancements.PERMISSION_DENIED);


        // LOCAL COMMUTER
        Advancement.Builder localCommuterBuilder = Advancement.Builder.advancement()
                .parent(AdvancementSubProvider.createPlaceholder(ModAdvancements.WELCOME_ABOARD.toString()))
                .display(
                        new ItemStackTemplate(Items.HAY_BLOCK),
                        Component.translatable("advancements." + ModConstants.MOD_ID + ".local_commuter.title"),
                        Component.translatable("advancements." + ModConstants.MOD_ID + ".local_commuter.description"),
                        null,
                        AdvancementType.GOAL,
                        true,
                        true,
                        false
                )
                .rewards(AdvancementRewards.Builder.experience(25))
                .addCriterion("board_flight_while_in_village", CriteriaTriggers.IMPOSSIBLE.createCriterion(new ImpossibleTrigger.TriggerInstance()));

        localCommuterBuilder.requirements(AdvancementRequirements.allOf(List.of("board_flight_while_in_village")));
        localCommuterBuilder.save(saver, ModAdvancements.LOCAL_COMMUTER);


        // A TERRIBLE DAY FOR RAIN
        Advancement.Builder terribleDayBuilder = Advancement.Builder.advancement()
                .parent(AdvancementSubProvider.createPlaceholder(ModAdvancements.WELCOME_ABOARD.toString()))
                .display(
                        new ItemStackTemplate(Items.GHAST_TEAR),
                        Component.translatable("advancements." + ModConstants.MOD_ID + ".terrible_day_for_rain.title"),
                        Component.translatable("advancements." + ModConstants.MOD_ID + ".terrible_day_for_rain.description"),
                        null,
                        AdvancementType.GOAL,
                        true,
                        true,
                        true
                )
                .rewards(AdvancementRewards.Builder.experience(-25)) // yes the negative is intentional. why would you do that?
                .addCriterion("turn_trader_ghast_hostile", CriteriaTriggers.IMPOSSIBLE.createCriterion(new ImpossibleTrigger.TriggerInstance()));

        terribleDayBuilder.requirements(AdvancementRequirements.allOf(List.of("turn_trader_ghast_hostile")));
        terribleDayBuilder.save(saver, ModAdvancements.TERRIBLE_DAY_FOR_RAIN);


        // NO REFUNDS
        Advancement.Builder noRefundsBuilder = Advancement.Builder.advancement()
                .parent(AdvancementSubProvider.createPlaceholder(ModAdvancements.WELCOME_ABOARD.toString()))
                .display(
                        new ItemStackTemplate(Items.EMERALD),
                        Component.translatable("advancements." + ModConstants.MOD_ID + ".no_refunds.title"),
                        Component.translatable("advancements." + ModConstants.MOD_ID + ".no_refunds.description"),
                        null,
                        AdvancementType.CHALLENGE,
                        true,
                        true,
                        false
                )
                .rewards(AdvancementRewards.Builder.experience(100))
                .addCriterion("scammed_by_sky_trader", CriteriaTriggers.IMPOSSIBLE.createCriterion(new ImpossibleTrigger.TriggerInstance()));

        noRefundsBuilder.requirements(AdvancementRequirements.allOf(List.of("scammed_by_sky_trader")));
        noRefundsBuilder.save(saver, ModAdvancements.NO_REFUNDS);
    }
}