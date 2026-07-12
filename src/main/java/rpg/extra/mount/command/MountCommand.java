package rpg.extra.mount.command;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import rpg.extra.mount.model.MountDefinition;
import rpg.extra.mount.service.MountService;

import java.util.Map;
import java.util.Set;

/**
 * {@code /ol mount [list|buy <id>|summon [id]|dismiss]} (SOW MountModule).
 */
public final class MountCommand implements CommandExecutor {

    private final MountService mountService;

    public MountCommand(MountService mountService) {
        this.mountService = mountService;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Players only.");
            return true;
        }
        if (args.length == 0 || args[0].equalsIgnoreCase("list")) {
            listMounts(sender, player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "buy" -> {
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.YELLOW + "Usage: /ol mount buy <id>");
                    return true;
                }
                report(sender, mountService.unlock(player, args[1]));
            }
            case "summon" -> {
                String mountId = args.length >= 2 ? args[1] : mountService.getSelectedMountId(player.getUniqueId());
                if (mountId == null) {
                    sender.sendMessage(ChatColor.YELLOW + "Usage: /ol mount summon <id>");
                    return true;
                }
                report(sender, mountService.summon(player, mountId));
            }
            case "dismiss" -> report(sender, mountService.dismiss(player));
            default -> sender.sendMessage(ChatColor.YELLOW + "Usage: /ol mount [list|buy <id>|summon [id]|dismiss]");
        }
        return true;
    }

    private void listMounts(CommandSender sender, Player player) {
        Map<String, MountDefinition> all = mountService.getAllMounts();
        Set<String> unlocked = mountService.getUnlockedMounts(player.getUniqueId());
        if (all.isEmpty()) {
            sender.sendMessage(ChatColor.YELLOW + "利用可能な乗り物がありません。");
            return;
        }
        sender.sendMessage(ChatColor.GREEN + "乗り物一覧:");
        all.values().forEach(mount -> {
            boolean owned = unlocked.contains(mount.getId());
            sender.sendMessage(ChatColor.GRAY + "- " + mount.getId() + " (" + mount.getName() + ") "
                    + (owned ? ChatColor.GREEN + "所持済み" : ChatColor.GOLD + (mount.getPrice() + "G")));
        });
    }

    private void report(CommandSender sender, MountService.ActionResult result) {
        if (result == MountService.ActionResult.OK) {
            sender.sendMessage(ChatColor.GREEN + "OK");
            return;
        }
        String message = switch (result) {
            case MOUNT_NOT_FOUND -> "指定した乗り物は存在しません。";
            case ALREADY_UNLOCKED -> "既に所持しています。";
            case NOT_UNLOCKED -> "その乗り物はまだ購入していません。";
            case INSUFFICIENT_FUNDS -> "所持金が足りません。";
            case NO_ACTIVE_MOUNT -> "呼び出し中の乗り物がいません。";
            case OK -> "OK";
        };
        sender.sendMessage(ChatColor.RED + message);
    }
}
