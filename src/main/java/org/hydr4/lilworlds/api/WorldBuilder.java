package org.hydr4.lilworlds.api;

import org.bukkit.World;
import org.hydr4.lilworlds.LilWorlds;
import org.hydr4.lilworlds.managers.WorldManager;

import java.util.concurrent.CompletableFuture;

/**
 * Builder class for creating worlds with fluent API
 */
public class WorldBuilder {
    
    private final LilWorlds plugin;
    private final String worldName;
    private World.Environment environment = World.Environment.NORMAL;
    private String generator = null;
    private boolean generateStructures = true;
    private long seed = 0;
    private boolean useRandomSeed = true;
    
    public WorldBuilder(LilWorlds plugin, String worldName) {
        this.plugin = plugin;
        this.worldName = worldName;
    }
    
    /**
     * Set the world environment
     */
    public WorldBuilder environment(World.Environment environment) {
        this.environment = environment;
        return this;
    }
    
    /**
     * Set the world generator
     */
    public WorldBuilder generator(String generator) {
        this.generator = generator;
        return this;
    }
    
    /**
     * Set whether to generate structures
     */
    public WorldBuilder generateStructures(boolean generateStructures) {
        this.generateStructures = generateStructures;
        return this;
    }
    
    /**
     * Set the world seed
     */
    public WorldBuilder seed(long seed) {
        this.seed = seed;
        this.useRandomSeed = false;
        return this;
    }
    
    /**
     * Use a random seed
     */
    public WorldBuilder randomSeed() {
        this.useRandomSeed = true;
        return this;
    }
    
    /**
     * Create the world synchronously
     * @return true if world was created successfully
     */
    public boolean create() {
        WorldManager worldManager = plugin.getWorldManager();
        return worldManager.createWorld(worldName, environment, generator, generateStructures);
    }
    
    /**
     * Create the world asynchronously using advanced options
     * @return CompletableFuture that completes with true if world was created successfully
     */
    public CompletableFuture<Boolean> createAsync() {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        
        WorldManager worldManager = plugin.getWorldManager();
        
        // Create an options object that matches what WorldManager expects
        WorldCreationOptions options = new WorldCreationOptions(environment, generator, generateStructures, useRandomSeed ? System.currentTimeMillis() : seed);
        
        worldManager.createWorldAdvancedAsync(worldName, options, future::complete);
        
        return future;
    }
    
    /**
     * Internal class to hold world creation options
     */
    private static class WorldCreationOptions {
        public final World.Environment environment;
        public final String generator;
        public final boolean generateStructures;
        public final long seed;
        
        public WorldCreationOptions(World.Environment environment, String generator, boolean generateStructures, long seed) {
            this.environment = environment;
            this.generator = generator;
            this.generateStructures = generateStructures;
            this.seed = seed;
        }
    }
    
    /**
     * Get the world name
     */
    public String getWorldName() {
        return worldName;
    }
    
    /**
     * Get the environment
     */
    public World.Environment getEnvironment() {
        return environment;
    }
    
    /**
     * Get the generator
     */
    public String getGenerator() {
        return generator;
    }
    
    /**
     * Check if structures will be generated
     */
    public boolean isGenerateStructures() {
        return generateStructures;
    }
    
    /**
     * Get the seed
     */
    public long getSeed() {
        return seed;
    }
    
    /**
     * Check if using random seed
     */
    public boolean isUsingRandomSeed() {
        return useRandomSeed;
    }
}