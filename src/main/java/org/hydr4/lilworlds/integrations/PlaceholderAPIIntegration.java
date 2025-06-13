package org.hydr4.lilworlds.integrations;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.hydr4.lilworlds.LilWorlds;
import org.hydr4.lilworlds.managers.WorldManager;
import org.hydr4.lilworlds.utils.LoggerUtils;

public class PlaceholderAPIIntegration extends PlaceholderExpansion {
    
    private final LilWorlds plugin;
    
    public PlaceholderAPIIntegration(LilWorlds plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean persist() {
        return true;
    }
    
    @Override
    public boolean canRegister() {
        return true;
    }
    
    @Override
    public String getAuthor() {
        return plugin.getDescription().getAuthors().toString();
    }
    
    @Override
    public String getIdentifier() {
        return "lilworlds";
    }
    
    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }
    
    @Override
    public String onPlaceholderRequest(Player player, String params) {
        if (player == null) {
            return "";
        }
        
        WorldManager worldManager = plugin.getWorldManager();
        
        // %lilworlds_current_world%
        if (params.equals("current_world")) {
            return player.getWorld().getName();
        }
        
        // %lilworlds_current_world_type%
        if (params.equals("current_world_type")) {
            return player.getWorld().getEnvironment().name();
        }
        
        // %lilworlds_current_world_difficulty%
        if (params.equals("current_world_difficulty")) {
            return player.getWorld().getDifficulty().name();
        }
        
        // %lilworlds_current_world_pvp%
        if (params.equals("current_world_pvp")) {
            return player.getWorld().getPVP() ? "enabled" : "disabled";
        }
        
        // %lilworlds_current_world_players%
        if (params.equals("current_world_players")) {
            return String.valueOf(player.getWorld().getPlayers().size());
        }
        
        // %lilworlds_total_worlds%
        if (params.equals("total_worlds")) {
            return String.valueOf(Bukkit.getWorlds().size());
        }
        
        // %lilworlds_managed_worlds%
        if (params.equals("managed_worlds")) {
            return String.valueOf(worldManager.getManagedWorlds().size());
        }
        
        // %lilworlds_world_exists_<worldname>%
        if (params.startsWith("world_exists_")) {
            String worldName = params.substring("world_exists_".length());
            return Bukkit.getWorld(worldName) != null ? "true" : "false";
        }
        
        // %lilworlds_world_loaded_<worldname>%
        if (params.startsWith("world_loaded_")) {
            String worldName = params.substring("world_loaded_".length());
            return Bukkit.getWorld(worldName) != null ? "true" : "false";
        }
        
        // %lilworlds_world_players_<worldname>%
        if (params.startsWith("world_players_")) {
            String worldName = params.substring("world_players_".length());
            World world = Bukkit.getWorld(worldName);
            return world != null ? String.valueOf(world.getPlayers().size()) : "0";
        }
        
        // %lilworlds_world_type_<worldname>%
        if (params.startsWith("world_type_")) {
            String worldName = params.substring("world_type_".length());
            World world = Bukkit.getWorld(worldName);
            return world != null ? world.getEnvironment().name() : "unknown";
        }
        
        // %lilworlds_world_difficulty_<worldname>%
        if (params.startsWith("world_difficulty_")) {
            String worldName = params.substring("world_difficulty_".length());
            World world = Bukkit.getWorld(worldName);
            return world != null ? world.getDifficulty().name() : "unknown";
        }
        
        // %lilworlds_world_pvp_<worldname>%
        if (params.startsWith("world_pvp_")) {
            String worldName = params.substring("world_pvp_".length());
            World world = Bukkit.getWorld(worldName);
            return world != null ? (world.getPVP() ? "enabled" : "disabled") : "unknown";
        }
        
        // %lilworlds_world_time_<worldname>%
        if (params.startsWith("world_time_")) {
            String worldName = params.substring("world_time_".length());
            World world = Bukkit.getWorld(worldName);
            return world != null ? String.valueOf(world.getTime()) : "0";
        }
        
        // %lilworlds_world_weather_<worldname>%
        if (params.startsWith("world_weather_")) {
            String worldName = params.substring("world_weather_".length());
            World world = Bukkit.getWorld(worldName);
            if (world != null) {
                if (world.hasStorm()) {
                    return world.isThundering() ? "thundering" : "raining";
                } else {
                    return "clear";
                }
            }
            return "unknown";
        }
        
        // %lilworlds_is_managed_<worldname>%
        if (params.startsWith("is_managed_")) {
            String worldName = params.substring("is_managed_".length());
            return worldManager.getManagedWorlds().containsKey(worldName) ? "true" : "false";
        }
        
        // %lilworlds_has_universal_spawn%
        if (params.equals("has_universal_spawn")) {
            return worldManager.getUniversalSpawn() != null ? "true" : "false";
        }
        
        // %lilworlds_universal_spawn_world%
        if (params.equals("universal_spawn_world")) {
            return worldManager.getUniversalSpawn() != null ? 
                worldManager.getUniversalSpawn().getWorld().getName() : "none";
        }
        
        // %lilworlds_custom_generators%
        if (params.equals("custom_generators")) {
            return String.valueOf(plugin.getGeneratorManager().getCustomGeneratorNames().size());
        }
        
        // %lilworlds_has_generator_<generatorname>%
        if (params.startsWith("has_generator_")) {
            String generatorName = params.substring("has_generator_".length());
            return plugin.getGeneratorManager().hasGenerator(generatorName) ? "true" : "false";
        }
        
        // %lilworlds_player_world_count%
        if (params.equals("player_world_count")) {
            int count = 0;
            for (String worldName : worldManager.getManagedWorlds().keySet()) {
                WorldManager.WorldInfo worldInfo = worldManager.getWorldInfo(worldName);
                if (worldInfo != null) {
                    // This is a simplified check - in a real implementation,
                    // you might want to track world ownership
                    count++;
                }
            }
            return String.valueOf(count);
        }
        
        // %lilworlds_version%
        if (params.equals("version")) {
            return plugin.getDescription().getVersion();
        }
        
        // %lilworlds_debug%
        if (params.equals("debug")) {
            return plugin.getConfigManager().isDebugEnabled() ? "enabled" : "disabled";
        }
        
        return null; // Placeholder not found
    }
    
    /**
     * Register the expansion
     */
    public void registerExpansion() {
        if (super.register()) {
            LoggerUtils.success("PlaceholderAPI expansion registered successfully!");
        } else {
            LoggerUtils.error("Failed to register PlaceholderAPI expansion!");
        }
    }
    
    /**
     * Unregister the expansion
     */
    public void unregisterExpansion() {
        super.unregister();
        LoggerUtils.info("PlaceholderAPI expansion unregistered");
    }
}