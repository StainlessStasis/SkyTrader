package io.github.stainlessstasis.skytrader.client;

import io.github.stainlessstasis.skytrader.ModConstants;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.WanderingTraderRenderer;
import net.minecraft.client.renderer.entity.state.VillagerRenderState;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

public class SkyTraderRenderer extends WanderingTraderRenderer {
    private static final Identifier TEXTURE = ModConstants.id("textures/entity/sky_trader.png");

    public SkyTraderRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    public @NonNull Identifier getTextureLocation(@NonNull VillagerRenderState state) {
        return TEXTURE;
    }
}
