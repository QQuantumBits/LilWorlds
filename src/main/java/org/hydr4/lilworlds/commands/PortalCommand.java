package org.hydr4.lilworlds.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.hydr4.lilworlds.LilWorlds;
import org.hydr4.lilworlds.portals.Portal;
import org.hydr4.lilworlds.portals.Portal.PortalType;
import org.hydr4.lilworlds.portals.PortalManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class PortalCommand implements CommandExecutor, TabCompleter {
    
    private final LilWorlds plugin;
    private final PortalManager portalManager;
    private final boolean worldEditEnabled;
    
    public PortalCommand(LilWorlds plugin) {
        this.plugin = plugin;
        this.portalManager = plugin.getPortalManager();
        this.worldEditEnabled = Bukkit.getPluginManager().isPluginEnabled("WorldEdit");
    }
    
    private void sendMessage(CommandSender sender, String message) {
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
    }
    
    private void sendError(CommandSender sender, String message) {
        sendMessage(sender, message);
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "create":
                return handleCreate(sender, args);
            case "delete":
            case "remove":
                return handleDelete(sender, args);
            case "list":
                return handleList(sender, args);
            case "info":
                return handleInfo(sender, args);
            case "tp":
            case "teleport":
                return handleTeleport(sender, args);
            case "reload":
                return handleReload(sender, args);
            default:
                sendHelp(sender);
                return true;
        }
    }
    
    private boolean handleCreate(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sendError(sender, plugin.getConfigManager().getMessage("portal-player-only", "{action}", "create"));
            return true;
        }
        
        Player player = (Player) sender;
        
        if (!player.hasPermission("lilworlds.portal.create")) {
            sendError(player, plugin.getConfigManager().getMessage("portal-permission-denied", "{action}", "create"));
            return true;
        }
        
        if (args.length < 3) {
            sendError(player, plugin.getConfigManager().getMessage("portal-usage-create"));
            if (worldEditEnabled) {
                sendMessage(player, plugin.getConfigManager().getMessage("portal-usage-create-worldedit"));
            }
            return true;
        }
        
        // Check for WorldEdit selection first
        if (worldEditEnabled && args.length == 3) {
            return handleCreateWithWorldEdit(player, args);
        }
        
        // Manual coordinate creation
        if (args.length < 9) {
            sendError(player, plugin.getConfigManager().getMessage("portal-usage-manual"));
            if (worldEditEnabled) {
                sendMessage(player, plugin.getConfigManager().getMessage("portal-usage-create-worldedit"));
            }
            return true;
        }
        
        return handleCreateManual(player, args);
    }
    
    private boolean handleCreateWithWorldEdit(Player player, String[] args) {
        String portalName = args[1];
        String destinationWorld = args[2];
        
        // Check if portal already exists
        if (portalManager.getPortal(portalName) != null) {
            sendError(player, plugin.getConfigManager().getMessage("portal-already-exists", "{portal}", portalName));
            return true;
        }
        
        // Check if destination world exists
        World destWorld = Bukkit.getWorld(destinationWorld);
        if (destWorld == null) {
            sendError(player, plugin.getConfigManager().getMessage("portal-destination-world-not-found", "{world}", destinationWorld));
            return true;
        }
        
        try {
            // Use reflection to get WorldEdit selection
            Class<?> worldEditClass = Class.forName("com.sk89q.worldedit.WorldEdit");
            Class<?> bukkitAdapterClass = Class.forName("com.sk89q.worldedit.bukkit.BukkitAdapter");
            Class<?> localSessionClass = Class.forName("com.sk89q.worldedit.LocalSession");
            Class<?> regionClass = Class.forName("com.sk89q.worldedit.regions.Region");
            Class<?> blockVector3Class = Class.forName("com.sk89q.worldedit.math.BlockVector3");
            
            // Get WorldEdit instance
            Object worldEdit = worldEditClass.getMethod("getInstance").invoke(null);
            Object sessionManager = worldEditClass.getMethod("getSessionManager").invoke(worldEdit);
            
            // Adapt player
            Object bukkitPlayer = bukkitAdapterClass.getMethod("adapt", Player.class).invoke(null, player);
            
            // Get session and selection
            Object session = sessionManager.getClass().getMethod("get", bukkitPlayer.getClass()).invoke(sessionManager, bukkitPlayer);
            Object bukkitWorld = bukkitAdapterClass.getMethod("adapt", org.bukkit.World.class).invoke(null, player.getWorld());
            Object selection = localSessionClass.getMethod("getSelection", bukkitWorld.getClass()).invoke(session, bukkitWorld);
            
            if (selection == null) {
                sendError(player, plugin.getConfigManager().getMessage("worldedit-no-selection"));
                return true;
            }
            
            // Get min and max points
            Object min = regionClass.getMethod("getMinimumPoint").invoke(selection);
            Object max = regionClass.getMethod("getMaximumPoint").invoke(selection);
            
            // Get coordinates
            int minX = (Integer) blockVector3Class.getMethod("getX").invoke(min);
            int minY = (Integer) blockVector3Class.getMethod("getY").invoke(min);
            int minZ = (Integer) blockVector3Class.getMethod("getZ").invoke(min);
            int maxX = (Integer) blockVector3Class.getMethod("getX").invoke(max);
            int maxY = (Integer) blockVector3Class.getMethod("getY").invoke(max);
            int maxZ = (Integer) blockVector3Class.getMethod("getZ").invoke(max);
            
            Location pos1 = new Location(player.getWorld(), minX, minY, minZ);
            Location pos2 = new Location(player.getWorld(), maxX, maxY, maxZ);
            Location destination = destWorld.getSpawnLocation();
            
            // Create the portal
            boolean success = portalManager.createPortal(
                portalName, 
                pos1, 
                pos2, 
                destinationWorld, 
                destination, 
                Material.OBSIDIAN, 
                PortalType.CUSTOM
            );
            
            if (success) {
                sendMessage(player, plugin.getConfigManager().getMessage("portal-created-success-worldedit", "{portal}", portalName));
                sendMessage(player, plugin.getConfigManager().getMessage("portal-created-destination", 
                    "{world}", destinationWorld, 
                    "{location}", "spawn"));
                
                // Save portals
                portalManager.savePortals();
                plugin.getLogger().info("Portal '" + portalName + "' created by " + player.getName() + " using WorldEdit selection");
            } else {
                sendError(player, plugin.getConfigManager().getMessage("portal-creation-failed"));
            }
            
            return true;
            
        } catch (ClassNotFoundException e) {
            sendError(player, plugin.getConfigManager().getMessage("worldedit-not-found"));
            return true;
        } catch (Exception e) {
            if (e.getClass().getSimpleName().equals("IncompleteRegionException")) {
                sendError(player, plugin.getConfigManager().getMessage("worldedit-incomplete-selection"));
            } else {
                sendError(player, plugin.getConfigManager().getMessage("worldedit-error", "{error}", e.getMessage()));
            }
            return true;
        }
    }
    
    private boolean handleCreateManual(Player player, String[] args) {
        String portalName = args[1];
        String destinationWorld = args[2];
        
        // Check if portal already exists
        if (portalManager.getPortal(portalName) != null) {
            sendError(player, plugin.getConfigManager().getMessage("portal-already-exists", "{portal}", portalName));
            return true;
        }
        
        // Check if destination world exists
        World destWorld = Bukkit.getWorld(destinationWorld);
        if (destWorld == null) {
            sendError(player, plugin.getConfigManager().getMessage("portal-destination-world-not-found", "{world}", destinationWorld));
            return true;
        }
        
        try {
            double x1 = Double.parseDouble(args[3]);
            double y1 = Double.parseDouble(args[4]);
            double z1 = Double.parseDouble(args[5]);
            double x2 = Double.parseDouble(args[6]);
            double y2 = Double.parseDouble(args[7]);
            double z2 = Double.parseDouble(args[8]);
            
            Location pos1 = new Location(player.getWorld(), x1, y1, z1);
            Location pos2 = new Location(player.getWorld(), x2, y2, z2);
            
            // Destination coordinates
            Location destination;
            if (args.length >= 12) {
                double destX = Double.parseDouble(args[9]);
                double destY = Double.parseDouble(args[10]);
                double destZ = Double.parseDouble(args[11]);
                destination = new Location(destWorld, destX, destY, destZ);
            } else {
                destination = destWorld.getSpawnLocation();
            }
            
            // Create the portal
            boolean success = portalManager.createPortal(
                portalName, 
                pos1, 
                pos2, 
                destinationWorld, 
                destination, 
                Material.OBSIDIAN, 
                PortalType.CUSTOM
            );
            
            if (success) {
                sendMessage(player, plugin.getConfigManager().getMessage("portal-created-success", "{portal}", portalName));
                sendMessage(player, plugin.getConfigManager().getMessage("portal-created-destination", 
                    "{world}", destinationWorld, 
                    "{location}", formatLocation(destination)));
                
                // Save portals
                portalManager.savePortals();
                plugin.getLogger().info("Portal '" + portalName + "' created by " + player.getName());
            } else {
                sendError(player, plugin.getConfigManager().getMessage("portal-creation-failed"));
            }
            
        } catch (NumberFormatException e) {
            sendError(player, plugin.getConfigManager().getMessage("portal-invalid-coordinates"));
        }
        
        return true;
    }
    
    private boolean handleDelete(CommandSender sender, String[] args) {
        if (!sender.hasPermission("lilworlds.portal.delete")) {
            sendError(sender, plugin.getConfigManager().getMessage("portal-permission-denied", "{action}", "delete"));
            return true;
        }
        
        if (args.length < 2) {
            sendError(sender, plugin.getConfigManager().getMessage("portal-usage-delete"));
            return true;
        }
        
        String portalName = args[1];
        Portal portal = portalManager.getPortal(portalName);
        
        if (portal == null) {
            sendError(sender, plugin.getConfigManager().getMessage("portal-not-found", "{portal}", portalName));
            return true;
        }
        
        boolean success = portalManager.deletePortal(portalName);
        
        if (success) {
            sendMessage(sender, plugin.getConfigManager().getMessage("portal-deleted-success", "{portal}", portalName));
            portalManager.savePortals();
            plugin.getLogger().info("Portal '" + portalName + "' deleted by " + sender.getName());
        } else {
            sendError(sender, plugin.getConfigManager().getMessage("portal-deletion-failed"));
        }
        
        return true;
    }
    
    private boolean handleList(CommandSender sender, String[] args) {
        if (!sender.hasPermission("lilworlds.portal.list")) {
            sendError(sender, plugin.getConfigManager().getMessage("portal-permission-denied", "{action}", "list"));
            return true;
        }
        
        java.util.Collection<Portal> portals = portalManager.getAllPortals();
        if (portals.isEmpty()) {
            sendMessage(sender, plugin.getConfigManager().getMessage("portal-list-empty"));
            return true;
        }
        
        sendMessage(sender, plugin.getConfigManager().getMessage("portal-list-header", "{count}", String.valueOf(portals.size())));
        for (Portal portal : portals) {
            sendMessage(sender, plugin.getConfigManager().getMessage("portal-list-item", 
                "{name}", portal.getName(),
                "{destination}", portal.getDestinationWorld(),
                "{type}", portal.getType().toString()));
        }
        
        return true;
    }
    
    private boolean handleInfo(CommandSender sender, String[] args) {
        if (!sender.hasPermission("lilworlds.portal.info")) {
            sendError(sender, plugin.getConfigManager().getMessage("portal-permission-denied", "{action}", "view"));
            return true;
        }
        
        if (args.length < 2) {
            sendError(sender, plugin.getConfigManager().getMessage("portal-usage-info"));
            return true;
        }
        
        String portalName = args[1];
        Portal portal = portalManager.getPortal(portalName);
        
        if (portal == null) {
            sendError(sender, plugin.getConfigManager().getMessage("portal-not-found", "{portal}", portalName));
            return true;
        }
        
        sendMessage(sender, plugin.getConfigManager().getMessage("portal-info-header", "{name}", portal.getName()));
        sendMessage(sender, plugin.getConfigManager().getMessage("portal-info-type", "{type}", portal.getType().toString()));
        sendMessage(sender, plugin.getConfigManager().getMessage("portal-info-world", "{world}", portal.getLocation1().getWorld().getName()));
        sendMessage(sender, plugin.getConfigManager().getMessage("portal-info-position1", "{location}", formatLocation(portal.getLocation1())));
        sendMessage(sender, plugin.getConfigManager().getMessage("portal-info-position2", "{location}", formatLocation(portal.getLocation2())));
        sendMessage(sender, plugin.getConfigManager().getMessage("portal-info-destination-world", "{world}", portal.getDestinationWorld()));
        sendMessage(sender, plugin.getConfigManager().getMessage("portal-info-destination", "{location}", formatLocation(portal.getDestinationLocation())));
        sendMessage(sender, plugin.getConfigManager().getMessage("portal-info-frame-material", "{material}", portal.getFrameMaterial().toString()));
        
        return true;
    }
    
    private boolean handleTeleport(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sendError(sender, plugin.getConfigManager().getMessage("portal-player-only", "{action}", "teleport through"));
            return true;
        }
        
        Player player = (Player) sender;
        
        if (!player.hasPermission("lilworlds.portal.teleport")) {
            sendError(player, plugin.getConfigManager().getMessage("portal-permission-denied", "{action}", "teleport through"));
            return true;
        }
        
        if (args.length < 2) {
            sendError(player, plugin.getConfigManager().getMessage("portal-usage-teleport"));
            return true;
        }
        
        String portalName = args[1];
        Portal portal = portalManager.getPortal(portalName);
        
        if (portal == null) {
            sendError(player, plugin.getConfigManager().getMessage("portal-not-found", "{portal}", portalName));
            return true;
        }
        
        boolean success = portalManager.teleportPlayer(player, portal);
        
        if (success) {
            sendMessage(player, plugin.getConfigManager().getMessage("portal-teleport-success", "{portal}", portalName));
        } else {
            sendError(player, plugin.getConfigManager().getMessage("portal-teleport-failed"));
        }
        
        return true;
    }
    
    private boolean handleReload(CommandSender sender, String[] args) {
        if (!sender.hasPermission("lilworlds.portal.reload")) {
            sendError(sender, plugin.getConfigManager().getMessage("portal-permission-denied", "{action}", "reload"));
            return true;
        }
        
        portalManager.loadPortals();
        sendMessage(sender, plugin.getConfigManager().getMessage("portal-reload-success"));
        plugin.getLogger().info("Portal configuration reloaded by " + sender.getName());
        
        return true;
    }
    
    private void sendHelp(CommandSender sender) {
        sendMessage(sender, plugin.getConfigManager().getMessage("portal-help-header"));
        sendMessage(sender, plugin.getConfigManager().getMessage("portal-help-create"));
        sendMessage(sender, plugin.getConfigManager().getMessage("portal-help-delete"));
        sendMessage(sender, plugin.getConfigManager().getMessage("portal-help-list"));
        sendMessage(sender, plugin.getConfigManager().getMessage("portal-help-info"));
        sendMessage(sender, plugin.getConfigManager().getMessage("portal-help-teleport"));
        sendMessage(sender, plugin.getConfigManager().getMessage("portal-help-reload"));
        
        if (worldEditEnabled) {
            sendMessage(sender, plugin.getConfigManager().getMessage("worldedit-enabled"));
        } else {
            sendMessage(sender, plugin.getConfigManager().getMessage("worldedit-disabled"));
        }
    }
    
    private String formatLocation(Location loc) {
        return String.format("%.1f, %.1f, %.1f", loc.getX(), loc.getY(), loc.getZ());
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            // Main subcommands
            List<String> subCommands = Arrays.asList("create", "delete", "list", "info", "tp", "reload");
            return subCommands.stream()
                .filter(cmd -> cmd.toLowerCase().startsWith(args[0].toLowerCase()))
                .collect(Collectors.toList());
        }
        
        if (args.length == 2) {
            String subCommand = args[0].toLowerCase();
            
            if (subCommand.equals("delete") || subCommand.equals("remove") || 
                subCommand.equals("info") || subCommand.equals("tp") || subCommand.equals("teleport")) {
                // Portal names
                return portalManager.getAllPortals().stream()
                    .map(Portal::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
            }
        }
        
        if (args.length == 3 && args[0].equalsIgnoreCase("create")) {
            // World names for destination
            return Bukkit.getWorlds().stream()
                .map(World::getName)
                .filter(name -> name.toLowerCase().startsWith(args[2].toLowerCase()))
                .collect(Collectors.toList());
        }
        
        return completions;
    }
}