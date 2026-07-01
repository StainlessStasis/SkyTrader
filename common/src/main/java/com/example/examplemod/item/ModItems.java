package com.example.examplemod.item;
import com.example.examplemod.platform.Services;
import net.minecraft.world.item.Item;

import java.util.function.Supplier;

public class ModItems {
    public static final Supplier<Item> EXAMPLE_ITEM = Services.REGISTRY.registerItem("example_item",
            (key) -> new Item(new Item.Properties().setId(key))
    );

    public static void init() {}
}
