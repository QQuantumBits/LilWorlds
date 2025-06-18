package org.hydr4.lilworlds.portals;

// WorldEdit imports moved to method level to avoid ClassNotFoundException
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.hydr4.lilworlds.LilWorlds;
import org.hydr4.lilworlds.portals.Portal.PortalType;
import org.hydr4.lilworlds.utils.ColorUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class PortalCommand implements CommandExecutor, TabCompleter {
    
    private final LilWorlds plugin;
    private final PortalManager portalManager;
    
    public PortalCommand(LilWorlds plugin) {
        this.plugin = plugin;
        this.portalManager = plugin.getPortalManager();
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            showHelp(sender);
            return true;
        }
        
        String subcommand = args[0].toLowerCase();
        
        switch (subcommand) {
            case "create":
                return handleCreate(sender, args);
            case "delete":
            case "remove":
                return handleDelete(sender, args);
            case "list":
                return handleList(sender);
            case "info":
                return handleInfo(sender, args);
            case "tp":
            case "teleport":
                return handleTeleport(sender, args);
            case "frame":
                return handleFrame(sender, args);
            case "reload":
                return handleReload(sender);
            case "help":
            default:
                showHelp(sender);
                return true;
        }
    }
    
    private boolean handleCreate(CommandSender sender, String[] args) {
        if (!sender.hasPermission("lilworlds.portal.create")) {
            sender.sendMessage(getMessage("portal-permission-denied"));
            return true;
        }
        
        if (!(sender instanceof Player)) {
            sender.sendMessage(getMessage("portal-player-only"));
            return true;
        }
        
        Player player = (Player) sender;
        
        if (args.length < 3) {
            player.sendMessage(getMessage("portal-usage-create"));
            return true;
        }
        
        // Try WorldEdit first, then manual coordinates
        if (args.length == 3 || args.length == 6) {
            return handleCreateWithWorldEdit(player, args);
        } else if (args.length >= 9) {
            return handleCreateManual(player, args);
        } else {
            player.sendMessage(getMessage("portal-usage-create"));
            player.sendMessage(getMessage("portal-usage-create-manual"));
            return true;
        }
    }
    
    private boolean handleCreateWithWorldEdit(Player player, String[] args) {
        String portalName = args[1];
        String destWorldName = args[2];
        
        // Check if portal already exists
        if (portalManager.getPortal(portalName) != null) {
            player.sendMessage(getMessage("portal-already-exists").replace("{name}", portalName));
            return true;
        }
        
        // Check if destination world exists
        World destWorld = Bukkit.getWorld(destWorldName);
        if (destWorld == null) {
            player.sendMessage(getMessage("portal-world-not-found").replace("{world}", destWorldName));
            return true;
        }
        
        try {
            // Use reflection to avoid ClassNotFoundException when WorldEdit is not present
            Class<?> worldEditClass = Class.forName("com.sk89q.worldedit.WorldEdit");
            Class<?> bukkitAdapterClass = Class.forName("com.sk89q.worldedit.bukkit.BukkitAdapter");
            Class<?> bukkitPlayerClass = Class.forName("com.sk89q.worldedit.bukkit.BukkitPlayer");
            Class<?> localSessionClass = Class.forName("com.sk89q.worldedit.LocalSession");
            Class<?> regionClass = Class.forName("com.sk89q.worldedit.regions.Region");
            Class<?> blockVector3Class = Class.forName("com.sk89q.worldedit.math.BlockVector3");
            
            // Get WorldEdit instance
            Object worldEdit = worldEditClass.getMethod("getInstance").invoke(null);
            Object sessionManager = worldEditClass.getMethod("getSessionManager").invoke(worldEdit);
            
            // Adapt player
            Object bukkitPlayer = bukkitAdapterClass.getMethod("adapt", org.bukkit.entity.Player.class).invoke(null, player);
            Object bukkitWorld = bukkitAdapterClass.getMethod("adapt", org.bukkit.World.class).invoke(null, player.getWorld());
            
            // Get session and selection
            Object session = sessionManager.getClass().getMethod("get", bukkitPlayerClass).invoke(sessionManager, bukkitPlayer);
            Object selection = localSessionClass.getMethod("getSelection", bukkitWorld.getClass()).invoke(session, bukkitWorld);
            
            if (selection == null) {
                player.sendMessage(getMessage("portal-no-selection"));
                return true;
            }
            
            // Get min and max points
            Object min = regionClass.getMethod("getMinimumPoint").invoke(selection);
            Object max = regionClass.getMethod("getMaximumPoint").invoke(selection);
            
            // Extract coordinates
            int minX = (Integer) blockVector3Class.getMethod("getX").invoke(min);
            int minY = (Integer) blockVector3Class.getMethod("getY").invoke(min);
            int minZ = (Integer) blockVector3Class.getMethod("getZ").invoke(min);
            int maxX = (Integer) blockVector3Class.getMethod("getX").invoke(max);
            int maxY = (Integer) blockVector3Class.getMethod("getY").invoke(max);
            int maxZ = (Integer) blockVector3Class.getMethod("getZ").invoke(max);
            
            World world = player.getWorld();
            Location loc1 = new Location(world, minX, minY, minZ);
            Location loc2 = new Location(world, maxX, maxY, maxZ);
            
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
                    player.sendMessage(getMessage("portal-invalid-destination"));
                    return true;
                }
            }
            
            // Create portal
            boolean success = portalManager.createPortal(portalName, loc1, loc2, destWorldName, 
                    destLocation, Material.OBSIDIAN, PortalType.CUSTOM);
            
            if (success) {
                player.sendMessage(getMessage("portal-created-success").replace("{name}", portalName));
                player.sendMessage(getMessage("portal-created-from")
                    .replace("{from}", formatLocation(loc1))
                    .replace("{to}", formatLocation(loc2)));
                player.sendMessage(getMessage("portal-created-destination")
                    .replace("{world}", destWorldName)
                    .replace("{location}", formatLocation(destLocation)));
            } else {
                player.sendMessage(getMessage("portal-creation-failed"));
            }
            
            return true;
            
        } catch (ClassNotFoundException e) {
            player.sendMessage(getMessage("portal-worldedit-not-found"));
            return true;
        } catch (Exception e) {
            if (e.getClass().getSimpleName().equals("IncompleteRegionException")) {
                player.sendMessage(getMessage("portal-incomplete-selection"));
            } else {
                player.sendMessage(getMessage("portal-selection-error").replace("{error}", e.getMessage()));
            }
            return true;
        }
    }
    
    private boolean handleCreateManual(Player player, String[] args) {
        String portalName = args[1];
        String destWorldName = args[2];

        // Check if portal already exists
        if (portalManager.getPortal(portalName) != null) {
            player.sendMessage(getMessage("portal-already-exists").replace("{name}", portalName));
            return true;
        }

        // Check if destination world exists
        World destWorld = Bukkit.getWorld(destWorldName);
        if (destWorld == null) {
            player.sendMessage(getMessage("portal-world-not-found").replace("{world}", destWorldName));
            return true;
        }

        try {
            // Parse coordinates
            double x1 = Double.parseDouble(args[3]);
            double y1 = Double.parseDouble(args[4]);
            double z1 = Double.parseDouble(args[5]);
            double x2 = Double.parseDouble(args[6]);
            double y2 = Double.parseDouble(args[7]);
            double z2 = Double.parseDouble(args[8]);

            World world = player.getWorld();
            Location loc1 = new Location(world, x1, y1, z1);
            Location loc2 = new Location(world, x2, y2, z2);

            // Default destination to spawn of destination world
            Location destLocation = destWorld.getSpawnLocation();

            // Check for custom destination coordinates
            if (args.length >= 12) {
                try {
                    double destX = Double.parseDouble(args[9]);
                    double destY = Double.parseDouble(args[10]);
                    double destZ = Double.parseDouble(args[11]);
                    destLocation = new Location(destWorld, destX, destY, destZ);
                } catch (NumberFormatException e) {
                    player.sendMessage(getMessage("portal-invalid-destination"));
                    return true;
                }
            }

            // Create portal
            boolean success = portalManager.createPortal(portalName, loc1, loc2, destWorldName, 
                    destLocation, Material.OBSIDIAN, PortalType.CUSTOM);

            if (success) {
                player.sendMessage(getMessage("portal-created-success").replace("{name}", portalName));
                player.sendMessage(getMessage("portal-created-from")
                    .replace("{from}", formatLocation(loc1))
                    .replace("{to}", formatLocation(loc2)));
                player.sendMessage(getMessage("portal-created-destination")
                    .replace("{world}", destWorldName)
                    .replace("{location}", formatLocation(destLocation)));
            } else {
                player.sendMessage(getMessage("portal-creation-failed"));
            }

            return true;

        } catch (NumberFormatException e) {
            player.sendMessage(getMessage("portal-invalid-coordinates"));
            return true;
        }
    }
    
    private boolean handleDelete(CommandSender sender, String[] args) {
        if (!sender.hasPermission("lilworlds.portal.delete")) {
            sender.sendMessage(getMessage("portal-permission-denied"));
            return true;
        }
        
        if (args.length < 2) {
            sender.sendMessage(getMessage("portal-usage-delete"));
            return true;
        }
        
        String portalName = args[1];
        Portal portal = portalManager.getPortal(portalName);
        
        if (portal == null) {
            sender.sendMessage(getMessage("portal-not-found").replace("{name}", portalName));
            return true;
        }
        
        boolean success = portalManager.deletePortal(portalName);
        
        if (success) {
            sender.sendMessage(getMessage("portal-deleted").replace("{name}", portalName));
        } else {
            sender.sendMessage(getMessage("portal-delete-failed").replace("{name}", portalName));
        }
        
        return true;
    }
    
    private boolean handleList(CommandSender sender) {
        if (!sender.hasPermission("lilworlds.portal.list")) {
            sender.sendMessage(getMessage("portal-permission-denied"));
            return true;
        }
        
        List<Portal> portals = new ArrayList<>(portalManager.getAllPortals());
        
        sender.sendMessage(getMessage("portal-list-header"));
        sender.sendMessage(getMessage("portal-list-title"));
        sender.sendMessage("");
        
        if (portals.isEmpty()) {
            sender.sendMessage(getMessage("portal-list-empty"));
        } else {
            for (Portal portal : portals) {
                String type = getPortalTypeString(portal.getType());
                sender.sendMessage(getMessage("portal-list-entry")
                    .replace("{name}", portal.getName())
                    .replace("{world}", portal.getDestinationWorld())
                    .replace("{type}", type));
            }
        }
        
        sender.sendMessage("");
        sender.sendMessage(getMessage("portal-list-count").replace("{count}", String.valueOf(portals.size())));
        sender.sendMessage(getMessage("portal-list-footer"));
        
        return true;
    }
    
    private boolean handleInfo(CommandSender sender, String[] args) {
        if (!sender.hasPermission("lilworlds.portal.info")) {
            sender.sendMessage(getMessage("portal-permission-denied"));
            return true;
        }
        
        if (args.length < 2) {
            sender.sendMessage(getMessage("portal-usage-info"));
            return true;
        }
        
        String portalName = args[1];
        Portal portal = portalManager.getPortal(portalName);
        
        if (portal == null) {
            sender.sendMessage(getMessage("portal-not-found").replace("{name}", portalName));
            return true;
        }
        
        sender.sendMessage(getMessage("portal-info-header"));
        sender.sendMessage(getMessage("portal-info-title").replace("{name}", portal.getName()));
        sender.sendMessage("");
        sender.sendMessage(getMessage("portal-info-name").replace("{name}", portal.getName()));
        sender.sendMessage(getMessage("portal-info-type").replace("{type}", getPortalTypeString(portal.getType())));
        sender.sendMessage(getMessage("portal-info-world").replace("{world}", portal.getLocation1().getWorld().getName()));
        sender.sendMessage(getMessage("portal-info-location1").replace("{location}", formatLocation(portal.getLocation1())));
        sender.sendMessage(getMessage("portal-info-location2").replace("{location}", formatLocation(portal.getLocation2())));
        sender.sendMessage(getMessage("portal-info-destination")
            .replace("{world}", portal.getDestinationWorld())
            .replace("{location}", formatLocation(portal.getDestinationLocation())));
        sender.sendMessage(getMessage("portal-info-frame").replace("{material}", portal.getFrameMaterial().toString()));
        sender.sendMessage(getMessage("portal-info-size").replace("{size}", calculatePortalSize(portal)));
        sender.sendMessage(getMessage("portal-info-enabled").replace("{status}", portal.isEnabled() ? getMessage("portal-enabled") : getMessage("portal-disabled")));
        sender.sendMessage(getMessage("portal-info-footer"));
        
        return true;
    }
    
    private boolean handleTeleport(CommandSender sender, String[] args) {
        if (!sender.hasPermission("lilworlds.portal.teleport")) {
            sender.sendMessage(getMessage("portal-permission-denied"));
            return true;
        }
        
        if (!(sender instanceof Player)) {
            sender.sendMessage(getMessage("portal-player-only"));
            return true;
        }
        
        if (args.length < 2) {
            sender.sendMessage(getMessage("portal-usage-tp"));
            return true;
        }
        
        Player player = (Player) sender;
        String portalName = args[1];
        Portal portal = portalManager.getPortal(portalName);
        
        if (portal == null) {
            player.sendMessage(getMessage("portal-not-found").replace("{name}", portalName));
            return true;
        }
        
        // Check cooldown (simplified for now)
        // TODO: Implement cooldown system in PortalManager
        
        // Check if already in destination world
        if (player.getWorld().getName().equals(portal.getDestinationWorld())) {
            player.sendMessage(getMessage("portal-teleport-same-world"));
            return true;
        }
        
        player.sendMessage(getMessage("portal-teleporting").replace("{name}", portalName));
        
        // Teleport player
        World destWorld = Bukkit.getWorld(portal.getDestinationWorld());
        if (destWorld != null) {
            player.teleport(portal.getDestinationLocation());
            player.sendMessage(getMessage("portal-teleport-success").replace("{world}", portal.getDestinationWorld()));
            // TODO: Set cooldown
        } else {
            player.sendMessage(getMessage("portal-teleport-failed"));
        }
        
        return true;
    }
    
    private boolean handleFrame(CommandSender sender, String[] args) {
        if (!sender.hasPermission("lilworlds.portal.frame")) {
            sender.sendMessage(getMessage("portal-permission-denied"));
            return true;
        }
        
        if (args.length < 3) {
            sender.sendMessage(getMessage("portal-usage-frame"));
            return true;
        }
        
        String action = args[1].toLowerCase();
        String portalName = args[2];
        
        Portal portal = portalManager.getPortal(portalName);
        if (portal == null) {
            sender.sendMessage(getMessage("portal-not-found").replace("{name}", portalName));
            return true;
        }
        
        switch (action) {
            case "create":
                // TODO: Implement frame creation
                sender.sendMessage(getMessage("portal-frame-created").replace("{name}", portalName));
                break;
            case "remove":
                // TODO: Implement frame removal
                sender.sendMessage(getMessage("portal-frame-removed").replace("{name}", portalName));
                break;
            default:
                sender.sendMessage(getMessage("portal-frame-invalid-action"));
                break;
        }
        
        return true;
    }
    
    private boolean handleReload(CommandSender sender) {
        if (!sender.hasPermission("lilworlds.portal.reload")) {
            sender.sendMessage(getMessage("portal-permission-denied"));
            return true;
        }
        
        try {
            // TODO: Implement reload functionality
            sender.sendMessage(getMessage("portal-config-reloaded"));
        } catch (Exception e) {
            sender.sendMessage(getMessage("portal-reload-failed"));
        }
        
        return true;
    }
    
    private void showHelp(CommandSender sender) {
        sender.sendMessage(getMessage("portal-help-header"));
        sender.sendMessage(getMessage("portal-help-title"));
        sender.sendMessage("");
        sender.sendMessage(getMessage("cmd-portal-create"));
        sender.sendMessage(getMessage("cmd-portal-delete"));
        sender.sendMessage(getMessage("cmd-portal-list"));
        sender.sendMessage(getMessage("cmd-portal-info"));
        sender.sendMessage(getMessage("cmd-portal-tp"));
        sender.sendMessage(getMessage("cmd-portal-frame"));
        sender.sendMessage(getMessage("cmd-portal-reload"));
        sender.sendMessage(getMessage("portal-help-footer"));
    }
    
    private String getMessage(String key) {
        return ColorUtils.colorize(plugin.getConfigManager().getMessagesConfig().getString(key, "&cMessage not found: " + key));
    }
    
    private String formatLocation(Location loc) {
        return String.format("%.1f, %.1f, %.1f", loc.getX(), loc.getY(), loc.getZ());
    }
    
    private String getPortalTypeString(PortalType type) {
        switch (type) {
            case CUSTOM:
                return getMessage("portal-type-custom");
            case NETHER_PORTAL:
                return getMessage("portal-type-nether");
            case END_PORTAL:
                return getMessage("portal-type-end");
            default:
                return type.toString();
        }
    }
    
    private String calculatePortalSize(Portal portal) {
        Location loc1 = portal.getLocation1();
        Location loc2 = portal.getLocation2();
        
        int width = Math.abs((int) (loc2.getX() - loc1.getX())) + 1;
        int height = Math.abs((int) (loc2.getY() - loc1.getY())) + 1;
        int depth = Math.abs((int) (loc2.getZ() - loc1.getZ())) + 1;
        
        return width + "x" + height + "x" + depth;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            // Main subcommands
            List<String> subcommands = Arrays.asList("create", "delete", "list", "info", "tp", "frame", "reload", "help");
            return subcommands.stream()
                    .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        
        String subcommand = args[0].toLowerCase();
        
        switch (subcommand) {
            case "create":
                if (args.length == 2) {
                    // Portal name - no suggestions
                    return completions;
                } else if (args.length == 3) {
                    // World names
                    return Bukkit.getWorlds().stream()
                            .map(World::getName)
                            .filter(name -> name.toLowerCase().startsWith(args[2].toLowerCase()))
                            .collect(Collectors.toList());
                }
                break;
                
            case "delete":
            case "info":
            case "tp":
                if (args.length == 2) {
                    // Portal names
                    return portalManager.getAllPortals().stream()
                            .map(Portal::getName)
                            .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                            .collect(Collectors.toList());
                }
                break;
                
            case "frame":
                if (args.length == 2) {
                    // Frame actions
                    return Arrays.asList("create", "remove").stream()
                            .filter(action -> action.toLowerCase().startsWith(args[1].toLowerCase()))
                            .collect(Collectors.toList());
                } else if (args.length == 3) {
                    // Portal names
                    return portalManager.getAllPortals().stream()
                            .map(Portal::getName)
                            .filter(name -> name.toLowerCase().startsWith(args[2].toLowerCase()))
                            .collect(Collectors.toList());
                }
                break;
        }
        
        return completions;
    }
}