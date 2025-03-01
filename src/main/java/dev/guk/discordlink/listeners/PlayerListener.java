package dev.guk.discordlink.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import dev.guk.discordlink.DiscordLink;

public class PlayerListener implements Listener {
    private final DiscordLink plugin;

    public PlayerListener(DiscordLink plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (!plugin.getVerificationManager().isVerified(player.getUniqueId())) {
            String message = plugin.getConfig().getString("messages.verify.not-verified",
                    "§7Please verify your account using §f/verify");
            player.sendMessage(plugin.getConfig().getString("messages.prefix") + message);
        }
    }
} 