package dev.guk.discordlink.tasks;

import java.util.List;
import java.util.Random;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import dev.guk.discordlink.DiscordLink;

public class BroadcastTask {
    private final DiscordLink plugin;
    private final Random random;
    private BukkitTask task;

    public BroadcastTask(DiscordLink plugin) {
        this.plugin = plugin;
        this.random = new Random();
    }

    public void start() {
        int interval = plugin.getConfig().getInt("broadcast.interval", 900);
        List<String> messages = plugin.getConfig().getStringList("broadcast.messages");

        if (messages.isEmpty()) {
            plugin.getLogger().warning("No broadcast messages configured!");
            return;
        }

        this.task = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            String message = messages.get(random.nextInt(messages.size()));
            for (Player player : plugin.getServer().getOnlinePlayers()) {
                if (!plugin.getVerificationManager().isVerified(player.getUniqueId())) {
                    player.sendMessage(plugin.getConfig().getString("messages.prefix") + message);
                }
            }
        }, interval * 20L, interval * 20L);
    }

    public void stop() {
        if (this.task != null) {
            this.task.cancel();
        }
    }
} 