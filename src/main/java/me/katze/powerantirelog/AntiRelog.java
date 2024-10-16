package me.katze.powerantirelog;

import me.katze.powerantirelog.listener.DamageListener;
import me.katze.powerantirelog.listener.WorldGuardListener;
import me.katze.powerantirelog.utility.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public final class AntiRelog extends JavaPlugin {

    public static boolean CMI_HOOK = false;
    public static boolean ESSENTIALS_HOOK = false;
    public static boolean WORLDGUARD_HOOK = false;

    private static final int PLUGIN_ID = 23642;
    public static final String VERSION = "0.1-Beta";

    private static AntiRelog instance;
    private static final Logger LOGGER = Logger.getLogger("PowerAntiRelog");
    private FileConfiguration config;

    @Override
    public void onEnable() {
        instance = this;
        config = getConfig();

        loadDepend();
        loadMetrics();
        loadConfig();

        loadListeners();
        loadCommands();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    private void loadCommands() {

    }

    private void loadListeners() {
        Bukkit.getPluginManager().registerEvents(new WorldGuardListener(), this);
        Bukkit.getPluginManager().registerEvents(new DamageListener(), this);
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
}
