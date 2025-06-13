package org.hydr4.lilworlds.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.hydr4.lilworlds.LilWorlds;
import org.hydr4.lilworlds.utils.ColorUtils;
import org.hydr4.lilworlds.utils.LoggerUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class BaseCommand implements CommandExecutor, TabCompleter {
    
    protected final LilWorlds plugin;
    protected final String commandName;
    protected final String permission;
    protected final boolean playerOnly;
    
    public BaseCommand(LilWorlds plugin, String commandName, String permission, boolean playerOnly) {
        this.plugin = plugin;
        this.commandName = commandName;
        this.permission = permission;
        this.playerOnly = playerOnly;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        try {
            // Log command execution
            LoggerUtils.logCommand(sender.getName(), command.getName() + " " + String.join(" ", args));
            
            // Check if command is player-only
            if (playerOnly && !(sender instanceof Player)) {
                sendError(sender, plugin.getConfigManager().getPlayerOnlyMessage());
                return true;
            }
            
            // Check permissions
            if (permission != null && !sender.hasPermission(permission)) {
                sendError(sender, plugin.getConfigManager().getNoPermissionMessage());
                LoggerUtils.warn("Player " + sender.getName() + " tried to execute " + command.getName() + " without permission");
                return true;
            }
            
            // Execute the command
            return executeCommand(sender, command, label, args);
            
        } catch (Exception e) {
            handleCommandError(sender, command, args, e);
            return true;
        }
    }
    
    /**
     * Execute the actual command logic
     */
    protected abstract boolean executeCommand(CommandSender sender, Command command, String label, String[] args);
    
    /**
     * Handle command errors with detailed logging and user feedback
     */
    protected void handleCommandError(CommandSender sender, Command command, String[] args, Exception e) {
        String errorId = "ERR-" + System.currentTimeMillis();
        
        // Log detailed error information
        LoggerUtils.error("Command error [" + errorId + "] in " + command.getName() + 
                         " executed by " + sender.getName() + " with args: " + Arrays.toString(args), e);
        
        // Send user-friendly error message
        sendError(sender, "An internal error occurred while executing this command.");
        sendError(sender, "Error ID: " + errorId + " (check console for details)");
        
        // Send additional help if available
        String usage = getUsage();
        if (usage != null && !usage.isEmpty()) {
            sendMessage(sender, "&7Usage: &f" + usage);
        }
    }
    
    /**
     * Send a success message to the sender
     */
    protected void sendSuccess(CommandSender sender, String message) {
        sender.sendMessage(ColorUtils.colorize("&a✓ &7" + message));
    }
    
    /**
     * Send an error message to the sender
     */
    protected void sendError(CommandSender sender, String message) {
        sender.sendMessage(ColorUtils.colorize("&c✗ &7" + message));
    }
    
    /**
     * Send a warning message to the sender
     */
    protected void sendWarning(CommandSender sender, String message) {
        sender.sendMessage(ColorUtils.colorize("&e⚠ &7" + message));
    }
    
    /**
     * Send an info message to the sender
     */
    protected void sendInfo(CommandSender sender, String message) {
        sender.sendMessage(ColorUtils.colorize("&b✦ &7" + message));
    }
    
    /**
     * Send a warning message to the sender
     */
    protected void sendWarn(CommandSender sender, String message) {
        sender.sendMessage(ColorUtils.colorize("&e⚠ &6" + message));
    }
    
    /**
     * Send a regular message to the sender
     */
    protected void sendMessage(CommandSender sender, String message) {
        sender.sendMessage(ColorUtils.colorize(message));
    }
    
    /**
     * Send multiple messages to the sender
     */
    protected void sendMessages(CommandSender sender, String... messages) {
        for (String message : messages) {
            sendMessage(sender, message);
        }
    }
    
    /**
     * Check if sender has a specific permission
     */
    protected boolean hasPermission(CommandSender sender, String perm) {
        return sender.hasPermission(perm);
    }
    
    /**
     * Check if sender is a player
     */
    protected boolean isPlayer(CommandSender sender) {
        return sender instanceof Player;
    }
    
    /**
     * Get player from sender (with null check)
     */
    protected Player getPlayer(CommandSender sender) {
        return isPlayer(sender) ? (Player) sender : null;
    }
    
    /**
     * Validate argument count
     */
    protected boolean validateArgs(CommandSender sender, String[] args, int minArgs, int maxArgs) {
        if (args.length < minArgs) {
            sendError(sender, "Too few arguments! Minimum required: " + minArgs);
            String usage = getUsage();
            if (usage != null) {
                sendMessage(sender, "&7Usage: &f" + usage);
            }
            return false;
        }
        
        if (maxArgs > 0 && args.length > maxArgs) {
            sendError(sender, "Too many arguments! Maximum allowed: " + maxArgs);
            String usage = getUsage();
            if (usage != null) {
                sendMessage(sender, "&7Usage: &f" + usage);
            }
            return false;
        }
        
        return true;
    }
    
    /**
     * Validate argument count (exact)
     */
    protected boolean validateArgs(CommandSender sender, String[] args, int exactArgs) {
        return validateArgs(sender, args, exactArgs, exactArgs);
    }
    
    /**
     * Get command usage string
     */
    protected abstract String getUsage();
    
    /**
     * Get command description
     */
    protected abstract String getDescription();
    
    /**
     * Get subcommands for tab completion
     */
    protected List<String> getSubcommands() {
        return new ArrayList<>();
    }
    
    /**
     * Filter tab completion results based on input
     */
    protected List<String> filterCompletions(List<String> completions, String input) {
        if (input == null || input.isEmpty()) {
            return completions;
        }
        
        List<String> filtered = new ArrayList<>();
        String lowerInput = input.toLowerCase();
        
        for (String completion : completions) {
            if (completion.toLowerCase().startsWith(lowerInput)) {
                filtered.add(completion);
            }
        }
        
        return filtered;
    }
    
    /**
     * Default tab completion implementation
     */
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        try {
            return getTabCompletions(sender, command, alias, args);
        } catch (Exception e) {
            LoggerUtils.error("Error in tab completion for " + command.getName(), e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Get tab completions for this command
     */
    protected abstract List<String> getTabCompletions(CommandSender sender, Command command, String alias, String[] args);
}