package me.katze.powerantirelog.utility;

import org.bukkit.entity.*;

public class PlayerUtility {

    public static Player getDamager(Entity damager) {
        if (damager instanceof Player) {
            return (Player) damager;
        } else if (damager instanceof Projectile) {
            Projectile projectile = (Projectile) damager;
            if (projectile.getShooter() instanceof Player) {
                return (Player) projectile.getShooter();
            }
        } else if (damager instanceof TNTPrimed) {
            TNTPrimed tntPrimed = (TNTPrimed) damager;
            return getDamager(tntPrimed.getSource());
        } else if (damager instanceof AreaEffectCloud) {
            AreaEffectCloud aec = (AreaEffectCloud) damager;
            if (aec.getSource() instanceof Player) {
                return (Player) aec.getSource();
            }
        } else if (damager instanceof Tameable) {
            AnimalTamer owner = ((Tameable) damager).getOwner();
            if (owner instanceof Player) {
                return (Player) owner;
            }
        } else if (damager instanceof org.bukkit.entity.AbstractWindCharge) {
            org.bukkit.entity.AbstractWindCharge wind = (org.bukkit.entity.AbstractWindCharge) damager;
            if (wind.getShooter() instanceof Player) {
                return (Player) wind.getShooter();
            }
        }
        return null;
    }
}
