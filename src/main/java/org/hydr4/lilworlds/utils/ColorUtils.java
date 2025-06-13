package org.hydr4.lilworlds.utils;

import org.bukkit.ChatColor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ColorUtils {
    
    // ANSI color codes for console output
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";
    public static final String ANSI_BRIGHT_BLACK = "\u001B[90m";
    public static final String ANSI_BRIGHT_RED = "\u001B[91m";
    public static final String ANSI_BRIGHT_GREEN = "\u001B[92m";
    public static final String ANSI_BRIGHT_YELLOW = "\u001B[93m";
    public static final String ANSI_BRIGHT_BLUE = "\u001B[94m";
    public static final String ANSI_BRIGHT_PURPLE = "\u001B[95m";
    public static final String ANSI_BRIGHT_CYAN = "\u001B[96m";
    public static final String ANSI_BRIGHT_WHITE = "\u001B[97m";
    
    // Bold and formatting
    public static final String ANSI_BOLD = "\u001B[1m";
    public static final String ANSI_UNDERLINE = "\u001B[4m";
    
    // Hex color pattern for modern Minecraft versions
    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");
    
    /**
     * Colorize a string with Minecraft color codes for in-game chat
     */
    public static String colorize(String message) {
        if (message == null) return null;
        
        // Handle hex colors for 1.16+
        message = translateHexColorCodes(message);
        
        // Handle standard color codes
        return ChatColor.translateAlternateColorCodes('&', message);
    }
    
    /**
     * Convert Minecraft color codes to ANSI codes for console output
     */
    public static String toConsoleColor(String message) {
        if (message == null) return null;
        
        message = message.replace("&0", ANSI_BLACK);
        message = message.replace("&1", ANSI_BLUE);
        message = message.replace("&2", ANSI_GREEN);
        message = message.replace("&3", ANSI_CYAN);
        message = message.replace("&4", ANSI_RED);
        message = message.replace("&5", ANSI_PURPLE);
        message = message.replace("&6", ANSI_YELLOW);
        message = message.replace("&7", ANSI_WHITE);
        message = message.replace("&8", ANSI_BRIGHT_BLACK);
        message = message.replace("&9", ANSI_BRIGHT_BLUE);
        message = message.replace("&a", ANSI_BRIGHT_GREEN);
        message = message.replace("&b", ANSI_BRIGHT_CYAN);
        message = message.replace("&c", ANSI_BRIGHT_RED);
        message = message.replace("&d", ANSI_BRIGHT_PURPLE);
        message = message.replace("&e", ANSI_BRIGHT_YELLOW);
        message = message.replace("&f", ANSI_BRIGHT_WHITE);
        message = message.replace("&l", ANSI_BOLD);
        message = message.replace("&n", ANSI_UNDERLINE);
        message = message.replace("&r", ANSI_RESET);
        
        return message + ANSI_RESET;
    }
    
    /**
     * Strip all color codes from a message
     */
    public static String stripColor(String message) {
        if (message == null) return null;
        return ChatColor.stripColor(colorize(message));
    }
    
    /**
     * Translate hex color codes for Minecraft 1.16+
     */
    private static String translateHexColorCodes(String message) {
        Matcher matcher = HEX_PATTERN.matcher(message);
        StringBuffer buffer = new StringBuffer(message.length() + 4 * 8);
        
        while (matcher.find()) {
            String group = matcher.group(1);
            matcher.appendReplacement(buffer, ChatColor.COLOR_CHAR + "x"
                    + ChatColor.COLOR_CHAR + group.charAt(0) + ChatColor.COLOR_CHAR + group.charAt(1)
                    + ChatColor.COLOR_CHAR + group.charAt(2) + ChatColor.COLOR_CHAR + group.charAt(3)
                    + ChatColor.COLOR_CHAR + group.charAt(4) + ChatColor.COLOR_CHAR + group.charAt(5));
        }
        
        return matcher.appendTail(buffer).toString();
    }
    
    /**
     * Create a gradient between two colors
     */
    public static String gradient(String text, String startColor, String endColor) {
        if (text == null || text.isEmpty()) return text;
        
        StringBuilder result = new StringBuilder();
        int length = text.length();
        
        for (int i = 0; i < length; i++) {
            char c = text.charAt(i);
            if (c == ' ') {
                result.append(c);
                continue;
            }
            
            // Simple gradient calculation (this is a basic implementation)
            double ratio = (double) i / (length - 1);
            String color = interpolateColor(startColor, endColor, ratio);
            result.append(color).append(c);
        }
        
        return result.toString();
    }
    
    /**
     * Interpolate between two hex colors
     */
    private static String interpolateColor(String color1, String color2, double ratio) {
        // This is a simplified implementation
        // In a real implementation, you'd convert hex to RGB, interpolate, then back to hex
        return ratio < 0.5 ? color1 : color2;
    }
    
    /**
     * Create a rainbow effect on text
     */
    public static String rainbow(String text) {
        if (text == null || text.isEmpty()) return text;
        
        String[] colors = {"&c", "&6", "&e", "&a", "&b", "&9", "&d"};
        StringBuilder result = new StringBuilder();
        int colorIndex = 0;
        
        for (char c : text.toCharArray()) {
            if (c == ' ') {
                result.append(c);
                continue;
            }
            
            result.append(colors[colorIndex % colors.length]).append(c);
            colorIndex++;
        }
        
        return result.toString();
    }
    
    /**
     * Create a progress bar with colors
     */
    public static String progressBar(double percentage, int length, String completeColor, String incompleteColor) {
        int completed = (int) (percentage * length / 100);
        int remaining = length - completed;
        
        StringBuilder bar = new StringBuilder();
        
        // Completed part
        bar.append(completeColor);
        for (int i = 0; i < completed; i++) {
            bar.append("█");
        }
        
        // Remaining part
        bar.append(incompleteColor);
        for (int i = 0; i < remaining; i++) {
            bar.append("█");
        }
        
        return bar.toString();
    }
}