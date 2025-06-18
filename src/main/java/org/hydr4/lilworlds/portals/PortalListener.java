package org.hydr4.lilworlds.portals;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.hydr4.lilworlds.LilWorlds;
import org.hydr4.lilworlds.api.events.WorldTeleportEvent;

/**
 * Handles portal-related events
 */
public class PortalListener implements Listener {
    
    private final LilWorlds plugin;
    private final PortalManager portalManager;
    
    public PortalListener(LilWorlds plugin, PortalManager portalManager) {
        this.plugin = plugin;
        this.portalManager = portalManager;
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerMove(PlayerMoveEvent event) {
        if (event.isCancelled()) return;
        
        Player player = event.getPlayer();
        Location to = event.getTo();
        
        if (to == null) return;
        
        // Check if player moved into a portal
        Portal portal = portalManager.getPortalAtLocation(to);
        if (portal != null) {
            // Check if player is on cooldown
            if (portalManager.isOnCooldown(player)) {
                long remaining = portalManager.getRemainingCooldown(player);
                if (remaining > 0) {
                    return; // Don't spam messages
                }
            }
            
            // Fire custom teleport event
            org.bukkit.World destWorld = org.bukkit.Bukkit.getWorld(portal.getDestinationWorld());
            if (destWorld != null) {
                WorldTeleportEvent teleportEvent = new WorldTeleportEvent(player, to.getWorld(), destWorld, portal.getDestinationLocation());
                plugin.getServer().getPluginManager().callEvent(teleportEvent);
                
                if (teleportEvent.isCancelled()) {
                    return;
                }
            }
            
            // Attempt teleportation
            boolean success = portalManager.teleportPlayer(player, portal);
            if (success) {
                player.sendMessage(ChatColor.GREEN + "Teleported through portal: " + ChatColor.YELLOW + portal.getName());
            } else {
                player.sendMessage(ChatColor.RED + "Failed to use portal: " + portal.getName());
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerPortal(PlayerPortalEvent event) {
        if (event.isCancelled()) return;
        
        Player player = event.getPlayer();
        Location from = event.getFrom();
        
        // Check if this is one of our custom portals
        Portal portal = portalManager.getPortalAtLocation(from);
        if (portal != null) {
            // Cancel the default portal behavior
            event.setCancelled(true);
            
            // Handle with our portal system instead
            boolean success = portalManager.teleportPlayer(player, portal);
            if (success) {
                player.sendMessage(ChatColor.GREEN + "Teleported through portal: " + ChatColor.YELLOW + portal.getName());
            } else {
                player.sendMessage(ChatColor.RED + "Failed to use portal: " + portal.getName());
            }
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        if (event.isCancelled()) return;
        
        // Log portal usage for debugging
        if (event.getCause() == PlayerTeleportEvent.TeleportCause.PLUGIN) {
            Player player = event.getPlayer();
            Location from = event.getFrom();
            Portal portal = portalManager.getPortalAtLocation(from);
            
            if (portal != null) {
                plugin.getLogger().info("Player " + player.getName() + " teleported via portal: " + portal.getName());
            }
        }
    }
}