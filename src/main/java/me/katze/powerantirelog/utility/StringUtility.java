package me.katze.powerantirelog.utility;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.ChatColor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtility {
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
