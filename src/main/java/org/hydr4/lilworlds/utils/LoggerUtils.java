package org.hydr4.lilworlds.utils;

import org.bukkit.Bukkit;
import org.hydr4.lilworlds.LilWorlds;

import java.util.logging.Level;
import java.util.logging.Logger;

public class LoggerUtils {
    
    private static final String PREFIX = "[LilWorlds] ";
    
    private static Logger getLogger() {
        return LilWorlds.getInstance() != null ? 
            LilWorlds.getInstance().getLogger() : 
            Bukkit.getLogger();
    }
    
    /**
     * Log an info message with colorful console output
     */
    public static void info(String message) {
        String coloredMessage = ColorUtils.toConsoleColor("&7[&bINFO&7] " + message);
        Bukkit.getConsoleSender().sendMessage(PREFIX + coloredMessage);
    }
    
    /**
     * Log a success message with colorful console output
     */
    public static void success(String message) {
        String coloredMessage = ColorUtils.toConsoleColor("&7[&aSUCCESS&7] " + message);
        Bukkit.getConsoleSender().sendMessage(PREFIX + coloredMessage);
    }
    
    /**
     * Log a warning message with colorful console output
     */
    public static void warn(String message) {
        String coloredMessage = ColorUtils.toConsoleColor("&7[&eWARN&7] " + message);
        Bukkit.getConsoleSender().sendMessage(PREFIX + coloredMessage);
    }
    
    /**
     * Log an error message with colorful console output
     */
    public static void error(String message) {
        String coloredMessage = ColorUtils.toConsoleColor("&7[&cERROR&7] " + message);
        Bukkit.getConsoleSender().sendMessage(PREFIX + coloredMessage);
    }
    
    /**
     * Log an error message with exception details
     */
    public static void error(String message, Throwable throwable) {
        String coloredMessage = ColorUtils.toConsoleColor("&7[&cERROR&7] " + message + ": " + throwable.getMessage());
        Bukkit.getConsoleSender().sendMessage(PREFIX + coloredMessage);
        getLogger().log(Level.SEVERE, message, throwable);
    }
    
    /**
     * Log a debug message (only if debug mode is enabled)
     */
    public static void debug(String message) {
        if (LilWorlds.getInstance() != null && 
            LilWorlds.getInstance().getConfigManager() != null &&
            LilWorlds.getInstance().getConfigManager().isDebugEnabled()) {
            String coloredMessage = ColorUtils.toConsoleColor("&7[&dDEBUG&7] " + message);
            Bukkit.getConsoleSender().sendMessage(PREFIX + coloredMessage);
            // Debug messages are only shown in console, not logged to file
        }
    }
    
    /**
     * Log a command execution
     */
    public static void logCommand(String player, String command) {
        info("Player " + player + " executed command: " + command);
    }
    
    /**
     * Log world operations
     */
    public static void logWorldOperation(String operation, String worldName) {
        info("World operation '" + operation + "' performed on world: " + worldName);
    }
    
    /**
     * Log world operations with additional details
     */
    public static void logWorldOperation(String operation, String worldName, String details) {
        info("World operation '" + operation + "' performed on world: " + worldName + " - " + details);
    }
}