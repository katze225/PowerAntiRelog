package me.katze.powerantirelog.data;

import java.time.LocalTime;

public class CooldownData {
    private LocalTime time;
    private String materialName;

    public CooldownData(LocalTime time, String materialName) {
        this.time = time;
        this.materialName = materialName;
    }

    public LocalTime getTime() {
        return time;
    }

    public void setTime(LocalTime time) {
        this.time = time;
    }

    public String getItem() {
        return materialName;
    }
}
