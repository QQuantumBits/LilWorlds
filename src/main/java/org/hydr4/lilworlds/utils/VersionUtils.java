package org.hydr4.lilworlds.utils;

import org.bukkit.Bukkit;

/**
 * Utility class for version compatibility checks and version-specific features
 */
public class VersionUtils {
    
    private static final String SERVER_VERSION = Bukkit.getVersion();
    private static final String BUKKIT_VERSION = Bukkit.getBukkitVersion();
    
    // Version constants
    public static final int VERSION_1_16 = 16;
    public static final int VERSION_1_17 = 17;
    public static final int VERSION_1_18 = 18;
    public static final int VERSION_1_19 = 19;
    public static final int VERSION_1_20 = 20;
    public static final int VERSION_1_21 = 21;
    
    // Cached version info
    private static Integer majorVersion = null;
    private static Integer minorVersion = null;
    private static Integer patchVersion = null;
    
    /**
     * Get the major version number (e.g., 21 for 1.21.6)
     */
    public static int getMajorVersion() {
        if (majorVersion == null) {
            parseVersion();
        }
        return majorVersion != null ? majorVersion : 16; // Default to 1.16 if parsing fails
    }
    
    /**
     * Get the minor version number (e.g., 6 for 1.21.6)
     */
    public static int getMinorVersion() {
        if (minorVersion == null) {
            parseVersion();
        }
        return minorVersion != null ? minorVersion : 0;
    }
    
    /**
     * Get the patch version number (e.g., 0 for 1.21.6.0)
     */
    public static int getPatchVersion() {
        if (patchVersion == null) {
            parseVersion();
        }
        return patchVersion != null ? patchVersion : 0;
    }
    
    /**
     * Parse the server version string
     */
    private static void parseVersion() {
        try {
            // Extract version from Bukkit version string (e.g., "1.21.6-R0.1-SNAPSHOT")
            String version = BUKKIT_VERSION.split("-")[0];
            String[] parts = version.split("\\.");
            
            if (parts.length >= 2) {
                majorVersion = Integer.parseInt(parts[1]); // Skip the "1." part
                if (parts.length >= 3) {
                    minorVersion = Integer.parseInt(parts[2]);
                }
                if (parts.length >= 4) {
                    patchVersion = Integer.parseInt(parts[3]);
                }
            }
        } catch (Exception e) {
            LoggerUtils.warn("Failed to parse server version: " + BUKKIT_VERSION);
            majorVersion = 16; // Safe fallback
            minorVersion = 0;
            patchVersion = 0;
        }
    }
    
    /**
     * Check if the server is running at least the specified version
     */
    public static boolean isAtLeast(int majorVersion) {
        return getMajorVersion() >= majorVersion;
    }
    
    /**
     * Check if the server is running at least the specified version
     */
    public static boolean isAtLeast(int majorVersion, int minorVersion) {
        int currentMajor = getMajorVersion();
        if (currentMajor > majorVersion) return true;
        if (currentMajor < majorVersion) return false;
        return getMinorVersion() >= minorVersion;
    }
    
    /**
     * Check if the server is running exactly the specified version
     */
    public static boolean isExactly(int majorVersion) {
        return getMajorVersion() == majorVersion;
    }
    
    /**
     * Check if the server is running exactly the specified version
     */
    public static boolean isExactly(int majorVersion, int minorVersion) {
        return getMajorVersion() == majorVersion && getMinorVersion() == minorVersion;
    }
    
    /**
     * Check if the server supports hex colors (1.16+)
     */
    public static boolean supportsHexColors() {
        return isAtLeast(VERSION_1_16);
    }
    
    /**
     * Check if the server supports custom world height (1.18+)
     */
    public static boolean supportsCustomWorldHeight() {
        return isAtLeast(VERSION_1_18);
    }
    
    /**
     * Check if the server supports the new world generation system (1.18+)
     */
    public static boolean supportsNewWorldGeneration() {
        return isAtLeast(VERSION_1_18);
    }
    
    /**
     * Check if the server supports chat signing (1.19+)
     */
    public static boolean supportsChatSigning() {
        return isAtLeast(VERSION_1_19);
    }
    
    /**
     * Check if the server supports the new command system improvements (1.20+)
     */
    public static boolean supportsImprovedCommands() {
        return isAtLeast(VERSION_1_20);
    }
    
    /**
     * Check if the server supports bundle items (1.21+)
     */
    public static boolean supportsBundles() {
        return isAtLeast(VERSION_1_21);
    }
    
    /**
     * Check if the server supports trial chambers and related features (1.21+)
     */
    public static boolean supportsTrialChambers() {
        return isAtLeast(VERSION_1_21);
    }
    
    /**
     * Check if the server supports the latest 1.21.6 features
     */
    public static boolean supports1216Features() {
        return isAtLeast(VERSION_1_21, 6);
    }
    
    /**
     * Get a formatted version string
     */
    public static String getFormattedVersion() {
        return String.format("1.%d.%d", getMajorVersion(), getMinorVersion());
    }
    
    /**
     * Get the full version string including patch version if available
     */
    public static String getFullVersion() {
        if (getPatchVersion() > 0) {
            return String.format("1.%d.%d.%d", getMajorVersion(), getMinorVersion(), getPatchVersion());
        }
        return getFormattedVersion();
    }
    
    /**
     * Get the server implementation name
     */
    public static String getServerImplementation() {
        String serverName = Bukkit.getName().toLowerCase();
        if (serverName.contains("paper")) {
            return "Paper";
        } else if (serverName.contains("spigot")) {
            return "Spigot";
        } else if (serverName.contains("craftbukkit")) {
            return "CraftBukkit";
        } else if (serverName.contains("bukkit")) {
            return "Bukkit";
        } else {
            return "Unknown";
        }
    }
    
    /**
     * Check if the server is running Paper
     */
    public static boolean isPaper() {
        return getServerImplementation().equals("Paper");
    }
    
    /**
     * Check if the server is running Spigot
     */
    public static boolean isSpigot() {
        return getServerImplementation().equals("Spigot");
    }
    
    /**
     * Get version compatibility information
     */
    public static String getCompatibilityInfo() {
        return String.format("Server: %s %s | LilWorlds supports: 1.16 - 1.21.6", 
                getServerImplementation(), getFormattedVersion());
    }
    
    /**
     * Check if the current server version is supported by LilWorlds
     */
    public static boolean isSupported() {
        int major = getMajorVersion();
        return major >= 16 && major <= 21;
    }
    
    /**
     * Get a warning message if the version is not supported
     */
    public static String getUnsupportedVersionWarning() {
        if (isSupported()) {
            return null;
        }
        
        int major = getMajorVersion();
        if (major < 16) {
            return "LilWorlds requires Minecraft 1.16 or higher. Current version: " + getFormattedVersion();
        } else {
            return "LilWorlds has not been tested with Minecraft " + getFormattedVersion() + 
                   ". Please check for updates or report compatibility issues.";
        }
    }
}