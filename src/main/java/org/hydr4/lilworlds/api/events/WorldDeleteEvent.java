package org.hydr4.lilworlds.api.events;

import org.bukkit.command.CommandSender;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

/**
 * Event fired when a world is about to be deleted
 * 
 * @author LilWorlds Team
 * @version 1.4.0
 * @since 1.4.0
 */
public class WorldDeleteEvent extends LilWorldsEvent implements Cancellable {
    
    private static final HandlerList handlers = new HandlerList();
    
    private final String worldName;
    private final CommandSender deleter;
    private boolean cancelled = false;
    private String cancelReason = null;
    
    /**
     * Create a new WorldDeleteEvent
     * 
     * @param worldName The name of the world being deleted
     * @param deleter The command sender deleting the world
     */
    public WorldDeleteEvent(String worldName, CommandSender deleter) {
        this.worldName = worldName;
        this.deleter = deleter;
    }
    
    /**
     * Get the name of the world being deleted
     * 
     * @return The world name
     */
    public String getWorldName() {
        return worldName;
    }
    
    /**
     * Get the command sender deleting the world
     * 
     * @return The deleter
     */
    public CommandSender getDeleter() {
        return deleter;
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