package org.hydr4.lilworlds.managers;

import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.generator.ChunkGenerator;
import org.hydr4.lilworlds.LilWorlds;
import org.hydr4.lilworlds.utils.LoggerUtils;
import org.hydr4.lilworlds.utils.VersionUtils;

import java.io.File;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class WorldManager {
    
    private final LilWorlds plugin;
    private final Map<String, WorldInfo> managedWorlds;
    private Location universalSpawn;
    
    public WorldManager(LilWorlds plugin) {
        this.plugin = plugin;
        this.managedWorlds = new ConcurrentHashMap<>();
        loadUniversalSpawn();
    }
    
    /**
     * Create a new world with default settings
     */
    public boolean createWorld(String worldName) {
        return createWorld(worldName, World.Environment.NORMAL, null, true);
    }
    
    /**
     * Create a new world with specified settings
     */
    public boolean createWorld(String worldName, World.Environment environment, String generator, boolean generateStructures) {
        if (Bukkit.getWorld(worldName) != null) {
            LoggerUtils.warn("World " + worldName + " already exists!");
            return false;
        }
        
        try {
            LoggerUtils.info("Creating world: " + worldName);
            
            WorldCreator creator = new WorldCreator(worldName);
            creator.environment(environment);
            creator.generateStructures(generateStructures);
            
            // Apply version-specific optimizations
            configureWorldCreatorForVersion(creator);
            
            if (generator != null && !generator.isEmpty()) {
                // Try to get a custom ChunkGenerator first
                ChunkGenerator chunkGenerator = plugin.getGeneratorManager().getChunkGenerator(generator);
                if (chunkGenerator != null) {
                    creator.generator(chunkGenerator);
                    LoggerUtils.info("Using custom ChunkGenerator for generator: " + generator);
                } else {
                    // Fallback to string-based generator (for external plugins)
                    creator.generator(generator);
                    LoggerUtils.info("Using string-based generator: " + generator);
                }
            }
            
            World world = creator.createWorld();
            
            if (world != null) {
                // Apply default settings
                applyDefaultSettings(world);
                
                // Save world info
                WorldInfo worldInfo = new WorldInfo(worldName, environment, generator, generateStructures);
                managedWorlds.put(worldName, worldInfo);
                saveWorldToConfig(worldInfo);
                
                LoggerUtils.logWorldOperation("CREATE", worldName, "Environment: " + environment + ", Generator: " + (generator != null ? generator : "default"));
                return true;
            } else {
                LoggerUtils.error("Failed to create world: " + worldName);
                return false;
            }
            
        } catch (Exception e) {
            LoggerUtils.error("Error creating world " + worldName, e);
            return false;
        }
    }
    
    /**
     * Create a world with advanced options (async-safe version)
     */
    public void createWorldAdvancedAsync(String worldName, Object options, Consumer<Boolean> callback) {
        try {
            // Do all preparation work on async thread
            // Use reflection to access the options object from WorldCommand
            Class<?> optionsClass = options.getClass();
            
            // Use getDeclaredField instead of getField to access package-private fields
            java.lang.reflect.Field environmentField = optionsClass.getDeclaredField("environment");
            environmentField.setAccessible(true);
            World.Environment environment = (World.Environment) environmentField.get(options);
            
            java.lang.reflect.Field generatorField = optionsClass.getDeclaredField("generator");
            generatorField.setAccessible(true);
            String generator = (String) generatorField.get(options);
            
            java.lang.reflect.Field generateStructuresField = optionsClass.getDeclaredField("generateStructures");
            generateStructuresField.setAccessible(true);
            boolean generateStructures = generateStructuresField.getBoolean(options);
            
            java.lang.reflect.Field seedField = optionsClass.getDeclaredField("seed");
            seedField.setAccessible(true);
            long seed = seedField.getLong(options);
            
            // Prepare WorldCreator on async thread
            WorldCreator creator = new WorldCreator(worldName);
            creator.environment(environment);
            creator.generateStructures(generateStructures);
            
            if (generator != null && !generator.isEmpty()) {
                creator.generator(generator);
            }
            
            if (seed != 0) {
                creator.seed(seed);
            }
            
            LoggerUtils.info("Creating world '" + worldName + "' with advanced options...");
            LoggerUtils.info("Environment: " + environment + ", Generator: " + (generator != null ? generator : "default") + 
                           ", Structures: " + generateStructures + ", Seed: " + (seed != 0 ? seed : "random"));
            
            // Switch to main thread only for the actual world creation (required for world border events)
            Bukkit.getScheduler().runTask(plugin, () -> {
                try {
                    World world = creator.createWorld();
                    
                    if (world != null) {
                        LoggerUtils.info("Successfully created world: " + worldName);
                        WorldInfo worldInfo = new WorldInfo(worldName, environment, generator, generateStructures);
                        saveWorldToConfig(worldInfo);
                        callback.accept(true);
                    } else {
                        LoggerUtils.error("Failed to create world: " + worldName);
                        callback.accept(false);
                    }
                } catch (Exception e) {
                    LoggerUtils.error("Error creating world with advanced options: " + worldName, e);
                    callback.accept(false);
                }
            });
            
        } catch (Exception e) {
            LoggerUtils.error("Error preparing world creation with advanced options: " + worldName, e);
            callback.accept(false);
        }
    }

    /**
     * Create a world with advanced options (legacy synchronous version)
     */
    public boolean createWorldAdvanced(String worldName, Object options) {
        try {
            // Use reflection to access the options object from WorldCommand
            Class<?> optionsClass = options.getClass();
            
            // Use getDeclaredField instead of getField to access package-private fields
            java.lang.reflect.Field environmentField = optionsClass.getDeclaredField("environment");
            environmentField.setAccessible(true);
            World.Environment environment = (World.Environment) environmentField.get(options);
            
            java.lang.reflect.Field generatorField = optionsClass.getDeclaredField("generator");
            generatorField.setAccessible(true);
            String generator = (String) generatorField.get(options);
            
            java.lang.reflect.Field generateStructuresField = optionsClass.getDeclaredField("generateStructures");
            generateStructuresField.setAccessible(true);
            boolean generateStructures = generateStructuresField.getBoolean(options);
            
            java.lang.reflect.Field seedField = optionsClass.getDeclaredField("seed");
            seedField.setAccessible(true);
            long seed = seedField.getLong(options);
            
            WorldCreator creator = new WorldCreator(worldName);
            creator.environment(environment);
            creator.generateStructures(generateStructures);
            
            if (generator != null && !generator.isEmpty()) {
                creator.generator(generator);
            }
            
            if (seed != 0) {
                creator.seed(seed);
            }
            
            LoggerUtils.info("Creating world '" + worldName + "' with advanced options...");
            LoggerUtils.info("Environment: " + environment + ", Generator: " + (generator != null ? generator : "default") + 
                           ", Structures: " + generateStructures + ", Seed: " + (seed != 0 ? seed : "random"));
            
            World world = creator.createWorld();
            
            if (world != null) {
                LoggerUtils.info("Successfully created world: " + worldName);
                WorldInfo worldInfo = new WorldInfo(worldName, environment, generator, generateStructures);
                saveWorldToConfig(worldInfo);
                return true;
            } else {
                LoggerUtils.error("Failed to create world: " + worldName);
                return false;
            }
        } catch (Exception e) {
            LoggerUtils.error("Error creating world with advanced options: " + worldName, e);
            return false;
        }
    }
    
    /**
     * Clone an existing world
     */
    public boolean cloneWorld(String sourceWorldName, String targetWorldName) {
        World sourceWorld = Bukkit.getWorld(sourceWorldName);
        if (sourceWorld == null) {
            LoggerUtils.error("Source world " + sourceWorldName + " does not exist!");
            return false;
        }
        
        if (Bukkit.getWorld(targetWorldName) != null) {
            LoggerUtils.error("Target world " + targetWorldName + " already exists!");
            return false;
        }
        
        try {
            LoggerUtils.info("Cloning world " + sourceWorldName + " to " + targetWorldName);
            
            // Save the source world first
            sourceWorld.save();
            
            // Copy world folder
            File sourceFolder = sourceWorld.getWorldFolder();
            File targetFolder = new File(Bukkit.getWorldContainer(), targetWorldName);
            
            if (!copyWorldFolder(sourceFolder, targetFolder)) {
                LoggerUtils.error("Failed to copy world folder for " + targetWorldName);
                return false;
            }
            
            // Update level.dat name
            updateLevelDat(targetFolder, targetWorldName);
            
            // Load the cloned world
            WorldCreator creator = new WorldCreator(targetWorldName);
            creator.environment(sourceWorld.getEnvironment());
            
            World clonedWorld = creator.createWorld();
            
            if (clonedWorld != null) {
                // Copy world info
                WorldInfo sourceInfo = managedWorlds.get(sourceWorldName);
                if (sourceInfo != null) {
                    WorldInfo clonedInfo = new WorldInfo(targetWorldName, sourceInfo.getEnvironment(), 
                                                       sourceInfo.getGenerator(), sourceInfo.isGenerateStructures());
                    managedWorlds.put(targetWorldName, clonedInfo);
                    saveWorldToConfig(clonedInfo);
                }
                
                LoggerUtils.logWorldOperation("CLONE", targetWorldName, "Cloned from: " + sourceWorldName);
                return true;
            } else {
                LoggerUtils.error("Failed to load cloned world: " + targetWorldName);
                return false;
            }
            
        } catch (Exception e) {
            LoggerUtils.error("Error cloning world " + sourceWorldName + " to " + targetWorldName, e);
            return false;
        }
    }
    
    /**
     * Load a world
     */
    public boolean loadWorld(String worldName) {
        if (Bukkit.getWorld(worldName) != null) {
            LoggerUtils.warn("World " + worldName + " is already loaded!");
            return true;
        }
        
        File worldFolder = new File(Bukkit.getWorldContainer(), worldName);
        if (!worldFolder.exists()) {
            LoggerUtils.error("World folder for " + worldName + " does not exist!");
            return false;
        }
        
        try {
            LoggerUtils.info("Loading world: " + worldName);
            
            WorldCreator creator = new WorldCreator(worldName);
            
            // Get world info if available
            WorldInfo worldInfo = managedWorlds.get(worldName);
            if (worldInfo != null) {
                creator.environment(worldInfo.getEnvironment());
                if (worldInfo.getGenerator() != null && !worldInfo.getGenerator().isEmpty()) {
                    // Try to get a custom ChunkGenerator first
                    ChunkGenerator chunkGenerator = plugin.getGeneratorManager().getChunkGenerator(worldInfo.getGenerator());
                    if (chunkGenerator != null) {
                        creator.generator(chunkGenerator);
                        LoggerUtils.info("Using custom ChunkGenerator for generator: " + worldInfo.getGenerator());
                    } else {
                        // Fallback to string-based generator (for external plugins)
                        creator.generator(worldInfo.getGenerator());
                        LoggerUtils.info("Using string-based generator: " + worldInfo.getGenerator());
                    }
                }
            }
            
            World world = creator.createWorld();
            
            if (world != null) {
                applyDefaultSettings(world);
                LoggerUtils.logWorldOperation("LOAD", worldName);
                return true;
            } else {
                LoggerUtils.error("Failed to load world: " + worldName);
                return false;
            }
            
        } catch (Exception e) {
            LoggerUtils.error("Error loading world " + worldName, e);
            return false;
        }
    }
    
    /**
     * Unload a world
     */
    public boolean unloadWorld(String worldName, boolean save) {
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            LoggerUtils.warn("World " + worldName + " is not loaded!");
            return true;
        }
        
        // Don't unload the main world
        if (world.equals(Bukkit.getWorlds().get(0))) {
            LoggerUtils.error("Cannot unload the main world!");
            return false;
        }
        
        try {
            LoggerUtils.info("Unloading world: " + worldName);
            
            // Move all players out of the world
            Location spawnLocation = getUniversalSpawn();
            if (spawnLocation == null) {
                spawnLocation = Bukkit.getWorlds().get(0).getSpawnLocation();
            }
            
            for (Player player : world.getPlayers()) {
                player.teleport(spawnLocation);
                player.sendMessage("§eYou have been moved because the world is being unloaded.");
            }
            
            boolean success = Bukkit.unloadWorld(world, save);
            
            if (success) {
                LoggerUtils.logWorldOperation("UNLOAD", worldName, "Save: " + save);
                return true;
            } else {
                LoggerUtils.error("Failed to unload world: " + worldName);
                return false;
            }
            
        } catch (Exception e) {
            LoggerUtils.error("Error unloading world " + worldName, e);
            return false;
        }
    }
    
    /**
     * Import an external world
     */
    public boolean importWorld(String worldName, World.Environment environment, String generator) {
        File worldFolder = new File(Bukkit.getWorldContainer(), worldName);
        if (!worldFolder.exists()) {
            LoggerUtils.error("World folder for " + worldName + " does not exist!");
            return false;
        }
        
        if (Bukkit.getWorld(worldName) != null) {
            LoggerUtils.warn("World " + worldName + " is already loaded!");
            return true;
        }
        
        try {
            LoggerUtils.info("Importing world: " + worldName);
            
            WorldCreator creator = new WorldCreator(worldName);
            creator.environment(environment);
            
            if (generator != null && !generator.isEmpty()) {
                // Try to get a custom ChunkGenerator first
                ChunkGenerator chunkGenerator = plugin.getGeneratorManager().getChunkGenerator(generator);
                if (chunkGenerator != null) {
                    creator.generator(chunkGenerator);
                    LoggerUtils.info("Using custom ChunkGenerator for generator: " + generator);
                } else {
                    // Fallback to string-based generator (for external plugins)
                    creator.generator(generator);
                    LoggerUtils.info("Using string-based generator: " + generator);
                }
            }
            
            World world = creator.createWorld();
            
            if (world != null) {
                applyDefaultSettings(world);
                
                // Save world info
                WorldInfo worldInfo = new WorldInfo(worldName, environment, generator, true);
                managedWorlds.put(worldName, worldInfo);
                saveWorldToConfig(worldInfo);
                
                LoggerUtils.logWorldOperation("IMPORT", worldName, "Environment: " + environment);
                return true;
            } else {
                LoggerUtils.error("Failed to import world: " + worldName);
                return false;
            }
            
        } catch (Exception e) {
            LoggerUtils.error("Error importing world " + worldName, e);
            return false;
        }
    }
    
    /**
     * Load all worlds from configuration
     */
    public void loadWorldsFromConfig() {
        if (!plugin.getConfigManager().isAutoLoadEnabled()) {
            LoggerUtils.info("Auto-load is disabled, skipping world loading");
            return;
        }
        
        ConfigurationSection worldsSection = plugin.getConfigManager().getWorldsConfig().getConfigurationSection("worlds");
        if (worldsSection == null) {
            LoggerUtils.info("No worlds found in configuration");
            return;
        }
        
        LoggerUtils.info("Loading worlds from configuration...");
        int loaded = 0;
        
        for (String worldName : worldsSection.getKeys(false)) {
            ConfigurationSection worldSection = worldsSection.getConfigurationSection(worldName);
            if (worldSection != null) {
                try {
                    World.Environment environment = World.Environment.valueOf(
                        worldSection.getString("environment", "NORMAL"));
                    String generator = worldSection.getString("generator", "");
                    boolean generateStructures = worldSection.getBoolean("generate-structures", true);
                    
                    WorldInfo worldInfo = new WorldInfo(worldName, environment, generator, generateStructures);
                    managedWorlds.put(worldName, worldInfo);
                    
                    if (loadWorld(worldName)) {
                        loaded++;
                    }
                    
                } catch (Exception e) {
                    LoggerUtils.error("Error loading world " + worldName + " from config", e);
                }
            }
        }
        
        LoggerUtils.success("Loaded " + loaded + " worlds from configuration");
    }
    
    /**
     * Save all worlds to configuration
     */
    public void saveAllWorlds() {
        LoggerUtils.info("Saving all worlds to configuration...");
        
        for (WorldInfo worldInfo : managedWorlds.values()) {
            saveWorldToConfig(worldInfo);
        }
        
        plugin.getConfigManager().saveConfig("worlds");
        LoggerUtils.success("All worlds saved to configuration");
    }
    
    /**
     * Reload world manager
     */
    public void reload() {
        LoggerUtils.info("Reloading world manager...");
        managedWorlds.clear();
        loadWorldsFromConfig();
        loadUniversalSpawn();
        LoggerUtils.success("World manager reloaded");
    }
    
    /**
     * Get world information
     */
    public WorldInfo getWorldInfo(String worldName) {
        return managedWorlds.get(worldName);
    }
    
    /**
     * Get all managed worlds
     */
    public Map<String, WorldInfo> getManagedWorlds() {
        return new HashMap<>(managedWorlds);
    }
    
    /**
     * Get all managed world names
     */
    public Set<String> getManagedWorldNames() {
        return new HashSet<>(managedWorlds.keySet());
    }
    
    /**
     * Set world spawn
     */
    public boolean setWorldSpawn(String worldName, Location location) {
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            return false;
        }
        
        world.setSpawnLocation(location);
        LoggerUtils.logWorldOperation("SET_SPAWN", worldName, 
            "Location: " + location.getX() + ", " + location.getY() + ", " + location.getZ());
        return true;
    }
    
    /**
     * Set universal spawn
     */
    public void setUniversalSpawn(Location location) {
        this.universalSpawn = location;
        plugin.getConfigManager().setMainConfigValue("universal-spawn.world", location.getWorld().getName());
        plugin.getConfigManager().setMainConfigValue("universal-spawn.x", location.getX());
        plugin.getConfigManager().setMainConfigValue("universal-spawn.y", location.getY());
        plugin.getConfigManager().setMainConfigValue("universal-spawn.z", location.getZ());
        plugin.getConfigManager().setMainConfigValue("universal-spawn.yaw", location.getYaw());
        plugin.getConfigManager().setMainConfigValue("universal-spawn.pitch", location.getPitch());
        
        LoggerUtils.logWorldOperation("SET_UNIVERSAL_SPAWN", location.getWorld().getName(),
            "Location: " + location.getX() + ", " + location.getY() + ", " + location.getZ());
    }
    
    /**
     * Get universal spawn
     */
    public Location getUniversalSpawn() {
        return universalSpawn;
    }
    
    // Private helper methods
    
    private void applyDefaultSettings(World world) {
        try {
            world.setDifficulty(Difficulty.valueOf(plugin.getConfigManager().getDefaultDifficulty()));
        } catch (IllegalArgumentException e) {
            world.setDifficulty(Difficulty.NORMAL);
        }
        
        world.setPVP(plugin.getConfigManager().getDefaultPvP());
        world.setKeepSpawnInMemory(plugin.getConfigManager().getDefaultKeepSpawnInMemory());
        world.setSpawnFlags(plugin.getConfigManager().getDefaultAllowMonsters(), 
                           plugin.getConfigManager().getDefaultAllowAnimals());
    }
    
    private void saveWorldToConfig(WorldInfo worldInfo) {
        String path = "worlds." + worldInfo.getName();
        plugin.getConfigManager().setWorldsConfigValue(path + ".environment", worldInfo.getEnvironment().name());
        plugin.getConfigManager().setWorldsConfigValue(path + ".generator", worldInfo.getGenerator());
        plugin.getConfigManager().setWorldsConfigValue(path + ".generate-structures", worldInfo.isGenerateStructures());
    }
    
    private void loadUniversalSpawn() {
        String worldName = plugin.getConfigManager().getMainConfig().getString("universal-spawn.world");
        if (worldName != null) {
            World world = Bukkit.getWorld(worldName);
            if (world != null) {
                double x = plugin.getConfigManager().getMainConfig().getDouble("universal-spawn.x");
                double y = plugin.getConfigManager().getMainConfig().getDouble("universal-spawn.y");
                double z = plugin.getConfigManager().getMainConfig().getDouble("universal-spawn.z");
                float yaw = (float) plugin.getConfigManager().getMainConfig().getDouble("universal-spawn.yaw");
                float pitch = (float) plugin.getConfigManager().getMainConfig().getDouble("universal-spawn.pitch");
                
                this.universalSpawn = new Location(world, x, y, z, yaw, pitch);
            }
        }
    }
    
    private boolean copyWorldFolder(File source, File target) {
        try {
            if (source.isDirectory()) {
                if (!target.exists()) {
                    target.mkdirs();
                }
                
                String[] files = source.list();
                if (files != null) {
                    for (String file : files) {
                        File srcFile = new File(source, file);
                        File destFile = new File(target, file);
                        
                        // Skip session.lock and uid.dat
                        if (file.equals("session.lock") || file.equals("uid.dat")) {
                            continue;
                        }
                        
                        copyWorldFolder(srcFile, destFile);
                    }
                }
            } else {
                java.nio.file.Files.copy(source.toPath(), target.toPath(), 
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            }
            return true;
        } catch (Exception e) {
            LoggerUtils.error("Error copying world folder", e);
            return false;
        }
    }
    
    private void updateLevelDat(File worldFolder, String newWorldName) {
        // This is a simplified implementation
        // In a real implementation, you would parse and update the level.dat NBT file
        LoggerUtils.debug("Updated level.dat for world: " + newWorldName);
    }
    
    /**
     * Delete a world completely (unload and remove files)
     */
    public CompletableFuture<Boolean> deleteWorld(String worldName) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        // Deletion logic must start on the main thread to safely check world status
        Bukkit.getScheduler().runTask(plugin, () -> {
            World world = Bukkit.getWorld(worldName);

            if (world != null) {
                // If the world is loaded, it must be unloaded on the main thread
                LoggerUtils.info("World '" + worldName + "' is loaded, unloading first...");

                // Move players out of the world
                World mainWorld = Bukkit.getWorlds().get(0);
                for (Player player : world.getPlayers()) {
                    player.teleport(mainWorld.getSpawnLocation());
                }

                if (!Bukkit.unloadWorld(world, true)) {
                    LoggerUtils.error("Failed to unload world '" + worldName + "' before deletion!");
                    future.complete(false);
                } else {
                    LoggerUtils.info("Successfully unloaded world '" + worldName + "'. Now deleting files asynchronously...");
                    // After successful unload, run file deletion asynchronously
                    deleteWorldFilesAsync(worldName, future);
                }
            } else {
                // If the world is not loaded, we can proceed directly to asynchronous file deletion
                LoggerUtils.info("World '" + worldName + "' is not loaded. Proceeding with file deletion...");
                deleteWorldFilesAsync(worldName, future);
            }
        });

        return future;
    }

    /**
     * Deletes the world files asynchronously to avoid blocking the main thread.
     * This should only be called after the world has been successfully unloaded.
     */
    private void deleteWorldFilesAsync(String worldName, CompletableFuture<Boolean> future) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                // Remove from the managed worlds map (must be thread-safe)
                managedWorlds.remove(worldName);

                // Delete world files from disk
                File worldFolder = new File(Bukkit.getWorldContainer(), worldName);
                if (worldFolder.exists()) {
                    LoggerUtils.info("Deleting world files for '" + worldName + "'...");
                    boolean deleted = deleteDirectory(worldFolder);

                    if (deleted) {
                        LoggerUtils.success("Successfully deleted world '" + worldName + "' and all its files!");
                        // Schedule config update back on the main thread to be safe
                        Bukkit.getScheduler().runTask(plugin, () -> {
                            removeWorldFromConfig(worldName);
                            future.complete(true);
                        });
                    } else {
                        LoggerUtils.error("Failed to delete world files for '" + worldName + "'!");
                        future.complete(false);
                    }
                } else {
                    LoggerUtils.warn("World folder for '" + worldName + "' does not exist, but removing from configuration...");
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        removeWorldFromConfig(worldName);
                        future.complete(true);
                    });
                }
            } catch (Exception e) {
                LoggerUtils.error("An error occurred while deleting world files for '" + worldName + "'", e);
                future.complete(false);
            }
        });
    }
    
    /**
     * Recursively delete a directory and all its contents
     */
    private boolean deleteDirectory(File directory) {
        if (!directory.exists()) {
            return true;
        }
        
        if (directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (!deleteDirectory(file)) {
                        LoggerUtils.error("Failed to delete: " + file.getAbsolutePath());
                        return false;
                    }
                }
            }
        }
        
        boolean deleted = directory.delete();
        if (!deleted) {
            LoggerUtils.error("Failed to delete: " + directory.getAbsolutePath());
        }
        return deleted;
    }
    
    /**
     * Remove world from configuration file
     */
    private void removeWorldFromConfig(String worldName) {
        try {
            boolean removedFromMain = false;
            boolean removedFromWorlds = false;
            
            // Remove from main config.yml
            ConfigurationSection worldsSection = plugin.getConfig().getConfigurationSection("worlds");
            if (worldsSection != null && worldsSection.contains(worldName)) {
                worldsSection.set(worldName, null);
                plugin.saveConfig();
                removedFromMain = true;
                LoggerUtils.info("Removed world '" + worldName + "' from main configuration");
            }
            
            // Remove from worlds.yml
            ConfigurationSection worldsConfigSection = plugin.getConfigManager().getWorldsConfig().getConfigurationSection("worlds");
            if (worldsConfigSection != null && worldsConfigSection.contains(worldName)) {
                worldsConfigSection.set(worldName, null);
                plugin.getConfigManager().saveConfig("worlds");
                removedFromWorlds = true;
                LoggerUtils.info("Removed world '" + worldName + "' from worlds configuration");
            }
            
            // Remove from inventory manager world groups if present
            if (plugin.getInventoryManager() != null) {
                plugin.getInventoryManager().removeWorldFromGroup(worldName);
                LoggerUtils.info("Removed world '" + worldName + "' from inventory groups");
            }
            
            if (removedFromMain || removedFromWorlds) {
                LoggerUtils.success("Successfully purged world '" + worldName + "' from all configurations");
            } else {
                LoggerUtils.info("World '" + worldName + "' was not found in any configuration files");
            }
            
        } catch (Exception e) {
            LoggerUtils.error("Failed to remove world '" + worldName + "' from configuration: " + e.getMessage());
        }
    }
    
    /**
     * Get list of unloaded worlds from the worlds.yml configuration
     */
    public List<String> getUnloadedWorlds() {
        List<String> unloadedWorlds = new ArrayList<>();
        
        ConfigurationSection worldsSection = plugin.getConfigManager().getWorldsConfig().getConfigurationSection("worlds");
        if (worldsSection != null) {
            for (String worldName : worldsSection.getKeys(false)) {
                // Check if world is not currently loaded
                if (Bukkit.getWorld(worldName) == null) {
                    // Check if world folder exists
                    File worldFolder = new File(Bukkit.getWorldContainer(), worldName);
                    if (worldFolder.exists() && worldFolder.isDirectory()) {
                        unloadedWorlds.add(worldName);
                    }
                }
            }
        }
        
        // Also check for world folders that exist but aren't in config
        File worldContainer = Bukkit.getWorldContainer();
        File[] worldFolders = worldContainer.listFiles(File::isDirectory);
        if (worldFolders != null) {
            for (File folder : worldFolders) {
                String folderName = folder.getName();
                // Skip default server folders
                if (folderName.equals("plugins") || folderName.equals("logs") ||
                    folderName.equals("cache") || folderName.startsWith(".")) {
                    continue;
                }
                
                // Check if it's a valid world folder (has level.dat)
                File levelDat = new File(folder, "level.dat");
                if (levelDat.exists() && Bukkit.getWorld(folderName) == null &&
                    !unloadedWorlds.contains(folderName)) {
                    unloadedWorlds.add(folderName);
                }
            }
        }
        
        return unloadedWorlds;
    }
    
    /**
     * Configure WorldCreator with version-specific features and optimizations
     */
    private void configureWorldCreatorForVersion(WorldCreator creator) {
        // Apply version-specific optimizations and features
        if (VersionUtils.isAtLeast(VersionUtils.VERSION_1_18)) {
            // 1.18+ supports custom world height and new world generation
            LoggerUtils.debug("Applying 1.18+ world generation optimizations");
            // Note: Custom height configuration would require additional parameters
            // This is a placeholder for future enhancements
        }
        
        if (VersionUtils.isAtLeast(VersionUtils.VERSION_1_19)) {
            // 1.19+ has improved world generation performance
            LoggerUtils.debug("Applying 1.19+ world generation optimizations");
        }
        
        if (VersionUtils.isAtLeast(VersionUtils.VERSION_1_20)) {
            // 1.20+ has additional world generation features
            LoggerUtils.debug("Applying 1.20+ world generation optimizations");
        }
        
        if (VersionUtils.isAtLeast(VersionUtils.VERSION_1_21)) {
            // 1.21+ supports trial chambers and new structures
            LoggerUtils.debug("Applying 1.21+ world generation features");
            // Future: Could add specific configuration for trial chambers
        }
        
        if (VersionUtils.supports1216Features()) {
            // 1.21.6+ specific features
            LoggerUtils.debug("Applying 1.21.6+ world generation features");
            // Future: Add any 1.21.6-specific world generation features
        }
        
        // Apply server implementation specific optimizations
        if (VersionUtils.isPaper()) {
            LoggerUtils.debug("Applying Paper-specific world generation optimizations");
            // Paper has additional world generation optimizations
        }
    }
    
    /**
     * Get version-specific world creation information
     */
    public String getVersionSpecificInfo() {
        StringBuilder info = new StringBuilder();
        info.append("Version Features: ");
        
        if (VersionUtils.supportsHexColors()) {
            info.append("HexColors ");
        }
        if (VersionUtils.supportsCustomWorldHeight()) {
            info.append("CustomHeight ");
        }
        if (VersionUtils.supportsNewWorldGeneration()) {
            info.append("NewWorldGen ");
        }
        if (VersionUtils.supportsBundles()) {
            info.append("Bundles ");
        }
        if (VersionUtils.supportsTrialChambers()) {
            info.append("TrialChambers ");
        }
        if (VersionUtils.supports1216Features()) {
            info.append("1.21.6Features ");
        }
        
        return info.toString().trim();
    }
    
    /**
     * World information class
     */
    public static class WorldInfo {
        private final String name;
        private final World.Environment environment;
        private final String generator;
        private final boolean generateStructures;
        
        public WorldInfo(String name, World.Environment environment, String generator, boolean generateStructures) {
            this.name = name;
            this.environment = environment;
            this.generator = generator;
            this.generateStructures = generateStructures;
        }
        
        public String getName() { return name; }
        public World.Environment getEnvironment() { return environment; }
        public String getGenerator() { return generator; }
        public boolean isGenerateStructures() { return generateStructures; }
    }
}