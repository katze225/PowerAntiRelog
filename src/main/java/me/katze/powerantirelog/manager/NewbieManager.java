package me.katze.powerantirelog.manager;

import me.katze.powerantirelog.utility.SchedulerUtility;

import me.katze.powerantirelog.AntiRelog;
import me.katze.powerantirelog.data.NewbieData;
import me.katze.powerantirelog.utility.StringUtility;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.codemc.worldguardwrapper.WorldGuardWrapper;
import org.codemc.worldguardwrapper.region.IWrappedRegion;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class NewbieManager {

    private static final Map<UUID, NewbieData> players = new HashMap<>();
    private static final Map<UUID, Long> messageCooldowns = new HashMap<>();

    private static YamlConfiguration storage;
    private static File storageFile;
    private static List<String> forbiddenRegions = new ArrayList<>();
    private static boolean dirty;
    private static int saveTick;
    private static boolean blockPlayerDrops;
    private static NamespacedKey playerDropKey;

    public NewbieManager() {
        reloadConfig();
        loadStorage();
        startTask();
    }

    public static void reloadConfig() {
        forbiddenRegions = new ArrayList<>(
                AntiRelog.getInstance().getConfig().getStringList("settings.newbie.worldguard.regions"));
        blockPlayerDrops = AntiRelog.getInstance().getConfig()
                .getBoolean("settings.newbie.block-player-drops", true);
    }

    public static boolean isFeatureEnabled() {
        return AntiRelog.getInstance().getConfig().getBoolean("settings.newbie.enabled", false);
    }

    public static boolean isProtected(Player player) {
        if (player == null || !isFeatureEnabled()) {
            return false;
        }

        NewbieData data = players.get(player.getUniqueId());
        return data != null && data.isEnabled() && data.getTimeLeft() > 0;
    }

    public static void handleJoin(Player player) {
        UUID uuid = player.getUniqueId();
        NewbieData data = players.get(uuid);

        if (data == null) {
            data = new NewbieData(0, false);

            if (isFeatureEnabled() && !player.hasPlayedBefore()) {
                data.setTimeLeft(getProtectionTime());
                data.setEnabled(true);
            }

            players.put(uuid, data);
            dirty = true;
        }

        data.setLastMoveLocation(player.getLocation().clone());
        data.setLastMoveTime(System.currentTimeMillis());
        data.setLastAllowedLocation(isInForbiddenRegion(player.getLocation())
                ? findSafeLocation(player)
                : player.getLocation().clone());

        if (isProtected(player)) {
            sendMessage(player, "messages.newbie.start");
            sendActionBar(player, data);
        }
    }

    public static void handleQuit(Player player) {
        messageCooldowns.remove(player.getUniqueId());
        saveNow();
    }

    public static boolean disable(Player player) {
        NewbieData data = players.get(player.getUniqueId());

        if (data == null || !data.isEnabled() || data.getTimeLeft() <= 0) {
            return false;
        }

        data.setEnabled(false);
        dirty = true;
        saveNow();
        return true;
    }

    public static boolean enable(Player player) {
        NewbieData data = players.get(player.getUniqueId());

        if (data == null || data.getTimeLeft() <= 0) {
            return false;
        }

        data.setEnabled(true);
        data.setLastMoveLocation(player.getLocation().clone());
        data.setLastMoveTime(System.currentTimeMillis());
        data.setLastAllowedLocation(isInForbiddenRegion(player.getLocation())
                ? findSafeLocation(player)
                : player.getLocation().clone());
        dirty = true;
        saveNow();
        return true;
    }

    public static boolean isInForbiddenRegion(Location location) {
        if (location == null || location.getWorld() == null) {
            return false;
        }
        if (!AntiRelog.getInstance().WORLDGUARD_HOOK) {
            return false;
        }

        List<String> regions = getForbiddenRegions();
        if (regions.isEmpty()) {
            return false;
        }

        try {
            Set<IWrappedRegion> applicable = WorldGuardWrapper.getInstance().getRegions(location);

            for (IWrappedRegion region : applicable) {
                for (String id : regions) {
                    if (region.getId().equalsIgnoreCase(id)) {
                        return true;
                    }
                }
            }
        } catch (Throwable ignored) {
            return false;
        }

        return false;
    }

    public static void sendBlockedMessage(Player player) {
        sendMessageWithCooldown(player, "messages.newbie.block");
    }

    public static void sendRegionMessage(Player player) {
        sendMessageWithCooldown(player, "messages.newbie.region");
    }

    public static void sendPickupMessage(Player player) {
        sendMessageWithCooldown(player, "messages.newbie.pickup");
    }

    public static boolean isPlayerDropsBlocked() {
        return blockPlayerDrops && isFeatureEnabled();
    }

    public static void markPlayerDrops(List<ItemStack> drops) {
        if (drops == null || drops.isEmpty() || !isPlayerDropsBlocked()) {
            return;
        }

        NamespacedKey key = getPlayerDropKey();
        for (ItemStack stack : drops) {
            if (stack == null || stack.getType() == Material.AIR) {
                continue;
            }

            ItemMeta meta = stack.getItemMeta();
            if (meta == null) {
                continue;
            }

            meta.getPersistentDataContainer().set(key, PersistentDataType.BYTE, (byte) 1);
            stack.setItemMeta(meta);
        }
    }

    public static boolean isPlayerDrop(ItemStack stack) {
        if (stack == null || stack.getType() == Material.AIR || !stack.hasItemMeta()) {
            return false;
        }

        ItemMeta meta = stack.getItemMeta();
        return meta != null && meta.getPersistentDataContainer().has(getPlayerDropKey(), PersistentDataType.BYTE);
    }

    private static NamespacedKey getPlayerDropKey() {
        if (playerDropKey == null) {
            playerDropKey = new NamespacedKey(AntiRelog.getInstance(), "player-drop");
        }
        return playerDropKey;
    }

    public static void notifyBlockedDamage(Player attacker, Player victim) {
        if (attacker == null || victim == null || attacker.equals(victim)) {
            return;
        }

        if (isProtected(attacker)) {
            sendMessageWithCooldown(attacker, "messages.newbie.damage-attacker");
            return;
        }

        if (isProtected(victim)) {
            sendMessageWithCooldown(attacker, "messages.newbie.damage-victim", "{player}", victim.getName());
        }
    }

    public static void saveNow() {
        if (storage == null || storageFile == null) {
            return;
        }

        storage.set("players", null);

        for (Map.Entry<UUID, NewbieData> entry : players.entrySet()) {
            NewbieData data = entry.getValue();
            String path = "players." + entry.getKey();
            storage.set(path + ".time", data.getTimeLeft());
            storage.set(path + ".enabled", data.isEnabled());
        }

        try {
            storage.save(storageFile);
            dirty = false;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void loadStorage() {
        AntiRelog.getInstance().getDataFolder().mkdirs();
        storageFile = new File(AntiRelog.getInstance().getDataFolder(), "newbie.yml");
        storage = YamlConfiguration.loadConfiguration(storageFile);

        players.clear();
        ConfigurationSection section = storage.getConfigurationSection("players");
        if (section == null) {
            return;
        }

        for (String key : section.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(key);
                int time = Math.max(0, section.getInt(key + ".time", 0));
                boolean enabled = section.getBoolean(key + ".enabled", false) && time > 0;
                players.put(uuid, new NewbieData(time, enabled));
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    private static void startTask() {
        SchedulerUtility.runGlobalTimer(NewbieManager::tick, 20L, 20L);
    }

    private static void tick() {
        if (players.isEmpty()) {
            return;
        }

        long now = System.currentTimeMillis();
        int afkTimeout = AntiRelog.getInstance().getConfig().getInt("settings.newbie.afk-timeout", 60) * 1000;

        for (Player player : Bukkit.getOnlinePlayers()) {
            NewbieData data = players.get(player.getUniqueId());
            if (data == null || data.getTimeLeft() <= 0) {
                continue;
            }

            Location current = player.getLocation();
            Location last = data.getLastMoveLocation();

            boolean moved = last == null
                    || !Objects.equals(last.getWorld(), current.getWorld())
                    || last.distanceSquared(current) > 0.0001;

            if (moved) {
                data.setLastMoveLocation(current.clone());
                data.setLastMoveTime(now);
            }

            boolean afk = afkTimeout > 0 && now - data.getLastMoveTime() > afkTimeout;
            if (!afk) {
                data.setTimeLeft(data.getTimeLeft() - 1);
                dirty = true;
            }

            if (data.getTimeLeft() <= 0) {
                boolean wasEnabled = data.isEnabled();
                data.setTimeLeft(0);
                data.setEnabled(false);
                dirty = true;
                saveNow();
                if (wasEnabled) {
                    sendMessage(player, "messages.newbie.end");
                }
                continue;
            }

            if (!data.isEnabled()) {
                continue;
            }

            if (isInForbiddenRegion(current)) {
                Location safe = data.getLastAllowedLocation();

                if (safe == null || safe.getWorld() == null || isInForbiddenRegion(safe)) {
                    safe = findSafeLocation(player);
                }

                data.setLastAllowedLocation(safe.clone());
                player.teleport(safe);
                sendRegionMessage(player);
            } else {
                data.setLastAllowedLocation(current.clone());
            }

            sendActionBar(player, data);
        }

        saveTick++;
        int saveInterval = Math.max(1,
                AntiRelog.getInstance().getConfig().getInt("settings.newbie.save-interval", 10));

        if (dirty && saveTick >= saveInterval) {
            saveTick = 0;
            saveNow();
        }
    }

    private static void sendActionBar(Player player, NewbieData data) {
        String format = AntiRelog.getInstance().getConfig().getString("messages.newbie.actionbar");
        if (format == null || format.isEmpty()) {
            return;
        }

        String message = format.replace("{time}", formatTime(data.getTimeLeft()));
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                TextComponent.fromLegacyText(StringUtility.getMessage(message)));
    }

    private static void sendMessage(Player player, String path) {
        String message = AntiRelog.getInstance().getConfig().getString(path);
        if (message == null || message.isEmpty()) {
            return;
        }

        player.sendMessage(StringUtility.getMessage(message));
    }

    private static void sendMessageWithCooldown(Player player, String path, String... replacements) {
        long now = System.currentTimeMillis();
        Long last = messageCooldowns.get(player.getUniqueId());

        if (last != null && now - last < 2000L) {
            return;
        }

        messageCooldowns.put(player.getUniqueId(), now);

        String message = AntiRelog.getInstance().getConfig().getString(path);
        if (message == null || message.isEmpty()) {
            return;
        }

        for (int i = 0; i + 1 < replacements.length; i += 2) {
            message = message.replace(replacements[i], replacements[i + 1]);
        }

        player.sendMessage(StringUtility.getMessage(message));
    }

    private static int getProtectionTime() {
        return Math.max(0, AntiRelog.getInstance().getConfig().getInt("settings.newbie.time", 60) * 60);
    }

    private static List<String> getForbiddenRegions() {
        return forbiddenRegions;
    }

    private static Location findSafeLocation(Player player) {
        Location spawn = player.getWorld().getSpawnLocation().clone().add(0.5, 0, 0.5);

        if (!isInForbiddenRegion(spawn)) {
            return spawn;
        }

        for (int radius = 1; radius <= 8; radius++) {
            for (int x = -radius; x <= radius; x++) {
                for (int z = -radius; z <= radius; z++) {
                    if (Math.abs(x) != radius && Math.abs(z) != radius) {
                        continue;
                    }

                    Location candidate = spawn.clone().add(x, 0, z);
                    if (!isInForbiddenRegion(candidate)) {
                        return candidate;
                    }
                }
            }
        }

        return spawn;
    }

    private static String formatTime(int seconds) {
        int minutes = Math.max(0, seconds) / 60;
        int secs = Math.max(0, seconds) % 60;
        return String.format("%02d:%02d", minutes, secs);
    }
}
