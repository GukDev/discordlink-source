package dev.guk.discordlink.listeners;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import dev.guk.discordlink.DiscordLink;
import dev.guk.discordlink.utils.ColorUtils;

public class PlayerListener implements Listener {
    private final DiscordLink plugin;
    private final List<String> ALLOWED_COMMANDS = Arrays.asList("/2fa", "/login", "/register", "/verify");

    public PlayerListener(DiscordLink plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        // Let the VerificationFreezeManager handle player join
        plugin.getVerificationFreezeManager().handlePlayerJoin(player);
        
        // Send verification reminder if not verified
        if (!plugin.getVerificationManager().isVerified(player.getUniqueId())) {
            String message = plugin.getConfig().getString("messages.minecraft.not-verified",
                    "§7Please verify your account using §f/verify");
            player.sendMessage(ColorUtils.translate(plugin.getConfig().getString("settings.prefix", "") + message));
        } 
        // Send 2FA code if verification is required
        else if (plugin.getConfig().getBoolean("two_factor_auth.enabled", false)) {
            plugin.getTwoFactorManager().sendTwoFactorCode(player);
        }
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        // Clean up 2FA data when player leaves
        plugin.getTwoFactorManager().removePlayer(player.getUniqueId());
        // Clean up verification freeze data
        plugin.getVerificationFreezeManager().removePlayer(player.getUniqueId());
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        
        // Check if the player is frozen for verification
        if (plugin.getVerificationFreezeManager().isFrozen(playerId)) {
            // If positions (x,y,z) changed, cancel the event
            // Head movement is handled in the VerificationFreezeManager
            if (event.getFrom().getX() != event.getTo().getX() || 
                event.getFrom().getY() != event.getTo().getY() || 
                event.getFrom().getZ() != event.getTo().getZ()) {
                event.setCancelled(true);
            }
            return;
        }
        
        // Handle 2FA freeze
        if (!plugin.getConfig().getBoolean("two_factor_auth.enabled", false) || 
            !plugin.getConfig().getBoolean("two_factor_auth.freeze_until_verified", true)) {
            return;
        }
        
        // Skip if player is not verified with Discord at all
        if (!plugin.getVerificationManager().isVerified(playerId)) {
            return;
        }
        
        // Prevent moving if 2FA is required but not completed
        if (!plugin.getTwoFactorManager().isVerified(playerId)) {
            // Allow small head movements but prevent walking
            if (event.getFrom().getBlockX() != event.getTo().getBlockX() || 
                event.getFrom().getBlockY() != event.getTo().getBlockY() || 
                event.getFrom().getBlockZ() != event.getTo().getBlockZ()) {
                event.setCancelled(true);
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        String command = event.getMessage().split(" ")[0].toLowerCase();
        
        // Check if player is frozen for verification
        if (plugin.getVerificationFreezeManager().isFrozen(playerId)) {
            // Get the list of allowed commands from config
            List<String> allowedCommands = plugin.getConfig().getStringList("verification.freeze.allowed_commands");
            if (allowedCommands.isEmpty()) {
                allowedCommands = ALLOWED_COMMANDS; // Use default if not configured
            }
            
            // Allow only specified commands if configured
            if (!plugin.getConfig().getBoolean("verification.freeze.allow_basic_commands", true) || 
                !allowedCommands.contains(command.substring(1))) {  // Remove the / from the command
                
                event.setCancelled(true);
                String message = plugin.getConfig().getString("messages.minecraft.verification-command-blocked",
                        "&c❌ Please verify your Discord account first!\n&7Use &f/verify &7to get started.");
                player.sendMessage(ColorUtils.translate(plugin.getConfig().getString("settings.prefix", "") + message));
                return;
            }
        }
        
        // Handle 2FA command blocking
        if (!plugin.getConfig().getBoolean("two_factor_auth.enabled", false) || 
            !plugin.getConfig().getBoolean("two_factor_auth.freeze_until_verified", true)) {
            return;
        }
        
        // Skip if player is not verified with Discord at all
        if (!plugin.getVerificationManager().isVerified(playerId)) {
            return;
        }
        
        // Block commands if 2FA is required but not completed
        if (!plugin.getTwoFactorManager().isVerified(playerId)) {
            // Allow only specific commands
            if (!ALLOWED_COMMANDS.contains(command)) {
                event.setCancelled(true);
                String message = plugin.getConfig().getString("messages.minecraft.two-factor-blocked",
                        "&c❌ You need to complete 2FA verification first!");
                player.sendMessage(ColorUtils.translate(plugin.getConfig().getString("settings.prefix", "") + message));
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        
        // First, check for verification freeze
        if (plugin.getVerificationFreezeManager().isFrozen(playerId)) {
            event.setCancelled(true);
            return;
        }
        
        // Next, check for 2FA restriction
        if (plugin.getConfig().getBoolean("two_factor_auth.enabled", false) && 
            plugin.getConfig().getBoolean("two_factor_auth.freeze_until_verified", true) &&
            plugin.getVerificationManager().isVerified(playerId) &&
            !plugin.getTwoFactorManager().isVerified(playerId)) {
            event.setCancelled(true);
            return;
        }
        
        // Then, check for server selector restriction
        if (plugin.getConfig().getBoolean("server_selector.block_unverified", false) &&
            !plugin.getVerificationManager().isVerified(playerId) &&
            event.hasItem()) {
            
            // Check if the item matches the configured server selector
            String material = plugin.getConfig().getString("server_selector.item_material", "COMPASS");
            String itemName = plugin.getConfig().getString("server_selector.item_name", "");
            
            if (event.getItem().getType().toString().equals(material)) {
                // If no name is specified, or name matches, block the interaction
                if (itemName.isEmpty() || 
                    (event.getItem().hasItemMeta() && 
                     event.getItem().getItemMeta().hasDisplayName() && 
                     event.getItem().getItemMeta().getDisplayName().contains(itemName))) {
                    
                    event.setCancelled(true);
                    player.sendMessage(ColorUtils.translate("&c❌ You need to verify your Discord account to use the server selector!"));
                    player.sendMessage(ColorUtils.translate("&7Use &f/verify &7to link your account."));
                }
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        
        // First, check for verification freeze
        if (plugin.getVerificationFreezeManager().isFrozen(playerId)) {
            event.setCancelled(true);
            return;
        }
        
        // Then check for 2FA restriction
        if (!plugin.getConfig().getBoolean("two_factor_auth.enabled", false) || 
            !plugin.getConfig().getBoolean("two_factor_auth.freeze_until_verified", true)) {
            return;
        }
        
        // Skip if player is not verified with Discord at all
        if (!plugin.getVerificationManager().isVerified(playerId)) {
            return;
        }
        
        // Prevent item dropping if 2FA is required but not completed
        if (!plugin.getTwoFactorManager().isVerified(playerId)) {
            event.setCancelled(true);
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerCommandServer(PlayerCommandPreprocessEvent event) {
        // Check if server command is disabled for unverified players
        if (!plugin.getConfig().getBoolean("server_selector.block_unverified", false)) {
            return;
        }
        
        Player player = event.getPlayer();
        
        // Block server command for unverified players
        if (!plugin.getVerificationManager().isVerified(player.getUniqueId())) {
            String command = event.getMessage().split(" ")[0].toLowerCase();
            if (command.equalsIgnoreCase("/server")) {
                event.setCancelled(true);
                player.sendMessage(ColorUtils.translate("&c❌ You need to verify your Discord account to use this command!"));
                player.sendMessage(ColorUtils.translate("&7Use &f/verify &7to link your account."));
            }
        }
    }
} 