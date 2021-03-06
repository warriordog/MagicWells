package net.acomputerdog.magicwells.well;

import net.acomputerdog.magicwells.PluginMagicWells;
import net.acomputerdog.magicwells.db.InternalDB;
import net.acomputerdog.magicwells.db.WellDB;
import org.bukkit.Chunk;
import org.bukkit.Location;

import java.util.Random;
import java.util.UUID;

public class WellList {
    private final PluginMagicWells plugin;
    private final WellNamer wellNamer;
    private final Random random;
    private final WellDB db;


    public WellList(PluginMagicWells plugin) {
        this.plugin = plugin;
        this.wellNamer = new WellNamer(plugin);
        this.random = new Random();

        if (plugin.getConfig().getBoolean("database.external", false)) {
            plugin.getLogger().warning("External database is not yet supported!");
            //TODO implement
        }

        // always use internal DB until external is implemented
        db = new InternalDB(plugin);
    }

    public void connect() {
        this.db.connect();
    }

    public void disconnect() {
        if (db != null && db.isConnected()) {
            db.disconnect();
        }
    }

    public Well createWell(Location loc) {
        Location bb1 = plugin.getStructureManager().getWellStruct().getBB1();
        Location bb2 = plugin.getStructureManager().getWellStruct().getBB2();

        Well well = new Well(loc, bb1, bb2, wellNamer.getRandomName());
        db.createWell(well);
        return well;
    }

    public int getIDByTrigger(Location loc) {
        return db.getWellIDFromTrigger(loc);
    }

    public Well getWellByID(int id) {
        return db.getWellByID(id);
    }

    public boolean isWellInChunk(Chunk c) {
        int bX = c.getX() * 16;
        int bZ = c.getZ() * 16;

        return db.numWellBBsInRange(bX, bZ, bX + 15, bZ + 15) > 0;
    }

    public Well getWellByCollision(Location l) {
        return db.getWellFromBB(l);
    }

    public void saveWellOwner(Well well) {
        db.setWellOwner(well.getDbID(), well.getOwner());
    }

    public Well[] getWellsByOwnerAndName(UUID owner, String name) {
        return db.getWellsByOwnerAndName(owner, name);
    }

    public Well[] getWellsByOwner(UUID owner) {
        return db.getWellsByOwner(owner);
    }

    public Well getHomeWell(UUID owner) {
        return db.getHomeWell(owner);
    }

    public void setHomeWell(UUID owner, Well homeWell) {
        db.setHomeWell(owner, homeWell);
    }

    public Well[] getWellsByOwnerAndNameOrID(UUID owner, String nameOrID) {
        try {
            int id = Integer.parseInt(nameOrID);
            return new Well[]{getWellByID(id)};
        } catch (NumberFormatException e) {
            return getWellsByOwnerAndName(owner, nameOrID);
        }
    }

    public Well getRandomWell() {
        int numWells = db.getNumberOfWells();
        int wellID = random.nextInt(numWells);
        return getWellByID(wellID);
    }

    public void setWellName(Well well, String name) {
        db.setWellName(well.getDbID(), name);
    }
}
