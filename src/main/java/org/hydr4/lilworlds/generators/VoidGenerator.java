package org.hydr4.lilworlds.generators;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.WorldInfo;
import org.hydr4.lilworlds.utils.LoggerUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Void World Generator
 * Creates a completely empty void world with only a single bedrock block at spawn
 */
public class VoidGenerator extends ChunkGenerator {
    
    private final boolean spawnPlatform;
    private final Material platformMaterial;
    private final int platformSize;
    private final int platformHeight;
    private final Biome worldBiome;
    
    public VoidGenerator() {
        this(true, Material.BEDROCK, 1, 64, Biome.PLAINS);
    }
    
    public VoidGenerator(boolean spawnPlatform, Material platformMaterial, int platformSize, int platformHeight, Biome worldBiome) {
        this.spawnPlatform = spawnPlatform;
        this.platformMaterial = platformMaterial;
        this.platformSize = platformSize;
        this.platformHeight = platformHeight;
        this.worldBiome = worldBiome;
        
        LoggerUtils.info("VoidGenerator initialized with platform: " + spawnPlatform + 
                        ", material: " + platformMaterial + 
                        ", size: " + platformSize + "x" + platformSize + 
                        ", height: " + platformHeight);
    }
    
    @Override
    public void generateNoise(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, ChunkData chunkData) {
        // Generate completely empty chunks except for spawn platform
        if (spawnPlatform && chunkX == 0 && chunkZ == 0) {
            // This is the spawn chunk, place the bedrock platform
            generateSpawnPlatform(chunkData);
        }
        // All other chunks remain completely empty (void)
    }
    
    @Override
    public void generateSurface(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, ChunkData chunkData) {
        // No surface generation needed for void world
    }
    
    @Override
    public void generateBedrock(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, ChunkData chunkData) {
        // No bedrock generation except for our custom spawn platform
    }
    
    @Override
    public void generateCaves(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, ChunkData chunkData) {
        // No caves in void world
    }
    
    @Override
    public BiomeProvider getDefaultBiomeProvider(WorldInfo worldInfo) {
        return new VoidBiomeProvider(worldBiome);
    }
    
    @Override
    public List<BlockPopulator> getDefaultPopulators(World world) {
        // No populators for void world
        return Collections.emptyList();
    }
    
    @Override
    public Location getFixedSpawnLocation(World world, Random random) {
        if (spawnPlatform) {
            // Spawn on top of the bedrock platform
            return new Location(world, 0.5, platformHeight + 1, 0.5);
        } else {
            // Spawn in the void at a safe height
            return new Location(world, 0.5, 128, 0.5);
        }
    }
    
    @Override
    public boolean shouldGenerateNoise() {
        return true;
    }
    
    @Override
    public boolean shouldGenerateSurface() {
        return false;
    }
    
    @Override
    public boolean shouldGenerateBedrock() {
        return false;
    }
    
    @Override
    public boolean shouldGenerateCaves() {
        return false;
    }
    
    @Override
    public boolean shouldGenerateDecorations() {
        return false;
    }
    
    @Override
    public boolean shouldGenerateMobs() {
        return false;
    }
    
    @Override
    public boolean shouldGenerateStructures() {
        return false;
    }
    
    /**
     * Generate the spawn platform in the spawn chunk
     */
    private void generateSpawnPlatform(ChunkData chunkData) {
        int centerX = 8; // Center of chunk (0-15, so 8 is middle)
        int centerZ = 8; // Center of chunk
        
        // Calculate platform bounds
        int halfSize = platformSize / 2;
        int startX = Math.max(0, centerX - halfSize);
        int endX = Math.min(15, centerX + halfSize);
        int startZ = Math.max(0, centerZ - halfSize);
        int endZ = Math.min(15, centerZ + halfSize);
        
        // Place platform blocks
        for (int x = startX; x <= endX; x++) {
            for (int z = startZ; z <= endZ; z++) {
                chunkData.setBlock(x, platformHeight, z, platformMaterial);
            }
        }
        
        LoggerUtils.debug("Generated void spawn platform at chunk (0,0) with " + 
                         platformMaterial + " from (" + startX + "," + startZ + ") to (" + 
                         endX + "," + endZ + ") at height " + platformHeight);
    }
    
    /**
     * Custom BiomeProvider for void worlds
     */
    private static class VoidBiomeProvider extends BiomeProvider {
        private final Biome biome;
        
        public VoidBiomeProvider(Biome biome) {
            this.biome = biome;
        }
        
        @Override
        public Biome getBiome(WorldInfo worldInfo, int x, int y, int z) {
            return biome;
        }
        
        @Override
        public List<Biome> getBiomes(WorldInfo worldInfo) {
            return Collections.singletonList(biome);
        }
    }
    
    /**
     * Create a VoidGenerator from configuration
     */
    public static VoidGenerator fromConfig(CustomGenerator config) {
        boolean spawnPlatform = true;
        Material platformMaterial = Material.BEDROCK;
        int platformSize = 1;
        int platformHeight = 64;
        Biome worldBiome = Biome.PLAINS;
        
        try {
            // Parse spawn platform settings
            if (config.getSettings().containsKey("spawn-platform")) {
                @SuppressWarnings("unchecked")
                Map<String, Object> platformSettings = (Map<String, Object>) config.getSettings().get("spawn-platform");
                
                spawnPlatform = (Boolean) platformSettings.getOrDefault("enabled", true);
                
                String materialName = (String) platformSettings.getOrDefault("material", "BEDROCK");
                try {
                    platformMaterial = Material.valueOf(materialName.toUpperCase());
                } catch (IllegalArgumentException e) {
                    LoggerUtils.warn("Invalid platform material '" + materialName + "', using BEDROCK");
                    platformMaterial = Material.BEDROCK;
                }
                
                platformSize = (Integer) platformSettings.getOrDefault("size", 1);
                platformHeight = (Integer) platformSettings.getOrDefault("height", 64);
            }
            
            // Parse biome setting
            if (config.getSettings().containsKey("biome")) {
                String biomeName = (String) config.getSettings().get("biome");
                try {
                    worldBiome = Biome.valueOf(biomeName.toUpperCase());
                } catch (IllegalArgumentException e) {
                    LoggerUtils.warn("Invalid biome '" + biomeName + "', using PLAINS");
                    worldBiome = Biome.PLAINS;
                }
            }
            
        } catch (Exception e) {
            LoggerUtils.error("Error parsing void generator config: " + e.getMessage());
            LoggerUtils.warn("Using default void generator settings");
        }
        
        return new VoidGenerator(spawnPlatform, platformMaterial, platformSize, platformHeight, worldBiome);
    }
}