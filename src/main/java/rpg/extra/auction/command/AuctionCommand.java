package rpg.extra.auction.command;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import rpg.extra.auction.gui.AuctionGuiScreen;
import rpg.extra.auction.model.AuctionListing;
import rpg.extra.auction.service.AuctionService;
import rpg.gui.framework.GuiManager;

import java.util.List;

/**
 * {@code /auction [list|sell <price>|collect]} (SOW AuctionModule). Plain {@code /auction}
 * opens the browse/buy GUI.
 */
public final class AuctionCommand implements CommandExecutor {

    private static final long DEFAULT_DURATION_MILLIS = 1000L * 60 * 60 * 24 * 3;

    private final AuctionService auctionService;
    private final AuctionGuiScreen guiScreen;
    private final GuiManager guiManager;

    public AuctionCommand(AuctionService auctionService, AuctionGuiScreen guiScreen, GuiManager guiManager) {
        this.auctionService = auctionService;
        this.guiScreen = guiScreen;
        this.guiManager = guiManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Players only.");
            return true;
        }
        if (args.length == 0 || args[0].equalsIgnoreCase("list")) {
            guiManager.open(player, guiScreen.build(player));
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "sell" -> {
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.YELLOW + "Usage: /auction sell <price>");
                    return true;
                }
                try {
                    double price = Double.parseDouble(args[1]);
                    AuctionService.ActionResult result = auctionService.list(player, price, DEFAULT_DURATION_MILLIS);
                    sender.sendMessage(result == AuctionService.ActionResult.OK
                            ? ChatColor.GREEN + "手に持っているアイテムを出品しました。"
                            : ChatColor.RED + "出品に失敗しました: " + result);
                } catch (NumberFormatException e) {
                    sender.sendMessage(ChatColor.RED + "価格は数値で指定してください。");
                }
            }
            case "collect" -> {
                List<AuctionListing> collectable = auctionService.getCollectable(player.getUniqueId());
                if (collectable.isEmpty()) {
                    sender.sendMessage(ChatColor.YELLOW + "受け取れるものはありません。");
                    return true;
                }
                int collected = 0;
                for (AuctionListing listing : collectable) {
                    if (auctionService.collect(player, listing.getId()) == AuctionService.ActionResult.OK) {
                        collected++;
                    }
                }
                sender.sendMessage(ChatColor.GREEN + (collected + "件の未売却アイテムを回収しました。"));
            }
            default -> sender.sendMessage(ChatColor.YELLOW + "Usage: /auction [list|sell <price>|collect]");
        }
        return true;
    }
}
