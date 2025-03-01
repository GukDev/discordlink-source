package dev.guk.discordlink.utils;

import org.bukkit.ChatColor;

public class ColorUtils {
    public static String translate(String message) {
        if (message == null) return null;
        return ChatColor.translateAlternateColorCodes('&', message);
    }
} 