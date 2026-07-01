package io.github.stainlessstasis.skytrader.entity;

import io.github.stainlessstasis.skytrader.ModConstants;
import io.github.stainlessstasis.skytrader.item.ModItems;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.happyghast.HappyGhast;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class SkyTraderGhast extends HappyGhast implements TraceableEntity, OwnableEntity {
    protected static final int BOARDING_DELAY_TICKS = 100;
    protected static final int MAX_NON_SKY_TRADER_PASSENGERS = 3;

    protected @Nullable EntityReference<LivingEntity> owner;
    protected final Set<UUID> paidPlayers = new HashSet<>();
    protected RideState rideState = RideState.IDLE;
    protected int stateTicks = 0;

    public SkyTraderGhast(EntityType<? extends HappyGhast> type, Level level) {
        super(type, level);
    }

    public enum RideState implements StringRepresentable {
        IDLE("idle"),
        BOARDING("boarding"),
        DEPARTING("departing"),
        EN_ROUTE("en_route"),
        ARRIVING("arriving"),
        ARRIVED("arrived");

        private final String name;

        RideState(String name) {
            this.name = name;
        }

        @Override
        public @NonNull String getSerializedName() {
            return name;
        }
    }

    @Override
    public @NonNull InteractionResult mobInteract(@NonNull Player player, @NonNull InteractionHand hand) {
        if (this.isBaby()) {
            return super.mobInteract(player, hand);
        }

        ItemStack itemStack = player.getItemInHand(hand);
        if (!itemStack.isEmpty()) {
            InteractionResult interactionResult = itemStack.interactLivingEntity(player, this, hand);
            if (interactionResult.consumesAction()) {
                return interactionResult;
            }
        }

        // hurry up yall no late boarding
        if (this.rideState != RideState.IDLE && this.rideState != RideState.BOARDING) {
            return InteractionResult.FAIL;
        }

        if (this.isWearingBodyArmor() && !player.isSecondaryUseActive()) {
            if (!canAddPassenger(player)) {
                player.sendOverlayMessage(Component.translatable(ModConstants.MOD_ID+".no_more_room").withColor(TextColor.RED));
                return InteractionResult.FAIL;
            }

            if (hasPaid(player) || (hasTicket(player) && tryTakeTicket(player))) {
                this.doPlayerRide(player);

                if (this.rideState == RideState.IDLE) {
                    this.rideState = RideState.BOARDING;
                    resetStateTicks();
                }

                return InteractionResult.SUCCESS;
            } else {
                player.sendOverlayMessage(Component.translatable(ModConstants.MOD_ID+".no_ride_ticket").withColor(TextColor.RED));
                return InteractionResult.FAIL;
            }
        }


        return InteractionResult.FAIL;
    }

    @Override
    public void tick() {
        super.tick();

        if (!level().isClientSide()) {
            tickServer();
        }
    }

    protected void tickServer() {
        this.stateTicks++;

        if (this.rideState == RideState.BOARDING) {
            if (this.stateTicks >= BOARDING_DELAY_TICKS) {
                beginDeparture();
            }
        }
    }

    protected void beginDeparture() {
        LivingEntity owner = getOwner();
        if (owner == null || owner.isRemoved() || !(owner instanceof SkyTrader trader)) {
            return;
        }

        System.out.println("BEGIN DEPARTURE");

        trader.startRiding(this);
        this.rideState = RideState.DEPARTING;
        resetStateTicks();
    }

    @Override
    public @Nullable LivingEntity getControllingPassenger() {
        for (Entity passenger : this.getPassengers()) {
            if (passenger instanceof SkyTrader trader) {
                return trader;
            }
        }
        return null;
    }

    @Override
    public @NonNull Vec3 getPassengerAttachmentPoint(@NonNull Entity passenger, @NonNull EntityDimensions dimensions, float partialTick) {
        int index = resolveSeatIndex(passenger);
        return this.getAttachments().getClamped(EntityAttachment.PASSENGER, index, getYRot());
    }

    private int resolveSeatIndex(Entity passenger) {
        if (passenger instanceof SkyTrader) {
            return 0;
        }
        int playerIndex = 0;
        for (Entity entity : this.getPassengers()) {
            if (entity == passenger) {
                return 1 + playerIndex;
            }
            if (!(entity instanceof SkyTrader)) {
                playerIndex++;
            }
        }
        return 1; // fallback, shouldnt happen
    }

    protected void resetStateTicks() {
        this.stateTicks = 0;
    }

    public boolean hasTicket(@NonNull Player player) {
        return player.getInventory().hasAnyOf(Set.of(ModItems.RIDE_TICKET.get()));
    }

    protected boolean tryTakeTicket(Player player) {
        if (this.level().isClientSide()) {
            return false;
        }
        ItemStack ticketStack = findTicket(player);
        if (!ticketStack.isEmpty()) {
            ticketStack.shrink(1);
            this.paidPlayers.add(player.getUUID());
            return true;
        }
        return false;
    }

    protected ItemStack findTicket(Player player) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.is(ModItems.RIDE_TICKET.get())) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    public boolean hasPaid(Player player) {
        return this.paidPlayers.contains(player.getUUID());
    }

    // mojang why must you make this private :(
    protected void doPlayerRide(Player player) {
        if (!this.level().isClientSide()) {
            player.startRiding(this);
        }
    }

    @Override
    public boolean canAddPassenger(@NonNull Entity passenger) {
        if (passenger instanceof SkyTrader) {
            return true;
        }
        long playerRiders = this.getPassengers().stream().filter(e -> e instanceof Player).count();
        return playerRiders < MAX_NON_SKY_TRADER_PASSENGERS;
    }

    public void setOwner(LivingEntity owner) {
        this.owner = EntityReference.of(owner);
    }

    @Override
    public @Nullable LivingEntity getOwner() {
        return OwnableEntity.super.getOwner();
    }

    @Override
    public @Nullable EntityReference<LivingEntity> getOwnerReference() {
        return owner;
    }

    @Override
    public void addAdditionalSaveData(@NonNull ValueOutput output) {
        super.addAdditionalSaveData(output);
        EntityReference.store(owner, output, "Owner");

        ValueOutput.TypedOutputList<UUID> paidList = output.list("PaidPlayers", UUIDUtil.CODEC);
        for (UUID uuid : this.paidPlayers) {
            paidList.add(uuid);
        }

        output.putInt("StateTicks", stateTicks);
    }

    @Override
    public void readAdditionalSaveData(@NonNull ValueInput input) {
        super.readAdditionalSaveData(input);
        this.owner = EntityReference.read(input, "Owner");

        this.paidPlayers.clear();
        input.listOrEmpty("PaidPlayers", UUIDUtil.CODEC).forEach(this.paidPlayers::add);

        stateTicks = input.getIntOr("StateTicks", 0);
    }
}
