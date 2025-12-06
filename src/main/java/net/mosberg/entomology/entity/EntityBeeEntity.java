package net.mosberg.entomology.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.level.Level;

public class EntityBeeEntity extends EntityInsectBase {
    
    public EntityBeeEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level, InsectType.BEE);
    }
    
    public static AttributeSupplier.Builder createAttributes() {
        return EntityInsectBase.createInsectAttributes(InsectType.BEE);
    }
}
