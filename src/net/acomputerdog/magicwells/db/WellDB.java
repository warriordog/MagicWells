package net.acomputerdog.magicwells.db;

import net.acomputerdog.magicwells.well.Well;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.UUID;

public interface WellDB {
    void setup(FileConfiguration config);

    void connect();
    void disconnect();

    boolean isConnected();

    Well getWellByID(int id);

    void createWell(Well well);

    int getWellFromTrigger(Location l);

    String getWellName(int wellID);

    UUID getWellOwner(int id);

    Location getWellLocation(int id);

    int numWellsInRange(int x1, int z1, int x2, int z2);

    Well getWellFromBB(Location l);

    void saveWellOwner(int wellID, UUID id);
}
