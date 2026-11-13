package me.katze.powerantirelog.listener;

import me.katze.powerantirelog.AntiRelog;
import me.katze.powerantirelog.manager.NewbieManager;
import me.katze.powerantirelog.manager.PvPManager;
import me.katze.powerantirelog.utility.PlayerUtility;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityCombustByEntityEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class DamageListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDamageByEntity(EntityDamageByEntityEvent e) {
        if (!(e.getEntity() instanceof Player)) return;

        Player target = (Player) e.getEntity();
        Player damager = PlayerUtility.getDamager(e.getDamager());

        if (damager == null) return;
        if (damager == target) return;

        if (NewbieManager.isProtected(target) || NewbieManager.isProtected(damager)) {
            NewbieManager.notifyBlockedDamage(damager, target);
            e.setCancelled(true);
            return;
        }

        updateScoreboard(target, damager);
        updateScoreboard(damager, target);

        PvPManager.addPlayer(target);
        PvPManager.addPlayer(damager);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onCombust(EntityCombustByEntityEvent e) {
        if (!(e.getEntity() instanceof Player)) return;

        Player target = (Player) e.getEntity();

        if (!(e.getCombuster() instanceof Player)) return;
        Player damager = (Player) e.getCombuster();

        if (damager == target) return;

        if (NewbieManager.isProtected(target) || NewbieManager.isProtected(damager)) {
            NewbieManager.notifyBlockedDamage(damager, target);
            e.setCancelled(true);
            return;
        }

        updateScoreboard(target, damager);
        updateScoreboard(damager, target);

        PvPManager.addPlayer(target);
        PvPManager.addPlayer(damager);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPotionSplash(PotionSplashEvent e) {
        if (e.getPotion() == null) return;
        if (!(e.getPotion().getShooter() instanceof Player)) return;

        Player damager = (Player) e.getPotion().getShooter();

        for (LivingEntity target : e.getAffectedEntities()) {
            if (target == damager) continue;

            if (!(target instanceof Player)) continue;
            if (NewbieManager.isProtected(damager) || NewbieManager.isProtected((Player) target)) continue;

            for (PotionEffect effect : e.getPotion().getEffects()) {
                if (effect.getType().equals(PotionEffectType.POISON)) {

                    updateScoreboard((Player) target, damager);
                    updateScoreboard(damager, (Player) target);

                    PvPManager.addPlayer(damager);
                    PvPManager.addPlayer((Player) target);
                }
            }
        }
    }

    private void updateScoreboard(Player victim, Player attacker) {
        if (victim == null || attacker == null) return;
        if (AntiRelog.getInstance().getScoreboardManager() == null) return;

        AntiRelog.getInstance().getScoreboardManager().setAttacker(victim, attacker);
    }
}
