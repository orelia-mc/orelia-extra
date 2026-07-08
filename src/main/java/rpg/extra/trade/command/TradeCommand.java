package rpg.extra.trade.command;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import rpg.extra.trade.model.TradeSession;
import rpg.extra.trade.service.TradeService;

/**
 * {@code /trade <player>|accept|add|remove <index>|confirm|cancel|view} (SOW TradeModule).
 */
public final class TradeCommand implements CommandExecutor {

    private final TradeService tradeService;

    public TradeCommand(TradeService tradeService) {
        this.tradeService = tradeService;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Players only.");
            return true;
        }
        if (args.length == 0) {
            sender.sendMessage(ChatColor.YELLOW + "Usage: /trade <player>|accept|add|remove <index>|confirm|cancel|view");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "accept" -> {
                TradeService.ActionResult result = tradeService.accept(player);
                if (result == TradeService.ActionResult.OK) {
                    player.sendMessage(ChatColor.GREEN + "取引を開始しました。/trade add でアイテムを追加できます。");
                } else {
                    report(sender, result);
                }
            }
            case "add" -> report(sender, tradeService.addHeldItem(player));
            case "remove" -> {
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.YELLOW + "Usage: /trade remove <index>");
                    return true;
                }
                try {
                    report(sender, tradeService.removeOfferedItem(player, Integer.parseInt(args[1])));
                } catch (NumberFormatException e) {
                    sender.sendMessage(ChatColor.RED + "数値を指定してください。");
                }
            }
            case "confirm" -> {
                boolean executed = tradeService.confirm(player);
                player.sendMessage(executed
                        ? ChatColor.GREEN + "取引が成立しました！"
                        : ChatColor.YELLOW + "確定しました。相手の確定を待っています。");
            }
            case "cancel" -> report(sender, tradeService.cancel(player));
            case "view" -> showOffers(sender, player);
            default -> {
                Player target = Bukkit.getPlayerExact(args[0]);
                if (target == null) {
                    sender.sendMessage(ChatColor.RED + "Player not found: " + args[0]);
                    return true;
                }
                TradeService.ActionResult result = tradeService.request(player, target);
                report(sender, result);
                if (result == TradeService.ActionResult.OK) {
                    target.sendMessage(ChatColor.GREEN + player.getName() + "から取引の申し込みが届きました。/trade accept で応じられます。");
                }
            }
        }
        return true;
    }

    private void showOffers(CommandSender sender, Player player) {
        TradeSession session = tradeService.getSession(player.getUniqueId()).orElse(null);
        if (session == null) {
            sender.sendMessage(ChatColor.YELLOW + "取引中ではありません。");
            return;
        }
        sender.sendMessage(ChatColor.GREEN + "あなたの提示アイテム:");
        printOffer(sender, session.offerOf(player.getUniqueId()).getItems());
        sender.sendMessage(ChatColor.GREEN + "相手の提示アイテム:");
        printOffer(sender, session.offerOf(session.getOtherPlayer(player.getUniqueId())).getItems());
    }

    private void printOffer(CommandSender sender, java.util.List<ItemStack> items) {
        if (items.isEmpty()) {
            sender.sendMessage(ChatColor.GRAY + "(なし)");
            return;
        }
        for (int i = 0; i < items.size(); i++) {
            ItemStack item = items.get(i);
            sender.sendMessage(ChatColor.GRAY + "[" + i + "] " + item.getType() + " x" + item.getAmount());
        }
    }

    private void report(CommandSender sender, TradeService.ActionResult result) {
        if (result == TradeService.ActionResult.OK) {
            sender.sendMessage(ChatColor.GREEN + "OK");
            return;
        }
        String message = switch (result) {
            case ALREADY_TRADING -> "既に取引中です。";
            case NOT_TRADING -> "取引中ではありません。";
            case NO_PENDING_REQUEST -> "取引の申し込みが届いていません。";
            case CANNOT_TARGET_SELF -> "自分自身とは取引できません。";
            case EMPTY_HAND -> "手にアイテムを持っていません。";
            case INVALID_SLOT -> "指定した番号のアイテムはありません。";
            case OK -> "OK";
        };
        sender.sendMessage(ChatColor.RED + message);
    }
}
