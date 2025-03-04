package dev.guk.discordlink.managers;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import dev.guk.discordlink.DiscordLink;

public class StorageManager {
    private final DiscordLink plugin;
    private final File dataFile;
    private FileConfiguration data;

    public StorageManager(DiscordLink plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "data.yml");
        loadData();
    }

    private void loadData() {
        if (!dataFile.exists()) {
            try {
                dataFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create data.yml: " + e.getMessage());
            }
        }
        this.data = YamlConfiguration.loadConfiguration(dataFile);
    }

    private void saveData() {
        try {
            data.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save data.yml: " + e.getMessage());
        }
    }

    public void storeVerification(UUID playerId, String discordId) {
        data.set("players." + playerId.toString() + ".discord_id", discordId);
        saveData();
    }

    public String getDiscordId(UUID playerId) {
        return data.getString("players." + playerId.toString() + ".discord_id");
    }

    public boolean isVerified(UUID playerId) {
        return data.contains("players." + playerId.toString() + ".discord_id");
    }

    public void removeVerification(UUID playerId) {
        data.set("players." + playerId.toString(), null);
        saveData();
    }

    public void findAndUnlinkDiscordId(String discordId) {
        if (discordId == null || discordId.isEmpty()) {
            return;
        }
        
        // Search through all players for this Discord ID
        if (data.contains("players")) {
            for (String uuidStr : data.getConfigurationSection("players").getKeys(false)) {
                String storedDiscordId = data.getString("players." + uuidStr + ".discord_id");
                if (discordId.equals(storedDiscordId)) {
                    try {
                        UUID playerId = UUID.fromString(uuidStr);
                        
                        // Remove the verification data
                        removeVerification(playerId);
                        
                        plugin.getLogger().info("Unlinked Minecraft account with UUID " + 
                                uuidStr + " because Discord user left the server");
                        return;
                    } catch (IllegalArgumentException e) {
                        plugin.getLogger().warning("Invalid UUID stored in data: " + uuidStr);
                    }
                }
            }
        }
    }
} 