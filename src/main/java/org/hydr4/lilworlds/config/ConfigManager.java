package org.hydr4.lilworlds.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.hydr4.lilworlds.LilWorlds;
import org.hydr4.lilworlds.utils.LoggerUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class ConfigManager {
    
    private final LilWorlds plugin;
    private final Map<String, FileConfiguration> configs;
    private final Map<String, File> configFiles;
    
    public ConfigManager(LilWorlds plugin) {
        this.plugin = plugin;
        this.configs = new HashMap<>();
        this.configFiles = new HashMap<>();
        
        initializeConfigs();
    }
    
    private void initializeConfigs() {
        // Create plugin data folder if it doesn't exist
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }
        
        // Initialize main config
        plugin.saveDefaultConfig();
        loadConfig("config");
        
        // Initialize worlds config
        loadConfig("worlds");
        
        // Initialize messages config
        loadConfig("messages");
        
        LoggerUtils.success("Configuration files loaded successfully!");
    }
    
    /**
     * Load a configuration file
     */
    public void loadConfig(String name) {
        File configFile = new File(plugin.getDataFolder(), name + ".yml");
        
        // Create default file if it doesn't exist
        if (!configFile.exists()) {
            if (plugin.getResource(name + ".yml") != null) {
                plugin.saveResource(name + ".yml", false);
            } else {
                try {
                    configFile.createNewFile();
                } catch (IOException e) {
                    LoggerUtils.error("Failed to create " + name + ".yml", e);
                    return;
                }
            }
        }
        
        FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);
        
        // Auto-add missing keys from default configuration
        addMissingKeys(name, config, configFile);
        
        configs.put(name, config);
        configFiles.put(name, configFile);
        
        LoggerUtils.debug("Loaded configuration: " + name + ".yml");
    }
    
    /**
     * Add missing keys from the default configuration to the user's config
     */
    private void addMissingKeys(String configName, FileConfiguration userConfig, File userConfigFile) {
        InputStream defaultConfigStream = plugin.getResource(configName + ".yml");
        if (defaultConfigStream == null) {
            return; // No default config to compare against
        }
        
        FileConfiguration defaultConfig = YamlConfiguration.loadConfiguration(
            new java.io.InputStreamReader(defaultConfigStream)
        );
        
        boolean configChanged = false;
        
        // Check for missing keys and add them
        for (String key : defaultConfig.getKeys(true)) {
            if (!userConfig.contains(key)) {
                Object defaultValue = defaultConfig.get(key);
                userConfig.set(key, defaultValue);
                configChanged = true;
                LoggerUtils.info("Added missing config key '" + key + "' with default value: " + defaultValue);
            }
        }
        
        // Save the config if we added any missing keys
        if (configChanged) {
            try {
                userConfig.save(userConfigFile);
                LoggerUtils.success("Updated " + configName + ".yml with missing configuration keys!");
            } catch (IOException e) {
                LoggerUtils.error("Failed to save updated " + configName + ".yml", e);
            }
        }
        
        try {
            defaultConfigStream.close();
        } catch (IOException e) {
            LoggerUtils.warn("Failed to close default config stream for " + configName + ".yml");
        }
    }
    
    /**
     * Save a configuration file
     */
    public void saveConfig(String name) {
        FileConfiguration config = configs.get(name);
        File configFile = configFiles.get(name);
        
        if (config != null && configFile != null) {
            try {
                config.save(configFile);
                LoggerUtils.debug("Saved configuration: " + name + ".yml");
            } catch (IOException e) {
                LoggerUtils.error("Failed to save " + name + ".yml", e);
            }
        }
    }
    
    /**
     * Reload all configuration files
     */
    public void reload() {
        LoggerUtils.info("Reloading configuration files...");
        
        for (String configName : configs.keySet()) {
            loadConfig(configName);
        }
        
        LoggerUtils.success("Configuration files reloaded!");
    }
    
    /**
     * Get a configuration by name
     */
    public FileConfiguration getConfig(String name) {
        return configs.get(name);
    }
    
    /**
     * Get the main config
     */
    public FileConfiguration getMainConfig() {
        return getConfig("config");
    }
    
    /**
     * Get the worlds config
     */
    public FileConfiguration getWorldsConfig() {
        return getConfig("worlds");
    }
    
    /**
     * Get the messages config
     */
    public FileConfiguration getMessagesConfig() {
        return getConfig("messages");
    }
    
    /**
     * Save all configurations
     */
    public void saveAll() {
        for (String configName : configs.keySet()) {
            saveConfig(configName);
        }
    }
    
    // Configuration value getters with defaults
    
    public boolean isDebugEnabled() {
        return getMainConfig().getBoolean("debug", false);
    }
    
    public boolean isAutoLoadEnabled() {
        return getMainConfig().getBoolean("auto-load-worlds", true);
    }
    
    public boolean isAutoSaveEnabled() {
        return getMainConfig().getBoolean("auto-save-worlds", true);
    }
    
    public int getAutoSaveInterval() {
        return getMainConfig().getInt("auto-save-interval", 300); // 5 minutes default
    }
    
    public boolean isMetricsEnabled() {
        return getMainConfig().getBoolean("metrics", true);
    }
    
    public boolean isPlaceholderAPIEnabled() {
        return getMainConfig().getBoolean("integrations.placeholderapi", true);
    }
    
    public String getDefaultWorldType() {
        return getMainConfig().getString("defaults.world-type", "NORMAL");
    }
    
    public String getDefaultGenerator() {
        return getMainConfig().getString("defaults.generator", "");
    }
    
    public boolean getDefaultGenerateStructures() {
        return getMainConfig().getBoolean("defaults.generate-structures", true);
    }
    
    public String getDefaultGameMode() {
        return getMainConfig().getString("defaults.gamemode", "SURVIVAL");
    }
    
    public String getDefaultDifficulty() {
        return getMainConfig().getString("defaults.difficulty", "NORMAL");
    }
    
    public boolean getDefaultPvP() {
        return getMainConfig().getBoolean("defaults.pvp", true);
    }
    
    public boolean getDefaultKeepSpawnInMemory() {
        return getMainConfig().getBoolean("defaults.keep-spawn-in-memory", true);
    }
    
    public boolean getDefaultAllowAnimals() {
        return getMainConfig().getBoolean("defaults.allow-animals", true);
    }
    
    public boolean getDefaultAllowMonsters() {
        return getMainConfig().getBoolean("defaults.allow-monsters", true);
    }
    
    public int getMaxWorldsPerPlayer() {
        return getMainConfig().getInt("limits.max-worlds-per-player", -1); // -1 = unlimited
    }
    
    public int getMaxTotalWorlds() {
        return getMainConfig().getInt("limits.max-total-worlds", -1); // -1 = unlimited
    }
    
    public long getWorldCreationCooldown() {
        return getMainConfig().getLong("limits.creation-cooldown", 0); // 0 = no cooldown
    }
    
    // Configuration setters
    
    public void setConfigValue(String config, String path, Object value) {
        FileConfiguration cfg = getConfig(config);
        if (cfg != null) {
            cfg.set(path, value);
            saveConfig(config);
            LoggerUtils.debug("Set " + config + "." + path + " to " + value);
        }
    }
    
    public void setMainConfigValue(String path, Object value) {
        setConfigValue("config", path, value);
    }
    
    public void setWorldsConfigValue(String path, Object value) {
        setConfigValue("worlds", path, value);
    }
    
    // Message getters
    
    public String getMessage(String key) {
        return getMessagesConfig().getString(key, "&cMessage not found: " + key);
    }
    
    public String getMessage(String key, String... placeholders) {
        String message = getMessage(key);
        
        for (int i = 0; i < placeholders.length; i += 2) {
            if (i + 1 < placeholders.length) {
                message = message.replace(placeholders[i], placeholders[i + 1]);
            }
        }
        
        return message;
    }
    
    public String getFormattedMessage(String key, Object... args) {
        String message = getMessage(key);
        
        // Replace numbered placeholders {0}, {1}, etc.
        for (int i = 0; i < args.length; i++) {
            message = message.replace("{" + i + "}", String.valueOf(args[i]));
        }
        
        return message;
    }
    
    // Specific message getters for common messages
    public String getNoPermissionMessage() {
        return getMessage("no-permission");
    }
    
    public String getPlayerOnlyMessage() {
        return getMessage("player-only");
    }
    
    public String getUnknownCommandMessage() {
        return getMessage("unknown-command");
    }
    
    public String getWorldCreatedMessage(String worldName) {
        return getMessage("world-created", "{world}", worldName);
    }
    
    public String getWorldLoadedMessage(String worldName) {
        return getMessage("world-loaded", "{world}", worldName);
    }
    
    public String getWorldUnloadedMessage(String worldName) {
        return getMessage("world-unloaded", "{world}", worldName);
    }
    
    public String getWorldClonedMessage(String source, String target) {
        return getMessage("world-cloned", "{source}", source, "{target}", target);
    }
    
    public String getWorldImportedMessage(String worldName) {
        return getMessage("world-imported", "{world}", worldName);
    }
    
    public String getWorldExistsMessage(String worldName) {
        return getMessage("world-exists", "{world}", worldName);
    }
    
    public String getWorldNotFoundMessage(String worldName) {
        return getMessage("world-not-found", "{world}", worldName);
    }
    
    public String getWorldCreationFailedMessage(String worldName) {
        return getMessage("world-creation-failed", "{world}", worldName);
    }
    
    public String getWorldLoadFailedMessage(String worldName) {
        return getMessage("world-load-failed", "{world}", worldName);
    }
    
    public String getWorldUnloadFailedMessage(String worldName) {
        return getMessage("world-unload-failed", "{world}", worldName);
    }
    
    public String getWorldCloneFailedMessage(String source, String target) {
        return getMessage("world-clone-failed", "{source}", source, "{target}", target);
    }
    
    public String getWorldImportFailedMessage(String worldName) {
        return getMessage("world-import-failed", "{world}", worldName);
    }
    
    public String getSpawnSetMessage(String worldName) {
        return getMessage("spawn-set", "{world}", worldName);
    }
    
    public String getUniversalSpawnSetMessage() {
        return getMessage("universal-spawn-set");
    }
    
    public String getConfigReloadedMessage() {
        return getMessage("config-reloaded");
    }
    
    public String getPluginReloadedMessage() {
        return getMessage("plugin-reloaded");
    }
}