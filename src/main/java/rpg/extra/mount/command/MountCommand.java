package rpg.extra.mount.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import rpg.core.message.MessageManager;
import rpg.extra.mount.model.MountDefinition;
import rpg.extra.mount.service.MountService;

import java.util.Map;
import java.util.Set;

/**
 * {@code /ol mount [list|buy <id>|summon [id]|dismiss]} (SOW MountModule).
 */
public final class MountCommand implements CommandExecutor {

    private final MountService mountService;
    private final MessageManager messages;

    public MountCommand(MountService mountService, MessageManager messages) {
        this.mountService = mountService;
        this.messages = messages;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            messages.send(sender, "command.player-only");
            return true;
        }
        if (args.length == 0 || args[0].equalsIgnoreCase("list")) {
            listMounts(sender, player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "buy" -> {
                if (args.length < 2) {
                    messages.send(sender, "usage.mount-buy");
                    return true;
                }
                report(sender, mountService.unlock(player, args[1]));
            }
            case "summon" -> {
                String mountId = args.length >= 2 ? args[1] : mountService.getSelectedMountId(player.getUniqueId());
                if (mountId == null) {
                    messages.send(sender, "usage.mount-summon");
                    return true;
                }
                report(sender, mountService.summon(player, mountId));
            }
            case "dismiss" -> report(sender, mountService.dismiss(player));
            default -> messages.send(sender, "usage.mount");
        }
        return true;
    }

    private void listMounts(CommandSender sender, Player player) {
        Map<String, MountDefinition> all = mountService.getAllMounts();
        Set<String> unlocked = mountService.getUnlockedMounts(player.getUniqueId());
        if (all.isEmpty()) {
            messages.send(sender, "mount.no-mounts-available");
            return;
        }
        messages.send(sender, "mount.list-header");
        all.values().forEach(mount -> {
            boolean owned = unlocked.contains(mount.getId());
            String status = owned ? messages.format("mount.owned-tag") : messages.format("mount.price-tag", "price", mount.getPrice());
            messages.sendRaw(sender, "mount.list-entry", "id", mount.getId(), "name", mount.getName(), "status", status);
        });
    }

    private void report(CommandSender sender, MountService.ActionResult result) {
        if (result == MountService.ActionResult.OK) {
            messages.send(sender, "command.ok");
            return;
        }
        String key = switch (result) {
            case MOUNT_NOT_FOUND -> "mount.not-found";
            case ALREADY_UNLOCKED -> "mount.already-unlocked";
            case NOT_UNLOCKED -> "mount.not-unlocked";
            case INSUFFICIENT_FUNDS -> "mount.insufficient-funds";
            case NO_ACTIVE_MOUNT -> "mount.no-active-mount";
            case OK -> "command.ok";
        };
        messages.send(sender, key);
    }
}
