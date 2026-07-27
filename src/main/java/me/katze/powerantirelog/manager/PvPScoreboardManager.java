package me.katze.powerantirelog.manager;

import me.katze.powerantirelog.AntiRelog;
import me.katze.powerantirelog.utility.StringUtility;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.util.*;

public class PvPScoreboardManager {

    private static final String OBJECTIVE_NAME = "par_pvp";

    private final AntiRelog plugin;
    private final Map<UUID, UUID> attackerIds = new HashMap<>();
    private final Map<UUID, String> attackerNames = new HashMap<>();
    private final Map<UUID, Integer> attackerHealth = new HashMap<>();
    private final Map<UUID, Scoreboard> activeBoards = new HashMap<>();

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
        if (player == null) return;
        if (!isEnabled()) {
            return;
        }

        activeBoards.computeIfAbsent(player.getUniqueId(), uuid -> {
            if (Bukkit.getScoreboardManager() == null) {
                return null;
            }

            return Bukkit.getScoreboardManager().getNewScoreboard();
        });
    }

    public void update(Player player, int timeLeft) {
        if (player == null) return;

        if (!isEnabled()) {
            if (activeBoards.containsKey(player.getUniqueId())) {
                clear(player);
            }
            return;
        }

        Scoreboard scoreboard = provideScoreboard(player);
        if (scoreboard == null) return;

        Objective objective = scoreboard.getObjective(OBJECTIVE_NAME);
        if (objective == null) {
            objective = scoreboard.registerNewObjective(OBJECTIVE_NAME, "dummy");
        }

        String title = plugin.getConfig().getString("settings.scoreboard.title", "&fPvP");
        objective.setDisplayName(replacePlaceholders(StringUtility.getMessage(title), player, timeLeft));
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        for (String entry : new HashSet<>(scoreboard.getEntries())) {
            scoreboard.resetScores(entry);
        }

        List<String> lines = getLines();
        int score = lines.size();

        for (int i = 0; i < lines.size(); i++) {
            String line = replacePlaceholders(StringUtility.getMessage(lines.get(i)), player, timeLeft);
            String uniqueLine = appendUniqueToken(line, i);
            objective.getScore(uniqueLine).setScore(score--);
        }

        player.setScoreboard(scoreboard);
    }

    public void clear(Player player) {
        if (player == null) return;

        UUID uuid = player.getUniqueId();
        attackerIds.remove(uuid);
        attackerNames.remove(uuid);
        attackerHealth.remove(uuid);

        Scoreboard board = activeBoards.remove(uuid);
        if (board != null) {
            for (String entry : new HashSet<>(board.getEntries())) {
                board.resetScores(entry);
            }
            if (Bukkit.getScoreboardManager() != null) {
                player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
            }
        }
    }

    public void clearAll() {
        for (UUID uuid : new HashSet<>(activeBoards.keySet())) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                clear(player);
            } else {
                activeBoards.remove(uuid);
            }
        }

        attackerIds.clear();
        attackerNames.clear();
        attackerHealth.clear();
    }

    private Scoreboard provideScoreboard(Player player) {
        return activeBoards.computeIfAbsent(player.getUniqueId(), uuid -> {
            if (Bukkit.getScoreboardManager() == null) {
                return null;
            }

            return Bukkit.getScoreboardManager().getNewScoreboard();
        });
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

    private String appendUniqueToken(String line, int index) {
        StringBuilder builder = new StringBuilder(line);
        String hex = Integer.toHexString(index);
        for (char c : hex.toCharArray()) {
            builder.append('\u00A7').append(c);
        }
        return builder.toString();
    }
}
