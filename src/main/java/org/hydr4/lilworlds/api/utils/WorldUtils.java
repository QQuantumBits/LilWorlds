package org.hydr4.lilworlds.api.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Utility class for world-related operations
 * 
 * @author LilWorlds Team
 * @version 1.4.0
 * @since 1.4.0
 */
public class WorldUtils {
    
    /**
     * Check if a world name is valid
     * 
     * @param worldName The world name to check
     * @return true if the name is valid
     */
    public static boolean isValidWorldName(String worldName) {
        if (worldName == null || worldName.trim().isEmpty()) {
            return false;
        }
        
        // Check length
        if (worldName.length() > 32) {
            return false;
        }
        
        // Check for invalid characters
        return worldName.matches("^[a-zA-Z0-9_-]+$");
    }
    
    /**
     * Check if a world folder exists
     * 
     * @param worldName The world name
     * @return true if the world folder exists
     */
    public static boolean worldFolderExists(String worldName) {
        File worldFolder = new File(Bukkit.getWorldContainer(), worldName);
        return worldFolder.exists() && worldFolder.isDirectory();
    }
    
    /**
     * Check if a folder is a valid world folder
     * 
     * @param folder The folder to check
     * @return true if it's a valid world folder
     */
    public static boolean isWorldFolder(File folder) {
        if (!folder.exists() || !folder.isDirectory()) {
            return false;
        }
        
        return new File(folder, "level.dat").exists() || 
               new File(folder, "region").exists();
    }
    
    /**
     * Get a safe spawn location for a world
     * 
     * @param world The world
     * @return A safe spawn location
     */
    public static Location getSafeSpawnLocation(World world) {
        if (world == null) {
            return null;
        }
        
        Location spawn = world.getSpawnLocation();
        
        // Check if spawn is safe
        if (isSafeLocation(spawn)) {
            return spawn;
        }
        
        // Try to find a safe location near spawn
        for (int x = -5; x <= 5; x++) {
            for (int z = -5; z <= 5; z++) {
                Location test = spawn.clone().add(x, 0, z);
                test.setY(world.getHighestBlockYAt(test) + 1);
                
                if (isSafeLocation(test)) {
                    return test;
                }
            }
        }
        
        // If no safe location found, return original spawn
        return spawn;
    }
    
    /**
     * Check if a location is safe for teleportation
     * 
     * @param location The location to check
     * @return true if the location is safe
     */
    public static boolean isSafeLocation(Location location) {
        if (location == null || location.getWorld() == null) {
            return false;
        }
        
        World world = location.getWorld();
        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();
        
        // Check if there's solid ground
        if (!world.getBlockAt(x, y - 1, z).getType().isSolid()) {
            return false;
        }
        
        // Check if there's enough space for the player
        if (world.getBlockAt(x, y, z).getType().isSolid() || 
            world.getBlockAt(x, y + 1, z).getType().isSolid()) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Get all players in a specific world
     * 
     * @param worldName The world name
     * @return List of players in the world
     */
    public static List<Player> getPlayersInWorld(String worldName) {
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            return List.of();
        }
        
        return world.getPlayers();
    }
    
    /**
     * Get player names in a specific world
     * 
     * @param worldName The world name
     * @return List of player names in the world
     */
    public static List<String> getPlayerNamesInWorld(String worldName) {
        return getPlayersInWorld(worldName).stream()
                .map(Player::getName)
                .collect(Collectors.toList());
    }
    
    /**
     * Move all players from one world to another
     * 
     * @param fromWorld The source world name
     * @param toWorld The target world name
     * @return Number of players moved
     */
    public static int moveAllPlayers(String fromWorld, String toWorld) {
        World from = Bukkit.getWorld(fromWorld);
        World to = Bukkit.getWorld(toWorld);
        
        if (from == null || to == null) {
            return 0;
        }
        
        List<Player> players = from.getPlayers();
        Location safeSpawn = getSafeSpawnLocation(to);
        
        for (Player player : players) {
            if (player.isOnline()) {
                player.teleport(safeSpawn);
            }
        }
        
        return players.size();
    }
    
    /**
     * Calculate the size of a world folder
     * 
     * @param worldName The world name
     * @return The size in bytes
     */
    public static long calculateWorldSize(String worldName) {
        File worldFolder = new File(Bukkit.getWorldContainer(), worldName);
        if (!worldFolder.exists()) {
            return 0;
        }
        
        return calculateFolderSize(worldFolder);
    }
    
    /**
     * Format bytes to human-readable string
     * 
     * @param bytes The number of bytes
     * @return Formatted string (e.g., "1.5 MB")
     */
    public static String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp - 1) + "";
        return String.format("%.1f %sB", bytes / Math.pow(1024, exp), pre);
    }
    
    /**
     * Get the formatted size of a world
     * 
     * @param worldName The world name
     * @return Formatted size string
     */
    public static String getFormattedWorldSize(String worldName) {
        return formatBytes(calculateWorldSize(worldName));
    }
    
    /**
     * Check if a world is the main world
     * 
     * @param worldName The world name
     * @return true if it's the main world
     */
    public static boolean isMainWorld(String worldName) {
        List<World> worlds = Bukkit.getWorlds();
        if (worlds.isEmpty()) {
            return false;
        }
        
        return worlds.get(0).getName().equals(worldName);
    }
    
    /**
     * Get the main world name
     * 
     * @return The main world name, or null if no worlds exist
     */
    public static String getMainWorldName() {
        List<World> worlds = Bukkit.getWorlds();
        return worlds.isEmpty() ? null : worlds.get(0).getName();
    }
    
    /**
     * Get weather description for a world
     * 
     * @param world The world
     * @return Weather description
     */
    public static String getWeatherDescription(World world) {
        if (world == null) {
            return "Unknown";
        }
        
        if (world.isThundering()) {
            return "Thundering";
        } else if (world.hasStorm()) {
            return "Raining";
        } else {
            return "Clear";
        }
    }
    
    /**
     * Format a location to a readable string
     * 
     * @param location The location
     * @return Formatted location string
     */
    public static String formatLocation(Location location) {
        if (location == null) {
            return "Unknown";
        }
        
        return String.format("%.1f, %.1f, %.1f", 
            location.getX(), location.getY(), location.getZ());
    }
    
    private static long calculateFolderSize(File folder) {
        long size = 0;
        File[] files = folder.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    size += file.length();
                } else if (file.isDirectory()) {
                    size += calculateFolderSize(file);
                }
            }
        }
        return size;
    }
}