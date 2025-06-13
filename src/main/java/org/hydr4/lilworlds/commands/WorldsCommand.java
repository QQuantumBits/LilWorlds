package org.hydr4.lilworlds.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.hydr4.lilworlds.LilWorlds;

import java.util.*;
import java.util.stream.Collectors;
import java.util.List;

public class WorldsCommand extends BaseCommand {
    
    public WorldsCommand(LilWorlds plugin) {
        super(plugin, "worlds", "lilworlds.worlds", false);
    }
    
    @Override
    protected boolean executeCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }
        
        String subcommand = args[0].toLowerCase();
        
        switch (subcommand) {
            case "reload":
            case "rl":
                return handleReload(sender, args);
            case "inventory":
            case "inv":
                return handleInventory(sender, args);
            case "groups":
            case "group":
                return handleGroups(sender, args);
            default:
                sendError(sender, plugin.getConfigManager().getMessage("unknown-subcommand", "{subcommand}", subcommand));
                sendHelp(sender);
                return true;
        }
    }
    
    private boolean handleReload(CommandSender sender, String[] args) {
        if (!hasPermission(sender, "lilworlds.worlds.reload")) {
            sendError(sender, "You don't have permission to reload LilWorlds!");
            return true;
        }
        
        String target = "all";
        if (args.length > 1) {
            target = args[1].toLowerCase();
        }
        
        switch (target) {
            case "all":
                sendInfo(sender, "Reloading LilWorlds...");
                plugin.reload();
                sendSuccess(sender, "LilWorlds reloaded successfully!");
                break;
                
            case "config":
                sendInfo(sender, "Reloading configuration...");
                plugin.getConfigManager().reload();
                sendSuccess(sender, "Configuration reloaded successfully!");
                break;
                
            case "generators":
                sendInfo(sender, "Reloading custom generators...");
                plugin.getGeneratorManager().reload();
                sendSuccess(sender, "Custom generators reloaded successfully!");
                break;
                
            case "worlds":
                sendInfo(sender, "Reloading world manager...");
                plugin.getWorldManager().reload();
                sendSuccess(sender, "World manager reloaded successfully!");
                break;
                
            default:
                sendError(sender, "Invalid reload target: " + target);
                sendMessage(sender, "&7Valid targets: &fall, config, generators, worlds");
                return true;
        }
        
        return true;
    }
    
    private boolean handleInventory(CommandSender sender, String[] args) {
        if (!hasPermission(sender, "lilworlds.worlds.inventory")) {
            sendError(sender, plugin.getConfigManager().getMessage("inventory-permission-denied"));
            return true;
        }
        
        if (args.length < 2) {
            sendError(sender, plugin.getConfigManager().getMessage("inventory-usage"));
            return true;
        }
        
        String action = args[1].toLowerCase();
        
        switch (action) {
            case "enable":
                plugin.getInventoryManager().setEnabled(true);
                sendSuccess(sender, "Separate inventories feature enabled!");
                sendInfo(sender, "Players will now have separate inventories per world.");
                break;
                
            case "disable":
                plugin.getInventoryManager().setEnabled(false);
                sendSuccess(sender, "Separate inventories feature disabled!");
                sendInfo(sender, "Players will now share inventories across all worlds.");
                break;
                
            case "status":
                String statusInfo = plugin.getInventoryManager().getStatusInfo();
                sendMessage(sender, "&6=== Inventory Manager Status ===");
                for (String line : statusInfo.split("\n")) {
                    if (line.startsWith("- ") || line.startsWith("  * ")) {
                        sendMessage(sender, "&7" + line);
                    } else {
                        sendMessage(sender, "&f" + line);
                    }
                }
                
                // Show world groups
                Map<String, String> worldGroups = plugin.getInventoryManager().getWorldGroups();
                if (!worldGroups.isEmpty()) {
                    sendMessage(sender, "&7World Groups:");
                    Map<String, List<String>> groupedWorlds = new HashMap<>();
                    for (Map.Entry<String, String> entry : worldGroups.entrySet()) {
                        groupedWorlds.computeIfAbsent(entry.getValue(), k -> new ArrayList<>()).add(entry.getKey());
                    }
                    for (Map.Entry<String, List<String>> entry : groupedWorlds.entrySet()) {
                        sendMessage(sender, "&7- &e" + entry.getKey() + "&7: &f" + String.join(", ", entry.getValue()));
                    }
                }
                break;
                
            case "clear":
                if (args.length < 3) {
                    sendError(sender, "Usage: /worlds inventory clear <player|all>");
                    return true;
                }
                
                String target = args[2];
                if (target.equalsIgnoreCase("all")) {
                    plugin.getInventoryManager().clearAllCache();
                    sendSuccess(sender, "Cleared all inventory cache data!");
                } else {
                    Player targetPlayer = Bukkit.getPlayer(target);
                    if (targetPlayer == null) {
                        sendError(sender, "Player not found: " + target);
                        return true;
                    }
                    
                    plugin.getInventoryManager().clearPlayerCache(targetPlayer.getUniqueId());
                    sendSuccess(sender, "Cleared inventory cache for " + targetPlayer.getName());
                }
                break;
                
            default:
                sendError(sender, "Invalid inventory action: " + action);
                sendMessage(sender, "&7Valid actions: &fenable, disable, status, clear");
                return true;
        }
        
        return true;
    }
    
    private boolean handleGroups(CommandSender sender, String[] args) {
        if (!hasPermission(sender, "lilworlds.worlds.groups")) {
            sendError(sender, "You don't have permission to manage inventory groups!");
            return true;
        }
        
        if (args.length < 2) {
            sendError(sender, "Usage: /worlds groups <list|add|remove> [args...]");
            return true;
        }
        
        String action = args[1].toLowerCase();
        
        switch (action) {
            case "list":
                Map<String, String> worldGroups = plugin.getInventoryManager().getWorldGroups();
                if (worldGroups.isEmpty()) {
                    sendInfo(sender, "No world groups configured. All worlds use the default group.");
                    return true;
                }
                
                sendMessage(sender, "&6=== Inventory World Groups ===");
                Map<String, List<String>> groupedWorlds = new HashMap<>();
                for (Map.Entry<String, String> entry : worldGroups.entrySet()) {
                    groupedWorlds.computeIfAbsent(entry.getValue(), k -> new ArrayList<>()).add(entry.getKey());
                }
                
                for (Map.Entry<String, List<String>> entry : groupedWorlds.entrySet()) {
                    sendMessage(sender, "&e" + entry.getKey() + "&7: &f" + String.join(", ", entry.getValue()));
                }
                break;
                
            case "add":
                if (args.length < 4) {
                    sendError(sender, "Usage: /worlds groups add <world> <group>");
                    return true;
                }
                
                String worldName = args[2];
                String groupName = args[3];
                
                plugin.getInventoryManager().addWorldToGroup(worldName, groupName);
                sendSuccess(sender, "Added world '" + worldName + "' to inventory group '" + groupName + "'");
                sendInfo(sender, "Players will now share inventories within this group.");
                break;
                
            case "remove":
                if (args.length < 3) {
                    sendError(sender, "Usage: /worlds groups remove <world>");
                    return true;
                }
                
                String worldToRemove = args[2];
                plugin.getInventoryManager().removeWorldFromGroup(worldToRemove);
                sendSuccess(sender, "Removed world '" + worldToRemove + "' from its inventory group");
                sendInfo(sender, "World will now use the default inventory group.");
                break;
                
            default:
                sendError(sender, "Unknown action: " + action);
                sendError(sender, "Usage: /worlds groups <list|add|remove> [args...]");
                break;
        }
        
        return true;
    }
    
    private void sendHelp(CommandSender sender) {
        sendMessage(sender, "");
        sendMessage(sender, "&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
        sendMessage(sender, "&b&lLilWorlds &8- &7Plugin Management Commands");
        sendMessage(sender, "");
        sendMessage(sender, "&7/worlds reload [target] &8- &fReload plugin components");
        sendMessage(sender, "&7/worlds rl [target] &8- &fAlias for reload");
        sendMessage(sender, "&7/worlds inventory <action> &8- &fManage separate inventories");
        sendMessage(sender, "&7/worlds groups <action> &8- &fManage inventory world groups");
        sendMessage(sender, "&7/worlds inv <action> &8- &fAlias for inventory");
        sendMessage(sender, "");
        sendMessage(sender, "&7Reload targets:");
        sendMessage(sender, "&8  • &fall &8- &7Reload everything");
        sendMessage(sender, "&8  • &fconfig &8- &7Reload configuration files");
        sendMessage(sender, "&8  • &fgenerators &8- &7Reload custom generators");
        sendMessage(sender, "&8  • &fworlds &8- &7Reload world manager");
        sendMessage(sender, "");
        sendMessage(sender, "&7Plugin version: &f" + plugin.getDescription().getVersion());
        sendMessage(sender, "&7Managed worlds: &f" + plugin.getWorldManager().getManagedWorlds().size());
        sendMessage(sender, "&7Custom generators: &f" + plugin.getGeneratorManager().getCustomGeneratorNames().size());
        sendMessage(sender, "&8▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
        sendMessage(sender, "");
    }
    
    @Override
    protected String getUsage() {
        return "/worlds <subcommand> [args...]";
    }
    
    @Override
    protected String getDescription() {
        return "Plugin management command";
    }
    
    @Override
    protected List<String> getSubcommands() {
        return Arrays.asList("reload", "rl", "inventory", "inv", "groups", "group");
    }
    
    @Override
    protected List<String> getTabCompletions(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            completions.addAll(getSubcommands());
        } else if (args.length == 2) {
            String subcommand = args[0].toLowerCase();
            
            if (subcommand.equals("reload") || subcommand.equals("rl")) {
                completions.addAll(Arrays.asList("all", "config", "generators", "worlds"));
            } else if (subcommand.equals("inventory") || subcommand.equals("inv")) {
                completions.addAll(Arrays.asList("enable", "disable", "status", "clear"));
            } else if (subcommand.equals("groups") || subcommand.equals("group")) {
                completions.addAll(Arrays.asList("list", "add", "remove"));
            }
        } else if (args.length == 3) {
            String subcommand = args[0].toLowerCase();
            String action = args[1].toLowerCase();
            
            if ((subcommand.equals("groups") || subcommand.equals("group")) && action.equals("remove")) {
                // Add world names that are in groups
                completions.addAll(plugin.getInventoryManager().getWorldGroups().keySet());
            }
        }
        
        return filterCompletions(completions, args[args.length - 1]);
    }
}