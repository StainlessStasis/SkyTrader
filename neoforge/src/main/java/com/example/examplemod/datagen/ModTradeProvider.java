package com.example.examplemod.datagen;

import com.example.examplemod.ModConstants;
import com.example.examplemod.ModRegistries;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class ModTradeProvider extends DatapackBuiltinEntriesProvider {
    public ModTradeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider, ModRegistries.SKY_TRADER_TRADES_BUILDER, Set.of(ModConstants.MOD_ID));
    }
}
