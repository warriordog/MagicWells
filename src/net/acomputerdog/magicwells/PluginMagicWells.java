package net.acomputerdog.magicwells;

import net.acomputerdog.magicwells.structure.StructureManager;
import net.acomputerdog.magicwells.well.WellList;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;

public class PluginMagicWells extends JavaPlugin {
    private StructureManager structureManager;
    private WellList wellList;

    @Override
    public void onEnable() {
        try {
            // create plugin directory and any missing config file
            setupPluginDir();

        } catch (Exception e) {
            getLogger().severe("Unhandled error starting plugin.  MagicWells will be disabled.");
            e.printStackTrace();
            super.setEnabled(false);
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

    }
}
