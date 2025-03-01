package dev.guk.discordlink;

import org.bukkit.plugin.java.JavaPlugin;

import dev.guk.discordlink.commands.UnlinkCommand;
import dev.guk.discordlink.commands.VerifyCommand;
import dev.guk.discordlink.discord.DiscordBot;
import dev.guk.discordlink.managers.StorageManager;
import dev.guk.discordlink.managers.VerificationManager;

public class DiscordLink extends JavaPlugin {
    private DiscordBot discordBot;
    private StorageManager storageManager;
    private VerificationManager verificationManager;

    @Override
    public void onEnable() {
        // Save default config
        saveDefaultConfig();

        // Initialize managers
        this.storageManager = new StorageManager(this);
        this.verificationManager = new VerificationManager(this);

        // Initialize Discord bot
        this.discordBot = new DiscordBot(this);
        if (!this.discordBot.start()) {
            getLogger().severe("Failed to start Discord bot! Plugin will be disabled.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Register commands
        getCommand("verify").setExecutor(new VerifyCommand(this));
        getCommand("unlink").setExecutor(new UnlinkCommand(this));

        getLogger().info("DiscordLink has been enabled!");
    }

    @Override
    public void onDisable() {
        if (discordBot != null) {
            discordBot.stop();
        }
        getLogger().info("DiscordLink has been disabled!");
    }

    public DiscordBot getDiscordBot() {
        return discordBot;
    }

    public StorageManager getStorageManager() {
        return storageManager;
    }

    public VerificationManager getVerificationManager() {
        return verificationManager;
    }
} 