package me.katze.powerantirelog.listener;

import me.katze.powerantirelog.manager.PvPManager;
import me.katze.powerantirelog.utility.DamagerUtility;
import org.bukkit.entity.EntityType;
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
        if (e.getEntity() instanceof Player) {
            Player target = (Player) e.getEntity();
            Player damager = DamagerUtility.getDamager(e.getDamager());

            if (damager == null || target == null) return;

            PvPManager.addPlayer(target);
            PvPManager.addPlayer(damager);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onCombust(EntityCombustByEntityEvent e) {
        if (e.getEntity() instanceof Player) {
            Player target = (Player) e.getEntity();
            Player damager = DamagerUtility.getDamager(e.getCombuster());

            PvPManager.addPlayer(target);
            PvPManager.addPlayer(damager);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPotionSplash(PotionSplashEvent e) {
        if (e.getEntity() instanceof Player) {
            if (e.getPotion() == null) return;

            Player damager = (Player) e.getPotion().getShooter();

            for (LivingEntity target : e.getAffectedEntities()) {
                if (target == damager) return;

                for (PotionEffect effect : e.getPotion().getEffects()) {
                    if (effect.getType().equals(PotionEffectType.POISON)) {

                        PvPManager.addPlayer(damager);
                        PvPManager.addPlayer((Player) target);
                    }
                }
            }
        }
    }
}
