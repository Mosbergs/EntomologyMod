package net.mosberg.entomology.item;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.component.DataComponents;

public class ItemBugNetAdvanced extends Item {
    
    private static final String NBT_CATCHES = "CatchCount";
    private static final String NBT_EFFICIENCY = "Efficiency";
    private static final String NBT_RARITY = "Rarity";
    
    public enum NetRarity {
        BASIC(1.0f, 250, 0.75f, "basic"),
        IRON_REINFORCED(1.3f, 375, 0.80f, "iron"),
        GOLDEN_THREAD(1.6f, 500, 0.85f, "golden"),
        DIAMOND_WEAVE(2.0f, 750, 0.90f, "diamond"),
        NETHERITE_MESH(2.5f, 1000, 0.95f, "netherite");
        
        public final float catchMultiplier;
        public final int durability;
        public final float baseCatchRate;
        public final String id;
        
        NetRarity(float catchMultiplier, int durability, float baseCatchRate, String id) {
            this.catchMultiplier = catchMultiplier;
            this.durability = durability;
            this.baseCatchRate = baseCatchRate;
            this.id = id;
        }
    }
    
    private final NetRarity rarity;
    
    public ItemBugNetAdvanced(NetRarity rarity) {
        super(new Item.Properties()
            .durability(rarity.durability)
            .enchantable(14 + rarity.ordinal())
        );
        this.rarity = rarity;
    }
    
    private void ensureNBT(ItemStack stack) {
        if (!stack.has(DataComponents.CUSTOM_DATA)) {
            CompoundTag tag = new CompoundTag();
            tag.putInt(NBT_CATCHES, 0);
            tag.putInt(NBT_EFFICIENCY, 0);
            tag.putString(NBT_RARITY, rarity.id);
            stack.set(DataComponents.CUSTOM_DATA, tag);
        }
    }
    
    public void recordCatch(ItemStack stack) {
        ensureNBT(stack);
        CompoundTag tag = stack.get(DataComponents.CUSTOM_DATA).copy();
        
        int catches = tag.getInt(NBT_CATCHES) + 1;
        tag.putInt(NBT_CATCHES, catches);
        
        int durabilityLeft = stack.getMaxDamage() - stack.getDamageValue();
        if (durabilityLeft > 0) {
            int efficiency = Math.min(100, (catches * 100) / durabilityLeft);
            tag.putInt(NBT_EFFICIENCY, efficiency);
        }
        
        stack.set(DataComponents.CUSTOM_DATA, tag);
    }
    
    public int getCatchCount(ItemStack stack) {
        ensureNBT(stack);
        return stack.get(DataComponents.CUSTOM_DATA).getInt(NBT_CATCHES);
    }
    
    public int getEfficiency(ItemStack stack) {
        ensureNBT(stack);
        return Math.min(100, stack.get(DataComponents.CUSTOM_DATA).getInt(NBT_EFFICIENCY));
    }
    
    public float getCatchMultiplier(ItemStack stack) {
        int efficiency = getEfficiency(stack);
        return rarity.catchMultiplier * (1.0f + (efficiency / 100.0f) * 0.25f);
    }
    
    public float getBaseCatchRate(ItemStack stack) {
        return rarity.baseCatchRate;
    }
    
    @Override
    public boolean isEnchantable(ItemStack stack) {
        return true;
    }
    
    public NetRarity getRarity() {
        return rarity;
    }
}
