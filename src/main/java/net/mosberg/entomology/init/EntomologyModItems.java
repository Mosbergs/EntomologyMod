package net.mosberg.entomology.init;

import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredItem;
import net.mosberg.entomology.item.*;
import net.mosberg.entomology.EntomologyMod;
import net.minecraft.world.item.Item;

public class EntomologyModItems {
    
    public static final DeferredRegister.Items REGISTRY = DeferredRegister.createItems(EntomologyMod.MODID);
    
    // Items
    public static final DeferredItem<Item> ITEM_BUG_NET = 
        REGISTRY.registerItem("bug_net", ItemBugNetItem::new, new Item.Properties());
    
    public static final DeferredItem<Item> SPECIMEN_JAR = 
        REGISTRY.registerItem("specimen_jar", SpecimenJarItem::new, new Item.Properties());
    
    public static final DeferredItem<Item> FIELD_GUIDE = 
        REGISTRY.registerItem("field_guide", FieldGuideItem::new, new Item.Properties());
    
    // Spawn eggs would use:
    // event.registerEntityRenderer with SpawnEggItem - SpawnEggItem now takes just EntityType and Properties
}
