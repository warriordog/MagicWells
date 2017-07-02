package net.acomputerdog.magicwells.well;

import org.bukkit.Location;

import java.util.UUID;

public class Well {
    private final Location location;
    private UUID owner;
    private String name;
    private int dbID;

    public Well(Location location, String name) {
        this(-1, location, name, null);
    }

    public Well(int dbID, Location location, String name, UUID owner) {
        this.dbID = dbID;
        this.location = location;
        this.name = name;
        this.owner = owner;
    }

    public boolean isIntact() {
        return true;
    }

    public int getDbID() {
        return dbID;
    }

    public void setDbID(int dbID) {
        this.dbID = dbID;
    }

    public Location getLocation() {
        return location;
    }

    public String getName() {
        return name;
    }
}
