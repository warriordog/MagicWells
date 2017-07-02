package net.acomputerdog.magicwells;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class MWCommandHandler {
    private final PluginMagicWells plugin;

    public MWCommandHandler(PluginMagicWells plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        switch (command.getName()) {

            default:
                sendRed(sender, "Internal error: that command was not recognised by MagicWells.  Please report this!");
                return true;
        }
    }

    private static void sendRed(CommandSender sender, String message) {
        sender.sendMessage(ChatColor.RED + message);
    }
}
