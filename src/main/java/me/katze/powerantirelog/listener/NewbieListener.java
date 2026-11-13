package me.katze.powerantirelog.listener;

import me.katze.powerantirelog.manager.NewbieManager;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.entity.LingeringPotionSplashEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashSet;
import java.util.Set;

public class NewbieListener implements Listener {

    private static final Set<PotionEffectType> HARMFUL_EFFECTS = new HashSet<>();

    static {
        HARMFUL_EFFECTS.add(PotionEffectType.POISON);
        HARMFUL_EFFECTS.add(PotionEffectType.WITHER);
        HARMFUL_EFFECTS.add(PotionEffectType.SLOWNESS);
        HARMFUL_EFFECTS.add(PotionEffectType.MINING_FATIGUE);
        HARMFUL_EFFECTS.add(PotionEffectType.WEAKNESS);
        HARMFUL_EFFECTS.add(PotionEffectType.INSTANT_DAMAGE);
        HARMFUL_EFFECTS.add(PotionEffectType.BLINDNESS);
        HARMFUL_EFFECTS.add(PotionEffectType.NAUSEA);
        HARMFUL_EFFECTS.add(PotionEffectType.HUNGER);
        HARMFUL_EFFECTS.add(PotionEffectType.UNLUCK);
        HARMFUL_EFFECTS.add(PotionEffectType.BAD_OMEN);
        HARMFUL_EFFECTS.add(PotionEffectType.LEVITATION);
        HARMFUL_EFFECTS.add(PotionEffectType.WIND_CHARGED);
        HARMFUL_EFFECTS.add(PotionEffectType.WEAVING);
        HARMFUL_EFFECTS.add(PotionEffectType.OOZING);
        HARMFUL_EFFECTS.add(PotionEffectType.INFESTED);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        NewbieManager.handleJoin(e.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        NewbieManager.handleQuit(e.getPlayer());
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent e) {
        if (!isBlocked(e.getPlayer(), e.getBlock().getLocation())) {
            return;
        }

        e.setCancelled(true);
        NewbieManager.sendBlockedMessage(e.getPlayer());
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent e) {
        if (!isBlocked(e.getPlayer(), e.getBlock().getLocation())) {
            return;
        }

        e.setCancelled(true);
        NewbieManager.sendBlockedMessage(e.getPlayer());
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent e) {
        if (e.getClickedBlock() == null) {
            return;
        }
        if (!isBlocked(e.getPlayer(), e.getClickedBlock().getLocation())) {
            return;
        }

        e.setCancelled(true);
        NewbieManager.sendBlockedMessage(e.getPlayer());
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBucketEmpty(PlayerBucketEmptyEvent e) {
        Location location = e.getBlockClicked().getRelative(e.getBlockFace()).getLocation();
        if (!isBlocked(e.getPlayer(), location)) {
            return;
        }

        e.setCancelled(true);
        NewbieManager.sendBlockedMessage(e.getPlayer());
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBucketFill(PlayerBucketFillEvent e) {
        if (!isBlocked(e.getPlayer(), e.getBlockClicked().getLocation())) {
            return;
        }

        e.setCancelled(true);
        NewbieManager.sendBlockedMessage(e.getPlayer());
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onTeleport(PlayerTeleportEvent e) {
        if (e.getTo() == null || !NewbieManager.isProtected(e.getPlayer())) {
            return;
        }
        if (!NewbieManager.isInForbiddenRegion(e.getTo())) {
            return;
        }

        e.setCancelled(true);
        NewbieManager.sendRegionMessage(e.getPlayer());
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPickup(EntityPickupItemEvent e) {
        if (!(e.getEntity() instanceof Player)) {
            return;
        }

        Player player = (Player) e.getEntity();
        if (!NewbieManager.isProtected(player) || !NewbieManager.isPlayerDropsBlocked()) {
            return;
        }
        if (!NewbieManager.isPlayerDrop(e.getItem().getItemStack())) {
            return;
        }

        e.setCancelled(true);
        NewbieManager.sendPickupMessage(player);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPotionSplash(PotionSplashEvent e) {
        ThrownPotion potion = e.getPotion();
        if (potion == null || !(potion.getShooter() instanceof Player)) {
            return;
        }

        Player shooter = (Player) potion.getShooter();
        boolean shooterProtected = NewbieManager.isProtected(shooter);

        for (LivingEntity affected : e.getAffectedEntities()) {
            if (!(affected instanceof Player)) {
                continue;
            }

            Player target = (Player) affected;
            if (target.equals(shooter)) {
                continue;
            }

            if (shooterProtected || NewbieManager.isProtected(target)) {
                e.setIntensity(target, 0);
                NewbieManager.notifyBlockedDamage(shooter, target);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onLingeringPotionSplash(LingeringPotionSplashEvent e) {
        if (!(e.getEntity().getShooter() instanceof Player)) {
            return;
        }

        Player shooter = (Player) e.getEntity().getShooter();
        if (!NewbieManager.isProtected(shooter)) {
            return;
        }

        e.setCancelled(true);
        NewbieManager.sendBlockedMessage(shooter);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPotionEffect(EntityPotionEffectEvent e) {
        if (!(e.getEntity() instanceof Player) || !NewbieManager.isProtected((Player) e.getEntity())) {
            return;
        }

        PotionEffect effect = e.getNewEffect();
        if (effect == null || !HARMFUL_EFFECTS.contains(effect.getType())) {
            return;
        }

        EntityPotionEffectEvent.Cause cause = e.getCause();
        if (cause == EntityPotionEffectEvent.Cause.POTION_SPLASH
                || cause == EntityPotionEffectEvent.Cause.AREA_EFFECT_CLOUD
                || cause == EntityPotionEffectEvent.Cause.ARROW) {
            e.setCancelled(true);
        }
    }

    private boolean isBlocked(Player player, Location location) {
        return NewbieManager.isProtected(player) && NewbieManager.isInForbiddenRegion(location);
    }
}
