package net.acomputerdog.magicwells.db;

import net.acomputerdog.magicwells.PluginMagicWells;
import net.acomputerdog.magicwells.well.Well;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.sql.*;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class InternalDB implements WellDB {
    private final PluginMagicWells plugin;

    private Connection connection;

    private Statement sharedStatement;

    private PreparedStatement insertWellLocStatement;

    private PreparedStatement getWellFromLoc;
    private PreparedStatement getWellFromTrigger;
    private PreparedStatement getWellsInArea;
    private PreparedStatement getWellFromBBStatement;
    private PreparedStatement getWellsFromOwner;
    private PreparedStatement getWellsFromOwnerAndName;

    private PreparedStatement getWellNameStatement;
    private PreparedStatement insertWellNameStatement;
    private PreparedStatement updateWellNameStatement;

    private PreparedStatement getWellOwnerStatement;
    private PreparedStatement insertWellOwnerStatement;
    private PreparedStatement updateWellOwnerStatement;
    private PreparedStatement deleteWellOwnerStatement;

    private PreparedStatement getWellLocationStatement;

    private PreparedStatement insertWellTriggerStatement;

    private PreparedStatement insertWellBBStatement;
    private PreparedStatement getWellBB1Statement;
    private PreparedStatement getWellBB2Statement;

    private PreparedStatement numWellBBsInArea;

    private PreparedStatement insertHomeWellStatement;
    private PreparedStatement updateHomeWellStatement;
    private PreparedStatement getHomeWellStatement;

    private PreparedStatement getNumberOfWellsStatement;

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
            // this one is owner THEN id
            updateWellOwnerStatement = connection.prepareStatement("UPDATE WellOwners SET ownerUUID = ? WHERE wellID = ?");
            deleteWellOwnerStatement = connection.prepareStatement("DELETE FROM WellOwners WHERE wellID = ?");

            insertWellTriggerStatement = connection.prepareStatement("INSERT INTO WellTriggers(wellID, worldName, offX, offY, offZ) VALUES (?, ?, ?, ?, ?)");
            getWellFromTrigger = connection.prepareStatement("SELECT wellID FROM WellTriggers WHERE worldName = ? AND offX = ? AND offY = ? AND offZ = ?");

            getWellLocationStatement = connection.prepareStatement("SELECT worldName, locX, locY, locZ FROM Wells WHERE wellID = ?");

            getWellsInArea = connection.prepareStatement("SELECT wellID FROM Wells WHERE locX >= ? AND locX <= ? AND locZ >= ? AND locZ <= ?");

            insertWellBBStatement = connection.prepareStatement("INSERT INTO WellBBs(wellID, worldName, x1, y1, z1, x2, y2, z2) VALUES(?, ?, ?, ?, ?, ?, ?, ?)");
            getWellFromBBStatement = connection.prepareStatement("SELECT wellID FROM WellBBs WHERE worldName = ? AND x1 <= ? AND y1 <= ? AND z1 <= ? AND x2 >= ? AND y2 >= ? AND z2 >= ?");

            getWellBB1Statement = connection.prepareStatement("SELECT worldName, x1, y1, z1 FROM WellBBs WHERE wellID = ?");
            getWellBB2Statement = connection.prepareStatement("SELECT worldName, x2, y2, z2 FROM WellBBs WHERE wellID = ?");

            getWellsFromOwner = connection.prepareStatement("SELECT wellID FROM WellOwners WHERE ownerUUID = ?");
            getWellsFromOwnerAndName = connection.prepareStatement("SELECT WellOwners.wellID FROM WellOwners INNER JOIN WellNames ON WellOwners.wellID = WellNames.wellID WHERE WellOwners.ownerUUID = ? AND wellNames.wellName LIKE ?");

            //X1 X2 X1 X2 Z1 Z2 Z1 Z2
            numWellBBsInArea = connection.prepareStatement("SELECT COUNT(DISTINCT wellID) FROM WellBBs WHERE ((x1 >= ? AND x1 <= ?) OR (x2 >= ? AND x2 >= ?)) AND ((z1 >= ? AND z1 <= ?) OR (z2 >= ? AND z2 >= ?))");

            insertHomeWellStatement = connection.prepareStatement("INSERT INTO WellHomes(ownerUUID, wellID) VALUES(?, ?)");
            // backwards, wellID then ownerUUID
            updateHomeWellStatement = connection.prepareStatement("UPDATE WellHomes SET wellID = ? WHERE ownerUUID = ?");
            getHomeWellStatement = connection.prepareStatement("SELECT wellID FROM WellHomes WHERE ownerUUID = ?");

            // no need for DISTINCT because wellID is the primary key
            getNumberOfWellsStatement = connection.prepareStatement("SELECT COUNT(wellID) FROM Wells");
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
            execUpdate("CREATE TABLE IF NOT EXISTS WellOwners (wellID INTEGER NOT NULL, ownerUUID CHAR(36) NOT NULL, FOREIGN KEY (wellID) REFERENCES Wells(wellID))");
            execUpdate("CREATE TABLE IF NOT EXISTS WellTriggers (wellID INTEGER NOT NULL, worldName VARCHAR(100), offX INTEGER NOT NULL, offY INTEGER NOT NULL, offZ INTEGER NOT NULL, FOREIGN KEY (wellID) REFERENCES Wells(wellID))");
            execUpdate("CREATE TABLE IF NOT EXISTS WellBBs (wellID INTEGER NOT NULL, worldName VARCHAR(100), x1 INTEGER NOT NULL, y1 INTEGER NOT NULL, z1 INTEGER NOT NULL, x2 INTEGER NOT NULL, y2 INTEGER NOT NULL, z2 INTEGER NOT NULL, FOREIGN KEY (wellID) REFERENCES Wells(wellID))");
            execUpdate("CREATE TABLE IF NOT EXISTS WellHomes (ownerUUID CHAR(36), wellID INTEGER NOT NULL, FOREIGN KEY (wellID) REFERENCES Wells(wellID))");
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
    public Well getWellByID(int wellID) {
        String name = getWellName(wellID);
        UUID owner = getWellOwner(wellID);
        Location loc = getWellLocation(wellID);
        Location bb1 = getWellBB1(wellID);
        Location bb2 = getWellBB2(wellID);

        Well well = new Well(loc, bb2, bb1, name);
        well.setDbID(wellID);
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

            insertWellBBStatement.setInt(1, id);
            insertWellBBStatement.setString(2, well.getLocation().getWorld().getName());
            insertWellBBStatement.setInt(3, well.getLocation().getBlockX() + well.getBB1().getBlockX());
            insertWellBBStatement.setInt(4, well.getLocation().getBlockY() - well.getBB1().getBlockY());
            insertWellBBStatement.setInt(5, well.getLocation().getBlockZ() + well.getBB1().getBlockZ());
            insertWellBBStatement.setInt(6, well.getLocation().getBlockX() + well.getBB2().getBlockX());
            insertWellBBStatement.setInt(7, well.getLocation().getBlockY() - well.getBB2().getBlockY());
            insertWellBBStatement.setInt(8, well.getLocation().getBlockZ() + well.getBB2().getBlockZ());
            if (insertWellBBStatement.executeUpdate() < 1) {
                throw new RuntimeException("Error adding well BB to database.");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Unable to save well", e);
        }
    }

    @Override
    public int getWellIDFromTrigger(Location l) {
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

    @Override
    public int numWellsInRange(int x1, int z1, int x2, int z2) {
        try {
            getWellsInArea.setInt(1, x1);
            getWellsInArea.setInt(2, x2);
            getWellsInArea.setInt(3, z1);
            getWellsInArea.setInt(4, z2);

            ResultSet results = getWellsInArea.executeQuery();
            int count = 0;
            while (results.next()) {
                count++;
            }
            return count;
        } catch (SQLException e) {
            throw new RuntimeException("Unable to search for wells", e);
        }
    }

    @Override
    public Well getWellFromBB(Location l) {
        try {
            getWellFromBBStatement.setString(1, l.getWorld().getName());
            getWellFromBBStatement.setInt(2, l.getBlockX());
            getWellFromBBStatement.setInt(3, l.getBlockY());
            getWellFromBBStatement.setInt(4, l.getBlockZ());
            getWellFromBBStatement.setInt(5, l.getBlockX());
            getWellFromBBStatement.setInt(6, l.getBlockY());
            getWellFromBBStatement.setInt(7, l.getBlockZ());

            ResultSet results = getWellFromBBStatement.executeQuery();
            if (!results.next()) {
                return null;
            } else {
                return getWellByID(results.getInt(1));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Unable to search for wells", e);
        }
    }

    @Override
    public void setWellOwner(int wellID, UUID owner) {
        try {
            if (owner == null) {
                deleteWellOwnerStatement.setInt(1, wellID);

                if (deleteWellOwnerStatement.executeUpdate() < 0) {
                    throw new RuntimeException("Error deleting well owner.");
                }
            } else {
                // this one is backwards
                updateWellOwnerStatement.setInt(2, wellID);
                updateWellOwnerStatement.setString(1, owner.toString());

                int result = updateWellOwnerStatement.executeUpdate();
                if (result < 0) {
                    throw new RuntimeException("Error updating well owner.");
                } else if (result == 0) {
                    insertWellOwnerStatement.setInt(1, wellID);
                    insertWellOwnerStatement.setString(2, owner.toString());

                    if (insertWellOwnerStatement.executeUpdate() < 0) {
                        throw new RuntimeException("Error inserting well owner.");
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Unable to save well owner.", e);
        }
    }

    @Override
    public void setWellName(int wellID, String name) {
        try {
            // update, so backwards
            updateWellNameStatement.setString(1, name);
            updateWellNameStatement.setInt(2, wellID);

            if (updateWellNameStatement.executeUpdate() < 0) {
                throw new RuntimeException("Error saving well name.");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Unable to save well owner.", e);
        }
    }

    @Override
    public Well[] getWellsByOwnerAndName(UUID owner, String name) {
        try {
            getWellsFromOwnerAndName.setString(1, owner.toString());
            getWellsFromOwnerAndName.setString(2, name);

            List<Integer> wellIDs = new LinkedList<>();
            ResultSet res = getWellsFromOwnerAndName.executeQuery();
            while (res.next()) {
                wellIDs.add(res.getInt(1));
            }

            Well[] wells = new Well[wellIDs.size()];
            int i = 0;
            for (int id : wellIDs) {
                wells[i] = getWellByID(id);
                i++;
            }
            return wells;
        } catch (SQLException e) {
            throw new RuntimeException("Unable to look up wells by owner and name.", e);
        }
    }

    @Override
    public Well[] getWellsByOwner(UUID owner) {
        try {
            getWellsFromOwner.setString(1, owner.toString());

            List<Integer> wellIDs = new LinkedList<>();
            ResultSet res = getWellsFromOwner.executeQuery();
            while (res.next()) {
                wellIDs.add(res.getInt(1));
            }

            Well[] wells = new Well[wellIDs.size()];
            int i = 0;
            for (int id : wellIDs) {
                wells[i] = getWellByID(id);
                i++;
            }
            return wells;
        } catch (SQLException e) {
            throw new RuntimeException("Unable to look up wells by owner.", e);
        }
    }

    @Override
    public int numWellBBsInRange(int x1, int z1, int x2, int z2) {
        try {
            numWellBBsInArea.setInt(1, x1);
            numWellBBsInArea.setInt(2, x2);
            numWellBBsInArea.setInt(3, x1);
            numWellBBsInArea.setInt(4, x2);
            numWellBBsInArea.setInt(5, z1);
            numWellBBsInArea.setInt(6, z2);
            numWellBBsInArea.setInt(7, z1);
            numWellBBsInArea.setInt(8, z2);

            ResultSet res = numWellBBsInArea.executeQuery();
            if (!res.next()) {
                throw new RuntimeException("Error counting wells");
            }
            return res.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException("Unable to count wells in area.", e);
        }
    }

    @Override
    public Well getHomeWell(UUID owner) {
        try {
            getHomeWellStatement.setString(1, owner.toString());

            ResultSet res = getHomeWellStatement.executeQuery();
            if (!res.next()) {
                throw new RuntimeException("Error getting home well");
            }
            return getWellByID(res.getInt(1));
        } catch (SQLException e) {
            throw new RuntimeException("Exception getting home well", e);
        }
    }

    @Override
    public void setHomeWell(UUID owner, Well homeWell) {
        try {
            // this one is backwards
            updateHomeWellStatement.setInt(1, homeWell.getDbID());
            updateHomeWellStatement.setString(2, owner.toString());

            int result = updateHomeWellStatement.executeUpdate();
            if (result < 0) {
                throw new RuntimeException("Error updating home well.");
            } else if (result == 0) {
                insertHomeWellStatement.setString(1, owner.toString());
                insertHomeWellStatement.setInt(2, homeWell.getDbID());

                if (insertHomeWellStatement.executeUpdate() < 0) {
                    throw new RuntimeException("Error inserting home well.");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Unable to save home well.", e);
        }
    }

    @Override
    public int getNumberOfWells() {
        try {
            ResultSet res = getNumberOfWellsStatement.executeQuery();

            if (!res.next()) {
                throw new RuntimeException("Unable to get number of wells");
            }
            return res.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException("Unable to get number of wells.", e);
        }
    }


    private Location getWellBB1(int wellID) {
        try {
            getWellBB1Statement.setInt(1, wellID);

            ResultSet results = getWellBB1Statement.executeQuery();
            if (!results.next()) {
                return null;
            } else {
                return new Location(plugin.getServer().getWorld(results.getString(1)), results.getInt(2), results.getInt(3), results.getInt(4));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Exception getting well bounding box 1", e);
        }
    }

    private Location getWellBB2(int wellID) {
        try {
            getWellBB2Statement.setInt(1, wellID);

            ResultSet results = getWellBB2Statement.executeQuery();
            if (!results.next()) {
                return null;
            } else {
                return new Location(plugin.getServer().getWorld(results.getString(1)), results.getInt(2), results.getInt(3), results.getInt(4));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Exception getting well bounding box 2", e);
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
