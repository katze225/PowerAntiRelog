package me.katze.powerantirelog.listener;

import io.papermc.paper.event.player.PlayerShieldDisableEvent;
import me.katze.powerantirelog.AntiRelog;
import me.katze.powerantirelog.manager.NewbieManager;
import me.katze.powerantirelog.manager.PvPManager;
import me.katze.powerantirelog.utility.PlayerUtility;
import me.katze.powerantirelog.utility.StringUtility;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ShieldListener implements Listener {

    private final Set<UUID> handledDefenders = new HashSet<>();

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onShieldDisable(PlayerShieldDisableEvent e) {
        if (!isShieldBreakEnabled()) {
            e.setCancelled(true);
            return;
        }

        Player defender = e.getPlayer();
        Player attacker = (e.getDamager() instanceof Player) ? (Player) e.getDamager() : null;

        if (isWorldDisabled(defender)) {
            return;
        }

        if (attacker != null && (NewbieManager.isProtected(defender) || NewbieManager.isProtected(attacker))) {
            e.setCancelled(true);
            return;
        }

        boolean pvpOnly = AntiRelog.getInstance().getConfig().getBoolean("settings.shield-break.pvp-only", true);
        if (pvpOnly && !PvPManager.isPvP(defender) && (attacker == null || !PvPManager.isPvP(attacker))) {
            return;
        }

        if (attacker != null) {
            ItemStack weapon = attacker.getInventory().getItemInMainHand();
            if (weapon == null || !isAxe(weapon.getType())) {
                return;
            }
        }

        int cooldownSeconds = AntiRelog.getInstance().getConfig().getInt("settings.shield-break.cooldown", 5);
        int cooldownTicks = Math.max(0, cooldownSeconds * 20);
        e.setCooldown(cooldownTicks);

        handledDefenders.add(defender.getUniqueId());
        Bukkit.getScheduler().runTask(AntiRelog.getInstance(), () -> handledDefenders.remove(defender.getUniqueId()));

        sendEffectsAndMessages(defender, attacker, cooldownSeconds, false);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onAxeAttack(EntityDamageByEntityEvent e) {
        if (!(e.getEntity() instanceof Player)) {
            return;
        }
        Player defender = (Player) e.getEntity();
        Player attacker = PlayerUtility.getDamager(e.getDamager());

        if (attacker == null || attacker == defender) {
            return;
        }

        if (!isShieldBreakEnabled()) {
            return;
        }

        if (isWorldDisabled(defender)) {
            return;
        }

        if (NewbieManager.isProtected(defender) || NewbieManager.isProtected(attacker)) {
            return;
        }

        boolean pvpOnly = AntiRelog.getInstance().getConfig().getBoolean("settings.shield-break.pvp-only", true);
        if (pvpOnly && !PvPManager.isPvP(defender) && !PvPManager.isPvP(attacker)) {
            return;
        }

        if (!defender.isBlocking()) {
            return;
        }

        ItemStack weapon = attacker.getInventory().getItemInMainHand();
        if (weapon == null || !isAxe(weapon.getType())) {
            return;
        }

        Bukkit.getScheduler().runTask(AntiRelog.getInstance(), () -> {
            if (!defender.isOnline()) {
                return;
            }
            if (handledDefenders.contains(defender.getUniqueId())) {
                return;
            }
            if (defender.hasCooldown(Material.SHIELD)) {
                return;
            }

            int cooldownSeconds = AntiRelog.getInstance().getConfig().getInt("settings.shield-break.cooldown", 5);
            int cooldownTicks = Math.max(0, cooldownSeconds * 20);

            defender.clearActiveItem();
            defender.setCooldown(Material.SHIELD, cooldownTicks);

            handledDefenders.add(defender.getUniqueId());
            Bukkit.getScheduler().runTask(AntiRelog.getInstance(), () -> handledDefenders.remove(defender.getUniqueId()));

            sendEffectsAndMessages(defender, attacker, cooldownSeconds, true);
        });
    }

    private void sendEffectsAndMessages(Player defender, Player attacker, int cooldownSeconds, boolean playSound) {
        if (playSound && AntiRelog.getInstance().getConfig().getBoolean("settings.shield-break.sound", true)) {
            defender.getWorld().playSound(defender.getLocation(), Sound.ITEM_SHIELD_BREAK, 1.0f, 1.0f);
        }

        String attackerName = (attacker != null) ? attacker.getName() : "";
        String defenderName = defender.getName();

        String victimMsg = AntiRelog.getInstance().getConfig().getString("messages.shield-broken", "");
        if (victimMsg != null && !victimMsg.isEmpty()) {
            victimMsg = victimMsg.replace("{player}", attackerName).replace("{time}", String.valueOf(cooldownSeconds));
            defender.sendMessage(StringUtility.getMessage(victimMsg));
        }

        String subtitle = AntiRelog.getInstance().getConfig().getString("messages.shield-broken-subtitle", "");
        if (subtitle != null && !subtitle.isEmpty()) {
            subtitle = subtitle.replace("{player}", attackerName).replace("{time}", String.valueOf(cooldownSeconds));
            defender.sendTitle("", StringUtility.getMessage(subtitle), 5, 30, 5);
        }

        if (attacker != null) {
            String attackerMsg = AntiRelog.getInstance().getConfig().getString("messages.shield-broken-attacker", "");
            if (attackerMsg != null && !attackerMsg.isEmpty()) {
                attackerMsg = attackerMsg.replace("{player}", defenderName).replace("{time}", String.valueOf(cooldownSeconds));
                attacker.sendMessage(StringUtility.getMessage(attackerMsg));
            }
        }
    }

    private boolean isShieldBreakEnabled() {
        return AntiRelog.getInstance().getConfig().getBoolean("settings.shield-break.enabled", true);
    }

    private boolean isWorldDisabled(Player player) {
        if (player == null) return false;
        for (String world : AntiRelog.getInstance().getConfig().getStringList("settings.disabled-worlds")) {
            if (player.getWorld().getName().contains(world)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isAxe(Material material) {
        if (material == null) return false;
        String name = material.name();
        return name.endsWith("_AXE") && !name.contains("PICKAXE");
    }
}
