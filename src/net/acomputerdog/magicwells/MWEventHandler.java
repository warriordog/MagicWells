package net.acomputerdog.magicwells;

import org.bukkit.event.Listener;

public class MWEventHandler implements Listener {
    private final PluginMagicWells plugin;

    public MWEventHandler(PluginMagicWells plugin) {
        this.plugin = plugin;
    }

    public void register() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
}
