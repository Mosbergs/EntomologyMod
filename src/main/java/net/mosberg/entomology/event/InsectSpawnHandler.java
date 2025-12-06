package net.mosberg.entomology.event;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.mosberg.entomology.EntomologyMod;
import net.mosberg.entomology.config.EntomologyConfig;
import net.mosberg.entomology.entity.EntityInsectBase;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Entity;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Population Management & Optimization
 * Tracks insect populations, enforces caps, and manages ecosystem health
 * 
 * IMPROVEMENTS:
 * - Thread-safe population tracking
 * - Efficient chunk-based culling
 * - Regular memory cleanup
 * - Proper logging and statistics
 * - Handles edge cases gracefully
 */
@EventBusSubscriber(modid = EntomologyMod.MODID)
public class InsectSpawnHandler {
    
    private static final Map<ChunkPos, Integer> CHUNK_INSECT_COUNT = new ConcurrentHashMap<>();
    private static final Map<EntityType<?>, Integer> GLOBAL_INSECT_COUNT = new ConcurrentHashMap<>();
    
    private static int cleanupTickCounter = 0;
    private static final int CLEANUP_INTERVAL = 1200; // 60 seconds
    private static final int STATISTICS_INTERVAL = 6000; // 5 minutes
    
    private static long lastStatisticsTime = 0;
    
    /**
     * Handle entity spawn and enforce population limits
     */
    @SubscribeEvent
    public static void onEntitySpawn(EntityJoinLevelEvent event) {
        Entity entity = event.getEntity();
        
        if (!(entity instanceof EntityInsectBase insect)) {
            return;
        }
        
        // Only run on server
        if (event.getLevel().isClientSide) {
            return;
        }
        
        // Check if spawning is enabled
        if (!isInsectTypeEnabled(insect.getInsectType())) {
            event.setCanceled(true);
            return;
        }
        
        // Enforce global population cap
        if (EntomologyConfig.GlobalConfig.ENABLE_POPULATION_CAP) {
            int globalCount = GLOBAL_INSECT_COUNT.values().stream()
                .mapToInt(Integer::intValue)
                .sum();
            
            if (globalCount >= EntomologyConfig.GlobalConfig.MAX_INSECTS_GLOBAL) {
                event.setCanceled(true);
                return;
            }
        }
        
        // Enforce chunk population cap
        ChunkPos chunkPos = new ChunkPos(entity.blockPosition());
        int chunkCount = CHUNK_INSECT_COUNT.getOrDefault(chunkPos, 0);
        
        if (chunkCount >= EntomologyConfig.GlobalConfig.MAX_INSECTS_PER_CHUNK) {
            event.setCanceled(true);
            return;
        }
        
        // Register the insect
        CHUNK_INSECT_COUNT.put(chunkPos, chunkCount + 1);
        EntityType<?> type = entity.getType();
        GLOBAL_INSECT_COUNT.merge(type, 1, Integer::sum);
        
        EntomologyMod.LOGGER.debug("🐛 Spawned {}: {} at {}", 
            insect.getInsectType().name,
            entity.getName().getString(),
            chunkPos
        );
    }
    
    /**
     * Periodic cleanup and optimization
     */
    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (event.getLevel().isClientSide) {
            return;
        }
        
        cleanupTickCounter++;
        
        // Cleanup every CLEANUP_INTERVAL ticks
        if (cleanupTickCounter >= CLEANUP_INTERVAL) {
            cleanupInvalidChunks(event.getLevel());
            cleanupTickCounter = 0;
        }
        
        // Log statistics periodically
        if (System.currentTimeMillis() - lastStatisticsTime > STATISTICS_INTERVAL * 50) {
            logPopulationStatistics();
            lastStatisticsTime = System.currentTimeMillis();
        }
    }
    
    /**
     * Remove tracking for unloaded chunks and recount insects
     */
    private static void cleanupInvalidChunks(net.minecraft.world.level.Level level) {
    int initialSize = CHUNK_INSECT_COUNT.size();
    
    CHUNK_INSECT_COUNT.entrySet().removeIf(entry -> {
        ChunkPos pos = entry.getKey();
        
        if (!level.hasChunk(pos.x, pos.z)) {
            return true;
        }
        
        try {
            // Use getBlockEntities() or iterate entity sections instead
            var chunk = level.getChunk(pos.x, pos.z);
            long insectCount = chunk.getEntitiesOfClass(EntityInsectBase.class, 
                new net.minecraft.world.phys.AABB(
                    pos.getMinBlockX(), level.getMinBuildHeight(), pos.getMinBlockZ(),
                    pos.getMaxBlockX(), level.getMaxBuildHeight(), pos.getMaxBlockZ()
                )
            ).size();
            
            if (insectCount == 0) {
                return true;
            }
            
            entry.setValue((int) insectCount);
            return false;
        } catch (Exception e) {
            EntomologyMod.LOGGER.debug("Error recounting insects in chunk {}: {}", pos, e.getMessage());
            return true;
        }
    });
    
    int finalSize = CHUNK_INSECT_COUNT.size();
    if (finalSize < initialSize) {
        EntomologyMod.LOGGER.debug("🐛 Cleaned up {} chunks. Active chunks: {}", 
            initialSize - finalSize, 
            finalSize
        );
    }
}

    
    /**
     * Log population statistics for monitoring
     */
    private static void logPopulationStatistics() {
        int totalGlobal = GLOBAL_INSECT_COUNT.values().stream()
            .mapToInt(Integer::intValue)
            .sum();
        
        int totalChunks = CHUNK_INSECT_COUNT.size();
        
        EntomologyMod.LOGGER.info("🐛 Population Stats - Global: {} insects across {} chunks",
            totalGlobal,
            totalChunks
        );
        
        // Log per-type breakdown if not empty
        if (!GLOBAL_INSECT_COUNT.isEmpty()) {
            GLOBAL_INSECT_COUNT.forEach((type, count) -> 
                EntomologyMod.LOGGER.debug("  - {}: {}", type.toString(), count)
            );
        }
    }
    
    /**
     * Get comprehensive population statistics
     */
    public static Map<String, Object> getPopulationStats() {
        Map<String, Object> stats = new LinkedHashMap<>();
        
        int totalGlobal = GLOBAL_INSECT_COUNT.values().stream()
            .mapToInt(Integer::intValue)
            .sum();
        
        stats.put("TOTAL_INSECTS", totalGlobal);
        stats.put("TOTAL_CHUNKS_ACTIVE", CHUNK_INSECT_COUNT.size());
        stats.put("MAX_INSECTS_GLOBAL", EntomologyConfig.GlobalConfig.MAX_INSECTS_GLOBAL);
        stats.put("UTILIZATION_PERCENT", totalGlobal > 0 ? 
            (totalGlobal * 100) / EntomologyConfig.GlobalConfig.MAX_INSECTS_GLOBAL : 0);
        
        // Add per-type breakdown
        GLOBAL_INSECT_COUNT.forEach((type, count) -> 
            stats.put("TYPE_" + type.toString().toUpperCase(), count)
        );
        
        return stats;
    }
    
    /**
     * Reset all population tracking (use with caution)
     */
    public static void resetPopulationTracking() {
        int previousCount = CHUNK_INSECT_COUNT.size() + GLOBAL_INSECT_COUNT.size();
        CHUNK_INSECT_COUNT.clear();
        GLOBAL_INSECT_COUNT.clear();
        EntomologyMod.LOGGER.warn("🐛 Population tracking reset (cleared {} entries)", previousCount);
    }
    
    /**
     * Check if a specific insect type is enabled
     */
    private static boolean isInsectTypeEnabled(EntityInsectBase.InsectType type) {
        return switch (type) {
            case BUTTERFLY -> EntomologyConfig.ButterflyConfig.CAN_SPAWN;
            case BEETLE -> EntomologyConfig.BeetleConfig.CAN_SPAWN;
            case BEE -> EntomologyConfig.BeeConfig.CAN_SPAWN;
            case ANT -> EntomologyConfig.AntConfig.CAN_SPAWN;
            case GRASSHOPPER -> EntomologyConfig.GrasshopperConfig.CAN_SPAWN;
        };
    }
    
    /**
     * Manually add to population tracking (useful for debugging)
     */
    public static void registerInsect(ChunkPos chunkPos, EntityType<?> type) {
        CHUNK_INSECT_COUNT.merge(chunkPos, 1, Integer::sum);
        GLOBAL_INSECT_COUNT.merge(type, 1, Integer::sum);
    }
    
    /**
     * Get chunk insect count
     */
    public static int getChunkInsectCount(ChunkPos pos) {
        return CHUNK_INSECT_COUNT.getOrDefault(pos, 0);
    }
}
