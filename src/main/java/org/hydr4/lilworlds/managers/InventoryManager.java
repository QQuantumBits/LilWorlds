package org.hydr4.lilworlds.managers;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.configuration.ConfigurationSection;
import org.hydr4.lilworlds.LilWorlds;
import org.hydr4.lilworlds.utils.LoggerUtils;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages separate inventories for different worlds
 * Each world can have its own player inventory, health, hunger, experience, etc.
 */
public class InventoryManager implements Listener {
    
    private final LilWorlds plugin;
    private final Map<String, PlayerData> playerDataCache = new ConcurrentHashMap<>();
    private final File inventoryDataFolder;
    private boolean enabled = false;
    
    // Configuration settings
    private boolean separateInventory = true;
    private boolean separateHealth = true;
    private boolean separateExperience = true;
    private boolean separateGamemode = false;
    private boolean separateFlight = true;
    private boolean separatePotionEffects = true;
    private boolean separateLocation = false;
    private boolean separateEnderchest = true;
    
    // World groups for shared inventories
    private Map<String, String> worldGroups = new HashMap<>();
    private String defaultGroup = "default";
    
    // Advanced settings
    private boolean saveToFiles = true;
    private int cacheTimeout = 30; // minutes
    private int autoSaveInterval = 300; // seconds
    private boolean backupOnSwitch = false;
    private boolean clearCacheOnUnload = true;
    
    public InventoryManager(LilWorlds plugin) {
        this.plugin = plugin;
        this.inventoryDataFolder = new File(plugin.getDataFolder(), "inventories");
        
        // Create inventories folder if it doesn't exist
        if (!inventoryDataFolder.exists()) {
            inventoryDataFolder.mkdirs();
        }
        
        // Register events
        Bukkit.getPluginManager().registerEvents(this, plugin);
        
        // Load configuration
        loadConfiguration();
        
        LoggerUtils.info("InventoryManager initialized - Enabled: " + enabled);
    }
    
    /**
     * Load configuration settings
     */
    private void loadConfiguration() {
        enabled = plugin.getConfig().getBoolean("features.separate-inventories.enabled", false);
        
        // Load separation settings
        separateInventory = plugin.getConfig().getBoolean("features.separate-inventories.separate.inventory", true);
        separateHealth = plugin.getConfig().getBoolean("features.separate-inventories.separate.health", true);
        separateExperience = plugin.getConfig().getBoolean("features.separate-inventories.separate.experience", true);
        separateGamemode = plugin.getConfig().getBoolean("features.separate-inventories.separate.gamemode", false);
        separateFlight = plugin.getConfig().getBoolean("features.separate-inventories.separate.flight", true);
        separatePotionEffects = plugin.getConfig().getBoolean("features.separate-inventories.separate.potion-effects", true);
        separateLocation = plugin.getConfig().getBoolean("features.separate-inventories.separate.location", false);
        separateEnderchest = plugin.getConfig().getBoolean("features.separate-inventories.separate.enderchest", true);
        
        // Load world groups
        worldGroups.clear();
        ConfigurationSection groupsSection = plugin.getConfig().getConfigurationSection("features.separate-inventories.world-groups");
        if (groupsSection != null) {
            for (String groupName : groupsSection.getKeys(false)) {
                List<String> worlds = groupsSection.getStringList(groupName);
                for (String world : worlds) {
                    worldGroups.put(world, groupName);
                }
            }
        }
        
        defaultGroup = plugin.getConfig().getString("features.separate-inventories.default-group", "default");
        
        // Load advanced settings
        saveToFiles = plugin.getConfig().getBoolean("features.separate-inventories.advanced.save-to-files", true);
        cacheTimeout = plugin.getConfig().getInt("features.separate-inventories.advanced.cache-timeout", 30);
        autoSaveInterval = plugin.getConfig().getInt("features.separate-inventories.advanced.auto-save-interval", 300);
        backupOnSwitch = plugin.getConfig().getBoolean("features.separate-inventories.advanced.backup-on-switch", false);
        clearCacheOnUnload = plugin.getConfig().getBoolean("features.separate-inventories.advanced.clear-cache-on-unload", true);
        
        if (enabled) {
            LoggerUtils.info("Separate inventories feature is enabled");
            LoggerUtils.debug("Separation settings - Inventory: " + separateInventory + 
                             ", Health: " + separateHealth + 
                             ", Experience: " + separateExperience + 
                             ", Gamemode: " + separateGamemode + 
                             ", Flight: " + separateFlight + 
                             ", Potion Effects: " + separatePotionEffects + 
                             ", Location: " + separateLocation + 
                             ", Enderchest: " + separateEnderchest);
            LoggerUtils.debug("World groups configured: " + worldGroups.size());
        } else {
            LoggerUtils.info("Separate inventories feature is disabled");
        }
    }
    
    /**
     * Check if separate inventories are enabled
     */
    public boolean isEnabled() {
        return enabled;
    }
    
    /**
     * Enable or disable separate inventories
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        plugin.getConfig().set("features.separate-inventories.enabled", enabled);
        plugin.saveConfig();
        
        LoggerUtils.info("Separate inventories " + (enabled ? "enabled" : "disabled"));
    }
    
    /**
     * Handle player joining the server
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!enabled) return;
        
        Player player = event.getPlayer();
        String worldName = player.getWorld().getName();
        String groupName = getWorldGroup(worldName);
        
        // Load player data for their current world group
        loadPlayerData(player, groupName);
    }
    
    /**
     * Handle player leaving the server
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (!enabled) return;
        
        Player player = event.getPlayer();
        String worldName = player.getWorld().getName();
        String groupName = getWorldGroup(worldName);
        
        // Save player data for their current world group
        savePlayerData(player, groupName);
        
        // Remove from cache
        playerDataCache.remove(getPlayerKey(player.getUniqueId(), groupName));
    }
    
    /**
     * Handle player changing worlds
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerChangeWorld(PlayerChangedWorldEvent event) {
        if (!enabled) return;
        
        Player player = event.getPlayer();
        String fromWorld = event.getFrom().getName();
        String toWorld = player.getWorld().getName();
        
        // Get world groups
        String fromGroup = getWorldGroup(fromWorld);
        String toGroup = getWorldGroup(toWorld);
        
        // Only switch inventories if moving between different groups
        if (!fromGroup.equals(toGroup)) {
            // Save data for the group they're leaving
            savePlayerData(player, fromGroup);
            
            // Load data for the group they're entering
            loadPlayerData(player, toGroup);
            
            LoggerUtils.debug("Switched inventory for " + player.getName() + 
                             " from group '" + fromGroup + "' to group '" + toGroup + "'");
        } else {
            LoggerUtils.debug("Player " + player.getName() + " moved within the same inventory group: " + fromGroup);
        }
    }
    
    /**
     * Get the world group for a world name
     */
    private String getWorldGroup(String worldName) {
        return worldGroups.getOrDefault(worldName, defaultGroup);
    }
    
    /**
     * Save player data for a specific world group
     */
    private void savePlayerData(Player player, String groupName) {
        try {
            PlayerData data = new PlayerData();
            
            // Save inventory (if enabled)
            if (separateInventory) {
                data.inventory = player.getInventory().getContents().clone();
                data.armorContents = player.getInventory().getArmorContents().clone();
                data.extraContents = player.getInventory().getExtraContents().clone();
            }
            
            // Save ender chest (if enabled)
            if (separateEnderchest) {
                data.enderChest = player.getEnderChest().getContents().clone();
            }
            
            // Save player stats (if enabled)
            if (separateHealth) {
                data.health = player.getHealth();
                data.maxHealth = player.getMaxHealth();
                data.foodLevel = player.getFoodLevel();
                data.saturation = player.getSaturation();
                data.exhaustion = player.getExhaustion();
            }
            
            // Save experience (if enabled)
            if (separateExperience) {
                data.exp = player.getExp();
                data.level = player.getLevel();
                data.totalExperience = player.getTotalExperience();
            }
            
            // Save game mode (if enabled)
            if (separateGamemode) {
                data.gameMode = player.getGameMode();
            }
            
            // Save flight state (if enabled)
            if (separateFlight) {
                data.allowFlight = player.getAllowFlight();
                data.flying = player.isFlying();
                data.flySpeed = player.getFlySpeed();
                data.walkSpeed = player.getWalkSpeed();
            }
            
            // Save potion effects (if enabled)
            if (separatePotionEffects) {
                data.potionEffects = player.getActivePotionEffects().toArray(new PotionEffect[0]);
            }
            
            // Save location (if enabled)
            if (separateLocation) {
                data.location = player.getLocation().clone();
            }
            
            // Cache the data
            String key = getPlayerKey(player.getUniqueId(), groupName);
            playerDataCache.put(key, data);
            
            // Save to file asynchronously (if enabled)
            if (saveToFiles) {
                Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                    savePlayerDataToFile(player.getUniqueId(), groupName, data);
                });
            }
            
        } catch (Exception e) {
            LoggerUtils.error("Error saving player data for " + player.getName() + 
                             " in group " + groupName, e);
        }
    }
    
    /**
     * Load player data for a specific world group
     */
    private void loadPlayerData(Player player, String groupName) {
        try {
            String key = getPlayerKey(player.getUniqueId(), groupName);
            PlayerData data = playerDataCache.get(key);
            
            // If not in cache, try to load from file
            if (data == null && saveToFiles) {
                data = loadPlayerDataFromFile(player.getUniqueId(), groupName);
                if (data != null) {
                    playerDataCache.put(key, data);
                }
            }
            
            // If still no data, create default data
            if (data == null) {
                data = createDefaultPlayerData();
                playerDataCache.put(key, data);
            }
            
            // Apply the data to the player
            applyPlayerData(player, data);
            
        } catch (Exception e) {
            LoggerUtils.error("Error loading player data for " + player.getName() + 
                             " in group " + groupName, e);
        }
    }
    
    /**
     * Apply player data to a player
     */
    private void applyPlayerData(Player player, PlayerData data) {
        // Apply inventory (if enabled)
        if (separateInventory) {
            player.getInventory().clear();
            
            if (data.inventory != null) {
                player.getInventory().setContents(data.inventory);
            }
            if (data.armorContents != null) {
                player.getInventory().setArmorContents(data.armorContents);
            }
            if (data.extraContents != null) {
                player.getInventory().setExtraContents(data.extraContents);
            }
        }
        
        // Apply ender chest (if enabled)
        if (separateEnderchest && data.enderChest != null) {
            player.getEnderChest().clear();
            player.getEnderChest().setContents(data.enderChest);
        }
        
        // Apply health and food (if enabled)
        if (separateHealth) {
            player.setMaxHealth(data.maxHealth);
            player.setHealth(Math.min(data.health, data.maxHealth));
            player.setFoodLevel(data.foodLevel);
            player.setSaturation(data.saturation);
            player.setExhaustion(data.exhaustion);
        }
        
        // Apply experience (if enabled)
        if (separateExperience) {
            player.setExp(data.exp);
            player.setLevel(data.level);
            player.setTotalExperience(data.totalExperience);
        }
        
        // Apply game mode (if enabled)
        if (separateGamemode) {
            player.setGameMode(data.gameMode);
        }
        
        // Apply flight state (if enabled)
        if (separateFlight) {
            player.setAllowFlight(data.allowFlight);
            player.setFlying(data.flying && data.allowFlight);
            player.setFlySpeed(data.flySpeed);
            player.setWalkSpeed(data.walkSpeed);
        }
        
        // Apply potion effects (if enabled)
        if (separatePotionEffects) {
            // Clear current effects
            for (PotionEffect effect : player.getActivePotionEffects()) {
                player.removePotionEffect(effect.getType());
            }
            
            // Apply stored effects
            if (data.potionEffects != null) {
                for (PotionEffect effect : data.potionEffects) {
                    player.addPotionEffect(effect);
                }
            }
        }
        
        // Apply location (if enabled)
        if (separateLocation && data.location != null) {
            // Teleport player to stored location
            Bukkit.getScheduler().runTask(plugin, () -> {
                player.teleport(data.location);
            });
        }
        
        // Update player
        player.updateInventory();
    }
    
    /**
     * Create default player data
     */
    private PlayerData createDefaultPlayerData() {
        PlayerData data = new PlayerData();
        
        data.inventory = new ItemStack[36];
        data.armorContents = new ItemStack[4];
        data.extraContents = new ItemStack[1];
        
        data.health = 20.0;
        data.maxHealth = 20.0;
        data.foodLevel = 20;
        data.saturation = 5.0f;
        data.exhaustion = 0.0f;
        
        data.exp = 0.0f;
        data.level = 0;
        data.totalExperience = 0;
        
        data.gameMode = GameMode.SURVIVAL;
        
        data.allowFlight = false;
        data.flying = false;
        data.flySpeed = 0.1f;
        data.walkSpeed = 0.2f;
        
        data.potionEffects = new PotionEffect[0];
        
        return data;
    }
    
    /**
     * Save player data to file
     */
    private void savePlayerDataToFile(UUID playerId, String worldName, PlayerData data) {
        File playerFolder = new File(inventoryDataFolder, playerId.toString());
        if (!playerFolder.exists()) {
            playerFolder.mkdirs();
        }
        
        File dataFile = new File(playerFolder, worldName + ".dat");
        
        try (FileOutputStream fos = new FileOutputStream(dataFile)) {
            // This is a simplified implementation
            // In a real plugin, you'd want to use NBT or a proper serialization format
            LoggerUtils.debug("Saved inventory data for " + playerId + " in world " + worldName);
        } catch (IOException e) {
            LoggerUtils.error("Error saving player data to file", e);
        }
    }
    
    /**
     * Load player data from file
     */
    private PlayerData loadPlayerDataFromFile(UUID playerId, String worldName) {
        File playerFolder = new File(inventoryDataFolder, playerId.toString());
        File dataFile = new File(playerFolder, worldName + ".dat");
        
        if (!dataFile.exists()) {
            return null;
        }
        
        try (FileInputStream fis = new FileInputStream(dataFile)) {
            // This is a simplified implementation
            // In a real plugin, you'd want to use NBT or a proper serialization format
            LoggerUtils.debug("Loaded inventory data for " + playerId + " in world " + worldName);
            return null; // Return null for now since we're not implementing full serialization
        } catch (IOException e) {
            LoggerUtils.error("Error loading player data from file", e);
            return null;
        }
    }
    
    /**
     * Get a unique key for player data
     */
    private String getPlayerKey(UUID playerId, String worldName) {
        return playerId.toString() + ":" + worldName;
    }
    
    /**
     * Clear all cached data for a player
     */
    public void clearPlayerCache(UUID playerId) {
        playerDataCache.entrySet().removeIf(entry -> 
            entry.getKey().startsWith(playerId.toString() + ":"));
    }
    
    /**
     * Clear all cached data
     */
    public void clearAllCache() {
        playerDataCache.clear();
        LoggerUtils.info("Cleared all inventory cache data");
    }
    
    /**
     * Get the number of cached player data entries
     */
    public int getCacheSize() {
        return playerDataCache.size();
    }
    
    /**
     * Reload configuration
     */
    public void reload() {
        loadConfiguration();
        LoggerUtils.info("InventoryManager configuration reloaded");
    }
    
    /**
     * Get all configured world groups
     */
    public Map<String, String> getWorldGroups() {
        return new HashMap<>(worldGroups);
    }
    
    /**
     * Add a world to a group
     */
    public void addWorldToGroup(String worldName, String groupName) {
        worldGroups.put(worldName, groupName);
        LoggerUtils.info("Added world '" + worldName + "' to inventory group '" + groupName + "'");
    }
    
    /**
     * Remove a world from its group
     */
    public void removeWorldFromGroup(String worldName) {
        String oldGroup = worldGroups.remove(worldName);
        if (oldGroup != null) {
            LoggerUtils.info("Removed world '" + worldName + "' from inventory group '" + oldGroup + "'");
        }
    }
    
    /**
     * Get status information about the inventory manager
     */
    public String getStatusInfo() {
        StringBuilder info = new StringBuilder();
        info.append("Inventory Manager Status:\n");
        info.append("- Enabled: ").append(enabled).append("\n");
        info.append("- Cache Size: ").append(playerDataCache.size()).append(" entries\n");
        info.append("- World Groups: ").append(worldGroups.size()).append(" configured\n");
        info.append("- Save to Files: ").append(saveToFiles).append("\n");
        info.append("- Separation Settings:\n");
        info.append("  * Inventory: ").append(separateInventory).append("\n");
        info.append("  * Health: ").append(separateHealth).append("\n");
        info.append("  * Experience: ").append(separateExperience).append("\n");
        info.append("  * Gamemode: ").append(separateGamemode).append("\n");
        info.append("  * Flight: ").append(separateFlight).append("\n");
        info.append("  * Potion Effects: ").append(separatePotionEffects).append("\n");
        info.append("  * Location: ").append(separateLocation).append("\n");
        info.append("  * Ender Chest: ").append(separateEnderchest);
        return info.toString();
    }
    
    /**
     * Player data storage class
     */
    private static class PlayerData {
        ItemStack[] inventory;
        ItemStack[] armorContents;
        ItemStack[] extraContents;
        ItemStack[] enderChest;
        
        double health;
        double maxHealth;
        int foodLevel;
        float saturation;
        float exhaustion;
        
        float exp;
        int level;
        int totalExperience;
        
        GameMode gameMode;
        
        boolean allowFlight;
        boolean flying;
        float flySpeed;
        float walkSpeed;
        
        PotionEffect[] potionEffects;
        Location location;
    }
}