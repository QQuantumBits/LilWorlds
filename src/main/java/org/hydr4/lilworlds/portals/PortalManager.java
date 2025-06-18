package org.hydr4.lilworlds.portals;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.hydr4.lilworlds.LilWorlds;
import org.hydr4.lilworlds.portals.Portal.PortalType;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages all portals in the plugin
 */
public class PortalManager {
    
    private final LilWorlds plugin;
    private final Map<String, Portal> portals;
    private final Map<UUID, Long> playerCooldowns;
    private final File portalsFile;
    private FileConfiguration portalsConfig;
    
    private static final long TELEPORT_COOLDOWN = 3000; // 3 seconds
    
    public PortalManager(LilWorlds plugin) {
        this.plugin = plugin;
        this.portals = new ConcurrentHashMap<>();
        this.playerCooldowns = new ConcurrentHashMap<>();
        this.portalsFile = new File(plugin.getDataFolder(), "portals.yml");
        
        loadPortals();
    }
    
    /**
     * Create a new portal
     */
    public boolean createPortal(String name, Location loc1, Location loc2, String destWorld, 
                               Location destLocation, Material frameMaterial, PortalType type) {
        if (portals.containsKey(name.toLowerCase())) {
            return false;
        }
        
        Portal portal = new Portal(name, loc1, loc2, destWorld, destLocation, frameMaterial, type);
        portals.put(name.toLowerCase(), portal);
        savePortals();
        
        plugin.getLogger().info("Created portal: " + name + " -> " + destWorld);
        return true;
    }
    
    /**
     * Delete a portal
     */
    public boolean deletePortal(String name) {
        Portal removed = portals.remove(name.toLowerCase());
        if (removed != null) {
            savePortals();
            plugin.getLogger().info("Deleted portal: " + name);
            return true;
        }
        return false;
    }
    
    /**
     * Get a portal by name
     */
    public Portal getPortal(String name) {
        return portals.get(name.toLowerCase());
    }
    
    /**
     * Get all portals
     */
    public Collection<Portal> getAllPortals() {
        return portals.values();
    }
    
    /**
     * Get portal names
     */
    public Set<String> getPortalNames() {
        return new HashSet<>(portals.keySet());
    }
    
    /**
     * Find portal at location
     */
    public Portal getPortalAtLocation(Location location) {
        for (Portal portal : portals.values()) {
            if (portal.isEnabled() && portal.isInPortal(location)) {
                return portal;
            }
        }
        return null;
    }
    
    /**
     * Teleport player through portal
     */
    public boolean teleportPlayer(Player player, Portal portal) {
        if (!portal.isEnabled()) {
            return false;
        }
        
        // Check cooldown
        UUID playerId = player.getUniqueId();
        long currentTime = System.currentTimeMillis();
        if (playerCooldowns.containsKey(playerId)) {
            long lastTeleport = playerCooldowns.get(playerId);
            if (currentTime - lastTeleport < TELEPORT_COOLDOWN) {
                return false;
            }
        }
        
        // Get destination world
        World destWorld = Bukkit.getWorld(portal.getDestinationWorld());
        if (destWorld == null) {
            plugin.getLogger().warning("Destination world not found for portal: " + portal.getName());
            return false;
        }
        
        // Teleport player
        Location destLocation = portal.getDestinationLocation().clone();
        destLocation.setWorld(destWorld);
        
        boolean success = player.teleport(destLocation);
        if (success) {
            playerCooldowns.put(playerId, currentTime);
            plugin.getLogger().info("Player " + player.getName() + " used portal: " + portal.getName());
        }
        
        return success;
    }
    
    /**
     * Create a portal frame
     */
    public void createPortalFrame(Portal portal) {
        Location loc1 = portal.getLocation1();
        Location loc2 = portal.getLocation2();
        Material frameMaterial = portal.getFrameMaterial();
        
        int minX = Math.min(loc1.getBlockX(), loc2.getBlockX());
        int maxX = Math.max(loc1.getBlockX(), loc2.getBlockX());
        int minY = Math.min(loc1.getBlockY(), loc2.getBlockY());
        int maxY = Math.max(loc1.getBlockY(), loc2.getBlockY());
        int minZ = Math.min(loc1.getBlockZ(), loc2.getBlockZ());
        int maxZ = Math.max(loc1.getBlockZ(), loc2.getBlockZ());
        
        World world = loc1.getWorld();
        
        // Create frame
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    boolean isFrame = (x == minX || x == maxX) || 
                                     (y == minY || y == maxY) || 
                                     (z == minZ || z == maxZ);
                    
                    if (isFrame) {
                        world.getBlockAt(x, y, z).setType(frameMaterial);
                    } else if (portal.getType() == PortalType.NETHER_PORTAL) {
                        world.getBlockAt(x, y, z).setType(Material.NETHER_PORTAL);
                    } else if (portal.getType() == PortalType.END_PORTAL) {
                        world.getBlockAt(x, y, z).setType(Material.END_PORTAL);
                    } else {
                        world.getBlockAt(x, y, z).setType(Material.AIR);
                    }
                }
            }
        }
    }
    
    /**
     * Remove portal frame
     */
    public void removePortalFrame(Portal portal) {
        Location loc1 = portal.getLocation1();
        Location loc2 = portal.getLocation2();
        
        int minX = Math.min(loc1.getBlockX(), loc2.getBlockX());
        int maxX = Math.max(loc1.getBlockX(), loc2.getBlockX());
        int minY = Math.min(loc1.getBlockY(), loc2.getBlockY());
        int maxY = Math.max(loc1.getBlockY(), loc2.getBlockY());
        int minZ = Math.min(loc1.getBlockZ(), loc2.getBlockZ());
        int maxZ = Math.max(loc1.getBlockZ(), loc2.getBlockZ());
        
        World world = loc1.getWorld();
        
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    world.getBlockAt(x, y, z).setType(Material.AIR);
                }
            }
        }
    }
    
    /**
     * Load portals from config
     */
    public void loadPortals() {
        if (!portalsFile.exists()) {
            try {
                portalsFile.getParentFile().mkdirs();
                portalsFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create portals.yml: " + e.getMessage());
                return;
            }
        }
        
        portalsConfig = YamlConfiguration.loadConfiguration(portalsFile);
        portals.clear();
        
        ConfigurationSection portalsSection = portalsConfig.getConfigurationSection("portals");
        if (portalsSection != null) {
            for (String portalName : portalsSection.getKeys(false)) {
                try {
                    ConfigurationSection portalSection = portalsSection.getConfigurationSection(portalName);
                    if (portalSection != null) {
                        Map<String, Object> portalData = new HashMap<>();
                        for (String key : portalSection.getKeys(true)) {
                            portalData.put(key, portalSection.get(key));
                        }
                        
                        Portal portal = Portal.deserialize(portalData);
                        portals.put(portalName.toLowerCase(), portal);
                    }
                } catch (Exception e) {
                    plugin.getLogger().warning("Failed to load portal: " + portalName + " - " + e.getMessage());
                }
            }
        }
        
        plugin.getLogger().info("Loaded " + portals.size() + " portals");
    }
    
    /**
     * Save portals to config
     */
    public void savePortals() {
        if (portalsConfig == null) {
            portalsConfig = new YamlConfiguration();
        }
        
        // Clear existing portals
        portalsConfig.set("portals", null);
        
        // Save all portals
        for (Map.Entry<String, Portal> entry : portals.entrySet()) {
            String portalName = entry.getKey();
            Portal portal = entry.getValue();
            
            Map<String, Object> portalData = portal.serialize();
            for (Map.Entry<String, Object> dataEntry : portalData.entrySet()) {
                portalsConfig.set("portals." + portalName + "." + dataEntry.getKey(), dataEntry.getValue());
            }
        }
        
        try {
            portalsConfig.save(portalsFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save portals.yml: " + e.getMessage());
        }
    }
    
    /**
     * Reload portals
     */
    public void reload() {
        loadPortals();
    }
    
    /**
     * Clean up cooldowns for offline players
     */
    public void cleanupCooldowns() {
        long currentTime = System.currentTimeMillis();
        playerCooldowns.entrySet().removeIf(entry -> 
            currentTime - entry.getValue() > TELEPORT_COOLDOWN * 2);
    }
    
    /**
     * Get portals in a world
     */
    public List<Portal> getPortalsInWorld(String worldName) {
        List<Portal> worldPortals = new ArrayList<>();
        for (Portal portal : portals.values()) {
            if (portal.getLocation1().getWorld().getName().equals(worldName)) {
                worldPortals.add(portal);
            }
        }
        return worldPortals;
    }
    
    /**
     * Check if player is on cooldown
     */
    public boolean isOnCooldown(Player player) {
        UUID playerId = player.getUniqueId();
        if (!playerCooldowns.containsKey(playerId)) {
            return false;
        }
        
        long lastTeleport = playerCooldowns.get(playerId);
        return System.currentTimeMillis() - lastTeleport < TELEPORT_COOLDOWN;
    }
    
    /**
     * Get remaining cooldown time
     */
    public long getRemainingCooldown(Player player) {
        UUID playerId = player.getUniqueId();
        if (!playerCooldowns.containsKey(playerId)) {
            return 0;
        }
        
        long lastTeleport = playerCooldowns.get(playerId);
        long remaining = TELEPORT_COOLDOWN - (System.currentTimeMillis() - lastTeleport);
        return Math.max(0, remaining);
    }
}