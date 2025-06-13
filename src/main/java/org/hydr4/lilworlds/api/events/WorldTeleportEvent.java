package org.hydr4.lilworlds.api.events;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

/**
 * Event fired when a player is about to be teleported to a world via LilWorlds
 * 
 * @author LilWorlds Team
 * @version 1.4.0
 * @since 1.4.0
 */
public class WorldTeleportEvent extends LilWorldsEvent implements Cancellable {
    
    private static final HandlerList handlers = new HandlerList();
    
    private final Player player;
    private final World fromWorld;
    private final World toWorld;
    private Location toLocation;
    private boolean cancelled = false;
    private String cancelReason = null;
    
    /**
     * Create a new WorldTeleportEvent
     * 
     * @param player The player being teleported
     * @param fromWorld The world the player is coming from
     * @param toWorld The world the player is going to
     * @param toLocation The location the player will be teleported to
     */
    public WorldTeleportEvent(Player player, World fromWorld, World toWorld, Location toLocation) {
        this.player = player;
        this.fromWorld = fromWorld;
        this.toWorld = toWorld;
        this.toLocation = toLocation;
    }
    
    /**
     * Get the player being teleported
     * 
     * @return The player
     */
    public Player getPlayer() {
        return player;
    }
    
    /**
     * Get the world the player is coming from
     * 
     * @return The from world
     */
    public World getFromWorld() {
        return fromWorld;
    }
    
    /**
     * Get the world the player is going to
     * 
     * @return The to world
     */
    public World getToWorld() {
        return toWorld;
    }
    
    /**
     * Get the location the player will be teleported to
     * 
     * @return The to location
     */
    public Location getToLocation() {
        return toLocation;
    }
    
    /**
     * Set the location the player will be teleported to
     * 
     * @param toLocation The new to location
     */
    public void setToLocation(Location toLocation) {
        this.toLocation = toLocation;
    }
    
    /**
     * Get the reason for cancellation
     * 
     * @return The cancel reason, or null if not cancelled
     */
    public String getCancelReason() {
        return cancelReason;
    }
    
    /**
     * Set the cancellation reason
     * 
     * @param reason The reason for cancellation
     */
    public void setCancelReason(String reason) {
        this.cancelReason = reason;
    }
    
    @Override
    public boolean isCancelled() {
        return cancelled;
    }
    
    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
    
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
    
    public static HandlerList getHandlerList() {
        return handlers;
    }
}