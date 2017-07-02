package net.acomputerdog.magicwells.db;

import net.acomputerdog.magicwells.well.Well;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

public interface WellDB {
    void setup(FileConfiguration config);

    void connect();
    void disconnect();

    boolean isConnected();

    Well getWellByName(String name);

    void getWellsNear(Location loc, List<Location> outList);

    void createWell(Well well);
}
