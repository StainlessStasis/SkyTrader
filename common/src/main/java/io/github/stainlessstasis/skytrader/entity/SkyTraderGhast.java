package io.github.stainlessstasis.skytrader.entity;

import com.mojang.serialization.Codec;
import io.github.stainlessstasis.skytrader.ModConstants;
import io.github.stainlessstasis.skytrader.ModGameRules;
import io.github.stainlessstasis.skytrader.VillageLocator;
import io.github.stainlessstasis.skytrader.advancement.ModAdvancements;
import io.github.stainlessstasis.skytrader.item.ModItems;
import io.github.stainlessstasis.skytrader.trader.SkyTraderConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.entity.animal.happyghast.HappyGhast;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.concurrent.CompletableFuture;

import static io.github.stainlessstasis.skytrader.ModConstants.RED;
import static io.github.stainlessstasis.skytrader.ModConstants.WHITE;

public class SkyTraderGhast extends HappyGhast implements TraceableEntity, OwnableEntity {
    protected int cachedTerrainHeight = 0;
    protected int terrainSampleCooldown = 0;

    protected @Nullable EntityReference<LivingEntity> owner;
    protected final Set<UUID> paidPlayers = new HashSet<>();
    protected RideState rideState = RideState.IDLE;
    protected int stateTicks = 0;
    protected int departureAttempts = 0;
    protected int totalBoardingTime = SkyTraderConfig.get().flight.boardingTicks;
    protected BlockPos destination;
    protected @Nullable Vec3 returnDirection;
    protected int landingAttempts = 0;
    protected @Nullable BlockPos villageCenter;
    protected boolean spawnDescent = false;
    protected @Nullable CompletableFuture<BlockPos> pendingVillageSearch = null;

    public SkyTraderGhast(EntityType<? extends HappyGhast> type, Level level) {
        super(type, level);
        this.lookControl = new SkyTraderGhastLookControl();
    }

    public enum RideState implements StringRepresentable {
        IDLE("idle", false, true, false),
        BOARDING("boarding", false, true, false),
        SEARCHING("searching", false, false, false),
        TAKEOFF("takeoff", true, false, false),
        CRUISE("cruise", true, false, false),
        GLIDING("gliding", true, false, false),
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
        if (!itemStack.isEmpty()) {
            InteractionResult interactionResult = itemStack.interactLivingEntity(player, this, hand);
            if (interactionResult.consumesAction()) {
                return interactionResult;
            }
        }

        if (player.isPassengerOfSameVehicle(this)) {
            if (this.rideState.hasMovement() && getOwner() instanceof SkyTrader trader) {
                if (trader.isTrading()) {
                    return InteractionResult.PASS;
                }
                return trader.mobInteract(player, hand);
            }
            return InteractionResult.PASS;
        }

        if (this.level().isClientSide()) {
            return this.isWearingBodyArmor() && !player.isSecondaryUseActive()
                    ? InteractionResult.SUCCESS
                    : InteractionResult.PASS;
        }

        if (this.spawnDescent) {
            player.sendOverlayMessage(Component.translatable(ModConstants.MOD_ID + ".still_arriving").withColor(RED));
            return InteractionResult.FAIL;
        }

        if (!this.rideState.isStartOfRide()) {
            if (this.rideState.isEndOfRide()) {
                player.sendOverlayMessage(Component.translatable(ModConstants.MOD_ID + ".flight_already_ended").withColor(RED));
                return InteractionResult.FAIL;
            }
            if (!hasPaid(player)) {
                player.sendOverlayMessage(Component.translatable(ModConstants.MOD_ID + ".flight_already_started").withColor(RED));
                return InteractionResult.FAIL;
            }
        }

        if (this.isWearingBodyArmor() && !player.isSecondaryUseActive()) {
            if (!canAddPassenger(player)) {
                player.sendOverlayMessage(Component.translatable(ModConstants.MOD_ID + ".no_more_room").withColor(RED));
                return InteractionResult.FAIL;
            }

            if (hasPaid(player) || (hasTicket(player) && tryTakeTicket(player))) {
                this.doPlayerRide(player);

                if (this.rideState == RideState.IDLE) {
                    this.departureAttempts = 0;
                    setRideState(RideState.BOARDING);
                } else if (this.rideState == RideState.BOARDING) {
                    this.totalBoardingTime += SkyTraderConfig.get().flight.boardingExtensionOnNewPassenger;
                }

                return InteractionResult.SUCCESS;
            } else {
                player.sendOverlayMessage(Component.translatable(ModConstants.MOD_ID + ".no_ticket").withColor(RED));
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
        var flight = SkyTraderConfig.get().flight;

        if (this.rideState == RideState.BOARDING) {
            boolean anyPlayers = this.getPassengers().stream().anyMatch(e -> e instanceof Player);
            if (!anyPlayers) {
                setRideState(RideState.IDLE);
            } else {
                if (this.stateTicks % 20 == 0) {
                    sendBoardingCountdown();
                }
                if (this.stateTicks >= this.totalBoardingTime) {
                    beginDeparture();
                }
            }
        }

        if (this.rideState == RideState.SEARCHING && this.stateTicks % 10 == 0) {
            sendMessageToPassengers(ModConstants.MOD_ID + ".searching", WHITE);
        }

        if (this.rideState == RideState.ARRIVED) {
            boolean anyPlayersLeft = this.getPassengers().stream().anyMatch(e -> e instanceof Player);
            if (!anyPlayersLeft) {
                beginReturn();
            } else if (this.stateTicks >= flight.dismountGraceTicks) {
                sendMessageToPassengers(ModConstants.MOD_ID + ".flight_departing", WHITE);
                forceDismountPassengers();
                beginReturn();
            }
        }

        if (this.rideState == RideState.RETURNING && this.stateTicks >= flight.returnFlightTicks) {
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
            this.goalSelector.removeAllGoals(_ -> true);
            this.getMoveControl().setWantedPosition(this.getX(), this.getY(), this.getZ(), 0);
        } else if (this.goalSelector.getAvailableGoals().isEmpty()) {
            this.registerGoals();
        }
    }

    public void beginSpawnDescent(BlockPos groundTarget) {
        this.destination = groundTarget;
        this.spawnDescent = true;
        setRideState(RideState.ARRIVING);
    }

    protected void completeSpawnDescent() {
        this.spawnDescent = false;
        setRideState(RideState.IDLE);

        if (getOwner() instanceof SkyTrader trader && !trader.isRemoved()) {
            BlockPos landingSpot = this.destination != null ? this.destination : this.blockPosition();
            trader.jumpOffSpawnDescent(landingSpot);
        }
    }

    protected void beginDeparture() {
        boolean anyPlayers = this.getPassengers().stream().anyMatch(e -> e instanceof Player);
        if (!anyPlayers) {
            setRideState(RideState.IDLE);
            return;
        }

        if (this.pendingVillageSearch != null) {
            return;
        }

        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        setRideState(RideState.SEARCHING);

        BlockPos searchOrigin = this.blockPosition();
        this.pendingVillageSearch = VillageLocator.findNearestVillageStructureAsync(
                serverLevel, searchOrigin, SkyTraderConfig.get().search.searchRadius);

        this.pendingVillageSearch.whenCompleteAsync((structureCenter, throwable) ->
                serverLevel.getServer().execute(() -> {
                    this.pendingVillageSearch = null;
                    if (this.isRemoved()) {
                        return;
                    }
                    if (throwable != null) {
                        ModConstants.LOG.error(Arrays.toString(throwable.getStackTrace()));
                        handleVillageSearchResult(null, serverLevel);
                        return;
                    }
                    handleVillageSearchResult(structureCenter, serverLevel);
                })
        );
    }

    protected void handleVillageSearchResult(@Nullable BlockPos structureCenter, ServerLevel serverLevel) {
        this.destination = structureCenter != null
                ? VillageLocator.resolveSurfacePosition(serverLevel, structureCenter)
                : null;

        if (this.destination == null) {
            this.departureAttempts++;
            if (this.departureAttempts >= SkyTraderConfig.get().flight.maxDepartureAttempts) {
                sendMessageToPassengers(ModConstants.MOD_ID + ".no_village_giving_up", RED);
                beginReturn();
                forceDismountPassengers();
            } else {
                sendMessageToPassengers(ModConstants.MOD_ID + ".no_village_retry", RED);
                setRideState(RideState.BOARDING);
            }
            return;
        }

        this.departureAttempts = 0;
        this.villageCenter = this.destination;
        this.landingAttempts = 0;

        double distance = Math.sqrt(this.blockPosition().distToCenterSqr(
                this.destination.getX(), this.getY(), this.destination.getZ()));
        sendMessageToPassengers(ModConstants.MOD_ID + ".village_found", WHITE, (int)distance);
        setRideState(RideState.TAKEOFF);
        setOwnerRiding();
    }

    protected void beginReturn() {
        this.paidPlayers.clear();
        double angle = this.random.nextDouble() * Math.PI * 2;
        this.returnDirection = new Vec3(Math.cos(angle), 0, Math.sin(angle));
        setRideState(RideState.RETURNING);
        setOwnerRiding();
    }

    public void setOwnerRiding() {
        LivingEntity ownerEntity = getOwner();
        if (ownerEntity == null || ownerEntity.isRemoved() || !(ownerEntity instanceof SkyTrader trader)) {
            return;
        }
        if (trader.isPassenger()) {
            return;
        }

        trader.startRiding(this, true, true);
    }

    @Override
    public void travel(@NonNull Vec3 input) {
        if (this.level().isClientSide()) {
            super.travel(input);
            return;
        }
        switch (this.rideState) {
            case TAKEOFF -> super.travel(computeTakeoffInput());
            case CRUISE -> super.travel(computeCruiseInput());
            case GLIDING -> super.travel(computeGlidingInput());
            case ARRIVING -> super.travel(computeArrivingInput());
            case ARRIVED, SEARCHING -> {}
            case RETURNING -> super.travel(computeReturnInput());
            default -> super.travel(input);
        }
    }

    protected int terrainHeightAt(BlockPos pos) {
        if (!this.level().hasChunkAt(pos)) {
            return this.cachedTerrainHeight;
        }
        return this.level().getHeight(Heightmap.Types.MOTION_BLOCKING, pos.getX(), pos.getZ());
    }

    protected int sampleMaxTerrainHeight(Vec3 direction, int... lookaheadDistances) {
        if (this.terrainSampleCooldown > 0) {
            this.terrainSampleCooldown--;
            return this.cachedTerrainHeight;
        }
        this.terrainSampleCooldown = SkyTraderConfig.get().flight.terrainSampleInterval;

        int max = terrainHeightAt(this.blockPosition());
        for (int distance : lookaheadDistances) {
            BlockPos sample = this.blockPosition().offset(
                    (int) Math.round(direction.x * distance), 0, (int) Math.round(direction.z * distance));
            max = Math.max(max, terrainHeightAt(sample));
        }
        this.cachedTerrainHeight = max;
        return max;
    }

    protected float computeVerticalInput(int targetTerrainHeight, int hoverHeight, float maxVerticalSpeed) {
        double targetY = targetTerrainHeight + hoverHeight;
        double dy = targetY - this.getY();
        float proportional = (float) Mth.clamp(dy * 0.1, -maxVerticalSpeed, maxVerticalSpeed);
        if (dy > 1.0 && proportional < maxVerticalSpeed * 0.3f) {
            return maxVerticalSpeed * 0.3f;
        }
        return proportional;
    }

    protected void steerYawToward(double dx, double dz) {
        float targetYaw = (float) (Mth.atan2(dz, dx) * (180.0 / Math.PI)) - 90.0F;
        float diff = Mth.wrapDegrees(targetYaw - this.getYRot());
        float newYaw = this.getYRot() + diff * SkyTraderConfig.get().flight.turnSpeed;
        this.setYRot(newYaw);
        this.yRotO = this.yBodyRot = this.yHeadRot = newYaw;
    }

    protected Vec3 computeTakeoffInput() {
        var flight = SkyTraderConfig.get().flight;

        if (this.destination == null) {
            setRideState(RideState.CRUISE);
            sendMessageToPassengers(ModConstants.MOD_ID + ".cruising", WHITE);
            return Vec3.ZERO;
        }

        double dx = this.destination.getX() + 0.5 - this.getX();
        double dz = this.destination.getZ() + 0.5 - this.getZ();
        steerYawToward(dx, dz);

        double heightAboveTerrain = this.getY() - terrainHeightAt(this.blockPosition());
        if (heightAboveTerrain >= flight.takeoffHeight - 2) {
            setRideState(RideState.CRUISE);
            sendMessageToPassengers(ModConstants.MOD_ID + ".cruising", WHITE);
            return Vec3.ZERO;
        }

        Vec3 direction = new Vec3(dx, 0, dz).normalize();
        int terrainY = sampleMaxTerrainHeight(direction, flight.terrainLookaheadDistances);
        float up = computeVerticalInput(terrainY, flight.takeoffHeight, flight.takeoffVerticalSpeed);

        return new Vec3(0, up, flight.takeoffForwardSpeed);
    }

    protected Vec3 computeCruiseInput() {
        var flight = SkyTraderConfig.get().flight;

        if (this.destination == null) {
            return Vec3.ZERO;
        }

        double dx = this.destination.getX() + 0.5 - this.getX();
        double dz = this.destination.getZ() + 0.5 - this.getZ();
        double horizontalDist = Math.sqrt(dx * dx + dz * dz);

        if (horizontalDist < flight.glideStartDistance) {
            setRideState(RideState.GLIDING);
            sendMessageToPassengers(ModConstants.MOD_ID + ".beginning_descent", WHITE);
            return Vec3.ZERO;
        }

        Vec3 direction = new Vec3(dx, 0, dz).normalize();
        steerYawToward(dx, dz);

        int terrainY = sampleMaxTerrainHeight(direction, flight.terrainLookaheadDistances);
        float up = computeVerticalInput(terrainY, flight.cruiseHoverHeight, flight.maxVerticalSpeed);

        if (this.stateTicks > 20 && this.stateTicks % 20 == 0) {
            sendMessageToPassengers(ModConstants.MOD_ID + ".en_route_status", WHITE, Math.round(horizontalDist), estimateTravelSeconds(horizontalDist));
        }

        return new Vec3(0, up, flight.cruiseSpeed);
    }

    protected Vec3 computeGlidingInput() {
        var flight = SkyTraderConfig.get().flight;

        if (this.destination == null) {
            setRideState(RideState.ARRIVING);
            sendMessageToPassengers(ModConstants.MOD_ID + ".arriving", WHITE);
            return Vec3.ZERO;
        }

        double dx = this.destination.getX() + 0.5 - this.getX();
        double dz = this.destination.getZ() + 0.5 - this.getZ();
        double horizontalDist = Math.sqrt(dx * dx + dz * dz);

        if (horizontalDist < flight.finalApproachDistance) {
            setRideState(RideState.ARRIVING);
            sendMessageToPassengers(ModConstants.MOD_ID + ".arriving", WHITE);
            return Vec3.ZERO;
        }

        Vec3 direction = new Vec3(dx, 0, dz).normalize();
        steerYawToward(dx, dz);

        double t = Mth.clamp(
                (horizontalDist - flight.finalApproachDistance) / (flight.glideStartDistance - flight.finalApproachDistance),
                0.0, 1.0);
        int targetHoverHeight = (int) Mth.lerp(t, flight.landingHoverHeight, flight.cruiseHoverHeight);

        int terrainY = sampleMaxTerrainHeight(direction, flight.terrainLookaheadDistances);
        float up = computeVerticalInput(terrainY, targetHoverHeight, flight.maxVerticalSpeed);

        if (this.stateTicks > 20 && this.stateTicks % 20 == 0) {
            sendMessageToPassengers(ModConstants.MOD_ID + ".en_route_status", WHITE, Math.round(horizontalDist), estimateTravelSeconds(horizontalDist));
        }

        return new Vec3(0, up, flight.cruiseSpeed);
    }

    protected Vec3 computeArrivingInput() {
        var flight = SkyTraderConfig.get().flight;

        if (this.destination == null) {
            if (this.spawnDescent) {
                completeSpawnDescent();
            } else {
                setRideState(RideState.ARRIVED);
                sendMessageToPassengers(ModConstants.MOD_ID + ".arrived", WHITE);
            }
            return Vec3.ZERO;
        }

        if (this.spawnDescent && getOwner() instanceof SkyTrader trader
                && trader.isPassenger() && trader.getVehicle() == this) {
            int jumpTerrainY = terrainHeightAt(this.blockPosition());
            double heightAboveTarget = this.getY() - (jumpTerrainY + flight.landingHoverHeight);
            if (heightAboveTarget <= flight.spawnJumpOffHeight) {
                BlockPos landingSpot = this.destination != null ? this.destination : this.blockPosition();
                trader.jumpOffSpawnDescent(landingSpot);
            }
        }

        if (this.stateTicks >= flight.arrivingTimeoutTicks) {
            this.landingAttempts++;
            if (this.landingAttempts >= flight.maxLandingAttempts) {
                if (this.spawnDescent) {
                    completeSpawnDescent();
                } else {
                    sendMessageToPassengers(ModConstants.MOD_ID + ".landing_blocked_giving_up", RED);
                    setRideState(RideState.ARRIVED);
                }
                return Vec3.ZERO;
            }
            if (!this.spawnDescent) {
                sendMessageToPassengers(ModConstants.MOD_ID + ".landing_blocked_retry", RED);
            }
            this.destination = pickNearbyLandingSpot();
            resetStateTicks();
            return Vec3.ZERO;
        }

        double dx = this.destination.getX() + 0.5 - this.getX();
        double dz = this.destination.getZ() + 0.5 - this.getZ();
        double horizontalDist = Math.sqrt(dx * dx + dz * dz);

        int terrainY = terrainHeightAt(this.blockPosition());
        double targetY = terrainY + flight.landingHoverHeight;
        double dy = targetY - this.getY();
        boolean nearGround = Math.abs(dy) < flight.landingArrivedThreshold;

        boolean closeEnough = horizontalDist < flight.finalApproachDistance
                || (nearGround && horizontalDist < flight.landingArrivedRadius);

        if (nearGround && closeEnough) {
            if (this.spawnDescent) {
                completeSpawnDescent();
            } else {
                setRideState(RideState.ARRIVED);
                sendMessageToPassengers(ModConstants.MOD_ID + ".arrived", WHITE);
            }
            return Vec3.ZERO;
        }

        float descentSpeed = this.spawnDescent ? flight.spawnDescentSpeed : flight.descendSpeed;
        float up = (float) Mth.clamp(dy * 0.05, -descentSpeed, descentSpeed);
        float forward = 0f;
        if (horizontalDist > flight.finalApproachDistance) {
            steerYawToward(dx, dz);
            forward = flight.descendHorizontalSpeed;
        }

        return new Vec3(0, up, forward);
    }

    protected BlockPos pickNearbyLandingSpot() {
        int retryRadius = SkyTraderConfig.get().flight.landingRetryRadius;
        BlockPos anchor = this.villageCenter != null ? this.villageCenter : this.destination;
        int offsetX = this.random.nextInt(retryRadius * 2) - retryRadius;
        int offsetZ = this.random.nextInt(retryRadius * 2) - retryRadius;
        BlockPos candidate = anchor.offset(offsetX, 0, offsetZ);
        int surfaceY = terrainHeightAt(candidate);
        return new BlockPos(candidate.getX(), surfaceY, candidate.getZ());
    }

    protected Vec3 computeReturnInput() {
        var flight = SkyTraderConfig.get().flight;

        if (this.returnDirection == null) {
            return Vec3.ZERO;
        }

        steerYawToward(returnDirection.x, returnDirection.z);

        double heightAboveTerrain = this.getY() - terrainHeightAt(this.blockPosition());
        if (heightAboveTerrain < flight.takeoffHeight) {
            int terrainY = sampleMaxTerrainHeight(returnDirection, flight.terrainLookaheadDistances);
            float up = computeVerticalInput(terrainY, flight.takeoffHeight, flight.takeoffVerticalSpeed);
            return new Vec3(0, up, flight.takeoffForwardSpeed);
        }

        int terrainY = sampleMaxTerrainHeight(returnDirection, flight.terrainLookaheadDistances);
        float up = computeVerticalInput(terrainY, flight.cruiseHoverHeight, flight.maxVerticalSpeed);

        return new Vec3(0, up, flight.returnSpeed);
    }

    protected void forceDismountPassengers() {
        for (Entity passenger : List.copyOf(this.getPassengers())) {
            if (!(passenger instanceof SkyTrader)) {
                passenger.stopRiding();
            }
        }
    }

    protected void sendMessageToPassengers(String translationKey, int color, Object... args) {
        for (Entity passenger : this.getPassengers()) {
            if (passenger instanceof Player player) {
                player.sendOverlayMessage(Component.translatable(translationKey, args).withColor(color));
            }
        }
    }

    protected int estimateTravelSeconds(double blocksRemaining) {
        return (int) (blocksRemaining / (SkyTraderConfig.get().flight.cruiseSpeed * 20));
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
        return player.getInventory().hasAnyOf(Set.of(ModItems.SKYFARE_TICKET.get()));
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
            if (stack.is(ModItems.SKYFARE_TICKET.get())) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    public boolean hasPaid(Player player) {
        return this.paidPlayers.contains(player.getUUID());
    }

    protected void doPlayerRide(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            player.startRiding(this);
            ModAdvancements.grant(serverPlayer, ModAdvancements.WELCOME_ABOARD);
        }
    }

    @Override
    public boolean canAddPassenger(@NonNull Entity passenger) {
        if (passenger instanceof SkyTrader) {
            return true;
        }
        long playerRiders = this.getPassengers().stream().filter(e -> e instanceof Player).count();
        return playerRiders < MAX_PASSANGERS-1;
    }

    @Override
    protected void removePassenger(@NonNull Entity passenger) {
        super.removePassenger(passenger);
        boolean isArrival = rideState == RideState.ARRIVING || rideState == RideState.ARRIVED;
        if (isArrival && passenger instanceof LivingEntity living) {
            living.addEffect(new MobEffectInstance(
                    MobEffects.SLOW_FALLING, 100, 0, true, true, true
            ));
        }

        if (rideState == RideState.CRUISE && passenger instanceof ServerPlayer player) {
            ModAdvancements.grant(player, ModAdvancements.FREE_BIRD);
        }
    }

    protected void sendBoardingCountdown() {
        int secondsRemaining = (this.totalBoardingTime - this.stateTicks) / 20;
        for (Entity passenger : this.getPassengers()) {
            if (passenger instanceof Player player) {
                player.sendOverlayMessage(Component.translatable(
                        ModConstants.MOD_ID + ".boarding_countdown", secondsRemaining));
            }
        }
    }

    public void turnHostile(@Nullable LivingEntity target) {
        if (!(level() instanceof ServerLevel serverLevel)) {
            return;
        }
        if (!serverLevel.getGameRules().get(ModGameRules.SKY_TRADER_GHAST_REVENGE.get())) {
            return;
        }

        if (isLeashed()) {
            dropLeash();
        }

        forceDismountPassengers();
        setRideState(RideState.IDLE);

        Ghast ghast = EntityType.GHAST.create(serverLevel, EntitySpawnReason.CONVERSION);
        if (ghast != null) {
            Vec3 pos = new Vec3(getX(), getY(), getZ());
            float xrot = getXRot();
            float yrot = getYRot();
            ghast.setOldPosAndRot(pos, yrot, xrot);
            ghast.setPos(pos);
            ghast.setXRot(xrot);
            ghast.setYRot(yrot);
            ghast.setDeltaMovement(getDeltaMovement());

            if (target != null) {
                ghast.setTarget(target);
            }

            serverLevel.addFreshEntity(ghast);
        }

        discard();
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
    public boolean canBeLeashed() {
        return false;
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
        output.putInt("LandingAttempts", landingAttempts);
        output.putInt("DepartureAttempts", departureAttempts);
        output.putInt("TotalBoardingTime", totalBoardingTime);
        output.store("RideState", RideState.CODEC, this.rideState);
        if (this.destination != null) {
            output.store("Destination", BlockPos.CODEC, this.destination);
        }
        if (this.villageCenter != null) {
            output.store("VillageCenter", BlockPos.CODEC, this.villageCenter);
        }
        if (this.returnDirection != null) {
            output.store("ReturnDirection", Vec3.CODEC, this.returnDirection);
        }
        output.putBoolean("SpawnDescent", this.spawnDescent);
    }

    @Override
    public void readAdditionalSaveData(@NonNull ValueInput input) {
        super.readAdditionalSaveData(input);
        this.owner = EntityReference.read(input, "Owner");
        this.paidPlayers.clear();
        input.listOrEmpty("PaidPlayers", UUIDUtil.CODEC).forEach(this.paidPlayers::add);
        this.stateTicks = input.getIntOr("StateTicks", 0);
        this.landingAttempts = input.getIntOr("LandingAttempts", 0);
        this.departureAttempts = input.getIntOr("DepartureAttempts", 0);
        this.totalBoardingTime = input.getIntOr("TotalBoardingTime", 0);
        this.rideState = input.read("RideState", RideState.CODEC).orElse(RideState.IDLE);
        if (this.rideState == RideState.SEARCHING) {
            this.rideState = RideState.BOARDING;
        }
        this.destination = input.read("Destination", BlockPos.CODEC).orElse(null);
        this.villageCenter = input.read("VillageCenter", BlockPos.CODEC).orElse(null);
        this.returnDirection = input.read("ReturnDirection", Vec3.CODEC).orElse(null);
        this.spawnDescent = input.getBooleanOr("SpawnDescent", false);
        applyGoalsForState(this.rideState);
    }

    protected class SkyTraderGhastLookControl extends LookControl {
        SkyTraderGhastLookControl() {
            super(SkyTraderGhast.this);
        }

        @Override
        public void tick() {
            if (SkyTraderGhast.this.rideState.hasMovement()) {
                return;
            }
            super.tick();
        }
    }
}