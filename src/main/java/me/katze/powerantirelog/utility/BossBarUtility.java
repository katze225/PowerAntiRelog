package me.katze.powerantirelog.utility;

import me.katze.powerantirelog.AntiRelog;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

public class BossBarUtility {

    public static void setTemporarily(String playerName, int time) {
        Player player = Bukkit.getPlayer(playerName);

        if (player == null) return;

        int maxTime = AntiRelog.getInstance().getConfig().getInt("settings.time");

        String rawBar = AntiRelog.getInstance().getConfig().getString("settings.bossbar.bar", "");
        String formattedBar = StringUtility.replacePlaceholders(player, rawBar.replace("{time}", String.valueOf(time)));
        BossBar bossBar = Bukkit.createBossBar(
                StringUtility.getMessage(formattedBar),
                BarColor.valueOf(AntiRelog.getInstance().getConfig().getString("settings.bossbar.color")),
                BarStyle.valueOf(AntiRelog.getInstance().getConfig().getString("settings.bossbar.style"))
        );

        float progress = (float) time / maxTime;
        bossBar.setProgress(progress);

        bossBar.addPlayer(player);

        SchedulerUtility.runEntityLater(player, () -> {
            bossBar.removePlayer(player);
        }, 20L);
    }
}
