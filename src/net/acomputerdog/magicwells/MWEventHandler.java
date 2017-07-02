package net.acomputerdog.magicwells;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldInitEvent;

public class MWEventHandler implements Listener {
    private final PluginMagicWells plugin;

    public MWEventHandler(PluginMagicWells plugin) {
        this.plugin = plugin;
    }

    public void register() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onWorldLoad(WorldInitEvent e) {
        // load world generator
        plugin.getLogger().info("Injecting into world.");
        plugin.getStructureManager().injectPopulator(e.getWorld());
    }
}
