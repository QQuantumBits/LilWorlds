package com.example.lilworldsexample;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.hydr4.lilworlds.api.LilWorldsAPI;
import org.hydr4.lilworlds.api.events.WorldCreatedEvent;
import org.hydr4.lilworlds.api.events.WorldTeleportEvent;
import org.hydr4.lilworlds.api.world.WorldInfo;
import org.hydr4.lilworlds.api.world.WorldManager;
import org.hydr4.lilworlds.api.utils.WorldUtils;

/**
 * Example plugin demonstrating LilWorlds API usage
 * 
 * This example shows:
 * - Basic API usage
 * - World creation with builder pattern
 * - Event handling
 * - Async operations
 * - Utility functions
 */
public class LilWorldsExample extends JavaPlugin implements Listener, CommandExecutor {
    
    private LilWorldsAPI api;
    private WorldManager worldManager;
    
    @Override
    public void onEnable() {
        // Get the LilWorlds API instance
        try {
            this.api = LilWorldsAPI.getInstance();
            this.worldManager = api.getWorldManager();
            
            getLogger().info("LilWorlds API connected successfully!");
            getLogger().info("LilWorlds version: " + api.getVersion());
            
        } catch (IllegalStateException e) {
            getLogger().severe("LilWorlds plugin not found! This plugin requires LilWorlds.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        
        // Register events
        getServer().getPluginManager().registerEvents(this, this);
        
        // Register commands
        getCommand("createworld").setExecutor(this);
        getCommand("worldinfo").setExecutor(this);
        getCommand("worldlist").setExecutor(this);
        getCommand("tpworld").setExecutor(this);
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        switch (command.getName().toLowerCase()) {
            case "createworld":
                return handleCreateWorld(sender, args);
            case "worldinfo":
                return handleWorldInfo(sender, args);
            case "worldlist":
                return handleWorldList(sender, args);
            case "tpworld":
                return handleTeleportWorld(sender, args);
            default:
                return false;
        }
    }
    
    /**
     * Example: Create a world using the WorldBuilder API
     */
    private boolean handleCreateWorld(CommandSender sender, String[] args) {
        if (args.length < 1) {
            sender.sendMessage("Usage: /createworld <name> [environment]");
            return true;
        }
        
        String worldName = args[0];
        World.Environment environment = World.Environment.NORMAL;
        
        if (args.length > 1) {
            try {
                environment = World.Environment.valueOf(args[1].toUpperCase());
            } catch (IllegalArgumentException e) {
                sender.sendMessage("Invalid environment! Use: NORMAL, NETHER, THE_END");
                return true;
            }
        }
        
        // Validate world name
        if (!WorldUtils.isValidWorldName(worldName)) {
            sender.sendMessage("Invalid world name! Use only letters, numbers, hyphens, and underscores.");
            return true;
        }
        
        // Check if world already exists
        if (worldManager.worldExists(worldName)) {
            sender.sendMessage("World '" + worldName + "' already exists!");
            return true;
        }
        
        sender.sendMessage("Creating world '" + worldName + "'...");
        
        // Create world using the builder pattern
        api.createWorld(worldName)
            .environment(environment)
            .structures(true)
            .keepSpawnLoaded(true)
            .onSuccess(world -> {
                sender.sendMessage("World '" + worldName + "' created successfully!");
                
                // Configure the world
                world.setDifficulty(org.bukkit.Difficulty.EASY);
                world.setGameRuleValue("doDaylightCycle", "true");
                world.setGameRuleValue("doMobSpawning", "true");
                
                // Teleport player if it's a player command
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    player.teleport(WorldUtils.getSafeSpawnLocation(world));
                    player.sendMessage("Welcome to your new world!");
                }
            })
            .onFailure(error -> {
                sender.sendMessage("Failed to create world: " + error);
                getLogger().warning("World creation failed for '" + worldName + "': " + error);
            })
            .buildAsync();
        
        return true;
    }
    
    /**
     * Example: Get detailed world information
     */
    private boolean handleWorldInfo(CommandSender sender, String[] args) {
        String worldName;
        
        if (args.length > 0) {
            worldName = args[0];
        } else if (sender instanceof Player) {
            worldName = ((Player) sender).getWorld().getName();
        } else {
            sender.sendMessage("Usage: /worldinfo <world>");
            return true;
        }
        
        WorldInfo info = worldManager.getWorldInfo(worldName);
        if (info == null) {
            sender.sendMessage("World '" + worldName + "' not found!");
            return true;
        }
        
        // Display comprehensive world information
        sender.sendMessage("=== World Information ===");
        sender.sendMessage("Name: " + info.getName());
        sender.sendMessage("Loaded: " + (info.isLoaded() ? "Yes" : "No"));
        sender.sendMessage("Managed: " + (info.isManaged() ? "Yes" : "No"));
        sender.sendMessage("Environment: " + info.getEnvironment().name());
        
        if (info.isLoaded()) {
            sender.sendMessage("Difficulty: " + info.getDifficulty().name());
            sender.sendMessage("PvP: " + (info.isPvPEnabled() ? "Enabled" : "Disabled"));
            sender.sendMessage("Players: " + info.getPlayerCount());
            sender.sendMessage("Time: " + info.getTime());
            sender.sendMessage("Weather: " + WorldUtils.getWeatherDescription(info.getWorld()));
            
            if (info.getSpawnLocation() != null) {
                sender.sendMessage("Spawn: " + WorldUtils.formatLocation(info.getSpawnLocation()));
            }
        }
        
        if (info.getGenerator() != null) {
            sender.sendMessage("Generator: " + info.getGenerator());
        }
        
        sender.sendMessage("Seed: " + info.getSeed());
        sender.sendMessage("Structures: " + (info.hasStructures() ? "Yes" : "No"));
        
        // Get world size asynchronously
        worldManager.getFormattedWorldSizeAsync(worldName).thenAccept(size -> {
            sender.sendMessage("Size: " + size);
        });
        
        return true;
    }
    
    /**
     * Example: List all worlds with their status
     */
    private boolean handleWorldList(CommandSender sender, String[] args) {
        sender.sendMessage("=== World List ===");
        
        // Loaded worlds
        sender.sendMessage("Loaded Worlds:");
        for (String worldName : worldManager.getLoadedWorlds()) {
            WorldInfo info = worldManager.getWorldInfo(worldName);
            String status = info.isManaged() ? "[Managed]" : "[Bukkit]";
            sender.sendMessage("  " + worldName + " " + status + " - Players: " + info.getPlayerCount());
        }
        
        // Unloaded worlds
        sender.sendMessage("Unloaded Worlds:");
        for (String worldName : worldManager.getUnloadedWorlds()) {
            WorldInfo info = worldManager.getWorldInfo(worldName);
            String status = (info != null && info.isManaged()) ? "[Managed]" : "[External]";
            sender.sendMessage("  " + worldName + " " + status);
        }
        
        return true;
    }
    
    /**
     * Example: Teleport to a world using the API
     */
    private boolean handleTeleportWorld(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players!");
            return true;
        }
        
        if (args.length < 1) {
            sender.sendMessage("Usage: /tpworld <world>");
            return true;
        }
        
        Player player = (Player) sender;
        String worldName = args[0];
        
        // Check if world exists
        if (!worldManager.worldExists(worldName)) {
            player.sendMessage("World '" + worldName + "' does not exist!");
            return true;
        }
        
        player.sendMessage("Teleporting to world '" + worldName + "'...");
        
        // Use the API teleport method
        worldManager.teleportPlayerToWorld(player, worldName).thenAccept(success -> {
            if (success) {
                player.sendMessage("Successfully teleported to '" + worldName + "'!");
            } else {
                player.sendMessage("Failed to teleport to '" + worldName + "'!");
            }
        });
        
        return true;
    }
    
    /**
     * Example: Listen to world creation events
     */
    @EventHandler
    public void onWorldCreated(WorldCreatedEvent event) {
        World world = event.getWorld();
        long creationTime = event.getCreationTime();
        
        getLogger().info("World '" + world.getName() + "' was created in " + creationTime + "ms");
        
        // Broadcast to all players
        Bukkit.broadcastMessage("New world '" + world.getName() + "' has been created!");
        
        // Set up default world settings
        world.setGameRuleValue("announceAdvancements", "true");
        world.setGameRuleValue("commandBlockOutput", "false");
        world.setGameRuleValue("logAdminCommands", "false");
        
        // Example: Create a backup after world creation
        worldManager.backupWorldAsync(world.getName(), world.getName() + "_initial_backup")
            .thenAccept(success -> {
                if (success) {
                    getLogger().info("Initial backup created for world: " + world.getName());
                }
            });
    }
    
    /**
     * Example: Listen to teleportation events and customize behavior
     */
    @EventHandler
    public void onWorldTeleport(WorldTeleportEvent event) {
        Player player = event.getPlayer();
        World fromWorld = event.getFromWorld();
        World toWorld = event.getToWorld();
        
        getLogger().info(player.getName() + " is teleporting from " + 
                        fromWorld.getName() + " to " + toWorld.getName());
        
        // Example: Special handling for certain worlds
        if (toWorld.getName().equals("pvp")) {
            player.sendMessage("§cWarning: You are entering a PvP world!");
            player.sendMessage("§cBe careful and watch out for other players!");
        } else if (toWorld.getName().equals("creative")) {
            player.sendMessage("§aWelcome to the creative world!");
            player.sendMessage("§aFeel free to build and experiment!");
        }
        
        // Example: Modify teleport location for specific worlds
        if (toWorld.getName().equals("spawn")) {
            // Teleport to a custom location instead of spawn
            event.setToLocation(toWorld.getSpawnLocation().add(0, 5, 0));
        }
        
        // Example: Cancel teleportation under certain conditions
        if (player.getHealth() < 5.0 && toWorld.getName().equals("dangerous")) {
            event.setCancelled(true);
            event.setCancelReason("You are too weak to enter this world!");
            player.sendMessage("§cYou need more health to enter the dangerous world!");
        }
    }
    
    /**
     * Example utility method: Batch world operations
     */
    public void performBatchOperations() {
        // Load multiple worlds
        String[] worldsToLoad = {"world1", "world2", "world3"};
        
        for (String worldName : worldsToLoad) {
            worldManager.loadWorldAsync(worldName).thenAccept(success -> {
                if (success) {
                    getLogger().info("Loaded world: " + worldName);
                } else {
                    getLogger().warning("Failed to load world: " + worldName);
                }
            });
        }
        
        // Create backup for all managed worlds
        String timestamp = String.valueOf(System.currentTimeMillis());
        for (String worldName : worldManager.getManagedWorlds()) {
            worldManager.backupWorldAsync(worldName, worldName + "_backup_" + timestamp)
                .thenAccept(success -> {
                    if (success) {
                        getLogger().info("Backup created for: " + worldName);
                    }
                });
        }
    }
}