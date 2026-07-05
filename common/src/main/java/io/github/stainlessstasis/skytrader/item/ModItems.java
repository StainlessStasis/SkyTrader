package io.github.stainlessstasis.skytrader.item;
import io.github.stainlessstasis.skytrader.platform.Services;
import net.minecraft.world.item.Item;

import java.util.function.Supplier;

public class ModItems {
    public static final Supplier<SkyfareTicketItem> SKYFARE_TICKET = Services.REGISTRY.registerItem("skyfare_ticket",
            (key) -> new SkyfareTicketItem(
                    new Item.Properties()
                            .setId(key)
                            .stacksTo(16)
            )
    );
    public static final Supplier<SkyflareItem> SKYFLARE = Services.REGISTRY.registerItem("skyflare",
            (key) -> new SkyflareItem(
                    new Item.Properties()
                            .setId(key)
                            .stacksTo(4)
            )
    );

    public static void init() {}
}
