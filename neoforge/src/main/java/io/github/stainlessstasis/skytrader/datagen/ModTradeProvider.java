package io.github.stainlessstasis.skytrader.datagen;

import io.github.stainlessstasis.skytrader.ModConstants;
import io.github.stainlessstasis.skytrader.trader.SkyTraderTrades;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class ModTradeProvider extends DatapackBuiltinEntriesProvider {
    public ModTradeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider, SkyTraderTrades.REGISTRY_BUILDER, Set.of(ModConstants.MOD_ID));
    }
}
