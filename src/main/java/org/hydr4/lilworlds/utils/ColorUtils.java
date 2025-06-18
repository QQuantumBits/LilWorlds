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
        // Remove # if present
        color1 = color1.replace("#", "").replace("&", "");
        color2 = color2.replace("#", "").replace("&", "");
        
        // Handle Minecraft color codes
        if (color1.length() == 1) color1 = getHexFromMinecraftColor(color1);
        if (color2.length() == 1) color2 = getHexFromMinecraftColor(color2);
        
        // Ensure we have valid hex colors
        if (color1.length() != 6 || color2.length() != 6) {
            return ratio < 0.5 ? color1 : color2;
        }
        
        try {
            // Parse RGB values
            int r1 = Integer.parseInt(color1.substring(0, 2), 16);
            int g1 = Integer.parseInt(color1.substring(2, 4), 16);
            int b1 = Integer.parseInt(color1.substring(4, 6), 16);
            
            int r2 = Integer.parseInt(color2.substring(0, 2), 16);
            int g2 = Integer.parseInt(color2.substring(2, 4), 16);
            int b2 = Integer.parseInt(color2.substring(4, 6), 16);
            
            // Interpolate
            int r = (int) (r1 + (r2 - r1) * ratio);
            int g = (int) (g1 + (g2 - g1) * ratio);
            int b = (int) (b1 + (b2 - b1) * ratio);
            
            // Clamp values
            r = Math.max(0, Math.min(255, r));
            g = Math.max(0, Math.min(255, g));
            b = Math.max(0, Math.min(255, b));
            
            // Convert back to hex
            return String.format("&#%02x%02x%02x", r, g, b);
        } catch (NumberFormatException e) {
            return ratio < 0.5 ? color1 : color2;
        }
    }
    
    /**
     * Convert Minecraft color code to hex
     */
    private static String getHexFromMinecraftColor(String colorCode) {
        switch (colorCode.toLowerCase()) {
            case "0": return "000000"; // Black
            case "1": return "0000AA"; // Dark Blue
            case "2": return "00AA00"; // Dark Green
            case "3": return "00AAAA"; // Dark Aqua
            case "4": return "AA0000"; // Dark Red
            case "5": return "AA00AA"; // Dark Purple
            case "6": return "FFAA00"; // Gold
            case "7": return "AAAAAA"; // Gray
            case "8": return "555555"; // Dark Gray
            case "9": return "5555FF"; // Blue
            case "a": return "55FF55"; // Green
            case "b": return "55FFFF"; // Aqua
            case "c": return "FF5555"; // Red
            case "d": return "FF55FF"; // Light Purple
            case "e": return "FFFF55"; // Yellow
            case "f": return "FFFFFF"; // White
            default: return "FFFFFF";
        }
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
    
    /**
     * Create a centered text with padding
     */
    public static String centerText(String text, int width, char paddingChar) {
        if (text == null) return null;
        
        String stripped = stripColor(text);
        if (stripped.length() >= width) return text;
        
        int padding = width - stripped.length();
        int leftPadding = padding / 2;
        int rightPadding = padding - leftPadding;
        
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < leftPadding; i++) {
            result.append(paddingChar);
        }
        result.append(text);
        for (int i = 0; i < rightPadding; i++) {
            result.append(paddingChar);
        }
        
        return result.toString();
    }
    
    /**
     * Create a bordered text box
     */
    public static String[] createTextBox(String[] lines, String borderColor, String textColor) {
        if (lines == null || lines.length == 0) return new String[0];
        
        // Find the longest line
        int maxLength = 0;
        for (String line : lines) {
            int length = stripColor(line).length();
            if (length > maxLength) {
                maxLength = length;
            }
        }
        
        // Create the box
        String[] result = new String[lines.length + 2];
        String border = borderColor + "+" + repeat("-", maxLength + 2) + "+";
        
        result[0] = border;
        for (int i = 0; i < lines.length; i++) {
            String paddedLine = lines[i];
            int padding = maxLength - stripColor(lines[i]).length();
            if (padding > 0) {
                paddedLine += repeat(" ", padding);
            }
            result[i + 1] = borderColor + "| " + textColor + paddedLine + borderColor + " |";
        }
        result[result.length - 1] = border;
        
        return result;
    }
    
    /**
     * Repeat a string n times
     */
    private static String repeat(String str, int times) {
        if (times <= 0) return "";
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < times; i++) {
            result.append(str);
        }
        return result.toString();
    }
    
    /**
     * Create a fade effect between multiple colors
     */
    public static String multiColorGradient(String text, String... colors) {
        if (text == null || text.isEmpty() || colors.length < 2) return text;
        
        StringBuilder result = new StringBuilder();
        int length = text.length();
        int segments = colors.length - 1;
        
        for (int i = 0; i < length; i++) {
            char c = text.charAt(i);
            if (c == ' ') {
                result.append(c);
                continue;
            }
            
            // Calculate which segment we're in
            double position = (double) i / (length - 1) * segments;
            int segment = (int) position;
            double localRatio = position - segment;
            
            // Ensure we don't go out of bounds
            if (segment >= segments) {
                segment = segments - 1;
                localRatio = 1.0;
            }
            
            String color = interpolateColor(colors[segment], colors[segment + 1], localRatio);
            result.append(color).append(c);
        }
        
        return result.toString();
    }
    
    /**
     * Apply a pulsing effect to text (for animations)
     */
    public static String pulseText(String text, String color1, String color2, long time, long period) {
        double phase = (double) (time % period) / period;
        double intensity = (Math.sin(phase * 2 * Math.PI) + 1) / 2; // 0 to 1
        
        String color = interpolateColor(color1, color2, intensity);
        return color + text;
    }
    
    /**
     * Validate if a string is a valid hex color
     */
    public static boolean isValidHexColor(String color) {
        if (color == null) return false;
        
        // Remove # and & prefixes
        color = color.replace("#", "").replace("&", "");
        
        // Check if it's a valid 6-character hex string
        return color.matches("^[0-9A-Fa-f]{6}$");
    }
    
    /**
     * Get the luminance of a color (for contrast calculations)
     */
    public static double getLuminance(String hexColor) {
        if (!isValidHexColor(hexColor)) return 0.5;
        
        hexColor = hexColor.replace("#", "").replace("&", "");
        
        try {
            int r = Integer.parseInt(hexColor.substring(0, 2), 16);
            int g = Integer.parseInt(hexColor.substring(2, 4), 16);
            int b = Integer.parseInt(hexColor.substring(4, 6), 16);
            
            // Convert to relative luminance
            double rLum = r / 255.0;
            double gLum = g / 255.0;
            double bLum = b / 255.0;
            
            // Apply gamma correction
            rLum = rLum <= 0.03928 ? rLum / 12.92 : Math.pow((rLum + 0.055) / 1.055, 2.4);
            gLum = gLum <= 0.03928 ? gLum / 12.92 : Math.pow((gLum + 0.055) / 1.055, 2.4);
            bLum = bLum <= 0.03928 ? bLum / 12.92 : Math.pow((bLum + 0.055) / 1.055, 2.4);
            
            return 0.2126 * rLum + 0.7152 * gLum + 0.0722 * bLum;
        } catch (NumberFormatException e) {
            return 0.5;
        }
    }
    
    /**
     * Get a contrasting color (black or white) for the given background color
     */
    public static String getContrastingColor(String backgroundColor) {
        double luminance = getLuminance(backgroundColor);
        return luminance > 0.5 ? "&0" : "&f"; // Black for light backgrounds, white for dark
    }
}