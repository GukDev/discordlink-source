package dev.guk.discordlink;

import org.bukkit.plugin.java.JavaPlugin;

import dev.guk.discordlink.commands.AdminCommand;
import dev.guk.discordlink.commands.TwoFactorCommand;
import dev.guk.discordlink.commands.UnlinkCommand;
import dev.guk.discordlink.commands.VerifyCommand;
import dev.guk.discordlink.discord.DiscordBot;
import dev.guk.discordlink.listeners.PlayerListener;
import dev.guk.discordlink.managers.StorageManager;
import dev.guk.discordlink.managers.TwoFactorManager;
import dev.guk.discordlink.managers.VerificationFreezeManager;
import dev.guk.discordlink.managers.VerificationManager;

public class DiscordLink extends JavaPlugin {
    private DiscordBot discordBot;
    private StorageManager storageManager;
    private VerificationManager verificationManager;
    private TwoFactorManager twoFactorManager;
    private VerificationFreezeManager verificationFreezeManager;

    @Override
    public void onEnable() {
        // Save default config
        saveDefaultConfig();

        // Initialize managers
        this.storageManager = new StorageManager(this);
        this.verificationManager = new VerificationManager(this);
        this.twoFactorManager = new TwoFactorManager(this);
        this.verificationFreezeManager = new VerificationFreezeManager(this);

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
        getCommand("discordlink").setExecutor(new AdminCommand(this));
        getCommand("2fa").setExecutor(new TwoFactorCommand(this));
        
        // Register listeners
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);

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
    
    public TwoFactorManager getTwoFactorManager() {
        return twoFactorManager;
    }

    public VerificationFreezeManager getVerificationFreezeManager() {
        return verificationFreezeManager;
    }
} 