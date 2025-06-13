package org.hydr4.lilworlds.api;

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.hydr4.lilworlds.LilWorlds;
import org.hydr4.lilworlds.api.events.*;
import org.hydr4.lilworlds.api.world.WorldBuilder;
import org.hydr4.lilworlds.api.world.WorldInfo;
import org.hydr4.lilworlds.api.world.WorldManager;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Main API class for LilWorlds plugin
 * Provides comprehensive world management functionality for developers
 * 
 * @author LilWorlds Team
 * @version 1.4.0
 * @since 1.4.0
 */
public class LilWorldsAPI {
    
    private static LilWorlds plugin;
    private static LilWorldsAPI instance;
    
    private LilWorldsAPI(LilWorlds plugin) {
        LilWorldsAPI.plugin = plugin;
    }
    
    /**
     * Initialize the API instance
     * This method is called internally by the plugin
     * 
     * @param plugin The LilWorlds plugin instance
     */
    public static void initialize(LilWorlds plugin) {
        if (instance == null) {
            instance = new LilWorldsAPI(plugin);
        }
    }
    
    /**
     * Get the API instance
     * 
     * @return The LilWorldsAPI instance
     * @throws IllegalStateException if the API is not initialized
     */
    public static LilWorldsAPI getInstance() {
        if (instance == null) {
            throw new IllegalStateException("LilWorldsAPI is not initialized! Make sure LilWorlds plugin is loaded.");
        }
        return instance;
    }
    
    /**
     * Get the world manager for advanced world operations
     * 
     * @return The WorldManager instance
     */
    public WorldManager getWorldManager() {
        return new WorldManager(plugin);
    }
    
    /**
     * Create a new world builder for easy world creation
     * 
     * @param name The name of the world to create
     * @return A new WorldBuilder instance
     */
    public WorldBuilder createWorld(String name) {
        return new WorldBuilder(plugin, name);
    }
    
    /**
     * Get information about a world
     * 
     * @param worldName The name of the world
     * @return WorldInfo object containing detailed information, or null if world doesn't exist
     */
    public WorldInfo getWorldInfo(String worldName) {
        return getWorldManager().getWorldInfo(worldName);
    }
    
    /**
     * Check if a world is managed by LilWorlds
     * 
     * @param worldName The name of the world
     * @return true if the world is managed by LilWorlds
     */
    public boolean isManagedWorld(String worldName) {
        return getWorldManager().isManagedWorld(worldName);
    }
    
    /**
     * Get a list of all managed worlds
     * 
     * @return List of managed world names
     */
    public List<String> getManagedWorlds() {
        return getWorldManager().getManagedWorlds();
    }
    
    /**
     * Get a list of all loaded worlds
     * 
     * @return List of loaded world names
     */
    public List<String> getLoadedWorlds() {
        return getWorldManager().getLoadedWorlds();
    }
    
    /**
     * Get a list of all unloaded worlds
     * 
     * @return List of unloaded world names
     */
    public List<String> getUnloadedWorlds() {
        return getWorldManager().getUnloadedWorlds();
    }
    
    /**
     * Load a world asynchronously
     * 
     * @param worldName The name of the world to load
     * @return CompletableFuture that completes when the world is loaded
     */
    public CompletableFuture<Boolean> loadWorldAsync(String worldName) {
        return getWorldManager().loadWorldAsync(worldName);
    }
    
    /**
     * Unload a world asynchronously
     * 
     * @param worldName The name of the world to unload
     * @return CompletableFuture that completes when the world is unloaded
     */
    public CompletableFuture<Boolean> unloadWorldAsync(String worldName) {
        return getWorldManager().unloadWorldAsync(worldName);
    }
    
    /**
     * Delete a world permanently
     * 
     * @param worldName The name of the world to delete
     * @return CompletableFuture that completes when the world is deleted
     */
    public CompletableFuture<Boolean> deleteWorldAsync(String worldName) {
        return getWorldManager().deleteWorldAsync(worldName);
    }
    
    /**
     * Clone a world
     * 
     * @param sourceWorld The name of the source world
     * @param targetWorld The name of the target world
     * @return CompletableFuture that completes when the world is cloned
     */
    public CompletableFuture<Boolean> cloneWorldAsync(String sourceWorld, String targetWorld) {
        return getWorldManager().cloneWorldAsync(sourceWorld, targetWorld);
    }
    
    /**
     * Import an external world
     * 
     * @param worldName The name of the world to import
     * @return CompletableFuture that completes when the world is imported
     */
    public CompletableFuture<Boolean> importWorldAsync(String worldName) {
        return getWorldManager().importWorldAsync(worldName);
    }
    
    /**
     * Teleport a player to a world
     * 
     * @param player The player to teleport
     * @param worldName The name of the target world
     * @return CompletableFuture that completes when the teleportation is done
     */
    public CompletableFuture<Boolean> teleportPlayerToWorld(Player player, String worldName) {
        return getWorldManager().teleportPlayerToWorld(player, worldName);
    }
    
    /**
     * Get the plugin version
     * 
     * @return The plugin version string
     */
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }
    
    /**
     * Check if the plugin is enabled
     * 
     * @return true if the plugin is enabled
     */
    public boolean isEnabled() {
        return plugin.isEnabled();
    }
    
    /**
     * Get the main plugin instance
     * Note: This should only be used if you need direct access to the plugin
     * 
     * @return The LilWorlds plugin instance
     */
    public LilWorlds getPlugin() {
        return plugin;
    }
}