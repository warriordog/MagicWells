package net.acomputerdog.magicwells.db;

import net.acomputerdog.magicwells.well.Well;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

public class FlatFileDB implements WellDB {
    @Override
    public void setup(FileConfiguration config) {

    }

    @Override
    public void connect() {

    }

    @Override
    public void disconnect() {

    }

    @Override
    public Well getWellByName(String name) {
        return null;
    }

    @Override
    public void getWellsNear(Location loc, List<Location> outList) {

    }

    @Override
    public void createWell(Well well) {

    }
}
