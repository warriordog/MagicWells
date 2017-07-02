package net.acomputerdog.magicwells.db;

import net.acomputerdog.magicwells.PluginMagicWells;
import net.acomputerdog.magicwells.well.Well;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

public class InternalDB implements WellDB {
    private final PluginMagicWells plugin;

    private Connection connection;

    public InternalDB(PluginMagicWells plugin) {
        this.plugin = plugin;
    }

    @Override
    public void setup(FileConfiguration config) {
        // nothing to do
    }

    @Override
    public void connect() {
        try {
            // this will cause static initialization of HSQLDB
            Class.forName("org.hsqldb.jdbcDriver");

            // database path
            String dbPath = "jdbc:hsqldb:" + new File(plugin.getDataFolder(), "wells.db").getPath();

            // SA is default username, no password
            connection = DriverManager.getConnection(dbPath, "SA", "");

        } catch (ClassNotFoundException e) {
            throw new RuntimeException("HSQLDB is missing from the jar!  Please add it or use an external database.", e);
        } catch (SQLException e) {
            throw new RuntimeException("Unable to connect to internal database", e);
        }
    }

    @Override
    public void disconnect() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.createStatement().execute("SHUTDOWN");
                connection.close();
            }
        } catch (SQLException ignored) {
            // we don't care, we are already shutting down
        }
    }

    @Override
    public boolean isConnected() {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException ignored) {
            return false;
        }
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
