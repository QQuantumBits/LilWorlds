package org.hydr4.lilworlds;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.hydr4.lilworlds.commands.WorldCommand;
import org.hydr4.lilworlds.commands.WorldsCommand;
import org.hydr4.lilworlds.config.ConfigManager;
import org.hydr4.lilworlds.generators.GeneratorManager;
import org.hydr4.lilworlds.integrations.BStatsIntegration;
import org.hydr4.lilworlds.integrations.PlaceholderAPIIntegration;
import org.hydr4.lilworlds.managers.InventoryManager;
import org.hydr4.lilworlds.managers.WorldManager;
import org.hydr4.lilworlds.utils.ColorUtils;
import org.hydr4.lilworlds.utils.LoggerUtils;
import org.hydr4.lilworlds.utils.SecurityUtils;
import org.hydr4.lilworlds.utils.ServerUtils;
import org.hydr4.lilworlds.api.LilWorldsAPI;

public class LilWorlds extends JavaPlugin {
    
    private static LilWorlds instance;
    private ConfigManager configManager;
    private WorldManager worldManager;
    private GeneratorManager generatorManager;
    private InventoryManager inventoryManager;
    private PlaceholderAPIIntegration placeholderAPIIntegration;
    private BStatsIntegration bStatsIntegration;
    
    @Override
    public void onEnable() {
        instance = this;
        
        // Initialize colorful startup message
        displayStartupMessage();
        
        // Initialize managers
        LoggerUtils.info("Initializing configuration manager...");
        this.configManager = new ConfigManager(this);
        
        LoggerUtils.info("Initializing generator manager...");
        this.generatorManager = new GeneratorManager(this);
        
        LoggerUtils.info("Initializing world manager...");
        this.worldManager = new WorldManager(this);
        
        LoggerUtils.info("Initializing inventory manager...");
        this.inventoryManager = new InventoryManager(this);
        
        // Register commands
        LoggerUtils.info("Registering commands...");
        registerCommands();
        
        // Initialize integrations
        LoggerUtils.info("Initializing integrations...");
        initializeIntegrations();
        
        // Initialize security and server features
        LoggerUtils.info("Initializing security and server optimizations...");
        initializeSecurityAndOptimizations();
        
        // Initialize API
        LoggerUtils.info("Initializing LilWorlds API...");
        LilWorldsAPI.initialize(this);
        
        // Load worlds
        LoggerUtils.info("Loading worlds from configuration...");
        worldManager.loadWorldsFromConfig();
        
        LoggerUtils.success("LilWorlds has been successfully enabled!");
        LoggerUtils.info("Plugin version: " + getDescription().getVersion());
        LoggerUtils.info("Supported Minecraft versions: 1.16 - 1.21.5");
        LoggerUtils.info("API initialized and ready for use!");
    }
    
    @Override
    public void onDisable() {
        LoggerUtils.info("Disabling LilWorlds...");
        
        if (worldManager != null) {
            LoggerUtils.info("Saving world configurations...");
            worldManager.saveAllWorlds();
        }
        
        if (placeholderAPIIntegration != null) {
            placeholderAPIIntegration.unregisterExpansion();
        }
        
        LoggerUtils.success("LilWorlds has been successfully disabled!");
    }
    
    private void displayStartupMessage() {
        String[] messages = {
            "",
            ColorUtils.colorize("&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"),
            "",
            ColorUtils.colorize("  &b&lLilWorlds &8- &7A performant world management plugin"),
            ColorUtils.colorize("  &7Version: &a" + getDescription().getVersion()),
            ColorUtils.colorize("  &7Author: &aHydr4"),
            ColorUtils.colorize("  &7Website: &ahttps://github.com/CarmineArcangelo/LilWorlds"),
            "",
            ColorUtils.colorize("  &7Starting up with &a" + Runtime.getRuntime().availableProcessors() + " &7CPU cores"),
            ColorUtils.colorize("  &7Java version: &a" + System.getProperty("java.version")),
            ColorUtils.colorize("  &7Server version: &a" + Bukkit.getVersion()),
            "",
            ColorUtils.colorize("&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬"),
            ""
        };
        
        for (String message : messages) {
            Bukkit.getConsoleSender().sendMessage(message);
        }
    }
    
    private void registerCommands() {
        WorldCommand worldCommand = new WorldCommand(this);
        WorldsCommand worldsCommand = new WorldsCommand(this);
        
        getCommand("world").setExecutor(worldCommand);
        getCommand("world").setTabCompleter(worldCommand);
        getCommand("w").setExecutor(worldCommand);
        getCommand("w").setTabCompleter(worldCommand);
        
        getCommand("worlds").setExecutor(worldsCommand);
        getCommand("worlds").setTabCompleter(worldsCommand);
    }
    
    private void initializeIntegrations() {
        // PlaceholderAPI Integration
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            LoggerUtils.info("PlaceholderAPI found! Enabling integration...");
            this.placeholderAPIIntegration = new PlaceholderAPIIntegration(this);
            this.placeholderAPIIntegration.registerExpansion();
            LoggerUtils.success("PlaceholderAPI integration enabled!");
        } else {
            LoggerUtils.warn("PlaceholderAPI not found. Placeholders will not be available.");
        }
        
        // bStats Integration
        LoggerUtils.info("Initializing bStats metrics...");
        this.bStatsIntegration = new BStatsIntegration(this);
        LoggerUtils.success("bStats metrics initialized!");
    }
    
    private void initializeSecurityAndOptimizations() {
        // Log server information
        LoggerUtils.info("Server Information: " + ServerUtils.getServerInfo());
        LoggerUtils.info("Memory Information: " + ServerUtils.getMemoryInfo());
        
        // Start security reset task (runs every hour)
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> {
            SecurityUtils.resetRateLimits();
            LoggerUtils.debug("Security rate limits reset");
        }, 72000L, 72000L); // 72000 ticks = 1 hour
        
        LoggerUtils.success("Security and optimization features initialized!");
    }
    
    public void reload() {
        LoggerUtils.info("Reloading LilWorlds...");
        
        // Reload configuration
        configManager.reload();
        
        // Reload generators
        generatorManager.reload();
        
        // Reload worlds
        worldManager.reload();
        
        LoggerUtils.success("LilWorlds has been reloaded successfully!");
    }
    
    // Getters
    public static LilWorlds getInstance() {
        return instance;
    }
    
    public ConfigManager getConfigManager() {
        return configManager;
    }
    
    public WorldManager getWorldManager() {
        return worldManager;
    }
    
    public GeneratorManager getGeneratorManager() {
        return generatorManager;
    }
    
    public InventoryManager getInventoryManager() {
        return inventoryManager;
    }
    
    public PlaceholderAPIIntegration getPlaceholderAPIIntegration() {
        return placeholderAPIIntegration;
    }
    
    public BStatsIntegration getBStatsIntegration() {
        return bStatsIntegration;
    }
}