package net.mosberg.entomology.item;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.Item;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.Level;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.component.DataComponents;
import net.mosberg.entomology.entity.EntityInsectBase;
import net.mosberg.entomology.config.EntomologyConfig;

import java.util.List;

public class ItemBugNetItem extends Item {
    
    private static final double CATCH_RADIUS = 2.5;
    private static final float BASE_CATCH_SUCCESS = 0.75f;
    private static final int SWING_DURATION = 20;
    private static final int DURABILITY_COST = 1;
    
    private static final String NBT_CATCHES = "BugNetCatches";
    private static final String NBT_EFFICIENCY = "BugNetEfficiency";
    
    public ItemBugNetItem(Item.Properties properties) {
        super(properties
            .durability(EntomologyConfig.ItemConfig.BUG_NET_DURABILITY)
            .enchantable(EntomologyConfig.ItemConfig.BUG_NET_ENCHANTABILITY)
        );
    }
    
    @Override
    public ItemUseAnimation getUseAnimation(ItemStack itemStack) {
        return ItemUseAnimation.BOW;
    }
    
    @Override
    public int getUseDuration(ItemStack itemStack, LivingEntity entity) {
        return SWING_DURATION;
    }
    
    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        player.startUsingItem(hand);
        return InteractionResult.SUCCESS;
    }
    
    @Override
    public ItemStack finishUsingItem(ItemStack itemStack, Level level, LivingEntity entity) {
        if (entity instanceof Player player && !level.isClientSide) {
            attemptCatch(level, player, itemStack);
        }
        return itemStack;
    }
    
    private void attemptCatch(Level level, Player player, ItemStack itemStack) {
        AABB searchArea = new AABB(
            player.getX() - CATCH_RADIUS,
            player.getY() - CATCH_RADIUS / 2,
            player.getZ() - CATCH_RADIUS,
            player.getX() + CATCH_RADIUS,
            player.getY() + CATCH_RADIUS,
            player.getZ() + CATCH_RADIUS
        );
        
        List<EntityInsectBase> nearbyInsects = level.getEntitiesOfClass(
            EntityInsectBase.class,
            searchArea,
            entity -> !(entity instanceof Player) && entity.isAlive()
        );
        
        if (nearbyInsects.isEmpty()) {
            handleMiss(player, itemStack);
            return;
        }
        
        EntityInsectBase target = nearbyInsects.get(0);
        
        if (calculateSuccess(player, target)) {
            handleCatchSuccess(level, player, target, itemStack);
        } else {
            handleCatchFail(player, itemStack);
        }
        
        itemStack.hurtAndBreak(DURABILITY_COST, player, 
            LivingEntity.getSlotForHand(player.getUsedItemHand()));
    }
    
    private boolean calculateSuccess(Player player, EntityInsectBase insect) {
        float successRate = BASE_CATCH_SUCCESS;
        
        float pitchAngle = Math.abs(player.getXRot());
        if (pitchAngle < 30f) {
            successRate += 0.15f;
        }
        
        float healthRatio = insect.getHealth() / insect.getMaxHealth();
        successRate += (1.0f - healthRatio) * 0.15f;
        
        successRate += switch (insect.getInsectType()) {
            case BUTTERFLY -> 0.10f;
            case BEE -> 0.05f;
            case GRASSHOPPER -> 0.05f;
            case BEETLE -> -0.05f;
            case ANT -> -0.10f;
        };
        
        successRate = Math.max(0.0f, Math.min(1.0f, successRate));
        return player.getRandom().nextFloat() < successRate;
    }
    
    private void handleCatchSuccess(Level level, Player player, EntityInsectBase insect, ItemStack itemStack) {
        player.playSound(SoundEvents.ITEM_PICKUP, 0.8f, 1.0f);
        updateCatchStats(itemStack);
        insect.discard();
        
        if (EntomologyConfig.GlobalConfig.PARTICLES_ENABLED) {
            spawnCatchParticles(level, insect.getX(), insect.getY(), insect.getZ());
        }
        
        player.displayClientMessage(
            Component.literal("§a✓ Caught " + insect.getInsectType().name),
            true
        );
    }
    
    private void handleCatchFail(Player player, ItemStack itemStack) {
        player.playSound(SoundEvents.ARMOR_EQUIP_LEATHER, 0.6f, 0.8f);
        player.displayClientMessage(
            Component.literal("§c✗ Missed the insect!"),
            true
        );
    }
    
    private void handleMiss(Player player, ItemStack itemStack) {
        player.playSound(SoundEvents.UI_BUTTON_CLICK, 0.5f, 1.2f);
        player.displayClientMessage(
            Component.literal("§7No insects nearby..."),
            true
        );
    }
    
    private void spawnCatchParticles(Level level, double x, double y, double z) {
        for (int i = 0; i < 12; i++) {
            double dx = (level.random.nextDouble() - 0.5) * 0.5;
            double dy = (level.random.nextDouble() - 0.5) * 0.5;
            double dz = (level.random.nextDouble() - 0.5) * 0.5;
            
            level.addParticle(
                net.minecraft.core.particles.ParticleTypes.HAPPY_VILLAGER,
                x + dx, y + dy, z + dz,
                dx, dy, dz
            );
        }
    }
    
    private void updateCatchStats(ItemStack itemStack) {
        if (!itemStack.has(DataComponents.CUSTOM_DATA)) {
            CompoundTag tag = new CompoundTag();
            tag.putInt(NBT_CATCHES, 0);
            tag.putInt(NBT_EFFICIENCY, 0);
            itemStack.set(DataComponents.CUSTOM_DATA, tag);
        }
        
        CompoundTag tag = itemStack.get(DataComponents.CUSTOM_DATA).copy();
        int catches = tag.getInt(NBT_CATCHES);
        tag.putInt(NBT_CATCHES, catches + 1);
        
        int durabilityLeft = itemStack.getMaxDamage() - itemStack.getDamageValue();
        if (durabilityLeft > 0) {
            int efficiency = (catches * 100) / durabilityLeft;
            tag.putInt(NBT_EFFICIENCY, efficiency);
        }
        
        itemStack.set(DataComponents.CUSTOM_DATA, tag);
    }
    
    @Override
    public Component getName(ItemStack stack) {
        if (!stack.has(DataComponents.CUSTOM_DATA)) {
            return Component.literal("§6Bug Net");
        }
        
        CompoundTag tag = stack.get(DataComponents.CUSTOM_DATA).copy();
        int catches = tag.getInt(NBT_CATCHES);
        String catchColor = catches > 50 ? "§a" : catches > 20 ? "§e" : "§f";
        
        return Component.literal("§6Bug Net " + catchColor + "(" + catches + " catches)");
    }
}
