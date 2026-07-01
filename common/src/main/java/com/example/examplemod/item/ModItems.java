package com.example.examplemod.item;
import com.example.examplemod.platform.Services;
import net.minecraft.world.item.Item;

import java.util.function.Supplier;

public class ModItems {
    public static final Supplier<RideTicketItem> RIDE_TICKET = Services.REGISTRY.registerItem("ride_ticket",
            (key) -> new RideTicketItem(new Item.Properties().setId(key))
    );

    public static void init() {}
}
