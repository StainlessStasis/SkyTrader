package io.github.stainlessstasis.skytrader.entity;

import io.github.stainlessstasis.skytrader.mixin.WanderingTraderInvoker;
import io.github.stainlessstasis.skytrader.trader.SkyTraderTrades;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
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
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.EnumSet;
import java.util.Objects;

public class SkyTrader extends WanderingTrader {
    protected @Nullable EntityReference<LivingEntity> ghast;
    protected int despawnTicks = -1;

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
    }

    @Override
    protected void readAdditionalSaveData(@NonNull ValueInput input) {
        super.readAdditionalSaveData(input);
        this.ghast = EntityReference.read(input, "Ghast");
        this.despawnTicks = input.getIntOr("DespawnTicks", 0);
    }
}