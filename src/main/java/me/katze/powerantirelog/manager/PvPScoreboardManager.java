package me.katze.powerantirelog.manager;

import fr.mrmicky.fastboard.FastBoard;
import me.katze.powerantirelog.AntiRelog;
import me.katze.powerantirelog.utility.StringUtility;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;

public class PvPScoreboardManager {

    private final AntiRelog plugin;
    private final Map<UUID, UUID> attackerIds = new HashMap<>();
    private final Map<UUID, String> attackerNames = new HashMap<>();
    private final Map<UUID, Integer> attackerHealth = new HashMap<>();
    private final Map<UUID, FastBoard> boards = new HashMap<>();

    public PvPScoreboardManager(AntiRelog plugin) {
        this.plugin = plugin;
    }

    public boolean isEnabled() {
        return plugin.getConfig().getBoolean("settings.scoreboard.enabled", false);
    }

    public void setAttacker(Player target, Player attacker) {
        if (target == null || attacker == null) return;

        UUID targetId = target.getUniqueId();
        attackerIds.put(targetId, attacker.getUniqueId());
        attackerNames.put(targetId, attacker.getName());
        attackerHealth.put(targetId, (int) Math.ceil(attacker.getHealth()));
    }

    public void handleStart(Player player) {
        if (player == null || !isEnabled()) return;

        boards.computeIfAbsent(player.getUniqueId(), uuid -> new FastBoard(player));
    }

    public void update(Player player, int timeLeft) {
        if (player == null) return;

        if (!isEnabled()) {
            if (boards.containsKey(player.getUniqueId())) {
                clear(player);
            }
            return;
        }

        FastBoard board = boards.computeIfAbsent(player.getUniqueId(), uuid -> new FastBoard(player));
        if (board.isDeleted()) {
            board = new FastBoard(player);
            boards.put(player.getUniqueId(), board);
        }

        String rawTitle = plugin.getConfig().getString("settings.scoreboard.title", "&fPvP");
        String titleWithPapi = StringUtility.replacePlaceholders(player, rawTitle);
        String title = replacePlaceholders(StringUtility.getMessage(titleWithPapi), player, timeLeft);
        board.updateTitle(title);

        List<String> rawLines = getLines();
        List<String> lines = new ArrayList<>(rawLines.size());
        for (String line : rawLines) {
            String lineWithPapi = StringUtility.replacePlaceholders(player, line);
            lines.add(replacePlaceholders(StringUtility.getMessage(lineWithPapi), player, timeLeft));
        }

        board.updateLines(lines);
    }

    public void clear(Player player) {
        if (player == null) return;

        UUID uuid = player.getUniqueId();
        attackerIds.remove(uuid);
        attackerNames.remove(uuid);
        attackerHealth.remove(uuid);

        FastBoard board = boards.remove(uuid);
        if (board != null && !board.isDeleted()) {
            board.delete();
        }
    }

    public void clearAll() {
        for (FastBoard board : boards.values()) {
            if (board != null && !board.isDeleted()) {
                board.delete();
            }
        }

        boards.clear();
        attackerIds.clear();
        attackerNames.clear();
        attackerHealth.clear();
    }

    private List<String> getLines() {
        List<String> lines = plugin.getConfig().getStringList("settings.scoreboard.lines");
        if (lines == null || lines.isEmpty()) {
            return Collections.singletonList("&f ");
        }

        return lines;
    }

    private String replacePlaceholders(String input, Player player, int timeLeft) {
        if (input == null) return "";

        UUID playerId = player.getUniqueId();
        UUID attackerId = attackerIds.get(playerId);

        String name = attackerNames.getOrDefault(playerId, "-");
        String hp = attackerHealth.containsKey(playerId) ? String.valueOf(attackerHealth.get(playerId)) : "0";

        if (attackerId != null) {
            Player attacker = Bukkit.getPlayer(attackerId);
            if (attacker != null) {
                name = attacker.getName();
                int health = Math.max(0, (int) Math.ceil(attacker.getHealth()));
                hp = String.valueOf(health);

                attackerNames.put(playerId, name);
                attackerHealth.put(playerId, health);
            }
        }

        return input.replace("{attacker}", name)
                .replace("{attacker_health}", hp)
                .replace("{time}", String.valueOf(Math.max(timeLeft, 0)));
    }
    public String getAttackerName(Player player) {
        if (player == null) return "-";
        UUID playerId = player.getUniqueId();
        UUID attackerId = attackerIds.get(playerId);
        if (attackerId != null) {
            Player attacker = Bukkit.getPlayer(attackerId);
            if (attacker != null) {
                return attacker.getName();
            }
        }
        return attackerNames.getOrDefault(playerId, "-");
    }

    public int getAttackerHealth(Player player) {
        if (player == null) return 0;
        UUID playerId = player.getUniqueId();
        UUID attackerId = attackerIds.get(playerId);
        if (attackerId != null) {
            Player attacker = Bukkit.getPlayer(attackerId);
            if (attacker != null) {
                return Math.max(0, (int) Math.ceil(attacker.getHealth()));
            }
        }
        return attackerHealth.getOrDefault(playerId, 0);
    }
}
