package net.acomputerdog.magicwells.structure;

import net.acomputerdog.magicwells.PluginMagicWells;
import org.bukkit.World;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class StructureManager {
    private final PluginMagicWells plugin;
    private final MWPopulator populator;

    private Structure wellStruct;

    public StructureManager(PluginMagicWells plugin) {
        this.plugin = plugin;
        this.populator = new MWPopulator(this);
    }

    public void load() throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(new File(plugin.getDataFolder(), "structure/well.structure")))) {
            wellStruct = new Structure(populator, reader);
        }
    }

    public void injectPopulator(World world) {
        world.getPopulators().add(populator);
    }

    public PluginMagicWells getPlugin() {
        return plugin;
    }

    public Structure getWellStruct() {
        return wellStruct;
    }
}
