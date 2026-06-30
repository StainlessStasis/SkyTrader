package com.example.examplemod.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.phys.Vec3;

public class ModEntities {
    public static final EntityType.Builder<SkyTrader> TRADER_BUILDER =
            EntityType.Builder.of(SkyTrader::new, MobCategory.MISC)
                    .sized(0.6F, 1.95F)
                    .eyeHeight(1.62F)
                    .clientTrackingRange(10);

    public static final EntityType.Builder<SkyTraderGhast> GHAST_BUILDER =
            EntityType.Builder.of(SkyTraderGhast::new, MobCategory.CREATURE)
                    .sized(4.0F, 4.0F)
                    .eyeHeight(2.6F)
                    .passengerAttachments(new Vec3(0.0, 4.0, 1.7), new Vec3(-1.7, 4.0, 0.0), new Vec3(0.0, 4.0, -1.7), new Vec3(1.7, 4.0, 0.0))
                    .ridingOffset(0.5F)
                    .clientTrackingRange(10);

    public static EntityType<SkyTrader> SKY_TRADER;
    public static EntityType<SkyTraderGhast> SKY_TRADER_GHAST;
}
