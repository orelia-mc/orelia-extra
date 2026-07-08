package rpg.extra.housing.command;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import rpg.extra.housing.model.HousePlot;
import rpg.extra.housing.service.HousingService;

import java.util.Map;

/**
 * {@code /house [list|buy <plotId>|home]} (SOW HousingModule).
 */
public final class HousingCommand implements CommandExecutor {

    private final HousingService housingService;

    public HousingCommand(HousingService housingService) {
        this.housingService = housingService;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Players only.");
            return true;
        }
        if (args.length == 0) {
            sender.sendMessage(ChatColor.YELLOW + "Usage: /house [list|buy <plotId>|home]");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "list" -> {
                Map<String, HousePlot> available = housingService.getAvailablePlots();
                if (available.isEmpty()) {
                    sender.sendMessage(ChatColor.YELLOW + "購入可能な土地はありません。");
                    return true;
                }
                sender.sendMessage(ChatColor.GREEN + "購入可能な土地:");
                available.values().forEach(plot -> sender.sendMessage(
                        ChatColor.GRAY + "- " + plot.getId() + " (" + plot.getName() + ") " + plot.getPrice() + "G"));
            }
            case "buy" -> {
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.YELLOW + "Usage: /house buy <plotId>");
                    return true;
                }
                report(sender, housingService.purchase(player, args[1]));
            }
            case "home" -> report(sender, housingService.teleportHome(player));
            default -> sender.sendMessage(ChatColor.YELLOW + "Usage: /house [list|buy <plotId>|home]");
        }
        return true;
    }

    private void report(CommandSender sender, HousingService.ActionResult result) {
        if (result == HousingService.ActionResult.OK) {
            sender.sendMessage(ChatColor.GREEN + "OK");
            return;
        }
        String message = switch (result) {
            case PLOT_NOT_FOUND -> "指定した土地は存在しません。";
            case PLOT_TAKEN -> "その土地は既に購入されています。";
            case ALREADY_OWN_A_HOUSE -> "既に自宅を所有しています。";
            case INSUFFICIENT_FUNDS -> "所持金が足りません。";
            case NO_HOUSE -> "自宅を所有していません。/house list で購入できます。";
            case WORLD_NOT_FOUND -> "自宅のワールドが見つかりません。";
            case OK -> "OK";
        };
        sender.sendMessage(ChatColor.RED + message);
    }
}
