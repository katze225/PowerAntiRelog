package me.katze.powerantirelog.data;

import org.bukkit.Location;

public class NewbieData {

    private int timeLeft;
    private boolean enabled;
    private long lastMoveTime;
    private Location lastMoveLocation;
    private Location lastAllowedLocation;

    public NewbieData(int timeLeft, boolean enabled) {
        this.timeLeft = timeLeft;
        this.enabled = enabled;
    }

    public int getTimeLeft() {
        return timeLeft;
    }

    public void setTimeLeft(int timeLeft) {
        this.timeLeft = Math.max(0, timeLeft);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public long getLastMoveTime() {
        return lastMoveTime;
    }

    public void setLastMoveTime(long lastMoveTime) {
        this.lastMoveTime = lastMoveTime;
    }

    public Location getLastMoveLocation() {
        return lastMoveLocation;
    }

    public void setLastMoveLocation(Location lastMoveLocation) {
        this.lastMoveLocation = lastMoveLocation;
    }

    public Location getLastAllowedLocation() {
        return lastAllowedLocation;
    }

    public void setLastAllowedLocation(Location lastAllowedLocation) {
        this.lastAllowedLocation = lastAllowedLocation;
    }
}
