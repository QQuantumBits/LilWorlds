package org.hydr4.lilworlds.generators;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.generator.ChunkGenerator;
import org.hydr4.lilworlds.LilWorlds;
import org.hydr4.lilworlds.utils.LoggerUtils;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class GeneratorManager {
    
    private final LilWorlds plugin;
    private final Map<String, CustomGenerator> customGenerators;
    private final File generatorsFolder;
    
    public GeneratorManager(LilWorlds plugin) {
        this.plugin = plugin;
        this.customGenerators = new HashMap<>();
        this.generatorsFolder = new File(plugin.getDataFolder(), "generators");
        
        initializeGenerators();
    }
    
    private void initializeGenerators() {
        // Create generators folder if it doesn't exist
        if (!generatorsFolder.exists()) {
            generatorsFolder.mkdirs();
            copyExampleGenerators();
        }
        
        loadCustomGenerators();
    }
    
    /**
     * Load all custom generators from the generators folder
     */
    public void loadCustomGenerators() {
        customGenerators.clear();
        
        if (!generatorsFolder.exists()) {
            LoggerUtils.info("Generators folder does not exist, skipping custom generator loading");
            return;
        }
        
        File[] generatorFiles = generatorsFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (generatorFiles == null || generatorFiles.length == 0) {
            LoggerUtils.info("No custom generators found");
            return;
        }
        
        LoggerUtils.info("Loading custom generators...");
        int loaded = 0;
        
        for (File generatorFile : generatorFiles) {
            try {
                YamlConfiguration config = YamlConfiguration.loadConfiguration(generatorFile);
                String generatorName = generatorFile.getName().replace(".yml", "");
                
                CustomGenerator generator = loadGeneratorFromConfig(generatorName, config);
                if (generator != null) {
                    customGenerators.put(generatorName, generator);
                    loaded++;
                    LoggerUtils.debug("Loaded custom generator: " + generatorName);
                }
                
            } catch (Exception e) {
                LoggerUtils.error("Error loading generator from " + generatorFile.getName(), e);
            }
        }
        
        LoggerUtils.success("Loaded " + loaded + " custom generators");
    }
    
    /**
     * Load a generator from configuration
     */
    private CustomGenerator loadGeneratorFromConfig(String name, YamlConfiguration config) {
        try {
            String displayName = config.getString("display-name", name);
            String description = config.getString("description", "Custom generator");
            String type = config.getString("type", "FLAT");
            
            CustomGenerator generator = new CustomGenerator(name, displayName, description, type);
            
            // Load biome settings
            if (config.contains("biomes")) {
                for (String biome : config.getStringList("biomes")) {
                    generator.addBiome(biome);
                }
            }
            
            // Load structure settings
            generator.setGenerateStructures(config.getBoolean("generate-structures", true));
            generator.setGenerateVillages(config.getBoolean("generate-villages", true));
            generator.setGenerateStrongholds(config.getBoolean("generate-strongholds", true));
            generator.setGenerateMineshafts(config.getBoolean("generate-mineshafts", true));
            generator.setGenerateTemples(config.getBoolean("generate-temples", true));
            generator.setGenerateRavines(config.getBoolean("generate-ravines", true));
            generator.setGenerateCaves(config.getBoolean("generate-caves", true));
            generator.setGenerateDungeons(config.getBoolean("generate-dungeons", true));
            
            // Load ore settings
            if (config.contains("ores")) {
                for (String ore : config.getConfigurationSection("ores").getKeys(false)) {
                    int frequency = config.getInt("ores." + ore + ".frequency", 10);
                    int minHeight = config.getInt("ores." + ore + ".min-height", 1);
                    int maxHeight = config.getInt("ores." + ore + ".max-height", 64);
                    generator.addOre(ore, frequency, minHeight, maxHeight);
                }
            }
            
            // Load layer settings for flat worlds
            if (type.equalsIgnoreCase("FLAT") && config.contains("layers")) {
                for (String layer : config.getStringList("layers")) {
                    generator.addLayer(layer);
                }
            }
            
            // Load custom settings
            if (config.contains("settings")) {
                for (String key : config.getConfigurationSection("settings").getKeys(false)) {
                    Object value = config.get("settings." + key);
                    generator.addSetting(key, value);
                }
            }
            
            return generator;
            
        } catch (Exception e) {
            LoggerUtils.error("Error parsing generator configuration for " + name, e);
            return null;
        }
    }
    
    /**
     * Get a custom generator by name
     */
    public CustomGenerator getCustomGenerator(String name) {
        return customGenerators.get(name);
    }
    
    /**
     * Get all custom generator names
     */
    public Set<String> getCustomGeneratorNames() {
        return customGenerators.keySet();
    }
    
    /**
     * Check if a generator exists
     */
    public boolean hasGenerator(String name) {
        if (name == null || name.isEmpty()) {
            return false;
        }
        
        // Check if it's a custom generator
        if (customGenerators.containsKey(name)) {
            return true;
        }
        
        // Check if it's a built-in generator (common ones)
        String lowerName = name.toLowerCase();
        return lowerName.equals("flat") || lowerName.equals("superflat") || 
               lowerName.equals("amplified") || lowerName.equals("large_biomes") ||
               lowerName.equals("default") || lowerName.equals("normal");
    }
    
    /**
     * Get a ChunkGenerator instance for the specified generator
     */
    public ChunkGenerator getChunkGenerator(String generatorName) {
        if (generatorName == null || generatorName.isEmpty()) {
            return null;
        }
        
        // Check for built-in generators first
        if (generatorName.equalsIgnoreCase("void")) {
            CustomGenerator config = customGenerators.get("void");
            if (config != null) {
                return VoidGenerator.fromConfig(config);
            } else {
                // Return default void generator if no config found
                LoggerUtils.info("Using default void generator (no config found)");
                return new VoidGenerator();
            }
        }
        
        // Check for other custom generators
        CustomGenerator generator = customGenerators.get(generatorName);
        if (generator != null) {
            // For now, we only support void generators
            // In the future, this could be expanded to support other generator types
            if (generator.getType().equalsIgnoreCase("VOID")) {
                return VoidGenerator.fromConfig(generator);
            } else {
                LoggerUtils.warn("Generator type '" + generator.getType() + "' is not yet supported");
                return null;
            }
        }
        
        LoggerUtils.warn("Generator '" + generatorName + "' not found");
        return null;
    }
    
    /**
     * Reload all custom generators
     */
    public void reload() {
        LoggerUtils.info("Reloading custom generators...");
        loadCustomGenerators();
        LoggerUtils.success("Custom generators reloaded");
    }
    
    /**
     * Copy example generators from resources
     */
    private void copyExampleGenerators() {
        try {
            // Copy example.yml
            if (plugin.getResource("generators/example.yml") != null) {
                plugin.saveResource("generators/example.yml", false);
                LoggerUtils.info("Copied example generator from resources");
            }
            
            // Copy superflat.yml
            if (plugin.getResource("generators/superflat.yml") != null) {
                plugin.saveResource("generators/superflat.yml", false);
                LoggerUtils.info("Copied superflat generator from resources");
            }
            
            // Copy void.yml
            if (plugin.getResource("generators/void.yml") != null) {
                plugin.saveResource("generators/void.yml", false);
                LoggerUtils.info("Copied void generator from resources");
            }
            
        } catch (Exception e) {
            LoggerUtils.error("Error copying example generators from resources", e);
            // Fallback to creating example generator manually
            createExampleGeneratorFallback();
        }
    }
    
    /**
     * Fallback method to create an example generator if resource copying fails
     */
    private void createExampleGeneratorFallback() {
        try {
            File exampleFile = new File(generatorsFolder, "example.yml");
            YamlConfiguration config = new YamlConfiguration();
            
            config.set("display-name", "Example Generator");
            config.set("description", "An example custom world generator");
            config.set("type", "FLAT");
            
            config.set("generate-structures", true);
            config.set("generate-villages", true);
            config.set("generate-strongholds", false);
            config.set("generate-mineshafts", true);
            config.set("generate-temples", true);
            config.set("generate-ravines", false);
            config.set("generate-caves", false);
            config.set("generate-dungeons", true);
            
            config.set("biomes", java.util.Arrays.asList("PLAINS", "FOREST", "DESERT"));
            
            config.set("layers", java.util.Arrays.asList(
                "minecraft:bedrock",
                "minecraft:stone:5",
                "minecraft:dirt:3",
                "minecraft:grass_block"
            ));
            
            config.set("ores.coal.frequency", 20);
            config.set("ores.coal.min-height", 1);
            config.set("ores.coal.max-height", 128);
            
            config.set("ores.iron.frequency", 15);
            config.set("ores.iron.min-height", 1);
            config.set("ores.iron.max-height", 64);
            
            config.set("ores.gold.frequency", 5);
            config.set("ores.gold.min-height", 1);
            config.set("ores.gold.max-height", 32);
            
            config.set("ores.diamond.frequency", 2);
            config.set("ores.diamond.min-height", 1);
            config.set("ores.diamond.max-height", 16);
            
            config.set("settings.sea-level", 63);
            config.set("settings.spawn-x", 0);
            config.set("settings.spawn-z", 0);
            
            config.save(exampleFile);
            LoggerUtils.info("Created fallback example generator configuration");
            
        } catch (Exception e) {
            LoggerUtils.error("Error creating fallback example generator", e);
        }
    }
    
    /**
     * Save a custom generator to file
     */
    public boolean saveGenerator(CustomGenerator generator) {
        try {
            File generatorFile = new File(generatorsFolder, generator.getName() + ".yml");
            YamlConfiguration config = new YamlConfiguration();
            
            config.set("display-name", generator.getDisplayName());
            config.set("description", generator.getDescription());
            config.set("type", generator.getType());
            
            config.set("generate-structures", generator.isGenerateStructures());
            config.set("generate-villages", generator.isGenerateVillages());
            config.set("generate-strongholds", generator.isGenerateStrongholds());
            config.set("generate-mineshafts", generator.isGenerateMineshafts());
            config.set("generate-temples", generator.isGenerateTemples());
            config.set("generate-ravines", generator.isGenerateRavines());
            config.set("generate-caves", generator.isGenerateCaves());
            config.set("generate-dungeons", generator.isGenerateDungeons());
            
            if (!generator.getBiomes().isEmpty()) {
                config.set("biomes", generator.getBiomes());
            }
            
            if (!generator.getLayers().isEmpty()) {
                config.set("layers", generator.getLayers());
            }
            
            // Save ore settings
            for (Map.Entry<String, CustomGenerator.OreSettings> entry : generator.getOres().entrySet()) {
                String ore = entry.getKey();
                CustomGenerator.OreSettings settings = entry.getValue();
                config.set("ores." + ore + ".frequency", settings.getFrequency());
                config.set("ores." + ore + ".min-height", settings.getMinHeight());
                config.set("ores." + ore + ".max-height", settings.getMaxHeight());
            }
            
            // Save custom settings
            for (Map.Entry<String, Object> entry : generator.getSettings().entrySet()) {
                config.set("settings." + entry.getKey(), entry.getValue());
            }
            
            config.save(generatorFile);
            LoggerUtils.info("Saved generator: " + generator.getName());
            return true;
            
        } catch (Exception e) {
            LoggerUtils.error("Error saving generator " + generator.getName(), e);
            return false;
        }
    }
    
    /**
     * Delete a custom generator
     */
    public boolean deleteGenerator(String name) {
        try {
            File generatorFile = new File(generatorsFolder, name + ".yml");
            if (generatorFile.exists()) {
                boolean deleted = generatorFile.delete();
                if (deleted) {
                    customGenerators.remove(name);
                    LoggerUtils.info("Deleted generator: " + name);
                    return true;
                } else {
                    LoggerUtils.error("Failed to delete generator file: " + name);
                    return false;
                }
            } else {
                LoggerUtils.warn("Generator file does not exist: " + name);
                return false;
            }
        } catch (Exception e) {
            LoggerUtils.error("Error deleting generator " + name, e);
            return false;
        }
    }
    

    
    /**
     * Get all available generator names
     */
    public Set<String> getAvailableGenerators() {
        return customGenerators.keySet();
    }
}