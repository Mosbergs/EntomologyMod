// EntityButterflyRenderer.java (TEMPLATE - apply to all renderers)

package net.mosberg.entomology.client.renderer;

import net.mosberg.entomology.entity.EntityButterflyEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.model.BeeModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.state.BeeRenderState;

public class EntityButterflyRenderer extends MobRenderer<EntityButterflyEntity, BeeRenderState> {
    
    private static final ResourceLocation BUTTERFLY_TEXTURE = 
        ResourceLocation.parse("entomology:textures/entities/butterfly.png");
    
    public EntityButterflyRenderer(EntityRendererProvider.Context context) {
        super(context, new BeeModel(context.bakeLayer(ModelLayers.BEE)), 0.5f);
    }
    
    @Override
    public BeeRenderState createRenderState() {
        return new BeeRenderState();
    }
    
    @Override
    protected void extractRenderState(EntityButterflyEntity entity, BeeRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
    }
    
    @Override
    public ResourceLocation getTextureLocation(BeeRenderState state) {
        return BUTTERFLY_TEXTURE;
    }
}
