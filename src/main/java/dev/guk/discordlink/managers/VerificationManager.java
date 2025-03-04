package dev.guk.discordlink.managers;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import dev.guk.discordlink.DiscordLink;

public class VerificationManager {
    private final DiscordLink plugin;
    private final Map<UUID, String> pendingCodes;
    private final Map<UUID, Long> cooldowns;
    private final Map<UUID, Long> codeExpiry;

    public VerificationManager(DiscordLink plugin) {
        this.plugin = plugin;
        this.pendingCodes = new HashMap<>();
        this.cooldowns = new HashMap<>();
        this.codeExpiry = new HashMap<>();
    }

    public String generateCode(UUID playerId) {
        // Generate a random code
        String code;
        if (plugin.getConfig().getString("verification.code.format", "ALPHANUMERIC").equalsIgnoreCase("NUMERIC")) {
            code = String.format("%06d", (int) (Math.random() * 1000000));
        } else {
            String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
            StringBuilder sb = new StringBuilder();
            int length = plugin.getConfig().getInt("verification.code.length", 6);
            for (int i = 0; i < length; i++) {
                sb.append(chars.charAt((int) (Math.random() * chars.length())));
            }
            code = sb.toString();
        }

        // Store the code with expiry time
        pendingCodes.put(playerId, code);
        int expiryTime = plugin.getConfig().getInt("verification.code.expiry", 300);
        codeExpiry.put(playerId, System.currentTimeMillis() + (expiryTime * 1000L));

        // Set cooldown
        int cooldown = plugin.getConfig().getInt("verification.cooldown", 60);
        cooldowns.put(playerId, System.currentTimeMillis() + (cooldown * 1000L));

        return code;
    }

    public String verifyCodeAndGetUUID(String discordId, String code) {
        // Find the player with this code
        for (Map.Entry<UUID, String> entry : pendingCodes.entrySet()) {
            if (entry.getValue().equals(code)) {
                UUID playerId = entry.getKey();
                
                // Check if code has expired
                Long expiry = codeExpiry.get(playerId);
                if (expiry == null || expiry < System.currentTimeMillis()) {
                    pendingCodes.remove(playerId);
                    codeExpiry.remove(playerId);
                    return null;
                }

                // Store the link
                plugin.getStorageManager().storeVerification(playerId, discordId);

                // Unfreeze the player if they're online
                Player player = Bukkit.getPlayer(playerId);
                if (player != null && player.isOnline()) {
                    plugin.getVerificationFreezeManager().unfreezePlayer(player);
                }

                // Clean up
                pendingCodes.remove(playerId);
                codeExpiry.remove(playerId);

                return playerId.toString();
            }
        }
        return null;
    }

    public boolean isVerified(UUID playerId) {
        return plugin.getStorageManager().isVerified(playerId);
    }

    public void unlink(UUID playerId) {
        plugin.getStorageManager().removeVerification(playerId);
        
        // If player is online, they might need to be frozen again
        Player player = Bukkit.getPlayer(playerId);
        if (player != null && player.isOnline() && 
            plugin.getConfig().getBoolean("verification.freeze.enabled", false) &&
            !player.hasPermission(plugin.getConfig().getString("verification.freeze.bypass_permission", "discordlink.bypass.freeze"))) {
            plugin.getVerificationFreezeManager().freezePlayer(player);
        }
    }

    public boolean isOnCooldown(UUID playerId) {
        Long cooldown = cooldowns.get(playerId);
        return cooldown != null && cooldown > System.currentTimeMillis();
    }

    public int getRemainingCooldown(UUID playerId) {
        Long cooldown = cooldowns.get(playerId);
        if (cooldown == null) return 0;
        return Math.max(0, (int) ((cooldown - System.currentTimeMillis()) / 1000));
    }
} 