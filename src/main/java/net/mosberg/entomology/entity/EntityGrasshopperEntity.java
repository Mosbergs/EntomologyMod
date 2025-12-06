package net.mosberg.entomology.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.level.Level;

public class EntityGrasshopperEntity extends EntityInsectBase {
    
    public EntityGrasshopperEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level, InsectType.GRASSHOPPER);
    }
    
    public static AttributeSupplier.Builder createAttributes() {
        return EntityInsectBase.createInsectAttributes(InsectType.GRASSHOPPER);
    }
}
