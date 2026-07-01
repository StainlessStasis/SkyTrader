package io.github.stainlessstasis.skytrader.entity;

import com.mojang.serialization.Codec;
import io.github.stainlessstasis.skytrader.ModConstants;
import io.github.stainlessstasis.skytrader.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.StructureTags;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.happyghast.HappyGhast;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class SkyTraderGhast extends HappyGhast implements TraceableEntity, OwnableEntity {
    protected static final int VILLAGE_SEARCH_RADIUS = 200;
    protected static final int BOARDING_DELAY_TICKS = 100;
    protected static final int MAX_NON_SKY_TRADER_PASSENGERS = 3;
    protected static final int HOVER_HEIGHT = 20;
    protected static final float EN_ROUTE_SPEED = 0.4f;
    protected static final double EN_ROUTE_ARRIVE_DISTANCE = 6d;
    protected static final float TURN_SPEED = 0.12f;
    protected static final int LANDING_HOVER_HEIGHT = 1;
    protected static final double LANDING_ARRIVED_THRESHOLD = 2d;
    protected static final float DESCEND_SPEED = 0.3f;
    protected static final float DESCEND_HORIZONTAL_SPEED = 0.2f;
    protected static final int DISMOUNT_GRACE_TICKS = 300;
    protected static final int RETURN_FLIGHT_TICKS = 600;
    protected static final float RETURN_SPEED = 0.4f;
    protected static final float MAX_VERTICAL_SPEED = 0.5f;
    protected static final int[] TERRAIN_LOOKAHEAD_DISTANCES = {8, 16, 24, 32};
    protected static final int MAX_DEPARTURE_ATTEMPTS = 3;

    protected @Nullable EntityReference<LivingEntity> owner;
    protected final Set<UUID> paidPlayers = new HashSet<>();
    protected RideState rideState = RideState.IDLE;
    protected int stateTicks = 0;
    protected int departureAttempts = 0;
    protected BlockPos destination;
    private @Nullable Vec3 returnDirection;

    public SkyTraderGhast(EntityType<? extends HappyGhast> type, Level level) {
        super(type, level);
    }

    public enum RideState implements StringRepresentable {
        IDLE("idle", false, true, false),
        BOARDING("boarding", false, true, false),
        EN_ROUTE("en_route", true, false, false),
        ARRIVING("arriving", true, false, false),
        ARRIVED("arrived", false, false, true),
        RETURNING("returning", true, false, true);

        public static final Codec<RideState> CODEC = StringRepresentable.fromEnum(RideState::values);

        private final String name;
        private final boolean hasMovement;
        private final boolean isStartOfRide;
        private final boolean isEndOfRide;

        RideState(String name, boolean hasMovement, boolean isStartOfRide, boolean isEndOfRide) {
            this.name = name;
            this.hasMovement = hasMovement;
            this.isStartOfRide = isStartOfRide;
            this.isEndOfRide = isEndOfRide;
        }

        @Override
        public @NonNull String getSerializedName() {
            return name;
        }

        public boolean hasMovement() {
            return this.hasMovement;
        }

        public boolean isStartOfRide() {
            return this.isStartOfRide;
        }

        public boolean isEndOfRide() {
            return this.isEndOfRide;
        }
    }

    @Override
    public @NonNull InteractionResult mobInteract(@NonNull Player player, @NonNull InteractionHand hand) {
        if (this.isBaby()) {
            return super.mobInteract(player, hand);
        }

        ItemStack itemStack = player.getItemInHand(hand);
        if (itemStack.is(Items.LEAD)) {
            return InteractionResult.FAIL;
        }
        if (!itemStack.isEmpty()) {
            InteractionResult interactionResult = itemStack.interactLivingEntity(player, this, hand);
            if (interactionResult.consumesAction()) {
                return interactionResult;
            }
        }

        if (!this.rideState.isStartOfRide()) {
            if (!hasPaid(player) || this.rideState.isEndOfRide()) {
                player.sendOverlayMessage(Component.translatable(ModConstants.MOD_ID + ".ride_already_started").withColor(TextColor.RED));
                return InteractionResult.FAIL;
            }
        }

        if (this.isWearingBodyArmor() && !player.isSecondaryUseActive()) {
            if (!canAddPassenger(player)) {
                player.sendOverlayMessage(Component.translatable(ModConstants.MOD_ID + ".no_more_room").withColor(TextColor.RED));
                return InteractionResult.FAIL;
            }

            if (hasPaid(player) || (hasTicket(player) && tryTakeTicket(player))) {
                this.doPlayerRide(player);

                if (this.rideState == RideState.IDLE) {
                    this.departureAttempts = 0;
                    setRideState(RideState.BOARDING);
                }

                return InteractionResult.SUCCESS;
            } else {
                player.sendOverlayMessage(Component.translatable(ModConstants.MOD_ID + ".no_ride_ticket").withColor(TextColor.RED));
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
            boolean anyPlayers = this.getPassengers().stream().anyMatch(e -> e instanceof Player);
            if (!anyPlayers) {
                setRideState(RideState.IDLE);
            } else {
                if (this.stateTicks % 20 == 0) {
                    sendBoardingCountdown();
                }
                if (this.stateTicks >= BOARDING_DELAY_TICKS) {
                    beginDeparture();
                }
            }
        }

        if (this.rideState == RideState.ARRIVED) {
            boolean anyPlayersLeft = this.getPassengers().stream().anyMatch(e -> e instanceof Player);
            if (!anyPlayersLeft) {
                beginReturn();
            } else if (this.stateTicks >= DISMOUNT_GRACE_TICKS) {
                forceDismountPlayers();
                beginReturn();
            }
        }

        if (this.rideState == RideState.RETURNING && this.stateTicks >= RETURN_FLIGHT_TICKS) {
            if (this.getOwner() instanceof SkyTrader trader) trader.discard();
            this.discard();
        }
    }

    protected void setRideState(RideState newState) {
        this.rideState = newState;
        resetStateTicks();
        applyGoalsForState(newState);
    }

    protected void applyGoalsForState(RideState newState) {
        if (newState.hasMovement()) {
            this.goalSelector.removeAllGoals(goal -> true);
        } else if (this.goalSelector.getAvailableGoals().isEmpty()) {
            this.registerGoals();
        }
    }

    protected void beginDeparture() {
        boolean anyPlayers = this.getPassengers().stream().anyMatch(e -> e instanceof Player);
        if (!anyPlayers) {
            setRideState(RideState.IDLE);
            return;
        }

        this.destination = findNearestVillage();
        if (this.destination == null) {
            this.departureAttempts++;
            if (this.departureAttempts >= MAX_DEPARTURE_ATTEMPTS) {
                sendMessageToPassengers(ModConstants.MOD_ID + ".no_village_giving_up");
                forceDismountPlayers();
                beginReturn();
            } else {
                sendMessageToPassengers(ModConstants.MOD_ID + ".no_village_retry");
                setRideState(RideState.BOARDING);
            }
            return;
        }

        this.departureAttempts = 0;
        setOwnerRiding();
        setRideState(RideState.EN_ROUTE);
    }

    protected void beginReturn() {
        setOwnerRiding();
        this.paidPlayers.clear();
        double angle = this.random.nextDouble() * Math.PI * 2;
        this.returnDirection = new Vec3(Math.cos(angle), 0, Math.sin(angle));
        setRideState(RideState.RETURNING);
    }

    protected void setOwnerRiding() {
        LivingEntity ownerEntity = getOwner();
        if (ownerEntity == null || ownerEntity.isRemoved() || !(ownerEntity instanceof SkyTrader trader)) {
            return;
        }
        if (trader.isPassenger()) {
            return;
        }
        trader.startRiding(this);
    }

    protected @Nullable BlockPos findNearestVillage() {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return null;
        }

        var structureRegistry = serverLevel.registryAccess().lookupOrThrow(Registries.STRUCTURE);
        var villageTag = structureRegistry.get(StructureTags.VILLAGE);
        if (villageTag.isEmpty()) {
            return null;
        }

        var closestVillage = serverLevel.getChunkSource().getGenerator().findNearestMapStructure(
                serverLevel,
                villageTag.get(),
                this.blockPosition(),
                VILLAGE_SEARCH_RADIUS / 16,
                false
        );

        if (closestVillage == null) {
            return null;
        }

        BlockPos structureCenter = closestVillage.getFirst();

        double distanceSq = this.blockPosition().distToCenterSqr(structureCenter.getX(), this.getY(), structureCenter.getZ());
        if (distanceSq > (VILLAGE_SEARCH_RADIUS * VILLAGE_SEARCH_RADIUS)) {
            return null;
        }

        int surfaceY = serverLevel.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, structureCenter.getX(), structureCenter.getZ());
        if (surfaceY <= serverLevel.getMinY()) {
            var randomState = serverLevel.getChunkSource().randomState();
            surfaceY = serverLevel.getChunkSource().getGenerator().getBaseHeight(
                    structureCenter.getX(),
                    structureCenter.getZ(),
                    Heightmap.Types.WORLD_SURFACE,
                    serverLevel,
                    randomState
            );
        }

        if (surfaceY <= serverLevel.getMinY()) {
            surfaceY = serverLevel.getSeaLevel();
        }

        return new BlockPos(structureCenter.getX(), surfaceY, structureCenter.getZ());
    }

    @Override
    public void travel(@NonNull Vec3 input) {
        if (this.level().isClientSide()) {
            super.travel(input);
            return;
        }
        switch (this.rideState) {
            case EN_ROUTE -> super.travel(computeRouteInput());
            case ARRIVING -> super.travel(computeArrivingInput());
            case ARRIVED -> {}
            case RETURNING -> super.travel(computeReturnInput());
            default -> super.travel(input);
        }
    }

    protected int terrainHeightAt(BlockPos pos) {
        return this.level().getHeight(Heightmap.Types.MOTION_BLOCKING, pos.getX(), pos.getZ());
    }

    protected int sampleMaxTerrainHeight(Vec3 direction, int... lookaheadDistances) {
        int max = terrainHeightAt(this.blockPosition());
        for (int distance : lookaheadDistances) {
            BlockPos sample = this.blockPosition().offset(
                    (int) Math.round(direction.x * distance), 0, (int) Math.round(direction.z * distance));
            max = Math.max(max, terrainHeightAt(sample));
        }
        return max;
    }

    protected float computeVerticalInput(int targetTerrainHeight, int hoverHeight, float maxVerticalSpeed) {
        double targetY = targetTerrainHeight + hoverHeight;
        double dy = targetY - this.getY();
        return (float) Mth.clamp(dy * 0.1, -maxVerticalSpeed, maxVerticalSpeed);
    }

    protected void steerYawToward(double dx, double dz) {
        float targetYaw = (float) (Mth.atan2(dz, dx) * (180.0 / Math.PI)) - 90.0F;
        float diff = Mth.wrapDegrees(targetYaw - this.getYRot());
        float newYaw = this.getYRot() + diff * TURN_SPEED;
        this.setYRot(newYaw);
        this.yRotO = this.yBodyRot = this.yHeadRot = newYaw;
    }

    protected Vec3 computeRouteInput() {
        if (this.destination == null) {
            return Vec3.ZERO;
        }

        double dx = this.destination.getX() + 0.5 - this.getX();
        double dz = this.destination.getZ() + 0.5 - this.getZ();
        double horizontalDist = Math.sqrt(dx * dx + dz * dz);

        if (horizontalDist < EN_ROUTE_ARRIVE_DISTANCE) {
            setRideState(RideState.ARRIVING);
            this.setDeltaMovement(0, this.getDeltaMovement().y, 0);
            return Vec3.ZERO;
        }

        Vec3 direction = new Vec3(dx, 0, dz).normalize();
        steerYawToward(dx, dz);

        int terrainY = sampleMaxTerrainHeight(direction, TERRAIN_LOOKAHEAD_DISTANCES);
        float up = computeVerticalInput(terrainY, HOVER_HEIGHT, MAX_VERTICAL_SPEED);

        return new Vec3(0, up, EN_ROUTE_SPEED);
    }

    protected Vec3 computeArrivingInput() {
        if (this.destination == null) {
            setRideState(RideState.ARRIVED);
            return Vec3.ZERO;
        }

        double dx = this.destination.getX() + 0.5 - this.getX();
        double dz = this.destination.getZ() + 0.5 - this.getZ();
        double horizontalDist = Math.sqrt(dx * dx + dz * dz);

        int terrainY = terrainHeightAt(this.blockPosition());
        double targetY = terrainY + LANDING_HOVER_HEIGHT;
        double dy = targetY - this.getY();

        if (Math.abs(dy) < LANDING_ARRIVED_THRESHOLD && horizontalDist < EN_ROUTE_ARRIVE_DISTANCE) {
            setRideState(RideState.ARRIVED);
            return Vec3.ZERO;
        }

        float up = (float) Mth.clamp(dy * 0.05, -DESCEND_SPEED, DESCEND_SPEED);
        float forward = 0f;
        if (horizontalDist > EN_ROUTE_ARRIVE_DISTANCE) {
            steerYawToward(dx, dz);
            forward = DESCEND_HORIZONTAL_SPEED;
        }

        return new Vec3(0, up, forward);
    }

    protected Vec3 computeReturnInput() {
        if (this.returnDirection == null) {
            return Vec3.ZERO;
        }

        steerYawToward(returnDirection.x, returnDirection.z);

        int terrainY = sampleMaxTerrainHeight(returnDirection, TERRAIN_LOOKAHEAD_DISTANCES);
        float up = computeVerticalInput(terrainY, HOVER_HEIGHT, MAX_VERTICAL_SPEED);

        return new Vec3(0, up, RETURN_SPEED);
    }

    protected void forceDismountPlayers() {
        for (Entity passenger : List.copyOf(this.getPassengers())) {
            if (!(passenger instanceof SkyTrader)) {
                passenger.stopRiding();
            }
        }
    }

    protected void sendMessageToPassengers(String translationKey) {
        for (Entity passenger : this.getPassengers()) {
            if (passenger instanceof Player player) {
                player.sendOverlayMessage(Component.translatable(translationKey).withColor(TextColor.RED));
            }
        }
    }

    @Override
    public @Nullable LivingEntity getControllingPassenger() {
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
        return 1;
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

    protected void sendBoardingCountdown() {
        int secondsRemaining = (BOARDING_DELAY_TICKS - this.stateTicks) / 20;
        for (Entity passenger : this.getPassengers()) {
            if (passenger instanceof Player player) {
                player.sendOverlayMessage(Component.translatable(
                        ModConstants.MOD_ID + ".boarding_countdown", secondsRemaining));
            }
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

        output.putInt("StateTicks", stateTicks);
        output.putInt("DepartureAttempts", departureAttempts);
        output.store("RideState", RideState.CODEC, this.rideState);
        if (this.destination != null) {
            output.store("Destination", BlockPos.CODEC, this.destination);
        }
        if (this.returnDirection != null) {
            output.store("ReturnDirection", Vec3.CODEC, this.returnDirection);
        }
    }

    @Override
    public void readAdditionalSaveData(@NonNull ValueInput input) {
        super.readAdditionalSaveData(input);
        this.owner = EntityReference.read(input, "Owner");

        this.paidPlayers.clear();
        input.listOrEmpty("PaidPlayers", UUIDUtil.CODEC).forEach(this.paidPlayers::add);

        stateTicks = input.getIntOr("StateTicks", 0);
        departureAttempts = input.getIntOr("DepartureAttempts", 0);
        this.rideState = input.read("RideState", RideState.CODEC).orElse(RideState.IDLE);
        this.destination = input.read("Destination", BlockPos.CODEC).orElse(null);
        this.returnDirection = input.read("ReturnDirection", Vec3.CODEC).orElse(null);
        applyGoalsForState(this.rideState);
    }
}