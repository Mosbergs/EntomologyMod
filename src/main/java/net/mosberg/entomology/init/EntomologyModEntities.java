package net.mosberg.entomology.init;

import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.mosberg.entomology.entity.*;
import net.mosberg.entomology.EntomologyMod;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.EntityType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.Registries;

@EventBusSubscriber
public class EntomologyModEntities {
    
    public static final DeferredRegister<EntityType<?>> REGISTRY = 
        DeferredRegister.create(Registries.ENTITY_TYPE, EntomologyMod.MODID);
    
    public static final DeferredHolder<EntityType<?>, EntityType<EntityButterflyEntity>> ENTITY_BUTTERFLY = 
        REGISTRY.register("butterfly", () ->  // ✅ CORRECT: Use ->
            EntityType.Builder.of(EntityButterflyEntity::new, MobCategory.CREATURE)
                .setShouldReceiveVelocityUpdates(true)
                .setTrackingRange(64)
                .setUpdateInterval(3)
                .sized(0.5f, 0.5f)
                .build(ResourceLocation.fromNamespaceAndPath(EntomologyMod.MODID, "butterfly"))
        );
    
    public static final DeferredHolder<EntityType<?>, EntityType<EntityBeetleEntity>> ENTITY_BEETLE = 
        REGISTRY.register("beetle", () ->  // ✅ CORRECT: Use ->
            EntityType.Builder.of(EntityBeetleEntity::new, MobCategory.CREATURE)
                .setShouldReceiveVelocityUpdates(true)
                .setTrackingRange(64)
                .setUpdateInterval(3)
                .sized(0.4f, 0.3f)
                .build(ResourceLocation.fromNamespaceAndPath(EntomologyMod.MODID, "beetle"))
        );
    
    public static final DeferredHolder<EntityType<?>, EntityType<EntityBeeEntity>> ENTITY_BEE = 
        REGISTRY.register("bee", () ->  // ✅ CORRECT: Use ->
            EntityType.Builder.of(EntityBeeEntity::new, MobCategory.CREATURE)
                .setShouldReceiveVelocityUpdates(true)
                .setTrackingRange(64)
                .setUpdateInterval(3)
                .sized(0.2f, 0.2f)
                .build(ResourceLocation.fromNamespaceAndPath(EntomologyMod.MODID, "bee"))
        );
    
    public static final DeferredHolder<EntityType<?>, EntityType<EntityAntEntity>> ENTITY_ANT = 
        REGISTRY.register("ant", () ->  // ✅ CORRECT: Use ->
            EntityType.Builder.of(EntityAntEntity::new, MobCategory.CREATURE)
                .setShouldReceiveVelocityUpdates(true)
                .setTrackingRange(32)
                .setUpdateInterval(4)
                .sized(0.3f, 0.2f)
                .build(ResourceLocation.fromNamespaceAndPath(EntomologyMod.MODID, "ant"))
        );
    
    public static final DeferredHolder<EntityType<?>, EntityType<EntityGrasshopperEntity>> ENTITY_GRASSHOPPER = 
        REGISTRY.register("grasshopper", () ->  // ✅ CORRECT: Use ->
            EntityType.Builder.of(EntityGrasshopperEntity::new, MobCategory.CREATURE)
                .setShouldReceiveVelocityUpdates(true)
                .setTrackingRange(64)
                .setUpdateInterval(3)
                .sized(0.35f, 0.35f)
                .build(ResourceLocation.fromNamespaceAndPath(EntomologyMod.MODID, "grasshopper"))
        );
    
    @SubscribeEvent
    public static void initSpawnPlacements(RegisterSpawnPlacementsEvent event) {
        EntityButterflyEntity.init(event);
    }
    
    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(ENTITY_BUTTERFLY.get(), EntityButterflyEntity.createAttributes().build());
        event.put(ENTITY_BEETLE.get(), EntityBeetleEntity.createAttributes().build());
        event.put(ENTITY_BEE.get(), EntityBeeEntity.createAttributes().build());
        event.put(ENTITY_ANT.get(), EntityAntEntity.createAttributes().build());
        event.put(ENTITY_GRASSHOPPER.get(), EntityGrasshopperEntity.createAttributes().build());
    }
}
