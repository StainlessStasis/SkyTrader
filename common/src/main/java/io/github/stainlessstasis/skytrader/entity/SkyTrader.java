package io.github.stainlessstasis.skytrader.entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.stainlessstasis.skytrader.ModConstants;
import io.github.stainlessstasis.skytrader.advancement.ModAdvancements;
import io.github.stainlessstasis.skytrader.mixin.WanderingTraderInvoker;
import io.github.stainlessstasis.skytrader.trader.SkyTraderSpawner;
import io.github.stainlessstasis.skytrader.trader.SkyTraderTrades;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.monster.Vex;
import net.minecraft.world.entity.monster.Zoglin;
import net.minecraft.world.entity.monster.illager.Evoker;
import net.minecraft.world.entity.monster.illager.Illusioner;
import net.minecraft.world.entity.monster.illager.Pillager;
import net.minecraft.world.entity.monster.illager.Vindicator;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.npc.wanderingtrader.WanderingTrader;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.*;

public class SkyTrader extends WanderingTrader {
    protected @Nullable EntityReference<LivingEntity> ghast;
    protected int despawnTicks = -1;
    protected @Nullable MerchantOffers flightOffers;
    protected final Map<UUID, Integer> hitCounts = new HashMap<>();

    public SkyTrader(EntityType<? extends WanderingTrader> type, Level level) {
        super(type, level);
    }

    public void setGhast(SkyTraderGhast ghast) {
        this.ghast = EntityReference.of(ghast);
    }

    public @Nullable SkyTraderGhast getGhast() {
        var entity = EntityReference.getLivingEntity(getGhastReference(), this.level());
        if (entity instanceof SkyTraderGhast ghast) return ghast;
        return null;
    }

    public @Nullable EntityReference<LivingEntity> getGhastReference() {
        return ghast;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!this.level().isClientSide()) {
            tickDespawn();
        }
    }

    public void setDespawnTicks(int ticks) {
        this.despawnTicks = ticks;
        this.setDespawnDelay(0); // make vanilla's own despawn logic never fire
    }

    /**
     * WanderingTrader's maybeDespawn() is private, so i had to make this
     */
    protected void tickDespawn() {
        if (despawnTicks > 0) {
            despawnTicks--;
            return;
        }

        if (isTrading()) {
            return;
        }

        SkyTraderGhast ghast = getGhast();
        if (ghast == null) {
            discard();
            return;
        }

        if (ghast.rideState == SkyTraderGhast.RideState.IDLE) {
            discard();
            ghast.discard();
        }
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(
            @NonNull ServerLevelAccessor level, @NonNull DifficultyInstance difficulty, @NonNull EntitySpawnReason spawnReason, @Nullable SpawnGroupData groupData)
    {
        if (spawnReason == EntitySpawnReason.COMMAND) {
            setDespawnTicks(SkyTraderSpawner.DESPAWN_TICKS);
        }
        return super.finalizeSpawn(level, difficulty, spawnReason, groupData);
    }

    @Override
    public void die(@NonNull DamageSource damageSource) {
        super.die(damageSource);

        if (damageSource.getEntity() instanceof Player player) {
            SkyTraderGhast ghast = getGhast();
            if (ghast != null && !ghast.isRemoved()) {
                ghast.turnHostile(player);
            }
        }
    }

    protected void jumpOffSpawnDescent(BlockPos landingSpot) {
        stopRiding();
        if (getGhast() instanceof SkyTraderGhast ghast) {
            ghast.setLeashedTo(this, true);
        }
        setWanderTarget(landingSpot);
        setHomeTo(landingSpot, 16);
    }

    @Override
    protected void updateTrades(@NonNull ServerLevel level) {
        MerchantOffers offers = this.getOffers();
        this.addOffersFromTradeSet(level, offers, SkyTraderTrades.SKY_TRADER_RIDE);
        this.addOffersFromTradeSet(level, offers, SkyTraderTrades.SKY_TRADER_HARNESS);
        this.addOffersFromTradeSet(level, offers, SkyTraderTrades.SKY_TRADER_BOAT);
        this.addOffersFromTradeSet(level, offers, SkyTraderTrades.SKY_TRADER_SKY_UTILITY);
        this.addOffersFromTradeSet(level, offers, SkyTraderTrades.SKY_TRADER_MOUNT_UTILITY);
        this.addOffersFromTradeSet(level, offers, SkyTraderTrades.SKY_TRADER_COMMON);
        this.addOffersFromTradeSet(level, offers, SkyTraderTrades.SKY_TRADER_RARE);
    }

    protected boolean isInFlight() {
        SkyTraderGhast ghast = getGhast();
        return ghast != null && ghast.rideState.hasMovement() && !ghast.spawnDescent;
    }

    @Override
    public @NonNull MerchantOffers getOffers() {
        if (isInFlight() && level() instanceof ServerLevel serverLevel) {
            if (flightOffers == null) {
                flightOffers = new MerchantOffers();
                addOffersFromTradeSet(serverLevel, flightOffers, SkyTraderTrades.SKY_TRADER_SNACKS);
            }
            return flightOffers;
        }

        flightOffers = null;
        return super.getOffers();
    }

    @Override
    public boolean stillValid(@NonNull Player player) {
        if (getTradingPlayer() != player || !isAlive()) {
            return false;
        }

        if (!player.isPassengerOfSameVehicle(this)) {
            return player.isWithinEntityInteractionRange(this, 4f);
        }

        return true;
    }

    @Override
    public boolean hurtServer(@NonNull ServerLevel level, DamageSource source, float damage) {
        if (source.getEntity() instanceof Player player) {
            registerHitFromRudePassenger(player);
        }
        return super.hurtServer(level, source, damage);
    }

    public int registerHitFromRudePassenger(Player player) {
        int newCount = hitCounts.merge(player.getUUID(), 1, Integer::sum);
        if (newCount >= 3) {
            hitCounts.remove(player.getUUID());
            kickPlayerFromFlight(player);
        }
        return newCount;
    }

    protected void kickPlayerFromFlight(Player player) {
        SkyTraderGhast ghast = getGhast();
        if (ghast != null && player.isPassengerOfSameVehicle(ghast)) {
            player.stopRiding();
            player.sendOverlayMessage(Component.translatable(ModConstants.MOD_ID + ".kicked_off_flight").withColor(ModConstants.RED));
            if (player instanceof ServerPlayer serverPlayer) {
                ModAdvancements.grant(serverPlayer, ModAdvancements.NO_FLY_LIST);
            }
        }
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(0, new UseSlowFallingPotionGoal(this));
        this.goalSelector.addGoal(1, new TradeWithPlayerGoal(this));
        this.goalSelector.addGoal(1, new AvoidEntityGoal<>(this, Zombie.class, 8.0F, 0.5, 0.5));
        this.goalSelector.addGoal(1, new AvoidEntityGoal<>(this, Evoker.class, 12.0F, 0.5, 0.5));
        this.goalSelector.addGoal(1, new AvoidEntityGoal<>(this, Vindicator.class, 8.0F, 0.5, 0.5));
        this.goalSelector.addGoal(1, new AvoidEntityGoal<>(this, Vex.class, 8.0F, 0.5, 0.5));
        this.goalSelector.addGoal(1, new AvoidEntityGoal<>(this, Pillager.class, 15.0F, 0.5, 0.5));
        this.goalSelector.addGoal(1, new AvoidEntityGoal<>(this, Illusioner.class, 12.0F, 0.5, 0.5));
        this.goalSelector.addGoal(1, new AvoidEntityGoal<>(this, Zoglin.class, 10.0F, 0.5, 0.5));
        this.goalSelector.addGoal(1, new PanicGoal(this, 0.5));
        this.goalSelector.addGoal(1, new LookAtTradingPlayerGoal(this));
        this.goalSelector.addGoal(2, new WanderToPositionGoal(this, 2.0, 0.35));
        this.goalSelector.addGoal(4, new MoveTowardsRestrictionGoal(this, 0.35));
        this.goalSelector.addGoal(8, new WaterAvoidingRandomStrollGoal(this, 0.35));
        this.goalSelector.addGoal(9, new InteractGoal(this, Player.class, 3.0F, 1.0F));
        this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, Mob.class, 8.0F));
    }

    public static class UseSlowFallingPotionGoal extends UseItemGoal<SkyTrader> {
        public UseSlowFallingPotionGoal(SkyTrader trader) {
            super(
                    trader,
                    PotionContents.createItemStack(Items.POTION, Potions.SLOW_FALLING),
                    SoundEvents.WANDERING_TRADER_DRINK_POTION,
                    skyTrader -> {
                        if (skyTrader.hasEffect(MobEffects.SLOW_FALLING)) {
                            return false;
                        }

                        Level level = skyTrader.level();
                        Vec3 lookAngle = skyTrader.getLookAngle();
                        Vec3 lookAheadVec = skyTrader.position().add(lookAngle.normalize().scale(2));
                        BlockPos lookAheadPos = BlockPos.containing(lookAheadVec.x, skyTrader.getY(), lookAheadVec.z);

                        if (level.getBlockState(lookAheadPos).isAir() && level.getBlockState(lookAheadPos.below()).isAir()) {
                            boolean isLethalDrop = true;
                            for (int i = 2; i <= 6; i++) {
                                if (level.getBlockState(lookAheadPos.below(i)).isSolid()) {
                                    isLethalDrop = false;
                                    break;
                                }
                            }
                            if (isLethalDrop && (Math.abs(lookAngle.x) > 0.05 || Math.abs(lookAngle.z) > 0.05)) {
                                return true;
                            }
                        }

                        return !skyTrader.onGround() && skyTrader.getDeltaMovement().y < -0.1 && skyTrader.fallDistance > 1.25;
                    }
            );
        }
    }

    public static class WanderToPositionGoal extends Goal {
        private final SkyTrader trader;
        private final double stopDistance;
        private final double speedModifier;

        public WanderToPositionGoal(SkyTrader trader, double stopDistance, double speedModifier) {
            this.trader = trader;
            this.stopDistance = stopDistance;
            this.speedModifier = speedModifier;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            BlockPos wanderTarget = getWanderTarget();
            return wanderTarget != null && this.isTooFarAway(wanderTarget, this.stopDistance);
        }

        @Override
        public void start() {
            BlockPos wanderTarget = getWanderTarget();
            if (wanderTarget != null) {
                this.trader.getNavigation().moveTo(wanderTarget.getX(), wanderTarget.getY(), wanderTarget.getZ(), this.speedModifier);
            }
        }

        @Override
        public void stop() {
            this.trader.getNavigation().stop();
        }

        protected boolean isTooFarAway(BlockPos pos, double distance) {
            return !pos.closerToCenterThan(this.trader.position(), distance);
        }

        protected @Nullable BlockPos getWanderTarget() {
            return ((WanderingTraderInvoker)trader).invokeGetWanderTarget();
        }
    }

    @Override
    protected void addAdditionalSaveData(@NonNull ValueOutput output) {
        super.addAdditionalSaveData(output);
        EntityReference.store(ghast, output, "Ghast");
        output.putInt("DespawnTicks", despawnTicks);

        ValueOutput.TypedOutputList<HitEntry> hitList = output.list("HitCounts", HitEntry.CODEC);
        hitCounts.forEach((uuid, count) -> hitList.add(new HitEntry(uuid, count)));
    }

    @Override
    protected void readAdditionalSaveData(@NonNull ValueInput input) {
        super.readAdditionalSaveData(input);
        this.ghast = EntityReference.read(input, "Ghast");
        this.despawnTicks = input.getIntOr("DespawnTicks", 0);

        hitCounts.clear();
        input.listOrEmpty("HitCounts", HitEntry.CODEC).forEach(e -> hitCounts.put(e.uuid(), e.count()));
    }

    record HitEntry(UUID uuid, int count) {
        static final Codec<HitEntry> CODEC = RecordCodecBuilder.create(i -> i.group(
                UUIDUtil.CODEC.fieldOf("UUID").forGetter(HitEntry::uuid),
                Codec.INT.fieldOf("Count").forGetter(HitEntry::count)
        ).apply(i, HitEntry::new));
    }
}