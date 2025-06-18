package org.hydr4.lilworlds.commands;

<<<<<<< HEAD
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.BukkitPlayer;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.math.BlockVector3;
=======
>>>>>>> da2d858 (Fixed WorldEdit Integration)
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
import org.hydr4.lilworlds.utils.ColorUtils;

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
<<<<<<< HEAD
            case "enable":
                return handleEnable(sender, args);
            case "disable":
                return handleDisable(sender, args);
            case "reload":
                return handleReload(sender, args);
            case "frame":
                return handleFrame(sender, args);
=======
            case "reload":
                return handleReload(sender, args);
>>>>>>> da2d858 (Fixed WorldEdit Integration)
            default:
                sendHelp(sender);
                return true;
        }
    }
    
    private boolean handleCreate(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ColorUtils.colorize("&cOnly players can create portals!"));
            return true;
        }
        
<<<<<<< HEAD
        if (!sender.hasPermission("lilworlds.portal.create")) {
            sender.sendMessage(ColorUtils.colorize("&cYou don't have permission to create portals!"));
            return true;
        }
        
        Player player = (Player) sender;
        
        // Check for WorldEdit selection first
        if (worldEditEnabled && args.length >= 3) {
            return handleCreateWithWorldEdit(player, args);
        }
        
        // Manual coordinate specification
        if (args.length < 9) {
            player.sendMessage(ColorUtils.colorize("&cUsage: /portal create <name> <destination_world> <x1> <y1> <z1> <x2> <y2> <z2> [dest_x] [dest_y] [dest_z]"));
=======
        Player player = (Player) sender;
        
        if (!player.hasPermission("lilworlds.portal.create")) {
            player.sendMessage(ColorUtils.colorize("&cYou don't have permission to create portals!"));
            return true;
        }
        
        if (args.length < 3) {
            player.sendMessage(ColorUtils.colorize("&cUsage: /portal create <name> <destination_world> [coordinates]"));
            if (worldEditEnabled) {
                player.sendMessage(ColorUtils.colorize("&eOr use WorldEdit selection: /portal create <name> <destination_world>"));
            }
            return true;
        }
        
        // Check for WorldEdit selection first
        if (worldEditEnabled && args.length == 3) {
            return handleCreateWithWorldEdit(player, args);
        }
        
        // Manual coordinate creation
        if (args.length < 9) {
            player.sendMessage(ColorUtils.colorize("&cUsage: /portal create <name> <dest_world> <x1> <y1> <z1> <x2> <y2> <z2> [dest_x] [dest_y] [dest_z]"));
>>>>>>> da2d858 (Fixed WorldEdit Integration)
            if (worldEditEnabled) {
                player.sendMessage(ColorUtils.colorize("&eOr use WorldEdit selection: /portal create <name> <destination_world>"));
            }
            return true;
        }
        
        return handleCreateManual(player, args);
    }
    
    private boolean handleCreateWithWorldEdit(Player player, String[] args) {
<<<<<<< HEAD
        if (args.length < 3) {
            player.sendMessage(ColorUtils.colorize("&cUsage: /portal create <name> <destination_world>"));
            return true;
        }
        
        String portalName = args[1];
        String destWorldName = args[2];
        
        // Check if portal already exists
        if (portalManager.getPortal(portalName) != null) {
            player.sendMessage(ColorUtils.colorize("&cPortal with name '" + portalName + "' already exists!"));
=======
        String portalName = args[1];
        String destinationWorld = args[2];
        
        // Check if portal already exists
        if (portalManager.getPortal(portalName) != null) {
            player.sendMessage(ColorUtils.colorize("&cPortal '" + portalName + "' already exists!"));
>>>>>>> da2d858 (Fixed WorldEdit Integration)
            return true;
        }
        
        // Check if destination world exists
<<<<<<< HEAD
        World destWorld = Bukkit.getWorld(destWorldName);
        if (destWorld == null) {
            player.sendMessage(ColorUtils.colorize("&cDestination world '" + destWorldName + "' not found!"));
=======
        World destWorld = Bukkit.getWorld(destinationWorld);
        if (destWorld == null) {
            player.sendMessage(ColorUtils.colorize("&cDestination world '" + destinationWorld + "' does not exist!"));
>>>>>>> da2d858 (Fixed WorldEdit Integration)
            return true;
        }
        
        try {
<<<<<<< HEAD
            // Get WorldEdit selection
            BukkitPlayer bukkitPlayer = BukkitAdapter.adapt(player);
            LocalSession session = WorldEdit.getInstance().getSessionManager().get(bukkitPlayer);
            Region selection = session.getSelection(bukkitPlayer.getWorld());
=======
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
>>>>>>> da2d858 (Fixed WorldEdit Integration)
            
            if (selection == null) {
                player.sendMessage(ColorUtils.colorize("&cNo WorldEdit selection found! Use //wand to select an area."));
                return true;
            }
            
<<<<<<< HEAD
            // Convert WorldEdit selection to Bukkit locations
            BlockVector3 min = selection.getMinimumPoint();
            BlockVector3 max = selection.getMaximumPoint();
            
            World world = player.getWorld();
            Location loc1 = new Location(world, min.getX(), min.getY(), min.getZ());
            Location loc2 = new Location(world, max.getX(), max.getY(), max.getZ());
            
            // Default destination to spawn of destination world
            Location destLocation = destWorld.getSpawnLocation();
            
            // Check for custom destination coordinates
            if (args.length >= 6) {
                try {
                    double destX = Double.parseDouble(args[3]);
                    double destY = Double.parseDouble(args[4]);
                    double destZ = Double.parseDouble(args[5]);
                    destLocation = new Location(destWorld, destX, destY, destZ);
                } catch (NumberFormatException e) {
                    player.sendMessage(ColorUtils.colorize("&cInvalid destination coordinates!"));
                    return true;
                }
            }
            
            // Create portal
            boolean success = portalManager.createPortal(portalName, loc1, loc2, destWorldName, 
                    destLocation, Material.OBSIDIAN, PortalType.CUSTOM);
            
            if (success) {
                player.sendMessage(ColorUtils.colorize("&aPortal '" + portalName + "' created successfully!"));
                player.sendMessage(ColorUtils.colorize("&7From: " + formatLocation(loc1) + " to " + formatLocation(loc2)));
                player.sendMessage(ColorUtils.colorize("&7Destination: " + destWorldName + " at " + formatLocation(destLocation)));
            } else {
                player.sendMessage(ColorUtils.colorize("&cFailed to create portal!"));
=======
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
                player.sendMessage(ColorUtils.colorize("&aPortal '" + portalName + "' created successfully using WorldEdit selection!"));
                player.sendMessage(ColorUtils.colorize("&eDestination: " + destinationWorld + " at spawn"));
                
                // Save portals
                portalManager.savePortals();
                plugin.getLogger().info("Portal '" + portalName + "' created by " + player.getName() + " using WorldEdit selection");
            } else {
                player.sendMessage(ColorUtils.colorize("&cFailed to create portal! Check console for errors."));
>>>>>>> da2d858 (Fixed WorldEdit Integration)
            }
            
            return true;
            
<<<<<<< HEAD
        } catch (IncompleteRegionException e) {
            player.sendMessage(ColorUtils.colorize("&cIncomplete WorldEdit selection! Use //wand to select an area."));
            return true;
        } catch (Exception e) {
            player.sendMessage(ColorUtils.colorize("&cError getting WorldEdit selection: " + e.getMessage()));
=======
        } catch (ClassNotFoundException e) {
            player.sendMessage(ColorUtils.colorize("&cWorldEdit not found! Please install WorldEdit or use manual coordinates."));
            return true;
        } catch (Exception e) {
            if (e.getClass().getSimpleName().equals("IncompleteRegionException")) {
                player.sendMessage(ColorUtils.colorize("&cIncomplete WorldEdit selection! Use //wand to select an area."));
            } else {
                player.sendMessage(ColorUtils.colorize("&cError getting WorldEdit selection: " + e.getMessage()));
            }
>>>>>>> da2d858 (Fixed WorldEdit Integration)
            return true;
        }
    }
    
    private boolean handleCreateManual(Player player, String[] args) {
        String portalName = args[1];
<<<<<<< HEAD
        String destWorldName = args[2];
        
        // Check if portal already exists
        if (portalManager.getPortal(portalName) != null) {
            player.sendMessage(ColorUtils.colorize("&cPortal with name '" + portalName + "' already exists!"));
=======
        String destinationWorld = args[2];
        
        // Check if portal already exists
        if (portalManager.getPortal(portalName) != null) {
            player.sendMessage(ColorUtils.colorize("&cPortal '" + portalName + "' already exists!"));
>>>>>>> da2d858 (Fixed WorldEdit Integration)
            return true;
        }
        
        // Check if destination world exists
<<<<<<< HEAD
        World destWorld = Bukkit.getWorld(destWorldName);
        if (destWorld == null) {
            player.sendMessage(ColorUtils.colorize("&cDestination world '" + destWorldName + "' not found!"));
=======
        World destWorld = Bukkit.getWorld(destinationWorld);
        if (destWorld == null) {
            player.sendMessage(ColorUtils.colorize("&cDestination world '" + destinationWorld + "' does not exist!"));
>>>>>>> da2d858 (Fixed WorldEdit Integration)
            return true;
        }
        
        try {
<<<<<<< HEAD
            // Parse coordinates
=======
>>>>>>> da2d858 (Fixed WorldEdit Integration)
            double x1 = Double.parseDouble(args[3]);
            double y1 = Double.parseDouble(args[4]);
            double z1 = Double.parseDouble(args[5]);
            double x2 = Double.parseDouble(args[6]);
            double y2 = Double.parseDouble(args[7]);
            double z2 = Double.parseDouble(args[8]);
            
<<<<<<< HEAD
            World world = player.getWorld();
            Location loc1 = new Location(world, x1, y1, z1);
            Location loc2 = new Location(world, x2, y2, z2);
            
            // Default destination to spawn of destination world
            Location destLocation = destWorld.getSpawnLocation();
            
            // Check for custom destination coordinates
=======
            Location pos1 = new Location(player.getWorld(), x1, y1, z1);
            Location pos2 = new Location(player.getWorld(), x2, y2, z2);
            
            // Destination coordinates
            Location destination;
>>>>>>> da2d858 (Fixed WorldEdit Integration)
            if (args.length >= 12) {
                double destX = Double.parseDouble(args[9]);
                double destY = Double.parseDouble(args[10]);
                double destZ = Double.parseDouble(args[11]);
<<<<<<< HEAD
                destLocation = new Location(destWorld, destX, destY, destZ);
            }
            
            // Create portal
            boolean success = portalManager.createPortal(portalName, loc1, loc2, destWorldName, 
                    destLocation, Material.OBSIDIAN, PortalType.CUSTOM);
            
            if (success) {
                player.sendMessage(ColorUtils.colorize("&aPortal '" + portalName + "' created successfully!"));
                player.sendMessage(ColorUtils.colorize("&7From: " + formatLocation(loc1) + " to " + formatLocation(loc2)));
                player.sendMessage(ColorUtils.colorize("&7Destination: " + destWorldName + " at " + formatLocation(destLocation)));
            } else {
                player.sendMessage(ColorUtils.colorize("&cFailed to create portal!"));
            }
            
            return true;
            
        } catch (NumberFormatException e) {
            player.sendMessage(ColorUtils.colorize("&cInvalid coordinates! Please use numbers."));
            return true;
        }
=======
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
                player.sendMessage(ColorUtils.colorize("&aPortal '" + portalName + "' created successfully!"));
                player.sendMessage(ColorUtils.colorize("&eDestination: " + destinationWorld + " at " + 
                    String.format("%.1f, %.1f, %.1f", destination.getX(), destination.getY(), destination.getZ())));
                
                // Save portals
                portalManager.savePortals();
                plugin.getLogger().info("Portal '" + portalName + "' created by " + player.getName());
            } else {
                player.sendMessage(ColorUtils.colorize("&cFailed to create portal! Check console for errors."));
            }
            
        } catch (NumberFormatException e) {
            player.sendMessage(ColorUtils.colorize("&cInvalid coordinates! Please use numbers."));
        }
        
        return true;
>>>>>>> da2d858 (Fixed WorldEdit Integration)
    }
    
    private boolean handleDelete(CommandSender sender, String[] args) {
        if (!sender.hasPermission("lilworlds.portal.delete")) {
            sender.sendMessage(ColorUtils.colorize("&cYou don't have permission to delete portals!"));
            return true;
        }
        
        if (args.length < 2) {
            sender.sendMessage(ColorUtils.colorize("&cUsage: /portal delete <name>"));
            return true;
        }
        
        String portalName = args[1];
        Portal portal = portalManager.getPortal(portalName);
        
        if (portal == null) {
<<<<<<< HEAD
            sender.sendMessage(ColorUtils.colorize("&cPortal '" + portalName + "' not found!"));
=======
            sender.sendMessage(ColorUtils.colorize("&cPortal '" + portalName + "' does not exist!"));
>>>>>>> da2d858 (Fixed WorldEdit Integration)
            return true;
        }
        
        boolean success = portalManager.deletePortal(portalName);
<<<<<<< HEAD
        if (success) {
            sender.sendMessage(ColorUtils.colorize("&aPortal '" + portalName + "' deleted successfully!"));
=======
        
        if (success) {
            sender.sendMessage(ColorUtils.colorize("&aPortal '" + portalName + "' deleted successfully!"));
            portalManager.savePortals();
            plugin.getLogger().info("Portal '" + portalName + "' deleted by " + sender.getName());
>>>>>>> da2d858 (Fixed WorldEdit Integration)
        } else {
            sender.sendMessage(ColorUtils.colorize("&cFailed to delete portal!"));
        }
        
        return true;
    }
    
    private boolean handleList(CommandSender sender, String[] args) {
        if (!sender.hasPermission("lilworlds.portal.list")) {
            sender.sendMessage(ColorUtils.colorize("&cYou don't have permission to list portals!"));
            return true;
        }
        
        java.util.Collection<Portal> portals = portalManager.getAllPortals();
        if (portals.isEmpty()) {
            sender.sendMessage(ColorUtils.colorize("&eNo portals found."));
            return true;
        }
        
        sender.sendMessage(ColorUtils.colorize("&6=== Portals (" + portals.size() + ") ==="));
        for (Portal portal : portals) {
<<<<<<< HEAD
            String status = portal.isEnabled() ? "&aEnabled" : "&cDisabled";
            sender.sendMessage(ColorUtils.colorize("&e" + portal.getName() + " &7-> &b" + portal.getDestinationWorld() + " " + status));
=======
            sender.sendMessage(ColorUtils.colorize("&e• &f" + portal.getName() + 
                " &7→ &b" + portal.getDestinationWorld() + 
                " &7(" + portal.getType() + ")"));
>>>>>>> da2d858 (Fixed WorldEdit Integration)
        }
        
        return true;
    }
    
    private boolean handleInfo(CommandSender sender, String[] args) {
        if (!sender.hasPermission("lilworlds.portal.info")) {
            sender.sendMessage(ColorUtils.colorize("&cYou don't have permission to view portal info!"));
            return true;
        }
        
        if (args.length < 2) {
            sender.sendMessage(ColorUtils.colorize("&cUsage: /portal info <name>"));
            return true;
        }
        
        String portalName = args[1];
        Portal portal = portalManager.getPortal(portalName);
        
        if (portal == null) {
<<<<<<< HEAD
            sender.sendMessage(ColorUtils.colorize("&cPortal '" + portalName + "' not found!"));
=======
            sender.sendMessage(ColorUtils.colorize("&cPortal '" + portalName + "' does not exist!"));
>>>>>>> da2d858 (Fixed WorldEdit Integration)
            return true;
        }
        
        sender.sendMessage(ColorUtils.colorize("&6=== Portal Info: " + portal.getName() + " ==="));
<<<<<<< HEAD
        sender.sendMessage(ColorUtils.colorize("&7World: &e" + portal.getLocation1().getWorld().getName()));
        sender.sendMessage(ColorUtils.colorize("&7Corner 1: &e" + formatLocation(portal.getLocation1())));
        sender.sendMessage(ColorUtils.colorize("&7Corner 2: &e" + formatLocation(portal.getLocation2())));
        sender.sendMessage(ColorUtils.colorize("&7Destination World: &b" + portal.getDestinationWorld()));
        sender.sendMessage(ColorUtils.colorize("&7Destination: &b" + formatLocation(portal.getDestinationLocation())));
        sender.sendMessage(ColorUtils.colorize("&7Type: &e" + portal.getType()));
        sender.sendMessage(ColorUtils.colorize("&7Frame Material: &e" + portal.getFrameMaterial()));
        sender.sendMessage(ColorUtils.colorize("&7Status: " + (portal.isEnabled() ? "&aEnabled" : "&cDisabled")));
        
        int[] size = portal.getSize();
        sender.sendMessage(ColorUtils.colorize("&7Size: &e" + size[0] + "x" + size[1] + "x" + size[2]));
=======
        sender.sendMessage(ColorUtils.colorize("&eType: &f" + portal.getType()));
        sender.sendMessage(ColorUtils.colorize("&eWorld: &f" + portal.getLocation1().getWorld().getName()));
        sender.sendMessage(ColorUtils.colorize("&ePosition 1: &f" + formatLocation(portal.getLocation1())));
        sender.sendMessage(ColorUtils.colorize("&ePosition 2: &f" + formatLocation(portal.getLocation2())));
        sender.sendMessage(ColorUtils.colorize("&eDestination World: &f" + portal.getDestinationWorld()));
        sender.sendMessage(ColorUtils.colorize("&eDestination: &f" + formatLocation(portal.getDestinationLocation())));
        sender.sendMessage(ColorUtils.colorize("&eFrame Material: &f" + portal.getFrameMaterial()));
>>>>>>> da2d858 (Fixed WorldEdit Integration)
        
        return true;
    }
    
    private boolean handleTeleport(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ColorUtils.colorize("&cOnly players can teleport through portals!"));
            return true;
        }
        
<<<<<<< HEAD
        if (!sender.hasPermission("lilworlds.portal.teleport")) {
            sender.sendMessage(ColorUtils.colorize("&cYou don't have permission to teleport through portals!"));
=======
        Player player = (Player) sender;
        
        if (!player.hasPermission("lilworlds.portal.teleport")) {
            player.sendMessage(ColorUtils.colorize("&cYou don't have permission to teleport through portals!"));
>>>>>>> da2d858 (Fixed WorldEdit Integration)
            return true;
        }
        
        if (args.length < 2) {
<<<<<<< HEAD
            sender.sendMessage(ColorUtils.colorize("&cUsage: /portal tp <name>"));
            return true;
        }
        
        Player player = (Player) sender;
=======
            player.sendMessage(ColorUtils.colorize("&cUsage: /portal tp <name>"));
            return true;
        }
        
>>>>>>> da2d858 (Fixed WorldEdit Integration)
        String portalName = args[1];
        Portal portal = portalManager.getPortal(portalName);
        
        if (portal == null) {
<<<<<<< HEAD
            player.sendMessage(ColorUtils.colorize("&cPortal '" + portalName + "' not found!"));
=======
            player.sendMessage(ColorUtils.colorize("&cPortal '" + portalName + "' does not exist!"));
>>>>>>> da2d858 (Fixed WorldEdit Integration)
            return true;
        }
        
        boolean success = portalManager.teleportPlayer(player, portal);
<<<<<<< HEAD
        if (success) {
            player.sendMessage(ColorUtils.colorize("&aTeleported through portal: " + portalName));
        } else {
            if (portalManager.isOnCooldown(player)) {
                long remaining = portalManager.getRemainingCooldown(player);
                player.sendMessage(ColorUtils.colorize("&cYou must wait " + (remaining / 1000) + " seconds before using another portal!"));
            } else {
                player.sendMessage(ColorUtils.colorize("&cFailed to teleport through portal!"));
            }
=======
        
        if (success) {
            player.sendMessage(ColorUtils.colorize("&aTeleported through portal '" + portalName + "'!"));
        } else {
            player.sendMessage(ColorUtils.colorize("&cFailed to teleport! You might be on cooldown or the destination world is not loaded."));
>>>>>>> da2d858 (Fixed WorldEdit Integration)
        }
        
        return true;
    }
    
<<<<<<< HEAD
    private boolean handleEnable(CommandSender sender, String[] args) {
        if (!sender.hasPermission("lilworlds.portal.frame")) {
            sender.sendMessage(ColorUtils.colorize("&cYou don't have permission to manage portals!"));
            return true;
        }
        
        if (args.length < 2) {
            sender.sendMessage(ColorUtils.colorize("&cUsage: /portal enable <name>"));
            return true;
        }
        
        // Note: Current Portal class doesn't support enabling/disabling
        // This would require modifying the Portal class to have a mutable enabled field
        sender.sendMessage(ColorUtils.colorize("&ePortal enable/disable functionality requires portal system update."));
        return true;
    }
    
    private boolean handleDisable(CommandSender sender, String[] args) {
        if (!sender.hasPermission("lilworlds.portal.frame")) {
            sender.sendMessage(ColorUtils.colorize("&cYou don't have permission to manage portals!"));
            return true;
        }
        
        if (args.length < 2) {
            sender.sendMessage(ColorUtils.colorize("&cUsage: /portal disable <name>"));
            return true;
        }
        
        // Note: Current Portal class doesn't support enabling/disabling
        // This would require modifying the Portal class to have a mutable enabled field
        sender.sendMessage(ColorUtils.colorize("&ePortal enable/disable functionality requires portal system update."));
        return true;
    }
    
=======
>>>>>>> da2d858 (Fixed WorldEdit Integration)
    private boolean handleReload(CommandSender sender, String[] args) {
        if (!sender.hasPermission("lilworlds.portal.reload")) {
            sender.sendMessage(ColorUtils.colorize("&cYou don't have permission to reload portals!"));
            return true;
        }
        
<<<<<<< HEAD
        portalManager.reload();
        sender.sendMessage(ColorUtils.colorize("&aPortals reloaded successfully!"));
        return true;
    }
    
    private boolean handleFrame(CommandSender sender, String[] args) {
        if (!sender.hasPermission("lilworlds.portal.frame")) {
            sender.sendMessage(ColorUtils.colorize("&cYou don't have permission to manage portal frames!"));
            return true;
        }
        
        if (args.length < 3) {
            sender.sendMessage(ColorUtils.colorize("&cUsage: /portal frame <create|remove> <portal_name>"));
            return true;
        }
        
        String action = args[1].toLowerCase();
        String portalName = args[2];
        
        Portal portal = portalManager.getPortal(portalName);
        if (portal == null) {
            sender.sendMessage(ColorUtils.colorize("&cPortal '" + portalName + "' not found!"));
            return true;
        }
        
        switch (action) {
            case "create":
            case "build":
                portalManager.createPortalFrame(portal);
                sender.sendMessage(ColorUtils.colorize("&aPortal frame created for: " + portalName));
                break;
            case "remove":
            case "delete":
                portalManager.removePortalFrame(portal);
                sender.sendMessage(ColorUtils.colorize("&aPortal frame removed for: " + portalName));
                break;
            default:
                sender.sendMessage(ColorUtils.colorize("&cInvalid action! Use 'create' or 'remove'."));
                break;
        }
=======
        portalManager.loadPortals();
        sender.sendMessage(ColorUtils.colorize("&aPortal configuration reloaded!"));
        plugin.getLogger().info("Portal configuration reloaded by " + sender.getName());
>>>>>>> da2d858 (Fixed WorldEdit Integration)
        
        return true;
    }
    
    private void sendHelp(CommandSender sender) {
<<<<<<< HEAD
        sender.sendMessage(ColorUtils.colorize("&6=== LilWorlds Portal Commands ==="));
        sender.sendMessage(ColorUtils.colorize("&e/portal create <name> <dest_world> &7- Create portal with WorldEdit selection"));
        sender.sendMessage(ColorUtils.colorize("&e/portal create <name> <dest_world> <x1> <y1> <z1> <x2> <y2> <z2> &7- Create portal manually"));
        sender.sendMessage(ColorUtils.colorize("&e/portal delete <name> &7- Delete a portal"));
        sender.sendMessage(ColorUtils.colorize("&e/portal list &7- List all portals"));
        sender.sendMessage(ColorUtils.colorize("&e/portal info <name> &7- Show portal information"));
        sender.sendMessage(ColorUtils.colorize("&e/portal tp <name> &7- Teleport through a portal"));
        sender.sendMessage(ColorUtils.colorize("&e/portal frame <create|remove> <name> &7- Manage portal frames"));
=======
        sender.sendMessage(ColorUtils.colorize("&6=== Portal Commands ==="));
        sender.sendMessage(ColorUtils.colorize("&e/portal create <name> <dest_world> &7- Create portal with WorldEdit selection"));
        sender.sendMessage(ColorUtils.colorize("&e/portal delete <name> &7- Delete portal"));
        sender.sendMessage(ColorUtils.colorize("&e/portal list &7- List all portals"));
        sender.sendMessage(ColorUtils.colorize("&e/portal info <name> &7- Show portal information"));
        sender.sendMessage(ColorUtils.colorize("&e/portal tp <name> &7- Teleport through portal"));
>>>>>>> da2d858 (Fixed WorldEdit Integration)
        sender.sendMessage(ColorUtils.colorize("&e/portal reload &7- Reload portal configuration"));
        
        if (worldEditEnabled) {
            sender.sendMessage(ColorUtils.colorize("&aWorldEdit integration is enabled! Use //wand to select areas."));
        } else {
            sender.sendMessage(ColorUtils.colorize("&cWorldEdit not found. Manual coordinates required."));
        }
    }
    
    private String formatLocation(Location loc) {
        return String.format("%.1f, %.1f, %.1f", loc.getX(), loc.getY(), loc.getZ());
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
<<<<<<< HEAD
            List<String> subCommands = Arrays.asList("create", "delete", "list", "info", "tp", "enable", "disable", "reload", "frame");
            return subCommands.stream()
                    .filter(cmd -> cmd.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
=======
            // Main subcommands
            List<String> subCommands = Arrays.asList("create", "delete", "list", "info", "tp", "reload");
            return subCommands.stream()
                .filter(cmd -> cmd.toLowerCase().startsWith(args[0].toLowerCase()))
                .collect(Collectors.toList());
>>>>>>> da2d858 (Fixed WorldEdit Integration)
        }
        
        if (args.length == 2) {
            String subCommand = args[0].toLowerCase();
<<<<<<< HEAD
            if (subCommand.equals("delete") || subCommand.equals("info") || subCommand.equals("tp") || 
                subCommand.equals("enable") || subCommand.equals("disable")) {
                return portalManager.getPortalNames().stream()
                        .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            }
            
            if (subCommand.equals("frame")) {
                return Arrays.asList("create", "remove").stream()
                        .filter(action -> action.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            }
            
            if (subCommand.equals("create")) {
                // Return world names for destination
                return Bukkit.getWorlds().stream()
                        .map(World::getName)
                        .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            }
        }
        
        if (args.length == 3) {
            String subCommand = args[0].toLowerCase();
            if (subCommand.equals("frame")) {
                return portalManager.getPortalNames().stream()
                        .filter(name -> name.toLowerCase().startsWith(args[2].toLowerCase()))
                        .collect(Collectors.toList());
            }
            
            if (subCommand.equals("create")) {
                // Return world names for destination
                return Bukkit.getWorlds().stream()
                        .map(World::getName)
                        .filter(name -> name.toLowerCase().startsWith(args[2].toLowerCase()))
                        .collect(Collectors.toList());
            }
        }
        

        
=======
            
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
        
>>>>>>> da2d858 (Fixed WorldEdit Integration)
        return completions;
    }
}