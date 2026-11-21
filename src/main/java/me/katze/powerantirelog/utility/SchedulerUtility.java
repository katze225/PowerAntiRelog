package me.katze.powerantirelog.utility;

import me.katze.powerantirelog.AntiRelog;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public final class SchedulerUtility {

    private static final boolean IS_FOLIA = isFoliaPresent();

    private SchedulerUtility() {
    }

    private static boolean isFoliaPresent() {
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            return true;
        } catch (ClassNotFoundException | LinkageError e) {
            return false;
        }
    }

    public static boolean isFolia() {
        return IS_FOLIA;
    }

    public static void runGlobalTimer(Runnable task, long delayTicks, long periodTicks) {
        AntiRelog plugin = AntiRelog.getInstance();
        if (IS_FOLIA) {
            long delayMs = Math.max(1, delayTicks * 50);
            long periodMs = Math.max(1, periodTicks * 50);
            Bukkit.getAsyncScheduler().runAtFixedRate(plugin, scheduledTask -> task.run(), delayMs, periodMs, TimeUnit.MILLISECONDS);
        } else {
            Bukkit.getScheduler().runTaskTimer(plugin, task, delayTicks, periodTicks);
        }
    }

    public static void runEntity(Entity entity, Runnable task) {
        if (entity == null) return;
        AntiRelog plugin = AntiRelog.getInstance();
        if (IS_FOLIA) {
            entity.getScheduler().run(plugin, scheduledTask -> task.run(), null);
        } else {
            if (Bukkit.isPrimaryThread()) {
                task.run();
            } else {
                Bukkit.getScheduler().runTask(plugin, task);
            }
        }
    }

    public static void runEntityLater(Entity entity, Runnable task, long delayTicks) {
        if (entity == null) return;
        AntiRelog plugin = AntiRelog.getInstance();
        if (IS_FOLIA) {
            entity.getScheduler().runDelayed(plugin, scheduledTask -> task.run(), null, Math.max(1, delayTicks));
        } else {
            Bukkit.getScheduler().runTaskLater(plugin, task, delayTicks);
        }
    }

    public static void runGlobal(Runnable task) {
        AntiRelog plugin = AntiRelog.getInstance();
        if (IS_FOLIA) {
            Bukkit.getGlobalRegionScheduler().run(plugin, scheduledTask -> task.run());
        } else {
            if (Bukkit.isPrimaryThread()) {
                task.run();
            } else {
                Bukkit.getScheduler().runTask(plugin, task);
            }
        }
    }
}
