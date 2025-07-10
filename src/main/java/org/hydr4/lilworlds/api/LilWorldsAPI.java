package org.hydr4.lilworlds.api;

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.hydr4.lilworlds.LilWorlds;
import org.hydr4.lilworlds.api.events.*;
import org.hydr4.lilworlds.managers.WorldManager;
import org.hydr4.lilworlds.portals.PortalManager;
import org.hydr4.lilworlds.portals.Portal;
import org.hydr4.lilworlds.api.WorldBuilder;

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
    public WorldManager.WorldInfo getWorldInfo(String worldName) {
        return getWorldManager().getWorldInfo(worldName);
    }
    
    /**
     * Check if a world is managed by LilWorlds
     * 
     * @param worldName The name of the world
     * @return true if the world is managed by LilWorlds
     */
    public boolean isManagedWorld(String worldName) {
        return getWorldManager().getManagedWorldNames().contains(worldName);
    }
    
    /**
     * Get a list of all managed worlds
     * 
     * @return List of managed world names
     */
    public List<String> getManagedWorlds() {
        return new java.util.ArrayList<>(getWorldManager().getManagedWorldNames());
    }
    
    /**
     * Get a list of all loaded worlds
     * 
     * @return List of loaded world names
     */
    public List<String> getLoadedWorlds() {
        List<String> loadedWorlds = new java.util.ArrayList<>();
        for (World world : org.bukkit.Bukkit.getWorlds()) {
            loadedWorlds.add(world.getName());
        }
        return loadedWorlds;
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
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            try {
                boolean result = getWorldManager().loadWorld(worldName);
                future.complete(result);
            } catch (Exception e) {
                future.completeExceptionally(e);
            }
        });
        return future;
    }
    
    /**
     * Unload a world asynchronously
     * 
     * @param worldName The name of the world to unload
     * @return CompletableFuture that completes when the world is unloaded
     */
    public CompletableFuture<Boolean> unloadWorldAsync(String worldName) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            try {
                boolean result = getWorldManager().unloadWorld(worldName, true);
                future.complete(result);
            } catch (Exception e) {
                future.completeExceptionally(e);
            }
        });
        return future;
    }
    
    /**
     * Delete a world permanently
     * 
     * @param worldName The name of the world to delete
     * @return CompletableFuture that completes when the world is deleted
     */
    public CompletableFuture<Boolean> deleteWorldAsync(String worldName) {
        return getWorldManager().deleteWorld(worldName);
    }
    
    /**
     * Clone a world
     * 
     * @param sourceWorld The name of the source world
     * @param targetWorld The name of the target world
     * @return CompletableFuture that completes when the world is cloned
     */
    public CompletableFuture<Boolean> cloneWorldAsync(String sourceWorld, String targetWorld) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            try {
                boolean result = getWorldManager().cloneWorld(sourceWorld, targetWorld);
                future.complete(result);
            } catch (Exception e) {
                future.completeExceptionally(e);
            }
        });
        return future;
    }
    
    /**
     * Import an external world
     * 
     * @param worldName The name of the world to import
     * @return CompletableFuture that completes when the world is imported
     */
    public CompletableFuture<Boolean> importWorldAsync(String worldName) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            try {
                boolean result = getWorldManager().importWorld(worldName, World.Environment.NORMAL, null);
                future.complete(result);
            } catch (Exception e) {
                future.completeExceptionally(e);
            }
        });
        return future;
    }
    
    /**
     * Teleport a player to a world
     * 
     * @param player The player to teleport
     * @param worldName The name of the target world
     * @return CompletableFuture that completes when the teleportation is done
     */
    public CompletableFuture<Boolean> teleportPlayerToWorld(Player player, String worldName) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            try {
                World world = org.bukkit.Bukkit.getWorld(worldName);
                if (world != null) {
                    boolean result = player.teleport(world.getSpawnLocation());
                    future.complete(result);
                } else {
                    future.complete(false);
                }
            } catch (Exception e) {
                future.completeExceptionally(e);
            }
        });
        return future;
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
    
    // ===== PORTAL API METHODS =====
    
    /**
     * Get the portal manager
     * 
     * @return The portal manager instance
     */
    public PortalManager getPortalManager() {
        return plugin.getPortalManager();
    }
    
    /**
     * Create a new portal
     * 
     * @param name Portal name
     * @param location1 First corner of the portal
     * @param location2 Second corner of the portal
     * @param destinationWorld Destination world name
     * @param destinationLocation Destination location
     * @param frameMaterial Frame material
     * @param type Portal type
     * @return true if portal was created successfully
     */
    public boolean createPortal(String name, org.bukkit.Location location1, org.bukkit.Location location2, 
                               String destinationWorld, org.bukkit.Location destinationLocation, 
                               org.bukkit.Material frameMaterial, Portal.PortalType type) {
        return getPortalManager().createPortal(name, location1, location2, destinationWorld, destinationLocation, frameMaterial, type);
    }
    
    /**
     * Delete a portal
     * 
     * @param name Portal name
     * @return true if portal was deleted successfully
     */
    public boolean deletePortal(String name) {
        return getPortalManager().deletePortal(name);
    }
    
    /**
     * Get a portal by name
     * 
     * @param name Portal name
     * @return Portal instance or null if not found
     */
    public Portal getPortal(String name) {
        return getPortalManager().getPortal(name);
    }
    
    /**
     * Get all portals
     * 
     * @return Collection of all portals
     */
    public java.util.Collection<Portal> getAllPortals() {
        return getPortalManager().getAllPortals();
    }
    
    /**
     * Teleport a player through a portal
     * 
     * @param player Player to teleport
     * @param portal Portal to use
     * @return true if teleportation was successful
     */
    public boolean teleportPlayerThroughPortal(Player player, Portal portal) {
        return getPortalManager().teleportPlayer(player, portal);
    }
    
    /**
     * Find portal at a specific location
     * 
     * @param location Location to check
     * @return Portal at location or null if none found
     */
    public Portal getPortalAtLocation(org.bukkit.Location location) {
        return getPortalManager().getPortalAtLocation(location);
    }
    
    /**
     * Create a portal frame
     * 
     * @param portal Portal to create frame for
     */
    public void createPortalFrame(Portal portal) {
        getPortalManager().createPortalFrame(portal);
    }
    
    /**
     * Remove a portal frame
     * 
     * @param portal Portal to remove frame from
     */
    public void removePortalFrame(Portal portal) {
        getPortalManager().removePortalFrame(portal);
    }
    
    /**
     * Check if a player is on teleportation cooldown
     * 
     * @param player Player to check
     * @return true if player is on cooldown
     */
    public boolean isPlayerOnPortalCooldown(Player player) {
        return getPortalManager().isOnCooldown(player);
    }
    
    /**
     * Get remaining cooldown time for a player
     * 
     * @param player Player to check
     * @return Remaining cooldown time in milliseconds
     */
    public long getPlayerPortalCooldown(Player player) {
        return getPortalManager().getRemainingCooldown(player);
    }
}