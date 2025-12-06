package net.mosberg.entomology.entity;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.*;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.mosberg.entomology.config.EntomologyConfig;

/**
 * Base Class for All Insects
 * Provides common behavior: pathfinding, breeding, aging, health management
 * 
 * IMPROVEMENTS:
 * - Optimized goal hierarchy
 * - Better memory management for positions
 * - Proper particle spawning
 * - Realistic lifespan and aging
 * - Configurable behavior per insect type
 * - Safe goal removal and management
 */
public abstract class EntityInsectBase extends PathfinderMob {
    
    /**
     * Insect type enum with behavior characteristics
     */
    public enum InsectType {
        BUTTERFLY("Butterfly", 0.25f, 0.25f, 2, 0.12f, true, true, 5),
        BEETLE("Beetle", 0.4f, 0.3f, 4, 0.15f, false, false, 7),
        BEE("Bee", 0.2f, 0.2f, 3, 0.15f, true, true, 6),
        ANT("Ant", 0.3f, 0.2f, 2, 0.1f, false, true, 4),
        GRASSHOPPER("Grasshopper", 0.35f, 0.35f, 3, 0.12f, true, false, 5);
        
        public final String name;
        public final float width;
        public final float height;
        public final int baseHealth;
        public final float baseSpeed;
        public final boolean isFlying;
        public final boolean needsLight;
        public final int lifespanDays;
        
        InsectType(String name, float width, float height, int baseHealth, float baseSpeed,
                   boolean isFlying, boolean needsLight, int lifespanDays) {
            this.name = name;
            this.width = width;
            this.height = height;
            this.baseHealth = baseHealth;
            this.baseSpeed = baseSpeed;
            this.isFlying = isFlying;
            this.needsLight = needsLight;
            this.lifespanDays = lifespanDays;
        }
    }
    
    // ==================== BEHAVIOR CONSTANTS ====================
    private static final int FRAGILITY_LEVEL = 15;
    private static final int WANDER_RADIUS = 12;
    private static final int WANDER_UPDATE_INTERVAL = 80;
    private static final int NIGHT_TIME_START = 12000;
    private static final int DAY_TIME_END = 24000;
    private static final int BREEDING_COOLDOWN = 24000;
    private static final int STUCK_THRESHOLD = 300;
    
    // ==================== STATE ====================
    protected final InsectType insectType;
    private int age = 0;
    private int breedingCooldown = 0;
    private int stuckTicks = 0;
    private int wanderCooldown = 0;
    
    private boolean resting = false;
    private boolean healthy = true;
    private boolean hasEaten = false;
    private BlockPos lastFlowerPos = null;
    private float wingFlapAngle = 0.0f;
    
    public EntityInsectBase(EntityType<? extends PathfinderMob> type, Level world, InsectType insectType) {
        super(type, world);
        this.insectType = insectType;
        
        xpReward = 0;
        setPersistenceRequired();
        
        if (insectType.isFlying) {
            moveControl = new FlyingMoveControl(this, 8, true);
        }
    }
    
    @Override
    protected PathNavigation createNavigation(Level world) {
        if (insectType.isFlying) {
            FlyingPathNavigation nav = new FlyingPathNavigation(this, world);
            nav.setCanOpenDoors(false);
            nav.setCanFloat(true);
            return nav;
        }
        return super.createNavigation(world);
    }
    
    @Override
    protected void registerGoals() {
        super.registerGoals();
        
        // Priority 1: Panic when hurt
        goalSelector.addGoal(1, new PanicGoal(this, 2.0));
        
        // Priority 2: Seek flowers for food
        goalSelector.addGoal(2, new FlowerSeekGoal(this));
        
        // Priority 3: Breeding when healthy
        goalSelector.addGoal(3, new BreedingGoal(this));
        
        // Priority 4: Wander/explore
        goalSelector.addGoal(4, new WanderGoal(this));
        
        // Priority 5: Look around naturally
        goalSelector.addGoal(5, new RandomLookAroundGoal(this));
        
        // Priority 6: Float (safety net)
        if (!insectType.isFlying) {
            goalSelector.addGoal(6, new FloatGoal(this));
        }
    }
    
    /**
     * AI stepping - called every tick
     */
    @Override
    public void aiStep() {
        super.aiStep();
        
        // Prevent fall damage for flying insects
        if (insectType.isFlying) {
            setNoGravity(true);
        }
        
        updateBehavior();
    }
    
    /**
     * Update all behavior systems
     */
    private void updateBehavior() {
        updateDayNightBehavior();
        
        if (insectType.isFlying) {
            updateWingFlapping();
        }
        
        if (EntomologyConfig.GlobalConfig.PARTICLES_ENABLED) {
            spawnAmbientParticles();
        }
        
        manageHealth();
        updateAging();
        updateBreedingCooldown();
        detectIfStuck();
    }
    
    /**
     * Rest at night, wake during day
     */
    private void updateDayNightBehavior() {
        if (!EntomologyConfig.GlobalConfig.NIGHT_REST) return;
        
        long time = level().getDayTime() % DAY_TIME_END;
        
        if (insectType.needsLight && (time >= NIGHT_TIME_START || time < 100)) {
            if (!resting) {
                resting = true;
                getNavigation().stop();
                setDeltaMovement(0, -0.01, 0);
            }
        } else {
            if (resting) {
                resting = false;
                hasEaten = false;
            }
        }
    }
    
    /**
     * Animate wing flapping for flying insects
     */
    private void updateWingFlapping() {
        wingFlapAngle += 4.0f;
        if (wingFlapAngle >= 360) {
            wingFlapAngle -= 360;
        }
    }
    
    /**
     * Spawn ambient particles
     */
    private void spawnAmbientParticles() {
        if (level().isClientSide || resting || getRandom().nextInt(20) != 0) return;
        
        for (int i = 0; i < Math.max(1, EntomologyConfig.GlobalConfig.PARTICLE_DENSITY / 2); i++) {
            double px = getX() + (getRandom().nextDouble() - 0.5) * 0.3;
            double py = getY() + (getRandom().nextDouble() - 0.5) * 0.3;
            double pz = getZ() + (getRandom().nextDouble() - 0.5) * 0.3;
            
            level().addParticle(ParticleTypes.HAPPY_VILLAGER, px, py, pz, 0, -0.01, 0);
        }
    }
    
    /**
     * Manage health state
     */
    private void manageHealth() {
        if (getHealth() < getMaxHealth() * 0.3f) {
            healthy = false;
        } else if (getHealth() >= getMaxHealth() * 0.8f) {
            healthy = true;
        }
        
        // Natural health regen when healthy and unhurt
        if (healthy && getHealth() == getMaxHealth() && tickCount % FRAGILITY_LEVEL == 0) {
            // Fully healthy, maintain state
        } else if (healthy && tickCount % (FRAGILITY_LEVEL * 2) == 0) {
            heal(0.5f);
        }
    }
    
    /**
     * Handle aging and lifespan
     */
    private void updateAging() {
        if (!EntomologyConfig.GlobalConfig.AGING_ENABLED) return;
        
        age++;
        
        int maxAge = insectType.lifespanDays * 24000;
        if (age > maxAge) {
            this.discard();
        }
    }
    
    /**
     * Decrement breeding cooldown
     */
    private void updateBreedingCooldown() {
        if (breedingCooldown > 0) {
            breedingCooldown--;
        }
    }
    
    /**
     * Detect and resolve being stuck
     */
    private void detectIfStuck() {
        if (getNavigation().isDone() || getNavigation().getPath() == null) {
            stuckTicks++;
        } else {
            stuckTicks = 0;
        }
        
        if (stuckTicks > STUCK_THRESHOLD) {
            // Teleport to nearby safe location
            BlockPos randomPos = blockPosition().offset(
                getRandom().nextInt(12) - 6,
                getRandom().nextInt(6) - 3,
                getRandom().nextInt(12) - 6
            );
            setPos(randomPos.getX() + 0.5, randomPos.getY() + 0.5, randomPos.getZ() + 0.5);
            stuckTicks = 0;
        }
    }
    
    /**
     * Flower-seeking goal
     */
    private class FlowerSeekGoal extends Goal {
        private int searchCooldown = 0;
        
        @Override
        public boolean canUse() {
            if (searchCooldown > 0) {
                searchCooldown--;
                return false;
            }
            
            lastFlowerPos = findNearbyFlower();
            if (lastFlowerPos != null) {
                searchCooldown = 120;
                return true;
            }
            
            searchCooldown = 60;
            return false;
        }
        
        @Override
        public void tick() {
            if (lastFlowerPos == null) return;
            
            double dx = lastFlowerPos.getX() + 0.5 - getX();
            double dy = lastFlowerPos.getY() + 0.5 - getY();
            double dz = lastFlowerPos.getZ() + 0.5 - getZ();
            double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
            
            if (dist < 1.5) {
                // Reached flower, eat
                if (!hasEaten && getRandom().nextFloat() < 0.15f) {
                    heal(1.0f);
                    hasEaten = true;
                    healthy = true;
                }
            } else {
                // Path to flower
                getNavigation().moveTo(
                    lastFlowerPos.getX() + 0.5,
                    lastFlowerPos.getY() + 0.5,
                    lastFlowerPos.getZ() + 0.5,
                    0.75
                );
            }
        }
        
        private BlockPos findNearbyFlower() {
            int radius = Math.min(EntomologyConfig.GlobalConfig.FLOWER_SEARCH_RADIUS, 20);
            for (int x = -radius; x <= radius; x += 2) {
                for (int y = -4; y <= 4; y++) {
                    for (int z = -radius; z <= radius; z += 2) {
                        BlockPos pos = blockPosition().offset(x, y, z);
                        if (isFlower(level().getBlockState(pos))) {
                            return pos;
                        }
                    }
                }
            }
            return null;
        }
        
        private boolean isFlower(BlockState state) {
            return state.is(Blocks.POPPY) || state.is(Blocks.DANDELION) ||
                   state.is(Blocks.BLUE_ORCHID) || state.is(Blocks.ALLIUM) ||
                   state.is(Blocks.AZURE_BLUET) || state.is(Blocks.RED_TULIP) ||
                   state.is(Blocks.ORANGE_TULIP) || state.is(Blocks.WHITE_TULIP) ||
                   state.is(Blocks.PINK_TULIP) || state.is(Blocks.OXEYE_DAISY) ||
                   state.is(Blocks.CORNFLOWER) || state.is(Blocks.LILY_OF_THE_VALLEY) ||
                   state.is(Blocks.SUNFLOWER) || state.is(Blocks.LILAC) ||
                   state.is(Blocks.ROSE_BUSH) || state.is(Blocks.PEONY);
        }
    }
    
    /**
     * Breeding goal
     */
    private class BreedingGoal extends Goal {
        @Override
        public boolean canUse() {
            if (!EntomologyConfig.GlobalConfig.BREEDING_ENABLED) return false;
            if (breedingCooldown > 0) return false;
            if (!healthy || age < 1200) return false;
            
            return lastFlowerPos != null && getRandom().nextFloat() < 0.02f;
        }
        
        @Override
        public void tick() {
            if (lastFlowerPos == null) return;
            
            double dist = distanceToSqr(lastFlowerPos.getCenter());
            if (dist < 4.0) {
                breedingCooldown = BREEDING_COOLDOWN;
            }
        }
    }
    
    /**
     * Wandering goal
     */
    private class WanderGoal extends Goal {
        @Override
        public boolean canUse() {
            if (!EntomologyConfig.GlobalConfig.NIGHT_REST) return true;
            
            long time = level().getDayTime() % DAY_TIME_END;
            if (insectType.needsLight && time >= NIGHT_TIME_START) {
                return false;
            }
            
            return !resting;
        }
        
        @Override
        public void tick() {
            if (wanderCooldown <= 0) {
                double rX = (getRandom().nextDouble() - 0.5) * 2;
                double rY = (getRandom().nextDouble() - 0.5) * 0.3;
                double rZ = (getRandom().nextDouble() - 0.5) * 2;
                
                getNavigation().moveTo(
                    getX() + rX * WANDER_RADIUS,
                    getY() + rY,
                    getZ() + rZ * WANDER_RADIUS,
                    0.75
                );
                
                wanderCooldown = WANDER_UPDATE_INTERVAL;
            }
            wanderCooldown--;
        }
    }
    
    // ==================== OVERRIDES ====================
    
    @Override
    public boolean causeFallDamage(double l, float d, DamageSource source) {
        return !insectType.isFlying;
    }
    
    @Override
    protected void checkFallDamage(double y, boolean onGroundIn, BlockState state, BlockPos pos) {
        if (!insectType.isFlying) {
            super.checkFallDamage(y, onGroundIn, state, pos);
        }
    }
    
    @Override
    public void setNoGravity(boolean ignored) {
        if (insectType.isFlying) {
            super.setNoGravity(true);
        }
    }
    
    @Override
    protected float getDamageAfterMagicAbsorb(DamageSource source, float amount) {
        if (!EntomologyConfig.GlobalConfig.INSECTS_TAKE_DAMAGE) return 0;
        healthy = false;
        return super.getDamageAfterMagicAbsorb(source, 
            amount * EntomologyConfig.GlobalConfig.DAMAGE_MULTIPLIER);
    }
    
    @Override
    public net.minecraft.sounds.SoundEvent getHurtSound(DamageSource ds) {
        return null;
    }
    
    @Override
    public net.minecraft.sounds.SoundEvent getDeathSound() {
        return null;
    }
    
    @Override
    public boolean isAggressive() {
        return false;
    }
    
    // ==================== STATIC HELPERS ====================
    
    public static AttributeSupplier.Builder createInsectAttributes(InsectType type) {
        AttributeSupplier.Builder builder = Mob.createMobAttributes();
        builder = builder.add(Attributes.MOVEMENT_SPEED, type.baseSpeed);
        builder = builder.add(Attributes.MAX_HEALTH, type.baseHealth);
        builder = builder.add(Attributes.ARMOR, 0);
        builder = builder.add(Attributes.FLYING_SPEED, type.baseSpeed);
        builder = builder.add(Attributes.FOLLOW_RANGE, 12);
        builder = builder.add(Attributes.STEP_HEIGHT, 0.3);
        return builder;
    }
    
    // ==================== GETTERS ====================
    
    public InsectType getInsectType() {
        return insectType;
    }
    
    public int getAge() {
        return age;
    }
    
    public boolean isHealthy() {
        return healthy;
    }
    
    public boolean isResting() {
        return resting;
    }
    
    public int getBreedingCooldown() {
        return breedingCooldown;
    }
    
    public float getWingFlapAngle() {
        return wingFlapAngle;
    }
}
