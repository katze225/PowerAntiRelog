package me.katze.powerantirelog;

import co.aikar.commands.PaperCommandManager;
import me.katze.powerantirelog.command.HelpCommand;
import me.katze.powerantirelog.command.NewbieCommand;
import me.katze.powerantirelog.command.ReloadCommand;
import me.katze.powerantirelog.command.TestCommand;
import me.katze.powerantirelog.listener.*;
import me.katze.powerantirelog.manager.CooldownManager;
import me.katze.powerantirelog.manager.NewbieManager;
import me.katze.powerantirelog.manager.PvPManager;
import me.katze.powerantirelog.manager.PvPScoreboardManager;
import me.katze.powerantirelog.utility.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class AntiRelog extends JavaPlugin {

    public boolean CMI_HOOK = false;
    public boolean ESSENTIALS_HOOK = false;
    public boolean WORLDGUARD_HOOK = false;

    private final int PLUGIN_ID = 23642;
    public final String VERSION = "1.6";
    public final String CREATOR = "https://github.com/katze225/PowerAntiRelog";
    public final String TELEGRAM_URL = "https://t.me/core2k21";

    private static AntiRelog instance;
    public PvPManager pvpmanager;
    public CooldownManager cooldownManager;
    public NewbieManager newbieManager;
    public PvPScoreboardManager scoreboardManager;

    @Override
    public void onEnable() {
        instance = this;

        System.out.println("Version: " + CREATOR);
        System.out.println("By: " + VERSION);
        System.out.println("Support: " + TELEGRAM_URL);

        loadDepend();
        loadMetrics();
        loadConfig();

        newbieManager = new NewbieManager();

        loadListeners();
        loadCommands();

        scoreboardManager = new PvPScoreboardManager(this);
        pvpmanager = new PvPManager();
        cooldownManager = new CooldownManager();
    }

    @Override
    public void onDisable() {
        if (newbieManager != null) {
            NewbieManager.saveNow();
        }

        if (scoreboardManager != null) {
            scoreboardManager.clearAll();
        }
    }

    private void loadCommands() {
        PaperCommandManager commandManager = new PaperCommandManager(AntiRelog.getInstance());

        commandManager.registerCommand(new HelpCommand());
        commandManager.registerCommand(new ReloadCommand());
        commandManager.registerCommand(new TestCommand());
        commandManager.registerCommand(new NewbieCommand());
    }

    private void loadListeners() {
        Bukkit.getPluginManager().registerEvents(new WorldGuardListener(), this);
        Bukkit.getPluginManager().registerEvents(new DamageListener(), this);
        Bukkit.getPluginManager().registerEvents(new DeathListener(), this);
        Bukkit.getPluginManager().registerEvents(new LeaveListener(), this);
        Bukkit.getPluginManager().registerEvents(new InteractListener(), this);
        Bukkit.getPluginManager().registerEvents(new CommandListener(), this);
        Bukkit.getPluginManager().registerEvents(new CooldownListener(), this);
        Bukkit.getPluginManager().registerEvents(new NewbieListener(), this);
    }

    private void loadConfig() {
        getConfig().options().copyDefaults(true);
        saveConfig();
    }

    public void loadMetrics() {
        Metrics metrics = new Metrics(this, PLUGIN_ID);
    }

    public void loadDepend() {
        if (getServer().getPluginManager().getPlugin("CMI") != null) {
            CMI_HOOK = true;
        }
        if (getServer().getPluginManager().getPlugin("Essentials") != null) {
            ESSENTIALS_HOOK = true;
        }
        if (getServer().getPluginManager().getPlugin("WorldGuard") != null) {
            WORLDGUARD_HOOK = true;
        }
    }

    public static AntiRelog getInstance() {
        return instance;
    }

    public PvPScoreboardManager getScoreboardManager() {
        return scoreboardManager;
    }
}
