package dev.guk.discordlink.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import dev.guk.discordlink.DiscordLink;
import dev.guk.discordlink.utils.ColorUtils;

public class VerifyCommand implements CommandExecutor {
    private final DiscordLink plugin;

    public VerifyCommand(DiscordLink plugin) {
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

        // Check if already verified
        if (plugin.getVerificationManager().isVerified(player.getUniqueId())) {
            String message = plugin.getConfig().getString("messages.minecraft.already-verified", "&c❌ Your account is already verified!");
            player.sendMessage(ColorUtils.translate(prefix + message));
            return true;
        }

        // Check cooldown
        if (plugin.getVerificationManager().isOnCooldown(player.getUniqueId())) {
            String message = plugin.getConfig().getString("messages.minecraft.cooldown", "&c⏳ Please wait &e%time% seconds &cbefore requesting another code.");
            message = message.replace("%time%", String.valueOf(plugin.getVerificationManager().getRemainingCooldown(player.getUniqueId())));
            player.sendMessage(ColorUtils.translate(prefix + message));
            return true;
        }

        try {
            // Generate verification code
            String code = plugin.getVerificationManager().generateCode(player.getUniqueId());
            
            // Send clickable code message
            String message = plugin.getConfig().getString("messages.minecraft.verify-start", 
                "&b=== Verification Instructions ===\n&7➊ Your unique code: &e%code%\n&7➋ Join our Discord server if you haven't\n&7➌ Click the code below to copy it, then use &f/verify &7in Discord\n&7➍ Wait for confirmation\n&b===========================");
            message = message.replace("%code%", code);
            player.sendMessage(ColorUtils.translate(message));
            
            // Send clickable code component
            net.md_5.bungee.api.chat.TextComponent clickableCode = new net.md_5.bungee.api.chat.TextComponent(ColorUtils.translate("&a&l[Click to Copy Code]"));
            clickableCode.setClickEvent(new net.md_5.bungee.api.chat.ClickEvent(
                net.md_5.bungee.api.chat.ClickEvent.Action.COPY_TO_CLIPBOARD,
                code
            ));
            clickableCode.setHoverEvent(new net.md_5.bungee.api.chat.HoverEvent(
                net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT,
                new net.md_5.bungee.api.chat.ComponentBuilder(ColorUtils.translate("&7Click to copy: &f" + code)).create()
            ));
            player.spigot().sendMessage(clickableCode);
        } catch (Exception e) {
            plugin.getLogger().severe("Error generating verification code: " + e.getMessage());
            String error = plugin.getConfig().getString("messages.minecraft.error", "&c❌ An error occurred: %error%\n&7Please try again later.");
            error = error.replace("%error%", e.getMessage());
            player.sendMessage(ColorUtils.translate(prefix + error));
        }

        return true;
    }
} 