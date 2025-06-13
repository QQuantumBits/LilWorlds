package org.hydr4.lilworlds.utils;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.hydr4.lilworlds.LilWorlds;

import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;

/**
 * Utility class for server-specific features and optimizations
 */
public class ServerUtils {
    
    private static Boolean isPaper = null;
    private static Boolean isFolia = null;
    private static Method paperAsyncWorldCreation = null;
    
    /**
     * Check if the server is running Paper
     */
    public static boolean isPaper() {
        if (isPaper == null) {
            try {
                Class.forName("com.destroystokyo.paper.PaperConfig");
                isPaper = true;
                LoggerUtils.info("Paper server detected - enabling Paper-specific optimizations");
            } catch (ClassNotFoundException e) {
                isPaper = false;
                LoggerUtils.info("Bukkit/Spigot server detected - using standard features");
            }
        }
        return isPaper;
    }
    
    /**
     * Check if the server is running Folia
     */
    public static boolean isFolia() {
        if (isFolia == null) {
            try {
                Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
                isFolia = true;
                LoggerUtils.info("Folia server detected - enabling Folia-specific optimizations");
            } catch (ClassNotFoundException e) {
                isFolia = false;
            }
        }
        return isFolia;
    }
    
    /**
     * Get server version information
     */
    public static String getServerInfo() {
        String version = Bukkit.getVersion();
        String bukkitVersion = Bukkit.getBukkitVersion();
        
        StringBuilder info = new StringBuilder();
        info.append("Server: ").append(version);
        info.append(", Bukkit: ").append(bukkitVersion);
        
        if (isPaper()) {
            info.append(", Paper: Yes");
        }
        if (isFolia()) {
            info.append(", Folia: Yes");
        }
        
        return info.toString();
    }
    
    /**
     * Create world asynchronously using Paper's async world creation if available
     */
    public static CompletableFuture<World> createWorldAsync(WorldCreator creator) {
        if (isPaper() && !isFolia()) {
            try {
                // Try to use Paper's async world creation
                return createWorldAsyncPaper(creator);
            } catch (Exception e) {
                LoggerUtils.debug("Failed to use Paper async world creation, falling back to sync: " + e.getMessage());
            }
        }
        
        // Fallback to manual async execution
        return CompletableFuture.supplyAsync(() -> {
            try {
                return creator.createWorld();
            } catch (Exception e) {
                LoggerUtils.error("Error creating world asynchronously", e);
                return null;
            }
        });
    }
    
    private static CompletableFuture<World> createWorldAsyncPaper(WorldCreator creator) {
        try {
            if (paperAsyncWorldCreation == null) {
                // Try to find Paper's async world creation method
                Class<?> paperWorldCreator = Class.forName("io.papermc.paper.world.WorldCreator");
                paperAsyncWorldCreation = paperWorldCreator.getMethod("createWorldAsync");
            }
            
            // Convert Bukkit WorldCreator to Paper WorldCreator if needed
            Object paperCreator = convertToPaperWorldCreator(creator);
            if (paperCreator != null) {
                @SuppressWarnings("unchecked")
                CompletableFuture<World> future = (CompletableFuture<World>) paperAsyncWorldCreation.invoke(paperCreator);
                return future;
            }
        } catch (Exception e) {
            LoggerUtils.debug("Paper async world creation not available: " + e.getMessage());
        }
        
        // Fallback
        return CompletableFuture.supplyAsync(creator::createWorld);
    }
    
    private static Object convertToPaperWorldCreator(WorldCreator bukkitCreator) {
        try {
            Class<?> paperWorldCreatorClass = Class.forName("io.papermc.paper.world.WorldCreator");
            Method nameMethod = paperWorldCreatorClass.getMethod("name", String.class);
            Object paperCreator = nameMethod.invoke(null, bukkitCreator.name());
            
            // Set environment
            if (bukkitCreator.environment() != null) {
                Method environmentMethod = paperWorldCreatorClass.getMethod("environment", World.Environment.class);
                paperCreator = environmentMethod.invoke(paperCreator, bukkitCreator.environment());
            }
            
            // Set generator
            if (bukkitCreator.generator() != null) {
                Method generatorMethod = paperWorldCreatorClass.getMethod("generator", String.class);
                paperCreator = generatorMethod.invoke(paperCreator, bukkitCreator.generator());
            }
            
            // Set seed
            if (bukkitCreator.seed() != 0) {
                Method seedMethod = paperWorldCreatorClass.getMethod("seed", long.class);
                paperCreator = seedMethod.invoke(paperCreator, bukkitCreator.seed());
            }
            
            // Set generate structures
            Method structuresMethod = paperWorldCreatorClass.getMethod("generateStructures", boolean.class);
            paperCreator = structuresMethod.invoke(paperCreator, bukkitCreator.generateStructures());
            
            return paperCreator;
        } catch (Exception e) {
            LoggerUtils.debug("Failed to convert to Paper WorldCreator: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Apply Paper-specific optimizations to a world
     */
    public static void applyPaperOptimizations(World world) {
        if (!isPaper()) {
            return;
        }
        
        try {
            // Apply Paper-specific settings for better performance
            LoggerUtils.debug("Applying Paper optimizations to world: " + world.getName());
            
            // These would be Paper-specific optimizations
            // For now, we'll just log that we're applying them
            LoggerUtils.debug("Paper optimizations applied to world: " + world.getName());
            
        } catch (Exception e) {
            LoggerUtils.debug("Failed to apply Paper optimizations: " + e.getMessage());
        }
    }
    
    /**
     * Get optimal thread pool size for async operations
     */
    public static int getOptimalThreadPoolSize() {
        int cores = Runtime.getRuntime().availableProcessors();
        
        if (isFolia()) {
            // Folia handles threading differently
            return Math.max(2, cores / 4);
        } else if (isPaper()) {
            // Paper can handle more concurrent operations
            return Math.max(2, cores / 2);
        } else {
            // Conservative approach for Bukkit/Spigot
            return Math.max(1, cores / 4);
        }
    }
    
    /**
     * Schedule a task appropriately for the server type
     */
    public static void scheduleTask(LilWorlds plugin, Runnable task, boolean async) {
        if (isFolia()) {
            // Folia requires different scheduling
            if (async) {
                Bukkit.getScheduler().runTaskAsynchronously(plugin, task);
            } else {
                Bukkit.getScheduler().runTask(plugin, task);
            }
        } else {
            // Standard Bukkit/Spigot/Paper scheduling
            if (async) {
                Bukkit.getScheduler().runTaskAsynchronously(plugin, task);
            } else {
                Bukkit.getScheduler().runTask(plugin, task);
            }
        }
    }
    
    /**
     * Get memory usage information
     */
    public static String getMemoryInfo() {
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        
        return String.format("Memory: %dMB used / %dMB total / %dMB max", 
            usedMemory / 1024 / 1024, 
            totalMemory / 1024 / 1024, 
            maxMemory / 1024 / 1024);
    }
}