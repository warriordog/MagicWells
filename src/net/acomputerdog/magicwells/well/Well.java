package net.acomputerdog.magicwells.well;

import org.bukkit.Location;

import java.util.UUID;

public class Well {
    private final Location location;
    private UUID owner;
    private String name;

    public Well(Location location, String name) {
        this(location, name, null);
    }

    public Well(Location location, String name, UUID owner) {
        this.location = location;
        this.name = name;
        this.owner = owner;
    }

    public boolean isIntact() {

    }
}
