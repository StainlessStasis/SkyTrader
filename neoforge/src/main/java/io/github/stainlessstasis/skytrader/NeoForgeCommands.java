package io.github.stainlessstasis.skytrader;

import io.github.stainlessstasis.skytrader.trader.SkyTraderSpawner;
import net.minecraft.commands.Commands;
import net.minecraft.server.permissions.Permissions;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@EventBusSubscriber
public class NeoForgeCommands {
    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("skytrader")
                .requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))
                .then(Commands.literal("forcespawn")
                        .executes(context -> {
                            SkyTraderSpawner.forceSpawn(context.getSource().getLevel());
                            return 1;
                        })
                )
        );
    }
}