package net.acomputerdog.magicwells.well;

import net.acomputerdog.magicwells.PluginMagicWells;
import net.acomputerdog.magicwells.db.InternalDB;
import net.acomputerdog.magicwells.db.WellDB;
import org.bukkit.Location;

public class WellList {
    private final PluginMagicWells plugin;
    private final WellNamer wellNamer;

    private WellDB db;


    public WellList(PluginMagicWells plugin) {
        this.plugin = plugin;
        this.wellNamer = new WellNamer(plugin);

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

    public void createWell(Location loc) {
        Well well = new Well(loc, wellNamer.getRandomName());
    }
}
