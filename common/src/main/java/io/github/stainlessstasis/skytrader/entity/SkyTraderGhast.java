package io.github.stainlessstasis.skytrader.entity;

import io.github.stainlessstasis.skytrader.item.ModItems;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.happyghast.HappyGhast;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class SkyTraderGhast extends HappyGhast implements TraceableEntity, OwnableEntity {
    private @Nullable EntityReference<LivingEntity> owner;
    private final Set<UUID> paidPlayers = new HashSet<>();

    public SkyTraderGhast(EntityType<? extends HappyGhast> type, Level level) {
        super(type, level);
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

        if (this.isWearingBodyArmor() && !player.isSecondaryUseActive()) {
            if (hasPaid(player) || (hasTicket(player) && tryTakeTicket(player))) {
                this.doPlayerRide(player);
                return InteractionResult.SUCCESS;
            } else {
                player.sendOverlayMessage(Component.literal("test"));
                return InteractionResult.FAIL;
            }
        }


        return InteractionResult.FAIL;
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
    }

    @Override
    public void readAdditionalSaveData(@NonNull ValueInput input) {
        super.readAdditionalSaveData(input);
        this.owner = EntityReference.read(input, "Owner");

        this.paidPlayers.clear();
        input.listOrEmpty("PaidPlayers", UUIDUtil.CODEC).forEach(this.paidPlayers::add);
    }
}
