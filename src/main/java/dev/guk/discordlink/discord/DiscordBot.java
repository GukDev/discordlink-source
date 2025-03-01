package dev.guk.discordlink.discord;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import dev.guk.discordlink.DiscordLink;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.requests.GatewayIntent;

public class DiscordBot extends ListenerAdapter {
    private final DiscordLink plugin;
    private JDA jda;
    private Guild guild;
    private Role verifiedRole;

    public DiscordBot(DiscordLink plugin) {
        this.plugin = plugin;
    }

    public Guild getGuild() {
        return guild;
    }

    public Role getVerifiedRole() {
        return verifiedRole;
    }

    public boolean start() {
        String token = plugin.getConfig().getString("discord.token");
        if (token == null || token.isEmpty()) {
            plugin.getLogger().severe("Discord bot token is not set in config.yml!");
            return false;
        }

        try {
            jda = JDABuilder.createDefault(token)
                    .enableIntents(GatewayIntent.GUILD_MEMBERS)
                    .addEventListeners(this)
                    .build();

            jda.awaitReady();

            // Get the guild
            String guildId = plugin.getConfig().getString("discord.guild_id");
            if (guildId == null || guildId.isEmpty()) {
                plugin.getLogger().severe("Discord guild ID is not set in config.yml!");
                return false;
            }

            guild = jda.getGuildById(guildId);
            if (guild == null) {
                plugin.getLogger().severe("Could not find Discord guild with ID: " + guildId);
                return false;
            }

            // Get the verified role
            String roleId = plugin.getConfig().getString("discord.verified_role_id");
            if (roleId == null || roleId.isEmpty()) {
                plugin.getLogger().severe("Discord verified role ID is not set in config.yml!");
                return false;
            }

            verifiedRole = guild.getRoleById(roleId);
            if (verifiedRole == null) {
                plugin.getLogger().severe("Could not find Discord role with ID: " + roleId);
                return false;
            }

            // Register the verify command
            guild.upsertCommand("verify", "Verify your Minecraft account")
                    .addOption(net.dv8tion.jda.api.interactions.commands.OptionType.STRING, "code", "The verification code from Minecraft", true)
                    .queue();

            plugin.getLogger().info("Discord bot started successfully!");
            return true;
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to start Discord bot: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public void stop() {
        if (jda != null) {
            jda.shutdown();
        }
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equals("verify")) return;

        OptionMapping codeOption = event.getOption("code");
        if (codeOption == null) {
            String message = plugin.getConfig().getString("messages.discord.no-code", "❌ Please provide a verification code!\n• Generate one in Minecraft using `/verify`");
            event.reply(message).setEphemeral(true).queue();
            return;
        }

        String code = codeOption.getAsString();
        String discordId = event.getUser().getId();

        // Verify the code and get the UUID
        String uuidStr = plugin.getVerificationManager().verifyCodeAndGetUUID(discordId, code);
        if (uuidStr == null) {
            String message = plugin.getConfig().getString("messages.discord.verify-failure", "❌ Invalid or expired verification code!\n• Please generate a new code in Minecraft using `/verify`\n• Make sure to use the code within 5 minutes");
            event.reply(message).setEphemeral(true).queue();
            return;
        }

        try {
            // Add the verified role
            Member member = event.getMember();
            if (member != null) {
                guild.addRoleToMember(member, verifiedRole).queue(success -> {
                    // Update nickname if enabled
                    if (plugin.getConfig().getBoolean("discord.sync_nickname", true)) {
                        UUID uuid = UUID.fromString(uuidStr);
                        Player player = Bukkit.getPlayer(uuid);
                        if (player != null) {
                            String format = plugin.getConfig().getString("discord.nickname_format", "%player%");
                            String nickname = format.replace("%player%", player.getName());
                            
                            // Check if bot has nickname permission
                            if (guild.getSelfMember().canInteract(member)) {
                                guild.modifyNickname(member, nickname).queue(
                                    nicknameSuccess -> {
                                        if (plugin.getConfig().getBoolean("settings.debug", false)) {
                                            plugin.getLogger().info("Updated nickname for " + member.getUser().getName() + " to " + nickname);
                                        }
                                        String message = plugin.getConfig().getString("messages.discord.verify-success", "✅ Successfully verified!\n• Your Minecraft account is now linked\n• You've been given the verified role\n• Your nickname has been updated\n• You can now access verified-only features");
                                        event.reply(message).setEphemeral(true).queue();
                                    },
                                    error -> {
                                        plugin.getLogger().warning("Failed to update nickname: " + error.getMessage());
                                        String message = plugin.getConfig().getString("messages.discord.verify-success", "✅ Successfully verified!\n• Your Minecraft account is now linked\n• You've been given the verified role\n• You can now access verified-only features\n• Note: Could not update nickname (insufficient permissions)");
                                        event.reply(message).setEphemeral(true).queue();
                                    }
                                );
                            } else {
                                plugin.getLogger().warning("Bot does not have permission to modify nicknames for this user");
                                String message = plugin.getConfig().getString("messages.discord.verify-success", "✅ Successfully verified!\n• Your Minecraft account is now linked\n• You've been given the verified role\n• You can now access verified-only features\n• Note: Could not update nickname (insufficient permissions)");
                                event.reply(message).setEphemeral(true).queue();
                            }
                        } else {
                            String message = plugin.getConfig().getString("messages.discord.verify-success", "✅ Successfully verified!\n• Your Minecraft account is now linked\n• You've been given the verified role\n• You can now access verified-only features");
                            event.reply(message).setEphemeral(true).queue();
                        }
                    } else {
                        String message = plugin.getConfig().getString("messages.discord.verify-success", "✅ Successfully verified!\n• Your Minecraft account is now linked\n• You've been given the verified role\n• You can now access verified-only features");
                        event.reply(message).setEphemeral(true).queue();
                    }
                }, error -> {
                    plugin.getLogger().severe("Error while adding role: " + error.getMessage());
                    String message = plugin.getConfig().getString("messages.discord.error", "❌ An error occurred while processing your request.\n• Please try again later\n• If the issue persists, contact an administrator");
                    event.reply(message).setEphemeral(true).queue();
                });
            } else {
                String message = plugin.getConfig().getString("messages.discord.error", "❌ An error occurred while processing your request.\n• Please try again later\n• If the issue persists, contact an administrator");
                event.reply(message).setEphemeral(true).queue();
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Error while verifying user: " + e.getMessage());
            String message = plugin.getConfig().getString("messages.discord.error", "❌ An error occurred while processing your request.\n• Please try again later\n• If the issue persists, contact an administrator");
            event.reply(message).setEphemeral(true).queue();
        }
    }
} 