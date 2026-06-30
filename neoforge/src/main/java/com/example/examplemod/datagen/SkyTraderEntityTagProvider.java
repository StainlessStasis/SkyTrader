package com.example.examplemod.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.EntityTypeTagsProvider;
import org.jspecify.annotations.NonNull;

import java.util.concurrent.CompletableFuture;

public class SkyTraderEntityTagProvider extends EntityTypeTagsProvider {
    public SkyTraderEntityTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, String modId) {
        super(output, lookupProvider, modId);
    }

    @Override
    protected void addTags(HolderLookup.@NonNull Provider registries) {
        SkyTraderEntityTags.generate((tag, type) -> this.tag(tag).add(type.builtInRegistryHolder().key()));
    }
}
