package org.hydr4.lilworlds.api.events;

import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

/**
 * Event fired when a world is about to be created
 * 
 * @author LilWorlds Team
 * @version 1.4.0
 * @since 1.4.0
 */
public class WorldCreateEvent extends LilWorldsEvent implements Cancellable {
    
    private static final HandlerList handlers = new HandlerList();
    
    private final String worldName;
    private final World.Environment environment;
    private final String generator;
    private final CommandSender creator;
    private boolean cancelled = false;
    private String cancelReason = null;
    
    /**
     * Create a new WorldCreateEvent
     * 
     * @param worldName The name of the world being created
     * @param environment The world environment
     * @param generator The generator being used (null for default)
     * @param creator The command sender creating the world
     */
    public WorldCreateEvent(String worldName, World.Environment environment, String generator, CommandSender creator) {
        this.worldName = worldName;
        this.environment = environment;
        this.generator = generator;
        this.creator = creator;
    }
    
    /**
     * Get the name of the world being created
     * 
     * @return The world name
     */
    public String getWorldName() {
        return worldName;
    }
    
    /**
     * Get the environment of the world being created
     * 
     * @return The world environment
     */
    public World.Environment getEnvironment() {
        return environment;
    }
    
    /**
     * Get the generator being used
     * 
     * @return The generator name, or null for default
     */
    public String getGenerator() {
        return generator;
    }
    
    /**
     * Get the command sender creating the world
     * 
     * @return The creator
     */
    public CommandSender getCreator() {
        return creator;
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