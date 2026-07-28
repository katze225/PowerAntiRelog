package me.katze.powerantirelog.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import me.katze.powerantirelog.AntiRelog;
import me.katze.powerantirelog.manager.PvPManager;
import me.katze.powerantirelog.utility.StringUtility;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandAlias("antirelog|ar|powerantirelog")
public class TestCommand extends BaseCommand {

    @Subcommand("test")
    @CommandCompletion("@players")
    @CommandPermission("powerantirelog.test")
    public void onTest(CommandSender sender, String target) {
        Player player = Bukkit.getPlayer(target);

        if (player == null) {
            sender.sendMessage(
                    StringUtility.getMessage(AntiRelog.getInstance().getConfig().getString("messages.not-found"))
                            .replace("{player}", target));
            return;
        }

        PvPManager.addPlayer(player);

        sender.sendMessage(StringUtility.getMessage(AntiRelog.getInstance().getConfig().getString("messages.test"))
                .replace("{player}", target));
    }
}