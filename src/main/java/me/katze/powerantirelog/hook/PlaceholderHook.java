package me.katze.powerantirelog.hook;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.katze.powerantirelog.AntiRelog;
import me.katze.powerantirelog.manager.NewbieManager;
import me.katze.powerantirelog.manager.PvPManager;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlaceholderHook extends PlaceholderExpansion {

    private final AntiRelog plugin;

    public PlaceholderHook(AntiRelog plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "powerantirelog";
    }

    @Override
    public @NotNull String getAuthor() {
        return "katze225";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String params) {
        if (player == null) {
            return "";
        }

        switch (params.toLowerCase()) {
            case "in_pvp":
            case "status":
                return String.valueOf(PvPManager.isPvP(player));

            case "time":
            case "time_left":
            case "time_seconds":
                return String.valueOf(PvPManager.getTimeLeft(player));

            case "attacker":
            case "attacker_name":
                return (plugin.getScoreboardManager() != null)
                        ? plugin.getScoreboardManager().getAttackerName(player)
                        : "-";

            case "attacker_health":
            case "attacker_hp":
                return (plugin.getScoreboardManager() != null)
                        ? String.valueOf(plugin.getScoreboardManager().getAttackerHealth(player))
                        : "0";

            case "newbie":
            case "newbie_protected":
                return String.valueOf(NewbieManager.isProtected(player));

            case "newbie_time":
            case "newbie_time_seconds":
                return String.valueOf(NewbieManager.getTimeLeft(player));

            case "newbie_time_formatted":
                return NewbieManager.getFormattedTimeLeft(player);

            default:
                return null;
        }
    }
}
