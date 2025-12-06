package net.mosberg.entomology.entity;

import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.mosberg.entomology.init.EntomologyModEntities;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.level.Level;

public class EntityButterflyEntity extends EntityInsectBase {
    
    public EntityButterflyEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level, InsectType.BUTTERFLY);
    }
    
    public static void init(RegisterSpawnPlacementsEvent event) {
        event.register(
            EntomologyModEntities.ENTITY_BUTTERFLY.get(),
            SpawnPlacementTypes.NO_RESTRICTIONS,
            Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
            (entityType, world, reason, pos, random) -> 
                world.getRawBrightness(pos, 0) > 8,
            RegisterSpawnPlacementsEvent.Operation.REPLACE
        );
    }
    
    public static AttributeSupplier.Builder createAttributes() {
        return EntityInsectBase.createInsectAttributes(InsectType.BUTTERFLY);
    }
}
