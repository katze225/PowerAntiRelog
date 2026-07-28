package me.katze.powerantirelog.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import me.katze.powerantirelog.AntiRelog;
import me.katze.powerantirelog.utility.StringUtility;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@CommandAlias("antirelog|ar|powerantirelog")
public class ReloadCommand extends BaseCommand {

    @Subcommand("reload")
    @CommandPermission("powerantirelog.reload")
    public void onReload(CommandSender sender) {
        AntiRelog.getInstance().saveDefaultConfig();
        AntiRelog.getInstance().reloadConfig();
        AntiRelog.getInstance().getConfig().setDefaults(YamlConfiguration.loadConfiguration(
                new InputStreamReader(AntiRelog.getInstance().getResource("config.yml"), StandardCharsets.UTF_8)));

        sender.sendMessage(StringUtility.getMessage(AntiRelog.getInstance().getConfig().getString("messages.reload")));
    }
}