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

    void createWell(Well well);

    int getNumberOfWells();

    Well getWellByID(int wellID);

    int getWellIDFromTrigger(Location l);
    Well getWellFromBB(Location l);

    Well getHomeWell(UUID owner);

    Well[] getWellsByOwnerAndName(UUID owner, String name);
    Well[] getWellsByOwner(UUID owner);

    String getWellName(int wellID);

    UUID getWellOwner(int wellID);

    Location getWellLocation(int wellID);

    void setWellOwner(int wellID, UUID owner);

    void setWellName(int wellID, String name);

    void setHomeWell(UUID owner, Well homeWell);

    int numWellBBsInRange(int x1, int z1, int x2, int z2);

    int numWellsInRange(int x1, int z1, int x2, int z2);
}
