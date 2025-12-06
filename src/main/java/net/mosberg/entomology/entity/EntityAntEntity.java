package net.mosberg.entomology.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.level.Level;

public class EntityAntEntity extends EntityInsectBase {
    
    public EntityAntEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level, InsectType.ANT);
    }
    
    public static AttributeSupplier.Builder createAttributes() {
        return EntityInsectBase.createInsectAttributes(InsectType.ANT);
    }
}
