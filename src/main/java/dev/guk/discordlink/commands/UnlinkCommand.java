package dev.guk.discordlink.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import dev.guk.discordlink.DiscordLink;
import dev.guk.discordlink.utils.ColorUtils;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;

public class UnlinkCommand implements CommandExecutor {
    private final DiscordLink plugin;

    public UnlinkCommand(DiscordLink plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        String prefix = plugin.getConfig().getString("settings.prefix", "&8[&b&lDiscordLink&8]&r ");

        if (!(sender instanceof Player)) {
            sender.sendMessage(ColorUtils.translate(prefix + plugin.getConfig().getString("messages.minecraft.player-only", "&cThis command can only be used by players!")));
            return true;
        }

        Player player = (Player) sender;

        // Check if verified
        if (!plugin.getVerificationManager().isVerified(player.getUniqueId())) {
            String message = plugin.getConfig().getString("messages.minecraft.not-verified", "&c❌ You don't have any linked accounts!");
            player.sendMessage(ColorUtils.translate(prefix + message));
            return true;
        }

        try {
            // Get Discord ID before unlinking
            String discordId = plugin.getStorageManager().getDiscordId(player.getUniqueId());
            if (discordId != null) {
                // Get Discord guild and role
                Guild guild = plugin.getDiscordBot().getGuild();
                Role verifiedRole = plugin.getDiscordBot().getVerifiedRole();
                
                if (guild != null && verifiedRole != null) {
                    // Remove role from Discord member
                    Member member = guild.getMemberById(discordId);
                    if (member != null) {
                        guild.removeRoleFromMember(member, verifiedRole).queue(
                            success -> {
                                if (plugin.getConfig().getBoolean("settings.debug", false)) {
                                    plugin.getLogger().info("Removed verified role from " + member.getUser().getName());
                                }
                            },
                            error -> plugin.getLogger().warning("Failed to remove role: " + error.getMessage())
                        );
                    }
                }
            }

            // First, clear any 2FA status
            // This is important to reset the 2FA state before unlinking
            if (plugin.getConfig().getBoolean("two_factor_auth.enabled", false)) {
                plugin.getTwoFactorManager().removePlayer(player.getUniqueId());
            }

            // Unlink account
            plugin.getVerificationManager().unlink(player.getUniqueId());
            
            // Apply appropriate freeze state
            if (plugin.getConfig().getBoolean("verification.freeze.enabled", false) &&
                !player.hasPermission(plugin.getConfig().getString("verification.freeze.bypass_permission", "discordlink.bypass.freeze"))) {
                // The player should be frozen now that they're unverified
                plugin.getVerificationFreezeManager().freezePlayer(player);
                if (plugin.getConfig().getBoolean("settings.debug", false)) {
                    plugin.getLogger().info("Player " + player.getName() + " has been frozen after unlinking");
                }
            }
            
            String message = plugin.getConfig().getString("messages.minecraft.unlink-success", "&a✔ Your accounts have been unlinked successfully!");
            player.sendMessage(ColorUtils.translate(prefix + message));
        } catch (Exception e) {
            plugin.getLogger().severe("Error while unlinking account: " + e.getMessage());
            String error = plugin.getConfig().getString("messages.minecraft.error", "&c❌ An error occurred: %error%\n&7Please try again later.");
            error = error.replace("%error%", e.getMessage());
            player.sendMessage(ColorUtils.translate(prefix + error));
        }

        return true;
    }
} 