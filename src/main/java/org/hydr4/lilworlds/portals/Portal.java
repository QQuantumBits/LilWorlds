package org.hydr4.lilworlds.portals;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a portal between worlds
 */
public class Portal implements ConfigurationSerializable {
    
    private final String name;
    private final Location location1;
    private final Location location2;
    private final String destinationWorld;
    private final Location destinationLocation;
    private final Material frameMaterial;
    private final boolean enabled;
    private final PortalType type;
    
    public enum PortalType {
        NETHER_PORTAL,
        END_PORTAL,
        CUSTOM
    }
    
    public Portal(String name, Location location1, Location location2, String destinationWorld, 
                  Location destinationLocation, Material frameMaterial, PortalType type) {
        this.name = name;
        this.location1 = location1;
        this.location2 = location2;
        this.destinationWorld = destinationWorld;
        this.destinationLocation = destinationLocation;
        this.frameMaterial = frameMaterial != null ? frameMaterial : Material.OBSIDIAN;
        this.type = type != null ? type : PortalType.CUSTOM;
        this.enabled = true;
    }
    
    public Portal(String name, Location location1, Location location2, String destinationWorld, 
                  Location destinationLocation, Material frameMaterial, PortalType type, boolean enabled) {
        this.name = name;
        this.location1 = location1;
        this.location2 = location2;
        this.destinationWorld = destinationWorld;
        this.destinationLocation = destinationLocation;
        this.frameMaterial = frameMaterial != null ? frameMaterial : Material.OBSIDIAN;
        this.type = type != null ? type : PortalType.CUSTOM;
        this.enabled = enabled;
    }
    
    // Getters
    public String getName() { return name; }
    public Location getLocation1() { return location1; }
    public Location getLocation2() { return location2; }
    public String getDestinationWorld() { return destinationWorld; }
    public Location getDestinationLocation() { return destinationLocation; }
    public Material getFrameMaterial() { return frameMaterial; }
    public PortalType getType() { return type; }
    public boolean isEnabled() { return enabled; }
    
    /**
     * Check if a location is within this portal
     */
    public boolean isInPortal(Location location) {
        if (!location.getWorld().equals(location1.getWorld())) {
            return false;
        }
        
        double minX = Math.min(location1.getX(), location2.getX());
        double maxX = Math.max(location1.getX(), location2.getX());
        double minY = Math.min(location1.getY(), location2.getY());
        double maxY = Math.max(location1.getY(), location2.getY());
        double minZ = Math.min(location1.getZ(), location2.getZ());
        double maxZ = Math.max(location1.getZ(), location2.getZ());
        
        return location.getX() >= minX && location.getX() <= maxX &&
               location.getY() >= minY && location.getY() <= maxY &&
               location.getZ() >= minZ && location.getZ() <= maxZ;
    }
    
    /**
     * Get the center location of the portal
     */
    public Location getCenterLocation() {
        double centerX = (location1.getX() + location2.getX()) / 2;
        double centerY = (location1.getY() + location2.getY()) / 2;
        double centerZ = (location1.getZ() + location2.getZ()) / 2;
        return new Location(location1.getWorld(), centerX, centerY, centerZ);
    }
    
    /**
     * Get the size of the portal
     */
    public int[] getSize() {
        int width = Math.abs((int)(location2.getX() - location1.getX())) + 1;
        int height = Math.abs((int)(location2.getY() - location1.getY())) + 1;
        int depth = Math.abs((int)(location2.getZ() - location1.getZ())) + 1;
        return new int[]{width, height, depth};
    }
    
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("name", name);
        map.put("location1", location1.serialize());
        map.put("location2", location2.serialize());
        map.put("destinationWorld", destinationWorld);
        map.put("destinationLocation", destinationLocation.serialize());
        map.put("frameMaterial", frameMaterial.name());
        map.put("type", type.name());
        map.put("enabled", enabled);
        return map;
    }
    
    @SuppressWarnings("unchecked")
    public static Portal deserialize(Map<String, Object> map) {
        String name = (String) map.get("name");
        Location location1 = Location.deserialize((Map<String, Object>) map.get("location1"));
        Location location2 = Location.deserialize((Map<String, Object>) map.get("location2"));
        String destinationWorld = (String) map.get("destinationWorld");
        Location destinationLocation = Location.deserialize((Map<String, Object>) map.get("destinationLocation"));
        Material frameMaterial = Material.valueOf((String) map.get("frameMaterial"));
        PortalType type = PortalType.valueOf((String) map.get("type"));
        boolean enabled = (Boolean) map.getOrDefault("enabled", true);
        
        return new Portal(name, location1, location2, destinationWorld, destinationLocation, frameMaterial, type, enabled);
    }
    
    @Override
    public String toString() {
        return "Portal{name='" + name + "', destination='" + destinationWorld + "', type=" + type + ", enabled=" + enabled + "}";
    }
}