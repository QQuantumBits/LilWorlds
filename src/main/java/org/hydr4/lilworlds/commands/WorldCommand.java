package org.hydr4.lilworlds.commands;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.hydr4.lilworlds.LilWorlds;
import org.hydr4.lilworlds.generators.CustomGenerator;
import org.hydr4.lilworlds.managers.WorldManager;
import org.hydr4.lilworlds.utils.ColorUtils;
import org.hydr4.lilworlds.utils.SecurityUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class WorldCommand extends BaseCommand {
    
    // Delete confirmation storage: sender -> (worldName, timestamp)
    private final Map<String, Map<String, Long>> deleteConfirmations = new ConcurrentHashMap<>();
    private static final long CONFIRMATION_TIMEOUT = 30000; // 30 seconds
    
    public WorldCommand(LilWorlds plugin) {
        super(plugin, "world", "lilworlds.world", false);
    }
    
    @Override
    protected boolean executeCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }
        
        String subcommand = args[0].toLowerCase();
        
        switch (subcommand) {
            case "info":
                return handleInfo(sender, args);
            case "create":
                return handleCreate(sender, args);
            case "clone":
                return handleClone(sender, args);
            case "load":
                return handleLoad(sender, args);
            case "unload":
                return handleUnload(sender, args);
            case "remove":
            case "delete":
                return handleDelete(sender, args);
            case "import":
                return handleImport(sender, args);
            case "list":
                return handleList(sender, args);
            case "setspawn":
                return handleSetSpawn(sender, args);
            case "setuniversalspawn":
                return handleSetUniversalSpawn(sender, args);
            case "config":
                return handleConfig(sender, args);
            default:
                sendError(sender, plugin.getConfigManager().getMessage("unknown-subcommand", "{subcommand}", subcommand));
                sendHelp(sender);
                return true;
        }
    }
    
    private boolean handleInfo(CommandSender sender, String[] args) {
        if (!hasPermission(sender, "lilworlds.world.info")) {
            sendError(sender, plugin.getConfigManager().getNoPermissionMessage());
            return true;
        }
        
        String worldName;
        if (args.length > 1) {
            worldName = args[1];
        } else if (isPlayer(sender)) {
            worldName = getPlayer(sender).getWorld().getName();
        } else {
            sendError(sender, plugin.getConfigManager().getMessage("specify-world-name"));
            return true;
        }
        
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            sendError(sender, plugin.getConfigManager().getWorldNotFoundMessage(worldName));
            return true;
        }
        
        WorldManager.WorldInfo worldInfo = plugin.getWorldManager().getWorldInfo(worldName);
        
        sendMessage(sender, "");
        sendMessage(sender, plugin.getConfigManager().getMessage("world-info-header"));
        sendMessage(sender, plugin.getConfigManager().getMessage("world-info-title", "{world}", worldName));
        sendMessage(sender, "");
        sendMessage(sender, plugin.getConfigManager().getMessage("environment", "{value}", world.getEnvironment().name()));
        sendMessage(sender, plugin.getConfigManager().getMessage("difficulty", "{value}", world.getDifficulty().name()));
        sendMessage(sender, plugin.getConfigManager().getMessage("pvp-status", "{value}", 
            world.getPVP() ? plugin.getConfigManager().getMessage("enabled") : plugin.getConfigManager().getMessage("disabled")));
        sendMessage(sender, plugin.getConfigManager().getMessage("player-count", "{value}", String.valueOf(world.getPlayers().size())));
        sendMessage(sender, plugin.getConfigManager().getMessage("world-time", "{value}", String.valueOf(world.getTime())));
        sendMessage(sender, plugin.getConfigManager().getMessage("weather-status", "{value}", getWeatherString(world)));
        sendMessage(sender, plugin.getConfigManager().getMessage("spawn-location", "{value}", formatLocation(world.getSpawnLocation())));
        sendMessage(sender, plugin.getConfigManager().getMessage("keep-spawn", "{value}", 
            world.getKeepSpawnInMemory() ? plugin.getConfigManager().getMessage("yes") : plugin.getConfigManager().getMessage("no")));
        sendMessage(sender, plugin.getConfigManager().getMessage("allow-animals", "{value}", 
            world.getAllowAnimals() ? plugin.getConfigManager().getMessage("yes") : plugin.getConfigManager().getMessage("no")));
        sendMessage(sender, plugin.getConfigManager().getMessage("allow-monsters", "{value}", 
            world.getAllowMonsters() ? plugin.getConfigManager().getMessage("yes") : plugin.getConfigManager().getMessage("no")));
        
        if (worldInfo != null) {
            sendMessage(sender, plugin.getConfigManager().getMessage("managed-status", "{value}", plugin.getConfigManager().getMessage("yes")));
            if (worldInfo.getGenerator() != null && !worldInfo.getGenerator().isEmpty()) {
                sendMessage(sender, plugin.getConfigManager().getMessage("generator-info", "{value}", worldInfo.getGenerator()));
            }
            sendMessage(sender, plugin.getConfigManager().getMessage("generate-structures", "{value}", 
                worldInfo.isGenerateStructures() ? plugin.getConfigManager().getMessage("yes") : plugin.getConfigManager().getMessage("no")));
        } else {
            sendMessage(sender, plugin.getConfigManager().getMessage("managed-status", "{value}", plugin.getConfigManager().getMessage("no")));
        }
        
        sendMessage(sender, plugin.getConfigManager().getMessage("world-info-footer"));
        sendMessage(sender, "");
        
        return true;
    }
    
    private boolean handleCreate(CommandSender sender, String[] args) {
        if (!hasPermission(sender, "lilworlds.world.create")) {
            sendError(sender, plugin.getConfigManager().getNoPermissionMessage());
            return true;
        }
        
        // Check security and rate limiting
        if (!SecurityUtils.canPerformOperation(sender, "create")) {
            long remaining = SecurityUtils.getRemainingCooldown(sender, "create", 30);
            if (remaining > 0) {
                sendError(sender, "You must wait " + (remaining / 1000) + " seconds before creating another world!");
            } else {
                sendError(sender, "You have reached the maximum number of world operations for this hour!");
            }
            SecurityUtils.logSecurityEvent(sender, "RATE_LIMITED", "World creation attempt blocked");
            return true;
        }
        
        if (!validateArgs(sender, args, 2, -1)) {
            sendCreateHelp(sender);
            return true;
        }
        
        String worldName = args[1];
        
        // Validate world name
        if (!SecurityUtils.isValidWorldName(worldName)) {
            sendError(sender, "Invalid world name! World names can only contain letters, numbers, hyphens, and underscores (1-32 characters).");
            SecurityUtils.logSecurityEvent(sender, "INVALID_WORLD_NAME", "Attempted to create world with invalid name: " + worldName);
            return true;
        }
        
        // Check if world already exists
        if (Bukkit.getWorld(worldName) != null) {
            sendError(sender, plugin.getConfigManager().getWorldExistsMessage(worldName));
            return true;
        }
        
        // Check if world folder already exists
        File worldFolder = new File(Bukkit.getWorldContainer(), worldName);
        if (worldFolder.exists()) {
            sendError(sender, "A world folder with that name already exists! Use '/w import " + worldName + "' to import it.");
            return true;
        }
        
        // Parse creation options
        WorldCreationOptions options = parseCreationOptions(sender, args);
        if (options == null) {
            return true; // Error already sent
        }
        
        // Display creation summary
        sendInfo(sender, "=== World Creation Summary ===");
        sendInfo(sender, "Name: " + ColorUtils.colorize("&e" + worldName));
        sendInfo(sender, "Environment: " + ColorUtils.colorize("&b" + options.environment.name()));
        sendInfo(sender, "Generator: " + ColorUtils.colorize("&d" + (options.generator != null ? options.generator : "Default")));
        sendInfo(sender, "Generate Structures: " + ColorUtils.colorize(options.generateStructures ? "&aYes" : "&cNo"));
        sendInfo(sender, "Generate Decorations: " + ColorUtils.colorize(options.generateDecorations ? "&aYes" : "&cNo"));
        sendInfo(sender, "Keep Spawn Loaded: " + ColorUtils.colorize(options.keepSpawnLoaded ? "&aYes" : "&cNo"));
        if (options.seed != 0) {
            sendInfo(sender, "Seed: " + ColorUtils.colorize("&6" + options.seed));
        }
        sendInfo(sender, "");
        
        sendInfo(sender, "Creating world '" + worldName + "'...");
        
        // Do preparation work asynchronously for better performance
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            plugin.getWorldManager().createWorldAdvancedAsync(worldName, options, (result) -> {
                // Handle result on main thread
                Bukkit.getScheduler().runTask(plugin, () -> {
                    if (result) {
                        sendSuccess(sender, "World '" + worldName + "' created successfully!");
                        sendInfo(sender, "Use '/w info " + worldName + "' to view detailed information.");
                        
                        // Auto-teleport player if they're online and have permission
                        if (sender instanceof Player && hasPermission(sender, "lilworlds.world.teleport")) {
                            Player player = (Player) sender;
                            World world = Bukkit.getWorld(worldName);
                            if (world != null) {
                                sendInfo(sender, "Teleporting you to the new world...");
                                player.teleport(world.getSpawnLocation());
                            }
                        }
                    } else {
                        sendError(sender, "Failed to create world '" + worldName + "'!");
                        sendError(sender, "Check console for detailed error information.");
                    }
                });
            });
        });
        
        return true;
    }
    
    private void sendCreateHelp(CommandSender sender) {
        sendInfo(sender, "=== World Creation Help ===");
        sendInfo(sender, ColorUtils.colorize("&e/w create <name> [environment] [options]"));
        sendInfo(sender, "");
        sendInfo(sender, ColorUtils.colorize("&bEnvironments:"));
        sendInfo(sender, ColorUtils.colorize("  &7• &fNORMAL &7- Standard overworld"));
        sendInfo(sender, ColorUtils.colorize("  &7• &fNETHER &7- Nether dimension"));
        sendInfo(sender, ColorUtils.colorize("  &7• &fTHE_END &7- End dimension"));
        sendInfo(sender, "");
        sendInfo(sender, ColorUtils.colorize("&bOptions:"));
        sendInfo(sender, ColorUtils.colorize("  &7• &f-g <generator> &7- Use custom generator"));
        sendInfo(sender, ColorUtils.colorize("  &7• &f--no-structures &7- Disable structure generation"));
        sendInfo(sender, ColorUtils.colorize("  &7• &f--no-decorations &7- Disable decoration generation"));
        sendInfo(sender, ColorUtils.colorize("  &7• &f--no-keep-spawn &7- Don't keep spawn chunks loaded"));
        sendInfo(sender, ColorUtils.colorize("  &7• &f--seed <seed> &7- Use specific seed"));
        sendInfo(sender, "");
        sendInfo(sender, ColorUtils.colorize("&bExamples:"));
        sendInfo(sender, ColorUtils.colorize("  &7• &f/w create myworld"));
        sendInfo(sender, ColorUtils.colorize("  &7• &f/w create nether_world NETHER"));
        sendInfo(sender, ColorUtils.colorize("  &7• &f/w create flat_world -g superflat"));
        sendInfo(sender, ColorUtils.colorize("  &7• &f/w create custom NORMAL -g example --seed 12345"));
    }
    

    
    private WorldCreationOptions parseCreationOptions(CommandSender sender, String[] args) {
        WorldCreationOptions options = new WorldCreationOptions();
        
        for (int i = 2; i < args.length; i++) {
            String arg = args[i];
            
            if (arg.equalsIgnoreCase("NORMAL") || arg.equalsIgnoreCase("NETHER") || arg.equalsIgnoreCase("THE_END")) {
                try {
                    options.environment = World.Environment.valueOf(arg.toUpperCase());
                } catch (IllegalArgumentException e) {
                    sendError(sender, "Invalid environment: " + arg);
                    return null;
                }
            } else if (arg.equalsIgnoreCase("-g") && i + 1 < args.length) {
                options.generator = args[i + 1];
                i++; // Skip next argument
                
                // Validate generator name for security
                if (!SecurityUtils.isValidGeneratorName(options.generator)) {
                    sendError(sender, "Invalid generator name for security reasons!");
                    SecurityUtils.logSecurityEvent(sender, "INVALID_GENERATOR", "Attempted to use invalid generator: " + options.generator);
                    return null;
                }
                
                // Validate generator exists
                if (!plugin.getGeneratorManager().hasGenerator(options.generator)) {
                    sendWarn(sender, "Generator '" + options.generator + "' not found. Using default generator.");
                    options.generator = null;
                }
            } else if (arg.equalsIgnoreCase("--no-structures")) {
                options.generateStructures = false;
            } else if (arg.equalsIgnoreCase("--no-decorations")) {
                options.generateDecorations = false;
            } else if (arg.equalsIgnoreCase("--no-keep-spawn")) {
                options.keepSpawnLoaded = false;
            } else if (arg.equalsIgnoreCase("--seed") && i + 1 < args.length) {
                String seedStr = args[i + 1];
                i++; // Skip next argument
                
                // Validate seed for security
                if (!SecurityUtils.isValidSeed(seedStr)) {
                    sendError(sender, "Invalid seed format: " + seedStr);
                    SecurityUtils.logSecurityEvent(sender, "INVALID_SEED", "Attempted to use invalid seed: " + seedStr);
                    return null;
                }
                
                try {
                    options.seed = Long.parseLong(seedStr);
                } catch (NumberFormatException e) {
                    sendError(sender, "Invalid seed: " + seedStr);
                    return null;
                }
            } else {
                sendError(sender, "Unknown option: " + arg);
                sendCreateHelp(sender);
                return null;
            }
        }
        
        return options;
    }
    
    private static class WorldCreationOptions {
        World.Environment environment = World.Environment.NORMAL;
        String generator = null;
        boolean generateStructures = true;
        boolean generateDecorations = true;
        boolean keepSpawnLoaded = true;
        long seed = 0;
    }
    
    private boolean handleClone(CommandSender sender, String[] args) {
        if (!hasPermission(sender, "lilworlds.world.clone")) {
            sendError(sender, plugin.getConfigManager().getMessage("clone-permission-denied"));
            return true;
        }
        
        if (!validateArgs(sender, args, 3, 3)) {
            return true;
        }
        
        String sourceWorld = args[1];
        String targetWorld = args[2];
        
        sendInfo(sender, "Cloning world '" + sourceWorld + "' to '" + targetWorld + "'...");
        sendWarning(sender, "This operation may take some time depending on world size.");
        
        boolean success = plugin.getWorldManager().cloneWorld(sourceWorld, targetWorld);
        
        if (success) {
            sendSuccess(sender, "World '" + sourceWorld + "' cloned to '" + targetWorld + "' successfully!");
        } else {
            sendError(sender, "Failed to clone world '" + sourceWorld + "' to '" + targetWorld + "'!");
        }
        
        return true;
    }
    
    private boolean handleLoad(CommandSender sender, String[] args) {
        if (!hasPermission(sender, "lilworlds.world.load")) {
            sendError(sender, plugin.getConfigManager().getMessage("load-permission-denied"));
            return true;
        }
        
        if (args.length == 1) {
            // Load all worlds
            sendInfo(sender, "Loading all worlds from configuration...");
            plugin.getWorldManager().loadWorldsFromConfig();
            sendSuccess(sender, "All worlds loaded from configuration!");
        } else {
            // Load specific world
            String worldName = args[1];
            sendInfo(sender, "Loading world '" + worldName + "'...");
            
            boolean success = plugin.getWorldManager().loadWorld(worldName);
            
            if (success) {
                sendSuccess(sender, "World '" + worldName + "' loaded successfully!");
            } else {
                sendError(sender, "Failed to load world '" + worldName + "'!");
            }
        }
        
        return true;
    }
    
    private boolean handleUnload(CommandSender sender, String[] args) {
        if (!hasPermission(sender, "lilworlds.world.unload")) {
            sendError(sender, plugin.getConfigManager().getMessage("unload-permission-denied"));
            return true;
        }
        
        if (args.length == 1) {
            // Unload all worlds (with confirmation but no timeout)
            sendWarning(sender, "This will unload ALL worlds except the main world!");
            sendWarning(sender, "All players will be moved to the main world.");
            sendMessage(sender, "&cTo confirm, type: &f/w unload confirm");
            sendMessage(sender, "&7This confirmation does not expire.");
            return true;
        }
        
        String worldName = args[1];
        
        if (worldName.equalsIgnoreCase("confirm")) {
            // Unload all worlds
            sendInfo(sender, "Unloading all worlds...");
            int unloaded = 0;
            
            for (World world : Bukkit.getWorlds()) {
                if (!world.equals(Bukkit.getWorlds().get(0))) { // Don't unload main world
                    if (plugin.getWorldManager().unloadWorld(world.getName(), true)) {
                        unloaded++;
                    }
                }
            }
            
            sendSuccess(sender, "Unloaded " + unloaded + " worlds!");
            return true;
        }
        
        // Unload specific world (no confirmation needed for single world)
        sendInfo(sender, "Unloading world '" + worldName + "'...");
        
        boolean success = plugin.getWorldManager().unloadWorld(worldName, true);
        
        if (success) {
            sendSuccess(sender, "World '" + worldName + "' unloaded successfully!");
        } else {
            sendError(sender, "Failed to unload world '" + worldName + "'!");
        }
        
        return true;
    }
    
    private boolean handleDelete(CommandSender sender, String[] args) {
        if (!hasPermission(sender, "lilworlds.world.delete")) {
            sendError(sender, plugin.getConfigManager().getMessage("delete-permission-denied"));
            return true;
        }
        
        // Security check - rate limiting (30 seconds for players, console exempt)
        if (!SecurityUtils.checkRateLimit(sender, "delete", 30)) {
            sendError(sender, plugin.getConfigManager().getMessage("delete-rate-limited"));
            return true;
        }
        
        if (!validateArgs(sender, args, 2, 3)) {
            sendError(sender, plugin.getConfigManager().getMessage("delete-usage"));
            return true;
        }
        
        String worldName = args[1];
        
        // Validate world name
        if (!SecurityUtils.isValidWorldName(worldName)) {
            sendError(sender, plugin.getConfigManager().getMessage("delete-invalid-world-name"));
            return true;
        }
        
        // Check if it's a confirmation
        if (args.length == 3 && args[2].equalsIgnoreCase("confirm")) {
            return executeWorldDeletion(sender, worldName);
        }
        
        // Show confirmation message (no timeout)
        sendWarning(sender, "⚠ DANGER: This will PERMANENTLY DELETE the world '" + worldName + "'!");
        sendWarning(sender, "⚠ This action CANNOT be undone!");
        sendWarning(sender, "⚠ All world data, builds, and progress will be lost forever!");
        sendWarning(sender, "⚠ All world configuration will be purged from config files!");
        sendMessage(sender, "");
        sendMessage(sender, "&7World information:");
        
        // Show world info if it exists
        World world = Bukkit.getWorld(worldName);
        if (world != null) {
            sendMessage(sender, "&7- Status: &aLoaded");
            sendMessage(sender, "&7- Environment: &e" + world.getEnvironment());
            sendMessage(sender, "&7- Players: &e" + world.getPlayers().size());
            sendMessage(sender, "&7- Chunks: &e" + world.getLoadedChunks().length);
        } else {
            File worldFolder = new File(Bukkit.getWorldContainer(), worldName);
            if (worldFolder.exists()) {
                sendMessage(sender, "&7- Status: &cUnloaded");
                sendMessage(sender, "&7- Size: &e" + formatFileSize(getFolderSize(worldFolder)));
            } else {
                sendError(sender, "World '" + worldName + "' does not exist!");
                return true;
            }
        }
        
        sendMessage(sender, "");
        sendMessage(sender, "&cTo confirm deletion, type: &f/w delete " + worldName + " confirm");
        sendMessage(sender, "&7This confirmation does not expire.");
        
        return true;
    }
    
    private boolean executeWorldDeletion(CommandSender sender, String worldName) {
        // Proceed with deletion (no timeout checks needed)
        
        sendInfo(sender, "Deleting world '" + worldName + "'...");
        
        // Security logging
        SecurityUtils.logSecurityEvent(sender, "WORLD_DELETE_ATTEMPT", 
            "Attempting to delete world: " + worldName);
        
        try {
            boolean success = plugin.getWorldManager().deleteWorld(worldName);
            
            if (success) {
                sendSuccess(sender, "World '" + worldName + "' has been permanently deleted!");
                sendInfo(sender, "All world configuration has been purged from config files.");
                SecurityUtils.logSecurityEvent(sender, "WORLD_DELETE_SUCCESS", 
                    "Successfully deleted world: " + worldName);
            } else {
                sendError(sender, "Failed to delete world '" + worldName + "'! Check console for details.");
                SecurityUtils.logSecurityEvent(sender, "WORLD_DELETE_FAILURE", 
                    "Failed to delete world: " + worldName);
            }
        } catch (Exception e) {
            sendError(sender, "An error occurred while deleting the world!");
            SecurityUtils.logSecurityEvent(sender, "WORLD_DELETE_ERROR", 
                "Error deleting world " + worldName + ": " + e.getMessage());
        }
        
        return true;
    }
    
    private boolean handleList(CommandSender sender, String[] args) {
        if (!hasPermission(sender, "lilworlds.world.list")) {
            sendError(sender, plugin.getConfigManager().getMessage("list-permission-denied"));
            return true;
        }
        
        sendMessage(sender, "");
        sendMessage(sender, plugin.getConfigManager().getMessage("list-header"));
        sendMessage(sender, plugin.getConfigManager().getMessage("list-title"));
        sendMessage(sender, "");
        
        // Get all worlds
        List<World> loadedWorlds = Bukkit.getWorlds();
        Map<String, WorldManager.WorldInfo> managedWorlds = plugin.getWorldManager().getManagedWorlds();
        
        if (loadedWorlds.isEmpty()) {
            sendMessage(sender, plugin.getConfigManager().getMessage("list-no-worlds"));
            sendMessage(sender, plugin.getConfigManager().getMessage("list-footer"));
            return true;
        }
        
        sendMessage(sender, plugin.getConfigManager().getMessage("list-loaded-worlds", "{count}", String.valueOf(loadedWorlds.size())));
        
        for (World world : loadedWorlds) {
            String worldName = world.getName();
            WorldManager.WorldInfo worldInfo = managedWorlds.get(worldName);
            
            // Basic world info
            StringBuilder info = new StringBuilder();
            info.append("&8  • &b").append(worldName);
            
            // Environment with colors
            String envColor = getEnvironmentColor(world.getEnvironment());
            info.append(" &7(").append(envColor).append(world.getEnvironment().name()).append("&7)");
            
            // Player count
            int playerCount = world.getPlayers().size();
            if (playerCount > 0) {
                String playerText = playerCount == 1 ? 
                    plugin.getConfigManager().getMessage("list-world-players") : 
                    plugin.getConfigManager().getMessage("list-world-players-plural");
                info.append(" &a").append(playerCount).append(" ").append(playerText);
            }
            
            // Generator info
            if (worldInfo != null && worldInfo.getGenerator() != null && !worldInfo.getGenerator().isEmpty()) {
                info.append(" &8[&6").append(worldInfo.getGenerator()).append("&8]");
            }
            
            // Main world indicator
            if (world.equals(Bukkit.getWorlds().get(0))) {
                info.append(" ").append(plugin.getConfigManager().getMessage("list-world-main"));
            }
            
            sendMessage(sender, info.toString());
        }
        
        // Show unloaded managed worlds
        Set<String> unloadedWorlds = new HashSet<>();
        for (String managedWorldName : managedWorlds.keySet()) {
            if (Bukkit.getWorld(managedWorldName) == null) {
                // Check if world folder exists
                File worldFolder = new File(Bukkit.getWorldContainer(), managedWorldName);
                if (worldFolder.exists()) {
                    unloadedWorlds.add(managedWorldName);
                }
            }
        }
        
        if (!unloadedWorlds.isEmpty()) {
            sendMessage(sender, "");
            sendMessage(sender, plugin.getConfigManager().getMessage("list-unloaded-worlds", "{count}", String.valueOf(unloadedWorlds.size())));
            
            for (String worldName : unloadedWorlds) {
                WorldManager.WorldInfo worldInfo = managedWorlds.get(worldName);
                StringBuilder info = new StringBuilder();
                info.append("&8  • &7").append(worldName);
                
                // Environment
                if (worldInfo != null) {
                    String envColor = getEnvironmentColor(worldInfo.getEnvironment());
                    info.append(" &7(").append(envColor).append(worldInfo.getEnvironment().name()).append("&7)");
                    
                    // Generator info
                    if (worldInfo.getGenerator() != null && !worldInfo.getGenerator().isEmpty()) {
                        info.append(" &8[&6").append(worldInfo.getGenerator()).append("&8]");
                    }
                }
                
                sendMessage(sender, info.toString());
            }
        }
        
        // Show summary
        sendMessage(sender, "");
        sendMessage(sender, plugin.getConfigManager().getMessage("list-summary", 
            "{loaded}", String.valueOf(loadedWorlds.size()),
            "{unloaded}", String.valueOf(unloadedWorlds.size())));
        sendMessage(sender, plugin.getConfigManager().getMessage("list-footer"));
        sendMessage(sender, "");
        
        return true;
    }
    
    private boolean handleImport(CommandSender sender, String[] args) {
        if (!hasPermission(sender, "lilworlds.world.import")) {
            sendError(sender, plugin.getConfigManager().getMessage("import-permission-denied"));
            return true;
        }
        
        if (!validateArgs(sender, args, 2, -1)) {
            return true;
        }
        
        String worldName = args[1];
        World.Environment environment = World.Environment.NORMAL;
        String generator = null;
        
        // Parse additional arguments
        for (int i = 2; i < args.length; i++) {
            String arg = args[i];
            
            if (arg.equalsIgnoreCase("NORMAL") || arg.equalsIgnoreCase("NETHER") || arg.equalsIgnoreCase("THE_END")) {
                try {
                    environment = World.Environment.valueOf(arg.toUpperCase());
                } catch (IllegalArgumentException e) {
                    sendError(sender, "Invalid environment: " + arg);
                    return true;
                }
            } else if (arg.equalsIgnoreCase("-g") && i + 1 < args.length) {
                generator = args[i + 1];
                i++; // Skip next argument
            }
        }
        
        sendInfo(sender, "Importing world '" + worldName + "'...");
        
        boolean success = plugin.getWorldManager().importWorld(worldName, environment, generator);
        
        if (success) {
            sendSuccess(sender, "World '" + worldName + "' imported successfully!");
        } else {
            sendError(sender, "Failed to import world '" + worldName + "'!");
        }
        
        return true;
    }

    
    private String formatWorldListEntry(World world, boolean loaded) {
        StringBuilder info = new StringBuilder();
        
        if (loaded) {
            info.append(ColorUtils.colorize("&a● "));
        } else {
            info.append(ColorUtils.colorize("&c● "));
        }
        
        info.append(ColorUtils.colorize("&f")).append(world.getName());
        
        if (loaded) {
            info.append(ColorUtils.colorize(" &7["));
            info.append(ColorUtils.colorize("&b")).append(world.getEnvironment().name());
            info.append(ColorUtils.colorize("&7] "));
            info.append(ColorUtils.colorize("&7Players: &e")).append(world.getPlayers().size());
            info.append(ColorUtils.colorize(" &7Chunks: &e")).append(world.getLoadedChunks().length);
            
            // Add weather info
            if (world.hasStorm()) {
                if (world.isThundering()) {
                    info.append(ColorUtils.colorize(" &7Weather: &c⚡"));
                } else {
                    info.append(ColorUtils.colorize(" &7Weather: &9🌧"));
                }
            } else {
                info.append(ColorUtils.colorize(" &7Weather: &e☀"));
            }
            
            // Add time info
            long time = world.getTime();
            String timeOfDay;
            if (time >= 0 && time < 6000) {
                timeOfDay = "Morning";
            } else if (time >= 6000 && time < 12000) {
                timeOfDay = "Day";
            } else if (time >= 12000 && time < 18000) {
                timeOfDay = "Evening";
            } else {
                timeOfDay = "Night";
            }
            info.append(ColorUtils.colorize(" &7Time: &f")).append(timeOfDay);
        }
        
        return info.toString();
    }
    
    private String formatWorldListEntry(String worldName, boolean loaded) {
        StringBuilder info = new StringBuilder();
        
        info.append(ColorUtils.colorize("&c● "));
        info.append(ColorUtils.colorize("&f")).append(worldName);
        info.append(ColorUtils.colorize(" &7[UNLOADED]"));
        
        return info.toString();
    }
    
    private boolean handleSetSpawn(CommandSender sender, String[] args) {
        if (!hasPermission(sender, "lilworlds.world.setspawn")) {
            sendError(sender, plugin.getConfigManager().getMessage("setspawn-permission-denied"));
            return true;
        }
        
        if (!isPlayer(sender)) {
            sendError(sender, plugin.getConfigManager().getMessage("setspawn-player-only"));
            return true;
        }
        
        Player player = getPlayer(sender);
        Location location = player.getLocation();
        String worldName = location.getWorld().getName();
        
        boolean success = plugin.getWorldManager().setWorldSpawn(worldName, location);
        
        if (success) {
            sendSuccess(sender, "Spawn location set for world '" + worldName + "'!");
            sendInfo(sender, "Location: " + formatLocation(location));
        } else {
            sendError(sender, "Failed to set spawn location for world '" + worldName + "'!");
        }
        
        return true;
    }
    
    private boolean handleSetUniversalSpawn(CommandSender sender, String[] args) {
        if (!hasPermission(sender, "lilworlds.world.setuniversalspawn")) {
            sendError(sender, plugin.getConfigManager().getMessage("setuniversalspawn-permission-denied"));
            return true;
        }
        
        if (!isPlayer(sender)) {
            sendError(sender, plugin.getConfigManager().getMessage("setuniversalspawn-player-only"));
            return true;
        }
        
        Player player = getPlayer(sender);
        Location location = player.getLocation();
        
        plugin.getWorldManager().setUniversalSpawn(location);
        
        sendSuccess(sender, "Universal spawn location set!");
        sendInfo(sender, "World: " + location.getWorld().getName());
        sendInfo(sender, "Location: " + formatLocation(location));
        
        return true;
    }
    
    private boolean handleConfig(CommandSender sender, String[] args) {
        if (!hasPermission(sender, "lilworlds.world.config")) {
            sendError(sender, plugin.getConfigManager().getMessage("config-permission-denied"));
            return true;
        }
        
        if (!validateArgs(sender, args, 3, 4)) {
            return true;
        }
        
        String action = args[1].toLowerCase();
        String key = args[2];
        
        switch (action) {
            case "disable":
                plugin.getConfigManager().setMainConfigValue(key, false);
                sendSuccess(sender, "Disabled configuration option: " + key);
                break;
            case "enable":
                plugin.getConfigManager().setMainConfigValue(key, true);
                sendSuccess(sender, "Enabled configuration option: " + key);
                break;
            case "set":
                if (args.length < 4) {
                    sendError(sender, "You must specify a value!");
                    return true;
                }
                String value = args[3];
                plugin.getConfigManager().setMainConfigValue(key, value);
                sendSuccess(sender, "Set configuration option '" + key + "' to: " + value);
                break;
            default:
                sendError(sender, "Invalid action: " + action);
                sendMessage(sender, "&7Valid actions: &fenable, disable, set");
                return true;
        }
        
        return true;
    }
    
    private void sendHelp(CommandSender sender) {
        sendMessage(sender, "");
        sendMessage(sender, plugin.getConfigManager().getMessage("help-header"));
        sendMessage(sender, plugin.getConfigManager().getMessage("help-title"));
        sendMessage(sender, "");
        sendMessage(sender, plugin.getConfigManager().getMessage("cmd-world-info"));
        sendMessage(sender, plugin.getConfigManager().getMessage("cmd-world-create"));
        sendMessage(sender, plugin.getConfigManager().getMessage("cmd-world-clone"));
        sendMessage(sender, plugin.getConfigManager().getMessage("cmd-world-load"));
        sendMessage(sender, plugin.getConfigManager().getMessage("cmd-world-unload"));
        sendMessage(sender, plugin.getConfigManager().getMessage("cmd-world-import"));
        sendMessage(sender, plugin.getConfigManager().getMessage("cmd-world-remove"));
        sendMessage(sender, plugin.getConfigManager().getMessage("cmd-world-list"));
        sendMessage(sender, plugin.getConfigManager().getMessage("cmd-world-setspawn"));
        sendMessage(sender, plugin.getConfigManager().getMessage("cmd-world-setuniversalspawn"));
        sendMessage(sender, plugin.getConfigManager().getMessage("cmd-world-config"));
        sendMessage(sender, "");
        sendMessage(sender, plugin.getConfigManager().getMessage("aliases-info", "{aliases}", "/w"));
        sendMessage(sender, plugin.getConfigManager().getMessage("help-footer"));
        sendMessage(sender, "");
    }
    
    @Override
    protected String getUsage() {
        return "/world <subcommand> [args...]";
    }
    
    @Override
    protected String getDescription() {
        return "Main world management command";
    }
    
    @Override
    protected List<String> getSubcommands() {
        return Arrays.asList("info", "create", "clone", "load", "unload", "remove", "delete", "import", "list", "setspawn", "setuniversalspawn", "config");
    }
    
    @Override
    protected List<String> getTabCompletions(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            completions.addAll(getSubcommands());
        } else if (args.length == 2) {
            String subcommand = args[0].toLowerCase();
            
            switch (subcommand) {
                case "info":
                case "load":
                case "unload":
                case "remove":
                case "delete":
                case "import":
                    completions.addAll(Bukkit.getWorlds().stream()
                        .map(World::getName)
                        .collect(Collectors.toList()));
                    break;
                case "clone":
                    completions.addAll(Bukkit.getWorlds().stream()
                        .map(World::getName)
                        .collect(Collectors.toList()));
                    break;
                case "config":
                    completions.addAll(Arrays.asList("enable", "disable", "set"));
                    break;
            }
        } else if (args.length == 3) {
            String subcommand = args[0].toLowerCase();
            
            if (subcommand.equals("create") || subcommand.equals("import")) {
                completions.addAll(Arrays.asList("NORMAL", "NETHER", "THE_END"));
            } else if (subcommand.equals("remove") || subcommand.equals("delete")) {
                completions.add("confirm");
            } else if (subcommand.equals("config")) {
                completions.addAll(Arrays.asList("debug", "auto-load-worlds", "auto-save-worlds", "metrics"));
            }
        } else if (args.length == 4) {
            String subcommand = args[0].toLowerCase();
            
            if (subcommand.equals("create") || subcommand.equals("import")) {
                if (args[2].equalsIgnoreCase("-g")) {
                    completions.addAll(plugin.getGeneratorManager().getCustomGeneratorNames());
                } else {
                    completions.add("-g");
                }
            }
        }
        
        return filterCompletions(completions, args[args.length - 1]);
    }
    
    // Helper methods
    
    private String getEnvironmentColor(World.Environment environment) {
        switch (environment) {
            case NORMAL:
                return "&a";
            case NETHER:
                return "&c";
            case THE_END:
                return "&d";
            default:
                return "&f";
        }
    }
    
    private String getWeatherString(World world) {
        if (world.hasStorm()) {
            return world.isThundering() ? 
                plugin.getConfigManager().getMessage("thunder-weather") : 
                plugin.getConfigManager().getMessage("rain-weather");
        } else {
            return plugin.getConfigManager().getMessage("clear-weather");
        }
    }
    
    private String formatLocation(Location location) {
        return String.format("%.1f, %.1f, %.1f", location.getX(), location.getY(), location.getZ());
    }
    
    // Delete confirmation management methods
    
    private void storeDeleteConfirmation(CommandSender sender, String worldName) {
        String senderKey = getSenderKey(sender);
        deleteConfirmations.computeIfAbsent(senderKey, k -> new HashMap<>())
                          .put(worldName, System.currentTimeMillis());
        
        // Schedule cleanup after timeout
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            removeDeleteConfirmation(sender, worldName);
        }, CONFIRMATION_TIMEOUT / 50); // Convert to ticks
    }
    
    private boolean isDeleteConfirmationValid(CommandSender sender, String worldName) {
        String senderKey = getSenderKey(sender);
        Map<String, Long> senderConfirmations = deleteConfirmations.get(senderKey);
        
        if (senderConfirmations == null) {
            return false;
        }
        
        Long timestamp = senderConfirmations.get(worldName);
        if (timestamp == null) {
            return false;
        }
        
        return (System.currentTimeMillis() - timestamp) <= CONFIRMATION_TIMEOUT;
    }
    
    private void removeDeleteConfirmation(CommandSender sender, String worldName) {
        String senderKey = getSenderKey(sender);
        Map<String, Long> senderConfirmations = deleteConfirmations.get(senderKey);
        
        if (senderConfirmations != null) {
            senderConfirmations.remove(worldName);
            if (senderConfirmations.isEmpty()) {
                deleteConfirmations.remove(senderKey);
            }
        }
    }
    
    private String getSenderKey(CommandSender sender) {
        if (sender instanceof Player) {
            return "player:" + ((Player) sender).getUniqueId().toString();
        } else {
            return "console:" + sender.getName();
        }
    }
    
    // File utility methods
    
    private long getFolderSize(File folder) {
        long size = 0;
        if (folder.isDirectory()) {
            File[] files = folder.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        size += getFolderSize(file);
                    } else {
                        size += file.length();
                    }
                }
            }
        } else {
            size = folder.length();
        }
        return size;
    }
    
    private String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp - 1) + "";
        return String.format("%.1f %sB", bytes / Math.pow(1024, exp), pre);
    }
}