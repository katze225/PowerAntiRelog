package me.katze.powerantirelog.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Subcommand;
import me.katze.powerantirelog.AntiRelog;
import me.katze.powerantirelog.utility.StringUtility;
import org.bukkit.command.CommandSender;

@CommandAlias("antirelog|ar|powerantirelog")
public class HelpCommand extends BaseCommand {

    @Default
    @Subcommand("help")
    @CommandPermission("powerantirelog.help")
    public void onHelp(CommandSender sender) {
        for (String string : AntiRelog.getInstance().getConfig().getStringList("messages.help")) {
            String replacedString = StringUtility.getMessage(string);
            sender.sendMessage(replacedString);
        }
    }
}