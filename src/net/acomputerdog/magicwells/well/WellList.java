package net.acomputerdog.magicwells.well;

import net.acomputerdog.magicwells.PluginMagicWells;
import net.acomputerdog.magicwells.db.FlatFileDB;
import net.acomputerdog.magicwells.db.WellDB;

public class WellList {
    private final PluginMagicWells plugin;
    private WellDB db;

    public WellList(PluginMagicWells plugin) {
        this.plugin = plugin;

        if (plugin.getConfig().getBoolean("database.enabled", false)) {
            plugin.getLogger().warning("Database is not yet supported!");
            //TODO implement
        }

        db = new FlatFileDB();
        db.setup(plugin.getConfig());
    }

    public void connect() {
        this.db.connect();
    }

    public void disconnect() {

    }
}
