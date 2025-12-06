package net.mosberg.entomology.item;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.core.component.DataComponents;

public class FieldGuideItem extends Item {
    
    private static final String NBT_DISCOVERIES = "Discoveries";
    private static final String NBT_OBSERVATIONS = "TotalObservations";
    private static final String NBT_COMPLETION = "CompletionPercent";
    private static final String NBT_NAME = "InsectName";
    private static final String NBT_RARITY = "Rarity";
    private static final String NBT_OBSERVATION_COUNT = "ObservationCount";
    private static final String NBT_BIOMES = "BiomesFound";
    
    private static final int TOTAL_DISCOVERABLE = 20;
    
    public FieldGuideItem() {
        super(new Item.Properties().stacksTo(1));
    }
    
    public void registerDiscovery(ItemStack guideStack, String insectName,
                                  String rarity, String biome) {
        ensureTag(guideStack);
        
        CompoundTag tag = getOrCreateTag(guideStack);
        ListTag discoveries = tag.getList(NBT_DISCOVERIES, Tag.TAG_COMPOUND);
        
        // Check if already discovered
        for (int i = 0; i < discoveries.size(); i++) {
            CompoundTag entry = discoveries.getCompound(i);
            if (entry.getString(NBT_NAME).equals(insectName)) {
                // Update existing entry
                int observations = entry.getInt(NBT_OBSERVATION_COUNT);
                entry.putInt(NBT_OBSERVATION_COUNT, observations + 1);
                
                // Add biome if not present
                ListTag biomes = entry.getList(NBT_BIOMES, Tag.TAG_STRING);
                boolean hasBiome = false;
                for (int j = 0; j < biomes.size(); j++) {
                    if (biomes.getString(j).equals(biome)) {
                        hasBiome = true;
                        break;
                    }
                }
                if (!hasBiome) {
                    biomes.add(Tag.TAG_STRING, Tag.newString(biome));
                    entry.put(NBT_BIOMES, biomes);
                }
                
                discoveries.set(i, entry);
                tag.put(NBT_DISCOVERIES, discoveries);
                tag.putInt(NBT_OBSERVATIONS, tag.getInt(NBT_OBSERVATIONS) + 1);
                updateCompletionPercent(tag);
                guideStack.set(DataComponents.CUSTOM_DATA, tag.copy());
                return;
            }
        }
        
        // New discovery
        CompoundTag newDiscovery = new CompoundTag();
        newDiscovery.putString(NBT_NAME, insectName);
        newDiscovery.putString(NBT_RARITY, rarity);
        newDiscovery.putInt(NBT_OBSERVATION_COUNT, 1);
        
        ListTag biomeList = new ListTag();
        biomeList.add(Tag.TAG_STRING, Tag.newString(biome));
        newDiscovery.put(NBT_BIOMES, biomeList);
        
        discoveries.add(newDiscovery);
        tag.put(NBT_DISCOVERIES, discoveries);
        tag.putInt(NBT_OBSERVATIONS, tag.getInt(NBT_OBSERVATIONS) + 1);
        updateCompletionPercent(tag);
        guideStack.set(DataComponents.CUSTOM_DATA, tag.copy());
    }
    
    public int getDiscoveredCount(ItemStack guideStack) {
        CompoundTag tag = getOrCreateTag(guideStack);
        return tag.getList(NBT_DISCOVERIES, Tag.TAG_COMPOUND).size();
    }
    
    public int getTotalObservations(ItemStack guideStack) {
        CompoundTag tag = getOrCreateTag(guideStack);
        return tag.getInt(NBT_OBSERVATIONS);
    }
    
    public int getCompletionPercent(ItemStack guideStack) {
        CompoundTag tag = getOrCreateTag(guideStack);
        return tag.getInt(NBT_COMPLETION);
    }
    
    private void updateCompletionPercent(CompoundTag tag) {
        int discovered = tag.getList(NBT_DISCOVERIES, Tag.TAG_COMPOUND).size();
        int percent = Math.min(100, (discovered * 100) / TOTAL_DISCOVERABLE);
        tag.putInt(NBT_COMPLETION, percent);
    }
    
    public int getRarityCount(ItemStack guideStack, String rarity) {
        CompoundTag tag = getOrCreateTag(guideStack);
        ListTag discoveries = tag.getList(NBT_DISCOVERIES, Tag.TAG_COMPOUND);
        int count = 0;
        
        for (int i = 0; i < discoveries.size(); i++) {
            CompoundTag entry = discoveries.getCompound(i);
            if (entry.getString(NBT_RARITY).equalsIgnoreCase(rarity)) {
                count++;
            }
        }
        return count;
    }
    
    public CompoundTag getInsectInfo(ItemStack guideStack, String insectName) {
        CompoundTag tag = getOrCreateTag(guideStack);
        ListTag discoveries = tag.getList(NBT_DISCOVERIES, Tag.TAG_COMPOUND);
        
        for (int i = 0; i < discoveries.size(); i++) {
            CompoundTag entry = discoveries.getCompound(i);
            if (entry.getString(NBT_NAME).equals(insectName)) {
                return entry;
            }
        }
        return null;
    }
    
    @Override
    public Component getName(ItemStack stack) {
        int discovered = getDiscoveredCount(stack);
        int completion = getCompletionPercent(stack);
        
        String completionColor = completion >= 75 ? "§a" :
                                completion >= 50 ? "§e" :
                                completion >= 25 ? "§c" : "§4";
        
        return Component.literal(String.format(
            "§6Field Guide §7(%d species, %s%d%%§7)",
            discovered, completionColor, completion
        ));
    }
    
    private void ensureTag(ItemStack stack) {
        if (!stack.has(DataComponents.CUSTOM_DATA)) {
            stack.set(DataComponents.CUSTOM_DATA, new CompoundTag());
        }
    }
    
    private CompoundTag getOrCreateTag(ItemStack stack) {
        if (!stack.has(DataComponents.CUSTOM_DATA)) {
            CompoundTag tag = new CompoundTag();
            stack.set(DataComponents.CUSTOM_DATA, tag);
            return tag;
        }
        return stack.get(DataComponents.CUSTOM_DATA).copy();
    }
}
