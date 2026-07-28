package me.katze.powerantirelog.listener;

import me.katze.powerantirelog.AntiRelog;
import me.katze.powerantirelog.manager.PvPManager;
import me.katze.powerantirelog.utility.StringUtility;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

public class InteractListener implements Listener {

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        if (!PvPManager.isPvP(e.getPlayer()))
            return;

        Block block = e.getClickedBlock();

        if (block == null) {
            return;
        }

        if (block.getType() == Material.ENDER_CHEST) {

            if (AntiRelog.getInstance().getConfig().getBoolean("settings.cancel.ender-chest")) {
                // e.getPlayer().sendMessage(
                // StringUtility.getMessage(AntiRelog.getInstance().getConfig().getString("messages.block")));
                e.setCancelled(true);
            }
        } else if (AntiRelog.getInstance().getConfig().getBoolean("settings.cancel.interact")) {
            // e.getPlayer().sendMessage(
            // StringUtility.getMessage(AntiRelog.getInstance().getConfig().getString("messages.block")));
            e.setCancelled(true);
        }
    }
}
