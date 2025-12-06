package net.mosberg.entomology.init;

import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.mosberg.entomology.EntomologyMod;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.core.registries.Registries;

@EventBusSubscriber
public class EntomologyModTabs {
    
    public static final DeferredRegister<CreativeModeTab> REGISTRY = 
        DeferredRegister.create(Registries.CREATIVE_MODE_TAB, EntomologyMod.MODID);
    
    @SubscribeEvent
    public static void buildTabContentsVanilla(BuildCreativeModeTabContentsEvent tabData) {
        // Add to Tools & Utilities
        if (tabData.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            tabData.accept(EntomologyModItems.ITEM_BUG_NET.get());
            tabData.accept(EntomologyModItems.BUG_NET_ADVANCED_BASIC.get());
            tabData.accept(EntomologyModItems.BUG_NET_ADVANCED_IRON.get());
            tabData.accept(EntomologyModItems.SPECIMEN_JAR.get());
            tabData.accept(EntomologyModItems.FIELD_GUIDE.get());
        }
        
        // Add to Spawn Eggs
        if (tabData.getTabKey() == CreativeModeTabs.SPAWN_EGGS) {
            tabData.accept(EntomologyModItems.ENTITY_BUTTERFLY_SPAWN_EGG.get());
            tabData.accept(EntomologyModItems.ENTITY_BEETLE_SPAWN_EGG.get());
            tabData.accept(EntomologyModItems.ENTITY_BEE_SPAWN_EGG.get());
            tabData.accept(EntomologyModItems.ENTITY_ANT_SPAWN_EGG.get());
            tabData.accept(EntomologyModItems.ENTITY_GRASSHOPPER_SPAWN_EGG.get());
        }
    }
}
