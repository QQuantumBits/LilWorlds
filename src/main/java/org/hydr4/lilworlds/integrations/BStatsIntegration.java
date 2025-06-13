package org.hydr4.lilworlds.integrations;

import org.bstats.bukkit.Metrics;
import org.bstats.charts.AdvancedPie;
import org.bstats.charts.SimplePie;
import org.bstats.charts.SingleLineChart;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.hydr4.lilworlds.LilWorlds;
import org.hydr4.lilworlds.utils.LoggerUtils;

import java.util.HashMap;
import java.util.Map;

public class BStatsIntegration {
    
    private final LilWorlds plugin;
    private final Metrics metrics;
    private static final int PLUGIN_ID = 26160; // Replace with actual bStats plugin ID
    
    public BStatsIntegration(LilWorlds plugin) {
        this.plugin = plugin;
        
        if (!plugin.getConfigManager().isMetricsEnabled()) {
            LoggerUtils.info("Metrics are disabled in configuration");
            this.metrics = null;
            return;
        }
        
        this.metrics = new Metrics(plugin, PLUGIN_ID);
        setupCustomCharts();
        
        LoggerUtils.success("bStats metrics initialized with plugin ID: " + PLUGIN_ID);
    }
    
    private void setupCustomCharts() {
        if (metrics == null) return;
        
        // Chart: Total number of worlds
        metrics.addCustomChart(new SingleLineChart("total_worlds", () -> Bukkit.getWorlds().size()));
        
        // Chart: Managed worlds count
        metrics.addCustomChart(new SingleLineChart("managed_worlds", () -> 
            plugin.getWorldManager().getManagedWorlds().size()));
        
        // Chart: Custom generators count
        metrics.addCustomChart(new SingleLineChart("custom_generators", () -> 
            plugin.getGeneratorManager().getCustomGeneratorNames().size()));
        
        // Chart: World environments distribution
        metrics.addCustomChart(new AdvancedPie("world_environments", () -> {
            Map<String, Integer> environmentMap = new HashMap<>();
            
            for (World world : Bukkit.getWorlds()) {
                String environment = world.getEnvironment().name();
                environmentMap.put(environment, environmentMap.getOrDefault(environment, 0) + 1);
            }
            
            return environmentMap;
        }));
        
        // Chart: PlaceholderAPI integration status
        metrics.addCustomChart(new SimplePie("placeholderapi_integration", () -> 
            plugin.getPlaceholderAPIIntegration() != null ? "Enabled" : "Disabled"));
        
        // Chart: Debug mode status
        metrics.addCustomChart(new SimplePie("debug_mode", () -> 
            plugin.getConfigManager().isDebugEnabled() ? "Enabled" : "Disabled"));
        
        // Chart: Auto-load worlds status
        metrics.addCustomChart(new SimplePie("auto_load_worlds", () -> 
            plugin.getConfigManager().isAutoLoadEnabled() ? "Enabled" : "Disabled"));
        
        // Chart: Auto-save worlds status
        metrics.addCustomChart(new SimplePie("auto_save_worlds", () -> 
            plugin.getConfigManager().isAutoSaveEnabled() ? "Enabled" : "Disabled"));
        
        // Chart: Server version
        metrics.addCustomChart(new SimplePie("server_version", () -> {
            String version = Bukkit.getVersion();
            if (version.contains("1.21")) return "1.21.x";
            if (version.contains("1.20")) return "1.20.x";
            if (version.contains("1.19")) return "1.19.x";
            if (version.contains("1.18")) return "1.18.x";
            if (version.contains("1.17")) return "1.17.x";
            if (version.contains("1.16")) return "1.16.x";
            return "Other";
        }));
        
        // Chart: Java version
        metrics.addCustomChart(new SimplePie("java_version", () -> {
            String version = System.getProperty("java.version");
            if (version.startsWith("1.8")) return "Java 8";
            if (version.startsWith("11")) return "Java 11";
            if (version.startsWith("17")) return "Java 17";
            if (version.startsWith("21")) return "Java 21";
            return "Other";
        }));
        
        // Chart: Operating system
        metrics.addCustomChart(new SimplePie("operating_system", () -> {
            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("win")) return "Windows";
            if (os.contains("mac")) return "macOS";
            if (os.contains("linux")) return "Linux";
            if (os.contains("unix")) return "Unix";
            return "Other";
        }));
        
        // Chart: World count ranges
        metrics.addCustomChart(new SimplePie("world_count_range", () -> {
            int worldCount = Bukkit.getWorlds().size();
            if (worldCount <= 1) return "1 world";
            if (worldCount <= 5) return "2-5 worlds";
            if (worldCount <= 10) return "6-10 worlds";
            if (worldCount <= 20) return "11-20 worlds";
            if (worldCount <= 50) return "21-50 worlds";
            return "50+ worlds";
        }));
        
        // Chart: Universal spawn usage
        metrics.addCustomChart(new SimplePie("universal_spawn", () -> 
            plugin.getWorldManager().getUniversalSpawn() != null ? "Configured" : "Not configured"));
        
        // Chart: Configuration features usage
        metrics.addCustomChart(new AdvancedPie("config_features", () -> {
            Map<String, Integer> features = new HashMap<>();
            
            if (plugin.getConfigManager().isDebugEnabled()) {
                features.put("Debug Mode", 1);
            }
            if (plugin.getConfigManager().isAutoLoadEnabled()) {
                features.put("Auto Load", 1);
            }
            if (plugin.getConfigManager().isAutoSaveEnabled()) {
                features.put("Auto Save", 1);
            }
            if (plugin.getConfigManager().isMetricsEnabled()) {
                features.put("Metrics", 1);
            }
            if (plugin.getConfigManager().isPlaceholderAPIEnabled()) {
                features.put("PlaceholderAPI", 1);
            }
            
            return features;
        }));
        
        LoggerUtils.debug("Custom bStats charts configured");
    }
    
    /**
     * Get the metrics instance
     */
    public Metrics getMetrics() {
        return metrics;
    }
    
    /**
     * Check if metrics are enabled
     */
    public boolean isEnabled() {
        return metrics != null;
    }
    
    /**
     * Shutdown metrics
     */
    public void shutdown() {
        if (metrics != null) {
            metrics.shutdown();
            LoggerUtils.info("bStats metrics shutdown");
        }
    }
}