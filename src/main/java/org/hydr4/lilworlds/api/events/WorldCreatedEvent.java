package org.hydr4.lilworlds.api.events;

import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.event.HandlerList;

/**
 * Event fired when a world has been successfully created
 * 
 * @author LilWorlds Team
 * @version 1.4.0
 * @since 1.4.0
 */
public class WorldCreatedEvent extends LilWorldsEvent {
    
    private static final HandlerList handlers = new HandlerList();
    
    private final World world;
    private final CommandSender creator;
    private final long creationTime;
    
    /**
     * Create a new WorldCreatedEvent
     * 
     * @param world The world that was created
     * @param creator The command sender who created the world
     * @param creationTime The time taken to create the world in milliseconds
     */
    public WorldCreatedEvent(World world, CommandSender creator, long creationTime) {
        this.world = world;
        this.creator = creator;
        this.creationTime = creationTime;
    }
    
    /**
     * Get the world that was created
     * 
     * @return The created world
     */
    public World getWorld() {
        return world;
    }
    
    /**
     * Get the command sender who created the world
     * 
     * @return The creator
     */
    public CommandSender getCreator() {
        return creator;
    }
    
    /**
     * Get the time taken to create the world
     * 
     * @return The creation time in milliseconds
     */
    public long getCreationTime() {
        return creationTime;
    }
    
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
    
    public static HandlerList getHandlerList() {
        return handlers;
    }
}