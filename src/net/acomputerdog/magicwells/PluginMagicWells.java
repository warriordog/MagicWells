package net.acomputerdog.magicwells;

import net.acomputerdog.magicwells.structure.StructureManager;
import net.acomputerdog.magicwells.well.WellList;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;

public class PluginMagicWells extends JavaPlugin {
    private WellList wellList;
    private StructureManager structureManager;
    private MWEventHandler eventHandler;
    private MWCommandHandler commandHandler;

    @Override
    public void onEnable() {
        try {
            // create plugin directory and any missing config file
            setupPluginDir();

            wellList = new WellList(this);
            structureManager = new StructureManager(this);
            eventHandler = new MWEventHandler(this);
            commandHandler = new MWCommandHandler(this);

            // load wells
            wellList.connect();

            // load structures
            structureManager.load();

            // register event handler
            eventHandler.register();
        } catch (Exception e) {
            getLogger().severe("Unhandled error starting plugin.  MagicWells will be disabled.");
            e.printStackTrace();

            onDisable();
            super.setEnabled(false);
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    private void setupPluginDir() {
        // create dir
        if (!getDataFolder().isDirectory() && !getDataFolder().mkdir()) {
            getLogger().warning("Unable to create plugin data directory, do you have permission for this folder?");
        }

        // create config file
        saveDefaultFile("config.yml");

        // create syllables file
        saveDefaultFile("syllables.lst");

        // create structure files
        saveDefaultFile("structure/well.structure");
    }

    private void saveDefaultFile(String name) {
        File out = new File(getDataFolder(), name);
        if (!out.exists()) {
            getLogger().info("Extracting resource: " + name);
            copyFile(out, getClass().getResourceAsStream("/".concat(name)));
        }
    }

    private void copyFile(File file, InputStream in) {
        try (OutputStream out = new FileOutputStream(file)) {
            byte[] buff = new byte[64];
            while (in.available() > 0) {
                int count = in.read(buff);
                out.write(buff, 0, count);
            }
        } catch (IOException e) {
            getLogger().warning("IO error while saving file: " + file.getPath());
        } finally {
            try {
                in.close();
            } catch (IOException ignored) {
                // no need to catch error when we are just closing a stream
            }
        }
    }

    @Override
    public void onDisable() {
        try {
            if (wellList != null) {
                // disconnect from database
                wellList.disconnect();
                wellList = null;
            }

            structureManager = null;
            eventHandler = null;
            commandHandler = null;

            // cancel any events from this plugin
            getServer().getScheduler().cancelTasks(this);
        } catch (Exception e) {
            getLogger().warning("Unhandled exception while shutting down plugin.");
            e.printStackTrace();
        }
    }


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        return commandHandler.onCommand(sender, command, label, args);
    }
}
