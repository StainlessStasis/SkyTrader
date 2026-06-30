package com.example.examplemod.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.wanderingtrader.WanderingTrader;
import net.minecraft.world.level.Level;

public class SkyTrader extends WanderingTrader {
    public SkyTrader(EntityType<? extends WanderingTrader> type, Level level) {
        super(type, level);
    }
}
