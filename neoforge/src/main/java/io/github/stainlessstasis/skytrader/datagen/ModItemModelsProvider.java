package io.github.stainlessstasis.skytrader.datagen;

import io.github.stainlessstasis.skytrader.item.ModItems;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.data.PackOutput;
import org.jetbrains.annotations.NotNull;

public class ModItemModelsProvider extends ModelProvider {
    public ModItemModelsProvider(PackOutput output, String modId) {
        super(output, modId);
    }

    @Override
    protected void registerModels(@NotNull BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        itemModels.generateFlatItem(
                ModItems.SKYFARE_TICKET.get(),
                ModelTemplates.FLAT_ITEM
        );
        itemModels.generateFlatItem(
                ModItems.SKYFLARE.get(),
                ModelTemplates.FLAT_ITEM
        );
    }
}
