package org.hydr4.lilworlds.api.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Base class for all LilWorlds events
 * 
 * @author LilWorlds Team
 * @version 1.4.0
 * @since 1.4.0
 */
public abstract class LilWorldsEvent extends Event {
    
    private static final HandlerList handlers = new HandlerList();
    
    /**
     * Create a new LilWorldsEvent
     */
    public LilWorldsEvent() {
        super();
    }
    
    /**
     * Create a new LilWorldsEvent
     * 
     * @param isAsync Whether the event is asynchronous
     */
    public LilWorldsEvent(boolean isAsync) {
        super(isAsync);
    }
    
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
    
    public static HandlerList getHandlerList() {
        return handlers;
    }
}