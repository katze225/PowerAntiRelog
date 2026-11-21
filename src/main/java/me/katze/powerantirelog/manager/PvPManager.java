package me.katze.powerantirelog.manager;

import me.katze.powerantirelog.utility.SchedulerUtility;

import com.Zrips.CMI.CMI;
import com.Zrips.CMI.Containers.CMIUser;
import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;
import me.katze.powerantirelog.AntiRelog;
import me.katze.powerantirelog.data.CooldownData;
import me.katze.powerantirelog.utility.BossBarUtility;
import me.katze.powerantirelog.utility.StringUtility;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class PvPManager {
    private static HashMap<String, Integer> pvpMap;

    public PvPManager() {
        this.pvpMap = new HashMap<>();
        startTask();
    }

    public static void addPlayer(Player player) {
        if (player == null)
            return;
        if (!(player instanceof Player))
            return;
        if (player.hasPermission("powerantirelog.bypass"))
            return;
        if (player.isOp())
            return;
        if (player.isInvulnerable())
            return;
        if (player.isDead())
            return;

        String name = player.getName();
        int time = AntiRelog.getInstance().getConfig().getInt("settings.time");

        for (String world : AntiRelog.getInstance().getConfig().getStringList("settings.disabled-worlds")) {
            if (player.getWorld().getName().contains(world)) {
                return;
            }
        }

        if (pvpMap.containsKey(name)) {
            pvpMap.replace(name, pvpMap.get(name), time);
            if (AntiRelog.getInstance().getScoreboardManager() != null) {
                AntiRelog.getInstance().getScoreboardManager().update(player, time);
            }
        } else {
            player.sendMessage(
                    StringUtility.getMessage(AntiRelog.getInstance().getConfig().getString("messages.start")));
            sendCommands(true, player);
            disable(player);
            pvpMap.put(name, time);
            if (AntiRelog.getInstance().getScoreboardManager() != null) {
                AntiRelog.getInstance().getScoreboardManager().handleStart(player);
                AntiRelog.getInstance().getScoreboardManager().update(player, time);
            }
        }
    }

    public static void removeFromMaps(Player player) {
        pvpMap.remove(player.getName());

        CooldownManager.removePlayer(player);

        if (AntiRelog.getInstance().getScoreboardManager() != null) {
            AntiRelog.getInstance().getScoreboardManager().clear(player);
        }
    }

    public static boolean isPvP(Player player) {
        if (player == null || pvpMap == null) return false;
        return pvpMap.containsKey(player.getName());
    }

    public static int getTimeLeft(Player player) {
        if (player == null || pvpMap == null) return 0;
        return pvpMap.getOrDefault(player.getName(), 0);
    }

    public static void leave(Player player) {
        if (AntiRelog.getInstance().getConfig().getBoolean("settings.leave.kill")) {
            player.damage(player.getHealth());
            player.setHealth(0);
        }

        for (String string : AntiRelog.getInstance().getConfig().getStringList("settings.leave.message")) {
            String replacedString = StringUtility.getMessage(string).replace("{player}", player.getName());
            Bukkit.getServer().broadcastMessage(replacedString);
        }

        removeFromMaps(player);
    }

    private static void disable(Player player) {
        if (AntiRelog.getInstance().getConfig().getBoolean("settings.disable.fly")) {
            player.setFlying(false);
            player.setAllowFlight(false);
            if (AntiRelog.getInstance().CMI_HOOK) {
                CMIUser user = CMI.getInstance().getPlayerManager().getUser(player);

                if (user != null) {
                    user.setFlying(false);
                    user.setWasFlying(false);
                    user.setTfly(0L);
                }
            }
        }

        if (AntiRelog.getInstance().getConfig().getBoolean("settings.disable.speed")) {
            player.setWalkSpeed(0.2F);
        }

        if (AntiRelog.getInstance().getConfig().getBoolean("settings.disable.gamemode")) {
            player.setGameMode(GameMode.SURVIVAL);
        }

        if (AntiRelog.getInstance().getConfig().getBoolean("settings.disable.invisibility")) {
            player.removePotionEffect(PotionEffectType.INVISIBILITY);
        }

        if (AntiRelog.getInstance().getConfig().getBoolean("settings.disable.elytra")
                && player.getInventory().getChestplate() != null
                && player.getInventory().getChestplate().getType() == Material.ELYTRA) {

            ItemStack elytra = player.getInventory().getChestplate().clone();
            player.getInventory().setChestplate(null);
            player.getInventory().addItem(elytra);
        }

        if (AntiRelog.getInstance().getConfig().getBoolean("settings.disable.godmode")) {
            if (AntiRelog.getInstance().CMI_HOOK) {
                CMIUser user = CMI.getInstance().getPlayerManager().getUser(player);

                if (user != null) {
                    CMI.getInstance().getNMS().changeGodMode(player, false);
                    user.setTgod(0L);
                }
            }
            if (AntiRelog.getInstance().ESSENTIALS_HOOK) {
                Essentials essentials = (Essentials) Bukkit.getPluginManager().getPlugin("Essentials");
                User user = essentials.getUser(player);

                user.setGodModeEnabled(false);
            }
        }
    }

    public static void startTask() {
        SchedulerUtility.runGlobalTimer(PvPManager::update, 0L, 20L);
    }

    private static void update() {
        if (pvpMap == null)
            return;

        Iterator<Map.Entry<String, Integer>> iterator = pvpMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Integer> entry = iterator.next();
            String name = entry.getKey();
            int time = entry.getValue() - 1;

            if (time <= 0) {
                iterator.remove();
                Player player = Bukkit.getPlayer(name);
                if (player != null) {
                    player.sendMessage(
                            StringUtility.getMessage(AntiRelog.getInstance().getConfig().getString("messages.end")));
                    sendCommands(false, player);
                    if (AntiRelog.getInstance().getScoreboardManager() != null) {
                        AntiRelog.getInstance().getScoreboardManager().clear(player);
                    }
                }
            } else {
                pvpMap.replace(name, time);
                BossBarUtility.setTemporarily(name, time);
                Player player = Bukkit.getPlayer(name);
                if (player != null && AntiRelog.getInstance().getScoreboardManager() != null) {
                    AntiRelog.getInstance().getScoreboardManager().update(player, time);
                }
            }
        }
    }

    public static void death(Player player) {
        String name = player.getName();

        if (pvpMap.containsKey(name)) {
            pvpMap.remove(name);
        }

        CooldownManager.removePlayer(player);

        if (AntiRelog.getInstance().getScoreboardManager() != null) {
            AntiRelog.getInstance().getScoreboardManager().clear(player);
        }
    }

    private static void sendCommands(boolean start, Player player) {
        List<String> commands;
        if (start) {
            commands = AntiRelog.getInstance().getConfig().getStringList("settings.command-start");
        } else {
            commands = AntiRelog.getInstance().getConfig().getStringList("settings.command-end");
        }

        for (String string : commands) {
            CommandSender sender = null;

            if (string.startsWith("[console]")) {
                sender = Bukkit.getConsoleSender();
            } else if (string.startsWith("[player]")) {
                sender = player.getPlayer();
            }

            String replacedString = string.replace("{player}", player.getName())
                    .replace("[player]", "")
                    .replace("[console]", "");
            Bukkit.getServer().dispatchCommand(sender, replacedString);
        }
    }
}
