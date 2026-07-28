package me.katze.powerantirelog.manager;

import me.katze.powerantirelog.data.CooldownData;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CooldownManager {
    private static HashMap<String, List<CooldownData>> cooldownMap;

    public CooldownManager() {
        this.cooldownMap = new HashMap<>();
    }

    public static void addPlayer(Player player, Material material) {
        if (material == null) return;

        CooldownData data = new CooldownData(LocalTime.now(), material.name());

        if (cooldownMap.containsKey(player.getName())) {
            List<CooldownData> arrayList = cooldownMap.get(player.getName());

            arrayList.add(data);

            cooldownMap.put(player.getName(), arrayList);
        } else {
            List<CooldownData> arrayList = new ArrayList();

            arrayList.add(data);

            cooldownMap.put(player.getName(), arrayList);
        }
    }

    public static CooldownData getCooldownData(Player player, Material material) {
        if (cooldownMap.containsKey(player.getName())) {
            for (CooldownData cooldownData : cooldownMap.get(player.getName())) {
                if (cooldownData.getItem().equals(material.name())) {
                    return cooldownData;
                }
            }
        }
        return null;
    }

    public static void removePlayer(Player player) {
        cooldownMap.remove(player.getName());
    }

    public static void clearMap() {
        cooldownMap.clear();
    }
}
