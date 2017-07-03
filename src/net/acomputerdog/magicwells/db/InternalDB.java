package net.acomputerdog.magicwells.db;

import net.acomputerdog.magicwells.PluginMagicWells;
import net.acomputerdog.magicwells.well.Well;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.sql.*;
import java.util.List;
import java.util.UUID;

public class InternalDB implements WellDB {
    private final PluginMagicWells plugin;

    private Connection connection;

    private Statement sharedStatement;

    private PreparedStatement insertWellLocStatement;

    private PreparedStatement getWellFromLoc;
    private PreparedStatement getWellFromTrigger;

    private PreparedStatement getWellNameStatement;
    private PreparedStatement insertWellNameStatement;
    private PreparedStatement updateWellNameStatement;

    private PreparedStatement getWellOwnerStatement;
    private PreparedStatement insertWellOwnerStatement;
    private PreparedStatement updateWellOwnerStatement;

    private PreparedStatement getWellLocationStatement;

    private PreparedStatement insertWellTriggerStatement;

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

            // create prepared statements
            insertWellLocStatement = connection.prepareStatement("INSERT INTO Wells(worldName, locX, locY, locZ) VALUES (?, ?, ?, ?)");

            getWellFromLoc = connection.prepareStatement("SELECT wellID FROM Wells WHERE worldName = ? AND locX = ? AND locY = ? AND locZ = ?");

            getWellNameStatement = connection.prepareStatement("SELECT wellName FROM WellNames WHERE wellID = ?");
            insertWellNameStatement = connection.prepareStatement("INSERT INTO WellNames(wellID, wellName) VALUES (?, ?)");
            updateWellNameStatement = connection.prepareStatement("UPDATE WellNames SET wellName = ? WHERE wellID = ?");

            getWellOwnerStatement = connection.prepareStatement("SELECT ownerUUID FROM WellOwners WHERE wellID = ?");
            insertWellOwnerStatement = connection.prepareStatement("INSERT INTO WellOwners(wellID, ownerUUID) VALUES (?, ?)");
            updateWellOwnerStatement = connection.prepareStatement("UPDATE WellOwners SET ownerUUID = ? WHERE wellID = ?");

            insertWellTriggerStatement = connection.prepareStatement("INSERT INTO WellTriggers(wellID, worldName, offX, offY, offZ) VALUES (?, ?, ?, ?, ?)");
            getWellFromTrigger = connection.prepareStatement("SELECT wellID FROM WellTriggers WHERE worldName = ? AND offX = ? AND offY = ? AND offZ = ?");

            getWellLocationStatement = connection.prepareStatement("SELECT worldName, locX, locY, locZ FROM Wells WHERE wellID = ?");
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
            execUpdate("CREATE TABLE IF NOT EXISTS Wells (wellID INTEGER IDENTITY, worldName VARCHAR(100), locX INTEGER NOT NULL, locY INTEGER NOT NULL, locZ INTEGER NOT NULL, PRIMARY KEY (wellID))");
            execUpdate("CREATE TABLE IF NOT EXISTS WellNames (wellID INTEGER NOT NULL, wellName VARCHAR(50) NOT NULL, FOREIGN KEY (wellID) REFERENCES Wells(wellID))");
            execUpdate("CREATE TABLE IF NOT EXISTS WellOwners (wellID INTEGER NOT NULL, ownerUUID UUID NOT NULL, FOREIGN KEY (wellID) REFERENCES Wells(wellID))");
            execUpdate("CREATE TABLE IF NOT EXISTS WellTriggers (wellID INTEGER NOT NULL, worldName VARCHAR(100), offX INTEGER NOT NULL, offY INTEGER NOT NULL, offZ INTEGER NOT NULL, FOREIGN KEY (wellID) REFERENCES Wells(wellID))");
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
    public void getWellsNear(Location loc, List<Location> outList) {

    }

    @Override
    public Well getWellByID(int id) {
        String name = getWellName(id);
        UUID owner = getWellOwner(id);
        Location loc = getWellLocation(id);

        Well well = new Well(loc, name);
        well.setDbID(id);
        well.setOwner(owner);
        return well;
    }

    @Override
    public void createWell(Well well) {
        try {
            Location loc = well.getLocation();
            insertWellLocStatement.setString(1, loc.getWorld().getName());
            insertWellLocStatement.setInt(2, loc.getBlockX());
            insertWellLocStatement.setInt(3, loc.getBlockY());
            insertWellLocStatement.setInt(4, loc.getBlockZ());
            if (insertWellLocStatement.executeUpdate() < 1) {
                throw new RuntimeException("Unable to insert well into database.");
            }

            getWellFromLoc.setString(1, loc.getWorld().getName());
            getWellFromLoc.setInt(2, loc.getBlockX());
            getWellFromLoc.setInt(3, loc.getBlockY());
            getWellFromLoc.setInt(4, loc.getBlockZ());
            ResultSet result = getWellFromLoc.executeQuery();

            int id;
            if (result.next()) {
                id = result.getInt(1);
                well.setDbID(id);
            } else {
                throw new RuntimeException("Well was incorrectly added to database: entry missing");
            }

            insertWellNameStatement.setInt(1, id);
            insertWellNameStatement.setString(2, well.getName());
            if (insertWellNameStatement.executeUpdate() < 1) {
                throw new RuntimeException("Error adding well name to database.");
            }

            insertWellTriggerStatement.setInt(1, id);
            insertWellTriggerStatement.setString(2, well.getLocation().getWorld().getName());
            insertWellTriggerStatement.setInt(3, getTriggerX(well));
            insertWellTriggerStatement.setInt(4, getTriggerY(well));
            insertWellTriggerStatement.setInt(5, getTriggerZ(well));
            if (insertWellTriggerStatement.executeUpdate() < 1) {
                throw new RuntimeException("Error adding well trigger to database.");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Unable to save well", e);
        }
    }

    @Override
    public int getWellFromTrigger(Location l) {
        try {
            getWellFromTrigger.setString(1, l.getWorld().getName());
            getWellFromTrigger.setInt(2, l.getBlockX());
            getWellFromTrigger.setInt(3, l.getBlockY());
            getWellFromTrigger.setInt(4, l.getBlockZ());

            ResultSet results = getWellFromTrigger.executeQuery();
            if (!results.next()) {
                return -1;
            } else {
                return results.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Exception looking up well by trigger", e);
        }
    }

    @Override
    public String getWellName(int wellID) {
        try {
            getWellNameStatement.setInt(1, wellID);

            ResultSet results = getWellNameStatement.executeQuery();
            if (!results.next()) {
                return null;
            } else {
                return results.getString(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Exception getting well name", e);
        }
    }

    @Override
    public UUID getWellOwner(int wellID) {
        try {
            getWellOwnerStatement.setInt(1, wellID);

            ResultSet results = getWellOwnerStatement.executeQuery();
            if (!results.next()) {
                return null;
            } else {
                return UUID.fromString(results.getString(1));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Exception getting well name", e);
        }
    }

    @Override
    public Location getWellLocation(int wellID) {
        try {
            getWellLocationStatement.setInt(1, wellID);

            ResultSet results = getWellLocationStatement.executeQuery();
            if (!results.next()) {
                return null;
            } else {
                return new Location(plugin.getServer().getWorld(results.getString(1)), results.getInt(2), results.getInt(3), results.getInt(4));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Exception getting well name", e);
        }
    }

    private int getTriggerX(Well well) {
        return well.getLocation().getBlockX() + plugin.getStructureManager().getWellStruct().getTriggerOffX();
    }

    private int getTriggerY(Well well) {
        return well.getLocation().getBlockY() + plugin.getStructureManager().getWellStruct().getTriggerOffY();
    }

    private int getTriggerZ(Well well) {
        return well.getLocation().getBlockZ() + plugin.getStructureManager().getWellStruct().getTriggerOffZ();
    }
}
