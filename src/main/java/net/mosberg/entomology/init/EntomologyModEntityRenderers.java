package net.mosberg.entomology.init;

import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.api.distmarker.Dist;
import net.mosberg.entomology.client.renderer.*;

@EventBusSubscriber(modid = "entomology", value = Dist.CLIENT)
public class EntomologyModEntityRenderers {
    
    @SubscribeEvent
    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(EntomologyModEntities.ENTITY_BUTTERFLY.get(), 
            EntityButterflyRenderer::new);
        event.registerEntityRenderer(EntomologyModEntities.ENTITY_BEETLE.get(), 
            EntityBeetleRenderer::new);
        event.registerEntityRenderer(EntomologyModEntities.ENTITY_BEE.get(), 
            EntityBeeRenderer::new);
        event.registerEntityRenderer(EntomologyModEntities.ENTITY_ANT.get(), 
            EntityAntRenderer::new);
        event.registerEntityRenderer(EntomologyModEntities.ENTITY_GRASSHOPPER.get(), 
            EntityGrasshopperRenderer::new);
    }
}
