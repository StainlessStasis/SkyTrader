package io.github.stainlessstasis.skytrader;

import io.github.stainlessstasis.skytrader.trader.SkyTraderSpawner;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.Commands;
import net.minecraft.server.permissions.Permissions;

public class FabricCommands {
    public static void registerCommands() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(Commands.literal("skytrader")
                    .requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))
                    .then(Commands.literal("forcespawn")
                            .executes(context -> {
                                SkyTraderSpawner.forceSpawn(context.getSource().getLevel());
                                return 1;
                            })
                    )
            );
        });
    }
}
