package net.acomputerdog.magicwells;

import net.acomputerdog.magicwells.structure.Structure;
import net.acomputerdog.magicwells.well.Well;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.metadata.FixedMetadataValue;

public class MWEventHandler implements Listener {
    private static final String PLAYER_WELL_NEARBY_KEY = "magicwells.player_well_nearby";
    private static final String PLAYER_IN_WELL_KEY = "magicwells.player_in_well";

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

                StringBuilder message = new StringBuilder();
                message.append(ChatColor.AQUA);
                message.append("You have found a well!  ");
                message.append(ChatColor.YELLOW);
                message.append(well.getName());
                message.append(ChatColor.AQUA);
                message.append(" is owned by ");
                message.append(ChatColor.YELLOW);

                if (well.getOwner() == null) {
                    message.append("no one");
                    message.append(ChatColor.AQUA);
                    message.append(".  Claim it by jumping in!");
                } else if (well.getOwner().equals(p.getUniqueId())) {
                    message.append("you.");
                } else {
                    OfflinePlayer op = plugin.getServer().getOfflinePlayer(well.getOwner());
                    message.append(op.getName());
                    message.append(ChatColor.AQUA);
                    message.append(".  Throw in nether blocks to break their claim!");
                }

                p.sendMessage(message.toString());
            }
        }
    }

    private Well getWell(int id) {
        return plugin.getWellList().getWellByID(id);
    }

    private void handlePlayerDiveInWell(Player p, Location loc, Well well) {
        if (well.getOwner() == null) {
            well.setOwner(p.getUniqueId());
            plugin.getWellList().saveWellOwner(well);
            p.sendMessage(ChatColor.AQUA + "You have claimed " + ChatColor.YELLOW + well.getName() + ChatColor.AQUA + "!");
        } else if (well.getOwner() == p.getUniqueId()) {
            p.sendMessage(ChatColor.AQUA + "This is your well, " + ChatColor.YELLOW + well.getName() + ChatColor.AQUA + ".");
        } else {
            p.sendMessage(ChatColor.RED + "The water around you churns and tries to drag you down.  This well belongs to someone else!");
        }
    }

    private void checkChunks(Chunk c, Player p) {
        if (plugin.getWellList().isWellInChunk(c)) {
            p.setMetadata(PLAYER_WELL_NEARBY_KEY, new FixedMetadataValue(plugin, true));
        } else {
            p.removeMetadata(PLAYER_WELL_NEARBY_KEY, plugin);
            p.removeMetadata(PLAYER_IN_WELL_KEY, plugin);
        }
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

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerLogin(PlayerJoinEvent e) {
        checkChunks(e.getPlayer().getLocation().getChunk(), e.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent e) {
        // TODO check for wells that overlap into this chunk (from another)
        if (e.getTo().getChunk() != e.getFrom().getChunk()) {
            checkChunks(e.getTo().getChunk(), e.getPlayer());
        }

        if (e.getTo().getBlockX() != e.getFrom().getBlockX() || e.getTo().getBlockY() != e.getFrom().getBlockY() || e.getTo().getBlockZ() != e.getFrom().getBlockZ()) {
            if (e.getPlayer().hasMetadata(PLAYER_WELL_NEARBY_KEY)) {
                Well well = plugin.getWellList().getWellByCollision(e.getTo());

                if (well != null) {
                    if (!e.getPlayer().hasMetadata(PLAYER_IN_WELL_KEY) ||
                            e.getPlayer().getMetadata(PLAYER_IN_WELL_KEY).get(0).asInt() != well.getDbID()) {
                        e.getPlayer().setMetadata(PLAYER_IN_WELL_KEY, new FixedMetadataValue(plugin, well.getDbID()));
                        handlePlayerDiveInWell(e.getPlayer(), e.getTo(), well);
                    }
                } else {
                    e.getPlayer().removeMetadata(PLAYER_IN_WELL_KEY, plugin);
                }
            }
        }
    }


}
