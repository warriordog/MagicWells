package net.acomputerdog.magicwells;

import net.acomputerdog.magicwells.structure.Structure;
import net.acomputerdog.magicwells.well.Well;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.inventory.EquipmentSlot;

public class MWEventHandler implements Listener {
    private final PluginMagicWells plugin;

    private Location tempLocation = new Location(null, 0, 0, 0);

    public MWEventHandler(PluginMagicWells plugin) {
        this.plugin = plugin;
    }

    public void register() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    private void handleBlockInteract(Block b, Player p) {
        Structure wellStruct = plugin.getStructureManager().getWellStruct();

        // make sure they are hitting the right type of block
        if (b.getType() == wellStruct.getTriggerBlock()) {
            b.getLocation(tempLocation);

            int wellID = plugin.getWellList().getIDByTrigger(tempLocation);
            if (wellID > -1) {
                Well well = getWell(wellID);
                p.sendMessage(ChatColor.BLUE + "You touched a well: " + well.getName());
            }
        }
    }

    private Well getWell(int id) {
        return plugin.getWellList().getWellByID(id);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockInteract(PlayerInteractEvent e) {
        if (e.getHand() == EquipmentSlot.HAND) {
            switch (e.getAction()) {
                case RIGHT_CLICK_BLOCK:
                case LEFT_CLICK_BLOCK:
                    handleBlockInteract(e.getClickedBlock(), e.getPlayer());
                    break;
                default:
                    break;
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onWorldLoad(WorldInitEvent e) {
        // load world generator
        plugin.getLogger().info("Injecting into world.");
        plugin.getStructureManager().injectPopulator(e.getWorld());
    }
}
