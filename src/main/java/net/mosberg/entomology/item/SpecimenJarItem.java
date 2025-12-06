package net.mosberg.entomology.item;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.core.component.DataComponents;

public class SpecimenJarItem extends Item {
    
    private static final String NBT_SPECIMENS = "Specimens";
    private static final String NBT_PRESERVATION = "Preservation";
    private static final String NBT_SPECIES_NAME = "SpeciesName";
    private static final String NBT_RARITY = "Rarity";
    
    public static final int MAX_SPECIMENS = 64;
    
    public SpecimenJarItem() {
        super(new Item.Properties().stacksTo(1));
    }
    
    public boolean addSpecimen(ItemStack jarStack, String speciesName, int health,
                               String rarity, String biome) {
        ensureTag(jarStack);
        
        CompoundTag tag = getOrCreateTag(jarStack);
        ListTag specimens = tag.getList(NBT_SPECIMENS, Tag.TAG_COMPOUND);
        
        if (specimens.size() >= MAX_SPECIMENS) {
            return false;
        }
        
        // Check if species exists and update
        for (int i = 0; i < specimens.size(); i++) {
            CompoundTag specimen = specimens.getCompound(i);
            if (specimen.getString(NBT_SPECIES_NAME).equals(speciesName)) {
                specimen.putInt("Count", specimen.getInt("Count") + 1);
                specimens.set(i, specimen);
                tag.put(NBT_SPECIMENS, specimens);
                jarStack.set(DataComponents.CUSTOM_DATA, tag);
                return true;
            }
        }
        
        // Add new specimen
        CompoundTag newSpecimen = new CompoundTag();
        newSpecimen.putString(NBT_SPECIES_NAME, speciesName);
        newSpecimen.putInt("Health", health);
        newSpecimen.putString(NBT_RARITY, rarity);
        newSpecimen.putString("Biome", biome);
        newSpecimen.putLong("CollectedDate", System.currentTimeMillis());
        newSpecimen.putInt(NBT_PRESERVATION, 100);
        newSpecimen.putInt("Count", 1);
        
        specimens.add(newSpecimen);
        tag.put(NBT_SPECIMENS, specimens);
        jarStack.set(DataComponents.CUSTOM_DATA, tag);
        return true;
    }
    
    public CompoundTag removeSpecimen(ItemStack jarStack, int index) {
        CompoundTag tag = getOrCreateTag(jarStack);
        ListTag specimens = tag.getList(NBT_SPECIMENS, Tag.TAG_COMPOUND);
        
        if (index < 0 || index >= specimens.size()) {
            return null;
        }
        
        CompoundTag specimen = specimens.getCompound(index);
        specimens.remove(index);
        tag.put(NBT_SPECIMENS, specimens);
        jarStack.set(DataComponents.CUSTOM_DATA, tag);
        return specimen;
    }
    
    public int getSpecimenCount(ItemStack jarStack) {
        CompoundTag tag = getOrCreateTag(jarStack);
        return tag.getList(NBT_SPECIMENS, Tag.TAG_COMPOUND).size();
    }
    
    public ListTag getSpecimens(ItemStack jarStack) {
        CompoundTag tag = getOrCreateTag(jarStack);
        return tag.getList(NBT_SPECIMENS, Tag.TAG_COMPOUND);
    }
    
    public void updatePreservation(ItemStack jarStack) {
        CompoundTag tag = getOrCreateTag(jarStack);
        ListTag specimens = tag.getList(NBT_SPECIMENS, Tag.TAG_COMPOUND);
        
        for (int i = 0; i < specimens.size(); i++) {
            CompoundTag specimen = specimens.getCompound(i);
            int preservation = specimen.getInt(NBT_PRESERVATION);
            int decay = preservation < 50 ? 2 : 1;
            specimen.putInt(NBT_PRESERVATION, Math.max(0, preservation - decay));
            specimens.set(i, specimen);
        }
        
        tag.put(NBT_SPECIMENS, specimens);
        jarStack.set(DataComponents.CUSTOM_DATA, tag);
    }
    
    public int getAveragePreservation(ItemStack jarStack) {
        ListTag specimens = getSpecimens(jarStack);
        if (specimens.isEmpty()) return 100;
        
        int total = 0;
        for (int i = 0; i < specimens.size(); i++) {
            total += specimens.getCompound(i).getInt(NBT_PRESERVATION);
        }
        return total / specimens.size();
    }
    
    public int getRarityCount(ItemStack jarStack, String rarity) {
        ListTag specimens = getSpecimens(jarStack);
        int count = 0;
        
        for (int i = 0; i < specimens.size(); i++) {
            CompoundTag specimen = specimens.getCompound(i);
            if (specimen.getString(NBT_RARITY).equalsIgnoreCase(rarity)) {
                count++;
            }
        }
        return count;
    }
    
    public int getCollectionScore(ItemStack jarStack) {
        int count = getSpecimenCount(jarStack);
        int preservation = getAveragePreservation(jarStack);
        int rarityCount = getRarityCount(jarStack, "Rare") +
                         getRarityCount(jarStack, "Epic") * 2 +
                         getRarityCount(jarStack, "Legendary") * 3;
        return Math.min(100, (count * 2) + (preservation / 2) + rarityCount);
    }
    
    @Override
    public Component getName(ItemStack stack) {
        int count = getSpecimenCount(stack);
        int preservation = getAveragePreservation(stack);
        
        if (count == 0) {
            return Component.literal("§6Specimen Jar §7(Empty)");
        }
        
        String preservationColor = preservation > 75 ? "§a" :
                                   preservation > 50 ? "§e" :
                                   preservation > 25 ? "§c" : "§4";
        
        return Component.literal(String.format(
            "§6Specimen Jar §7(%d/%d) %s%d%%",
            count, MAX_SPECIMENS, preservationColor, preservation
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
