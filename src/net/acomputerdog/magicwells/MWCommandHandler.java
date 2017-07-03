package net.acomputerdog.magicwells;

import net.acomputerdog.magicwells.well.Well;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MWCommandHandler {
    private final PluginMagicWells plugin;

    public MWCommandHandler(PluginMagicWells plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        switch (command.getName()) {
            case "mwreload":
                handleReload(sender);
                break;
            case "mwlist":
                handleList(sender);
                break;
            case "mwport":
                handlePort(sender, args);
                break;
            case "mwrecheck":
                handleRecheck(sender);
                break;
            case "mwinfo":
                handleInfo(sender, args);
                break;
            case "mwrepair":
                handleRepair(sender, args);
                break;
            case "mwrename":
                handleRename(sender, args);
                break;
            case "mwsethome":
                handleSethome(sender, args);
                break;
            default:
                sendRed(sender, "Internal error: that command was not recognised by MagicWells.  Please report this!");
        }
        return true;
    }

    private boolean checkPerms(CommandSender sender, String perm) {
        if (!sender.hasPermission("magicwells.command." + perm)) {
            sendRed(sender, "You do not have permission to use that command.");
            return false;
        }
        return true;
    }

    private void handleReload(CommandSender sender) {
        if (checkPerms(sender, "mwreload")) {
            sendAqua(sender, "Reloading...");
            plugin.onDisable();
            plugin.onEnable();
            sendAqua(sender, "Done.");
        }
    }

    private void handleList(CommandSender sender) {
        if (checkPerms(sender, "mwlist")) {
            sendYellow(sender, "Sorry, that command is not implemented.");
        }
    }

    private void handlePort(CommandSender sender, String[] args) {
        if (checkPerms(sender, "mwport")) {
            sendYellow(sender, "Sorry, that command is not implemented.");
        }
    }

    private void handleRecheck(CommandSender sender) {
        if (checkPerms(sender, "mwrecheck")) {
            sendYellow(sender, "Sorry, that command is not implemented.");
        }
    }

    private void handleInfo(CommandSender sender, String[] args) {
        if (checkPerms(sender, "mwinfo")) {
            if (sender instanceof Player) {
                Player p = (Player) sender;

                // get wells
                Well[] wells;
                if (args.length == 1) {
                    String wellName = args[0];
                    wells = plugin.getWellList().getWellsByOwnerAndName(p.getUniqueId(), wellName);
                } else {
                    wells = plugin.getWellList().getWellsByOwner(p.getUniqueId());
                }

                // display info
                sendYellow(p, "Your wells:");
                for (Well well : wells) {
                    StringBuilder m = new StringBuilder();
                    m.append(ChatColor.YELLOW);
                    m.append(well.getName());
                    m.append(ChatColor.AQUA);
                    m.append(" - ID");
                    m.append(well.getDbID());
                    m.append(" (");
                    m.append(well.getLocation().getBlockX());
                    m.append(", ");
                    m.append(well.getLocation().getBlockY());
                    m.append(", ");
                    m.append(well.getLocation().getBlockZ());
                    m.append(')');

                    p.sendMessage(m.toString());
                }
            } else {
                sendRed(sender, "This command can only be used by a player.");
            }
        }
    }

    private void handleRepair(CommandSender sender, String[] args) {
        if (checkPerms(sender, "mwrepair")) {
            sendYellow(sender, "Sorry, that command is not implemented.");
        }
    }

    private void handleRename(CommandSender sender, String[] args) {
        if (checkPerms(sender, "mwrename")) {
            sendYellow(sender, "Sorry, that command is not implemented.");
        }
    }

    private void handleSethome(CommandSender sender, String[] args) {
        if (checkPerms(sender, "mwsethome")) {
            if (sender instanceof Player) {
                Player p = (Player) sender;
                if (args.length == 1) {

                    Well well;
                    try {
                        int id = Integer.parseInt(args[0]);
                        well = plugin.getWellList().getWellByID(id);
                    } catch (NumberFormatException e) {
                        Well[] wells = plugin.getWellList().getWellsByOwnerAndName(p.getUniqueId(), args[0]);
                        if (wells.length == 0) {
                            well = null;
                        } else {
                            if (wells.length > 1) {
                                sendRed(p, "Multiple wells matched that name, the first will be selected.");
                            }

                            well = wells[0];
                        }
                    }

                    if (well != null) {
                        if (p.getUniqueId().equals(well.getOwner())) {
                            plugin.getWellList().setHomeWell(p.getUniqueId(), well);
                            p.sendMessage(ChatColor.AQUA + "Your home well is now " + ChatColor.YELLOW + well.getName() + ChatColor.AQUA + ".");
                        } else {
                            sendRed(sender, "You are not the owner of that well.");
                        }
                    } else {
                        sendRed(sender, "No well could be found by that name or id.");
                    }
                } else {
                    sendRed(sender, "You must specify the well name or ID to use as home.");
                }
            } else {
                sendRed(sender, "This command can only be used by a player.");
            }
        }
    }

    private static void sendYellow(CommandSender sender, String message) {
        sender.sendMessage(ChatColor.YELLOW + message);
    }

    private static void sendAqua(CommandSender sender, String message) {
        sender.sendMessage(ChatColor.AQUA + message);
    }

    private static void sendRed(CommandSender sender, String message) {
        sender.sendMessage(ChatColor.RED + message);
    }
}
