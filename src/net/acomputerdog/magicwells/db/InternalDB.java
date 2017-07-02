package net.acomputerdog.magicwells.db;

import net.acomputerdog.magicwells.PluginMagicWells;
import net.acomputerdog.magicwells.well.Well;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

public class InternalDB implements WellDB {
    private final PluginMagicWells plugin;

    private Connection connection;
    private Statement sharedStatement;

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
            plugin.getLogger().info("Connected to internal database.");

            // create shared statement
            sharedStatement = connection.createStatement();

            // verify database structure
            verifyDB();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("HSQLDB is missing from the jar!  Please add it or use an external database.", e);
        } catch (SQLException e) {
            throw new RuntimeException("Unable to connect to internal database", e);
        }
    }

    private int execUpdate(String query) throws SQLException {
        int i = sharedStatement.executeUpdate(query);
        if (i == -1) {
            throw new SQLException("Internal DB error");
        }
        return i;
    }

    private void verifyDB() {
        try {
            execUpdate("CREATE TABLE IF NOT EXISTS Wells (wellID INTEGER IDENTITY, locX INTEGER NOT NULL, locY INTEGER NOT NULL, locZ INTEGER NOT NULL, PRIMARY KEY (wellID))");
            execUpdate("CREATE TABLE IF NOT EXISTS WellNames (wellID INTEGER NOT NULL, wellName VARCHAR(50) NOT NULL, FOREIGN KEY (wellID) REFERENCES Wells(wellID))");
            execUpdate("CREATE TABLE IF NOT EXISTS WellOwners (wellID INTEGER NOT NULL, ownerUUID UUID NOT NULL, FOREIGN KEY (wellID) REFERENCES Wells(wellID))");
        } catch (SQLException e) {
            throw new RuntimeException("SQL error while verifying database", e);
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
