package rpg.extra.core.command;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import rpg.extra.core.OreliaExtraPlugin;

public final class ExtraAdminCommand implements CommandExecutor {

    private final OreliaExtraPlugin plugin;

    public ExtraAdminCommand(OreliaExtraPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0 || !args[0].equalsIgnoreCase("reload")) {
            sender.sendMessage(ChatColor.YELLOW + "Usage: /rpgextraadmin reload");
            return true;
        }
        plugin.reload();
        sender.sendMessage(ChatColor.GREEN + "Orelia Extra configuration reloaded.");
        return true;
    }
}
