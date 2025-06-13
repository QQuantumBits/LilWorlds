package org.hydr4.lilworlds.utils;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * Security utilities for LilWorlds plugin
 * Provides rate limiting, input validation, and security logging
 */
public class SecurityUtils {
    
    // Rate limiting storage: senderKey -> action -> lastActionTime
    private static final Map<String, Map<String, Long>> rateLimits = new ConcurrentHashMap<>();
    private static final Map<UUID, Map<String, Long>> playerRateLimits = new ConcurrentHashMap<>();
    private static final Map<UUID, Integer> operationCounts = new ConcurrentHashMap<>();
    
    // Rate limiting constants (in milliseconds)
    private static final long WORLD_CREATE_COOLDOWN = 30000; // 30 seconds
    private static final long WORLD_CLONE_COOLDOWN = 60000;  // 1 minute
    private static final long WORLD_IMPORT_COOLDOWN = 15000; // 15 seconds
    private static final long WORLD_DELETE_COOLDOWN = 120000; // 2 minutes
    
    // Operation limits per hour
    private static final int MAX_WORLD_OPERATIONS_PER_HOUR = 10;
    
    // World name validation pattern (letters, numbers, hyphens, underscores)
    private static final Pattern VALID_WORLD_NAME = Pattern.compile("^[a-zA-Z0-9_-]{1,32}$");
    private static final Pattern SAFE_PATH = Pattern.compile("^[a-zA-Z0-9_.-]+$");
    
    // Maximum world name length
    private static final int MAX_WORLD_NAME_LENGTH = 32;
    
    /**
     * Check if a sender is rate limited for a specific action
     * 
     * @param sender The command sender
     * @param action The action being performed
     * @param cooldownSeconds The cooldown period in seconds
     * @return true if the action is allowed, false if rate limited
     */
    public static boolean checkRateLimit(CommandSender sender, String action, int cooldownSeconds) {
        // Rate limiting disabled - always allow operations
        return true;
    }
    
    /**
     * Check if a player can perform a world operation (enhanced version)
     */
    public static boolean canPerformOperation(CommandSender sender, String operation) {
        // Rate limiting and operation count checks disabled - always allow operations
        return true;
    }
    
    /**
     * Check rate limiting for UUID-based tracking
     */
    private static boolean checkRateLimit(UUID playerId, String operation) {
        long currentTime = System.currentTimeMillis();
        long cooldown = getCooldownForOperation(operation);
        
        Map<String, Long> playerLimits = playerRateLimits.computeIfAbsent(playerId, k -> new ConcurrentHashMap<>());
        
        Long lastTime = playerLimits.get(operation);
        if (lastTime != null && (currentTime - lastTime) < cooldown) {
            return false;
        }
        
        playerLimits.put(operation, currentTime);
        return true;
    }
    
    /**
     * Get cooldown time for specific operations
     */
    private static long getCooldownForOperation(String operation) {
        switch (operation.toLowerCase()) {
            case "create":
                return WORLD_CREATE_COOLDOWN;
            case "clone":
                return WORLD_CLONE_COOLDOWN;
            case "import":
                return WORLD_IMPORT_COOLDOWN;
            case "delete":
            case "remove":
                return WORLD_DELETE_COOLDOWN;
            default:
                return 5000; // 5 seconds default
        }
    }
    
    /**
     * Check if player hasn't exceeded operation count
     */
    private static boolean checkOperationCount(UUID playerId) {
        int count = operationCounts.getOrDefault(playerId, 0);
        return count < MAX_WORLD_OPERATIONS_PER_HOUR;
    }
    
    /**
     * Increment operation count for player
     */
    private static void incrementOperationCount(UUID playerId) {
        operationCounts.merge(playerId, 1, Integer::sum);
    }
    
    /**
     * Reset operation counts (should be called hourly)
     */
    public static void resetOperationCounts() {
        operationCounts.clear();
        LoggerUtils.info("Operation counts reset for all players");
    }
    
    /**
     * Validate world name for security
     * 
     * @param worldName The world name to validate
     * @return true if the world name is valid and safe
     */
    public static boolean isValidWorldName(String worldName) {
        if (worldName == null || worldName.trim().isEmpty()) {
            return false;
        }
        
        // Check length
        if (worldName.length() > MAX_WORLD_NAME_LENGTH) {
            return false;
        }
        
        // Check pattern (only alphanumeric, hyphens, and underscores)
        return VALID_WORLD_NAME.matcher(worldName).matches();
    }
    
    /**
     * Validate file path for security
     */
    public static boolean isValidPath(String path) {
        if (path == null || path.trim().isEmpty()) {
            return false;
        }
        
        // Check for path traversal attempts
        if (path.contains("..") || path.contains("/") || path.contains("\\")) {
            return false;
        }
        
        return SAFE_PATH.matcher(path).matches();
    }
    
    /**
     * Log security events
     * 
     * @param sender The command sender
     * @param event The security event type
     * @param details Additional details about the event
     */
    public static void logSecurityEvent(CommandSender sender, String event, String details) {
        String senderInfo = getSenderInfo(sender);
        LoggerUtils.warn("[SECURITY] " + event + " by " + senderInfo + ": " + details);
    }
    
    /**
     * Get a unique key for the sender (for rate limiting)
     */
    private static String getSenderKey(CommandSender sender) {
        if (sender instanceof Player) {
            return "player:" + ((Player) sender).getUniqueId().toString();
        } else {
            return "console:" + sender.getName();
        }
    }
    
    /**
     * Get sender information for logging
     */
    private static String getSenderInfo(CommandSender sender) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            return player.getName() + " (" + player.getUniqueId() + ")";
        } else {
            return "Console";
        }
    }
    
    /**
     * Clean up old rate limit entries (should be called periodically)
     */
    public static void cleanupRateLimits() {
        long currentTime = System.currentTimeMillis();
        long maxAge = 3600000; // 1 hour
        
        rateLimits.entrySet().removeIf(entry -> {
            Map<String, Long> actionMap = entry.getValue();
            actionMap.entrySet().removeIf(actionEntry -> 
                (currentTime - actionEntry.getValue()) > maxAge);
            return actionMap.isEmpty();
        });
        
        playerRateLimits.entrySet().removeIf(entry -> {
            Map<String, Long> actionMap = entry.getValue();
            actionMap.entrySet().removeIf(actionEntry -> 
                (currentTime - actionEntry.getValue()) > maxAge);
            return actionMap.isEmpty();
        });
        
        LoggerUtils.debug("Cleaned up old rate limit entries");
    }
    
    /**
     * Check if sender has permission and log if denied
     */
    public static boolean checkPermissionWithLogging(CommandSender sender, String permission, String action) {
        if (sender.hasPermission(permission)) {
            return true;
        }
        
        logSecurityEvent(sender, "PERMISSION_DENIED", 
            "Attempted " + action + " without permission: " + permission);
        return false;
    }
    
    /**
     * Validate command arguments for potential exploits
     */
    public static boolean validateCommandArgs(String[] args) {
        if (args == null) {
            return true;
        }
        
        for (String arg : args) {
            if (arg == null) {
                continue;
            }
            
            // Check for potential command injection
            if (arg.contains(";") || arg.contains("&") || arg.contains("|") || 
                arg.contains("$") || arg.contains("`") || arg.contains("$(")) {
                return false;
            }
            
            // Check for excessively long arguments
            if (arg.length() > 256) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Get remaining cooldown time for a player and action
     */
    public static long getRemainingCooldown(CommandSender sender, String action, int cooldownSeconds) {
        if (!(sender instanceof Player)) {
            return 0; // Console has no cooldown
        }
        
        String senderKey = getSenderKey(sender);
        Map<String, Long> senderLimits = rateLimits.get(senderKey);
        
        if (senderLimits == null) {
            return 0;
        }
        
        Long lastActionTime = senderLimits.get(action);
        if (lastActionTime == null) {
            return 0;
        }
        
        long currentTime = System.currentTimeMillis();
        long cooldownMs = cooldownSeconds * 1000L;
        long elapsed = currentTime - lastActionTime;
        
        return Math.max(0, cooldownMs - elapsed);
    }
    
    /**
     * Reset all rate limits (called periodically)
     */
    public static void resetRateLimits() {
        rateLimits.clear();
        playerRateLimits.clear();
        LoggerUtils.info("All rate limits have been reset");
    }
    
    /**
     * Validate generator name
     */
    public static boolean isValidGeneratorName(String generatorName) {
        if (generatorName == null || generatorName.trim().isEmpty()) {
            return false;
        }
        
        // Allow alphanumeric, dots, hyphens, underscores
        return Pattern.matches("^[a-zA-Z0-9._-]+$", generatorName) && generatorName.length() <= 64;
    }
    
    /**
     * Validate seed value
     */
    public static boolean isValidSeed(String seed) {
        if (seed == null || seed.trim().isEmpty()) {
            return true; // Empty seed is valid (random)
        }
        
        try {
            // Try to parse as long
            Long.parseLong(seed);
            return true;
        } catch (NumberFormatException e) {
            // If not a number, check if it's a valid string seed
            return seed.length() <= 32 && Pattern.matches("^[a-zA-Z0-9_-]+$", seed);
        }
    }
}