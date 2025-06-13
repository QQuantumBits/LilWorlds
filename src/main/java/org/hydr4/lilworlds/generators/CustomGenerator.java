package org.hydr4.lilworlds.generators;

import java.util.*;

public class CustomGenerator {
    
    private final String name;
    private final String displayName;
    private final String description;
    private final String type;
    
    private final List<String> biomes;
    private final List<String> layers;
    private final Map<String, OreSettings> ores;
    private final Map<String, Object> settings;
    
    // Structure generation settings
    private boolean generateStructures = true;
    private boolean generateVillages = true;
    private boolean generateStrongholds = true;
    private boolean generateMineshafts = true;
    private boolean generateTemples = true;
    private boolean generateRavines = true;
    private boolean generateCaves = true;
    private boolean generateDungeons = true;
    
    public CustomGenerator(String name, String displayName, String description, String type) {
        this.name = name;
        this.displayName = displayName;
        this.description = description;
        this.type = type;
        this.biomes = new ArrayList<>();
        this.layers = new ArrayList<>();
        this.ores = new HashMap<>();
        this.settings = new HashMap<>();
    }
    
    // Getters
    public String getName() { return name; }
    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
    public String getType() { return type; }
    public List<String> getBiomes() { return new ArrayList<>(biomes); }
    public List<String> getLayers() { return new ArrayList<>(layers); }
    public Map<String, OreSettings> getOres() { return new HashMap<>(ores); }
    public Map<String, Object> getSettings() { return new HashMap<>(settings); }
    
    // Structure generation getters
    public boolean isGenerateStructures() { return generateStructures; }
    public boolean isGenerateVillages() { return generateVillages; }
    public boolean isGenerateStrongholds() { return generateStrongholds; }
    public boolean isGenerateMineshafts() { return generateMineshafts; }
    public boolean isGenerateTemples() { return generateTemples; }
    public boolean isGenerateRavines() { return generateRavines; }
    public boolean isGenerateCaves() { return generateCaves; }
    public boolean isGenerateDungeons() { return generateDungeons; }
    
    // Structure generation setters
    public void setGenerateStructures(boolean generateStructures) { this.generateStructures = generateStructures; }
    public void setGenerateVillages(boolean generateVillages) { this.generateVillages = generateVillages; }
    public void setGenerateStrongholds(boolean generateStrongholds) { this.generateStrongholds = generateStrongholds; }
    public void setGenerateMineshafts(boolean generateMineshafts) { this.generateMineshafts = generateMineshafts; }
    public void setGenerateTemples(boolean generateTemples) { this.generateTemples = generateTemples; }
    public void setGenerateRavines(boolean generateRavines) { this.generateRavines = generateRavines; }
    public void setGenerateCaves(boolean generateCaves) { this.generateCaves = generateCaves; }
    public void setGenerateDungeons(boolean generateDungeons) { this.generateDungeons = generateDungeons; }
    
    // Biome management
    public void addBiome(String biome) {
        if (!biomes.contains(biome)) {
            biomes.add(biome);
        }
    }
    
    public void removeBiome(String biome) {
        biomes.remove(biome);
    }
    
    public boolean hasBiome(String biome) {
        return biomes.contains(biome);
    }
    
    // Layer management (for flat worlds)
    public void addLayer(String layer) {
        layers.add(layer);
    }
    
    public void removeLayer(int index) {
        if (index >= 0 && index < layers.size()) {
            layers.remove(index);
        }
    }
    
    public void insertLayer(int index, String layer) {
        if (index >= 0 && index <= layers.size()) {
            layers.add(index, layer);
        }
    }
    
    // Ore management
    public void addOre(String ore, int frequency, int minHeight, int maxHeight) {
        ores.put(ore, new OreSettings(frequency, minHeight, maxHeight));
    }
    
    public void removeOre(String ore) {
        ores.remove(ore);
    }
    
    public OreSettings getOreSettings(String ore) {
        return ores.get(ore);
    }
    
    // Settings management
    public void addSetting(String key, Object value) {
        settings.put(key, value);
    }
    
    public void removeSetting(String key) {
        settings.remove(key);
    }
    
    public Object getSetting(String key) {
        return settings.get(key);
    }
    
    public Object getSetting(String key, Object defaultValue) {
        return settings.getOrDefault(key, defaultValue);
    }
    
    /**
     * Generate the generator string for world creation
     */
    public String generateGeneratorString() {
        StringBuilder generator = new StringBuilder();
        
        if (type.equalsIgnoreCase("FLAT")) {
            generator.append("minecraft:flat;");
            
            // Add layers
            if (!layers.isEmpty()) {
                for (int i = 0; i < layers.size(); i++) {
                    if (i > 0) generator.append(",");
                    generator.append(layers.get(i));
                }
            } else {
                // Default flat world layers
                generator.append("minecraft:bedrock,2*minecraft:dirt,minecraft:grass_block");
            }
            
            generator.append(";");
            
            // Add biome
            if (!biomes.isEmpty()) {
                generator.append(biomes.get(0).toLowerCase());
            } else {
                generator.append("plains");
            }
            
            // Add structures
            if (generateStructures) {
                generator.append(";");
                List<String> structures = new ArrayList<>();
                if (generateVillages) structures.add("village");
                if (generateStrongholds) structures.add("stronghold");
                if (generateMineshafts) structures.add("mineshaft");
                if (generateTemples) structures.add("temple");
                if (generateDungeons) structures.add("dungeon");
                
                if (!structures.isEmpty()) {
                    generator.append(String.join(",", structures));
                }
            }
            
        } else if (type.equalsIgnoreCase("AMPLIFIED")) {
            generator.append("minecraft:amplified");
        } else if (type.equalsIgnoreCase("LARGE_BIOMES")) {
            generator.append("minecraft:large_biomes");
        } else {
            // Default or custom generator
            generator.append("minecraft:default");
        }
        
        return generator.toString();
    }
    
    /**
     * Get a formatted description of the generator
     */
    public String getFormattedDescription() {
        StringBuilder desc = new StringBuilder();
        desc.append("§b").append(displayName).append("\n");
        desc.append("§7").append(description).append("\n");
        desc.append("§7Type: §f").append(type).append("\n");
        
        if (!biomes.isEmpty()) {
            desc.append("§7Biomes: §f").append(String.join(", ", biomes)).append("\n");
        }
        
        if (!layers.isEmpty()) {
            desc.append("§7Layers: §f").append(layers.size()).append(" layers\n");
        }
        
        if (!ores.isEmpty()) {
            desc.append("§7Ores: §f").append(ores.size()).append(" configured\n");
        }
        
        desc.append("§7Structures: §f").append(generateStructures ? "Enabled" : "Disabled");
        
        return desc.toString();
    }
    
    @Override
    public String toString() {
        return "CustomGenerator{" +
                "name='" + name + '\'' +
                ", displayName='" + displayName + '\'' +
                ", type='" + type + '\'' +
                ", biomes=" + biomes.size() +
                ", layers=" + layers.size() +
                ", ores=" + ores.size() +
                '}';
    }
    
    /**
     * Ore settings class
     */
    public static class OreSettings {
        private final int frequency;
        private final int minHeight;
        private final int maxHeight;
        
        public OreSettings(int frequency, int minHeight, int maxHeight) {
            this.frequency = frequency;
            this.minHeight = minHeight;
            this.maxHeight = maxHeight;
        }
        
        public int getFrequency() { return frequency; }
        public int getMinHeight() { return minHeight; }
        public int getMaxHeight() { return maxHeight; }
        
        @Override
        public String toString() {
            return "OreSettings{" +
                    "frequency=" + frequency +
                    ", minHeight=" + minHeight +
                    ", maxHeight=" + maxHeight +
                    '}';
        }
    }
}