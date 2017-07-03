package net.acomputerdog.magicwells.well;

import org.bukkit.Location;

import java.util.UUID;

public class Well {
    private final Location location;
    private final Location bb1, bb2;

    private UUID owner;
    private String name;
    private int dbID;

    public Well(Location location, Location bb1, Location bb2, String name) {
        this(-1, location, bb1, bb2, name, null);
    }

    public Well(int dbID, Location location, Location bb1, Location bb2, String name, UUID owner) {
        this.dbID = dbID;
        this.location = location;
        this.bb1 = bb1;
        this.bb2 = bb2;
        this.name = name;
        this.owner = owner;
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

    public UUID getOwner() {
        return owner;
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
    }

    public Location getBB1() {
        return bb1;
    }

    public Location getBB2() {
        return bb2;
    }
}
