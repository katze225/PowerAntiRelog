package me.katze.powerantirelog.utility;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtility {
    private static final boolean HAS_PAPI = hasPlaceholderAPI();

    private static boolean hasPlaceholderAPI() {
        try {
            return Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null;
        } catch (Throwable t) {
            return false;
        }
    }

    public static String replacePlaceholders(org.bukkit.entity.Player player, String message) {
        if (message == null || message.isEmpty()) return "";
        if (HAS_PAPI && player != null) {
            try {
                return me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, message);
            } catch (Throwable ignored) {
            }
        }
        return message;
    }
    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");

    public static String getMessage(String string) {
        if (string == null || string.isEmpty()) {
            return "";
        }

        if (string.contains("<") && string.contains(">")) {
            try {
                Component parsed = MiniMessage.miniMessage().deserialize(string);
                return LegacyComponentSerializer.legacySection().serialize(parsed);
            } catch (Exception ignored) {
            }
        }

        String translated = ChatColor.translateAlternateColorCodes('&', string);

        Matcher matcher = HEX_PATTERN.matcher(translated);
        StringBuffer buffer = new StringBuffer();

        while (matcher.find()) {
            String hexCode = matcher.group(1);
            StringBuilder hex = new StringBuilder("§x");
            for (char c : hexCode.toCharArray()) {
                hex.append('§').append(c);
            }
            matcher.appendReplacement(buffer, hex.toString());
        }

        matcher.appendTail(buffer);
        return buffer.toString();
    }
}
