package me.katze.powerantirelog.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Subcommand;
import me.katze.powerantirelog.AntiRelog;
import me.katze.powerantirelog.manager.NewbieManager;
import me.katze.powerantirelog.utility.StringUtility;
import org.bukkit.entity.Player;

@CommandAlias("newbie")
public class NewbieCommand extends BaseCommand {

    @Subcommand("disable")
    public void onDisable(Player player) {
        if (!NewbieManager.isFeatureEnabled()) {
            player.sendMessage(StringUtility.getMessage(
                    AntiRelog.getInstance().getConfig().getString("messages.newbie.not-active")));
            return;
        }

        if (!NewbieManager.disable(player)) {
            player.sendMessage(StringUtility.getMessage(
                    AntiRelog.getInstance().getConfig().getString("messages.newbie.not-active")));
            return;
        }

        player.sendMessage(StringUtility.getMessage(
                AntiRelog.getInstance().getConfig().getString("messages.newbie.disabled")));
    }

    @Subcommand("enable")
    public void onEnable(Player player) {
        if (!NewbieManager.isFeatureEnabled()) {
            player.sendMessage(StringUtility.getMessage(
                    AntiRelog.getInstance().getConfig().getString("messages.newbie.not-active")));
            return;
        }

        if (!NewbieManager.enable(player)) {
            player.sendMessage(StringUtility.getMessage(
                    AntiRelog.getInstance().getConfig().getString("messages.newbie.expired")));
            return;
        }

        player.sendMessage(StringUtility.getMessage(
                AntiRelog.getInstance().getConfig().getString("messages.newbie.enabled")));
    }

    @Default
    public void onDefault(Player player) {
        player.sendMessage(StringUtility.getMessage(
                AntiRelog.getInstance().getConfig().getString("messages.newbie.usage")));
    }
}
