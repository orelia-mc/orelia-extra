package rpg.extra.auction.gui;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import rpg.extra.auction.model.AuctionListing;
import rpg.extra.auction.service.AuctionService;
import rpg.gui.framework.Gui;
import rpg.gui.framework.GuiButton;
import rpg.util.ColorUtil;
import rpg.util.ItemBuilder;

import java.util.List;

/**
 * Browse/buy screen for active auction listings (SOW AuctionModule). Reuses orelia-core's
 * generic {@code Gui}/{@code GuiButton} framework directly, the same way MailGuiScreen does.
 */
public final class AuctionGuiScreen {

    private final AuctionService auctionService;

    public AuctionGuiScreen(AuctionService auctionService) {
        this.auctionService = auctionService;
    }

    public Gui build(Player viewer) {
        Gui gui = new Gui(ColorUtil.colorize("&%8オークション"), 54);
        List<AuctionListing> listings = auctionService.getActiveListings();

        int slot = 0;
        for (AuctionListing listing : listings) {
            if (slot >= 54) {
                break;
            }
            boolean own = listing.getSellerId().equals(viewer.getUniqueId());
            gui.set(slot++, new GuiButton(new ItemBuilder(listing.getItem().getType())
                    .name((own ? "&%e" : "&%f") + listing.getItem().getType().name())
                    .lore(List.of(
                            "&%7出品者: &%f" + listing.getSellerName(),
                            "&%7価格: &%6" + listing.getPrice(),
                            own ? "&%8クリックでキャンセル" : "&%aクリックで購入"))
                    .build(), (clicker, clickType) -> {
                AuctionService.ActionResult result = own
                        ? auctionService.cancel(clicker, listing.getId())
                        : auctionService.buy(clicker, listing.getId());
                clicker.sendMessage(result == AuctionService.ActionResult.OK
                        ? ChatColor.GREEN + (own ? "出品を取り消しました。" : "購入しました。")
                        : ChatColor.RED + "処理に失敗しました: " + result);
            }));
        }
        if (listings.isEmpty()) {
            gui.set(22, new GuiButton(new ItemBuilder(Material.BARRIER).name("&%7出品がありません").build(), (clicker, clickType) -> {
            }));
        }
        return gui;
    }
}
