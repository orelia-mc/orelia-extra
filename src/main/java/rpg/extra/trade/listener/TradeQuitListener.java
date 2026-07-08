package rpg.extra.trade.listener;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import rpg.extra.trade.service.TradeService;

/** Cancels and refunds any in-progress trade the moment either side disconnects. */
public final class TradeQuitListener implements Listener {

    private final TradeService tradeService;

    public TradeQuitListener(TradeService tradeService) {
        this.tradeService = tradeService;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        tradeService.getSession(player.getUniqueId()).ifPresent(session -> {
            Player other = Bukkit.getPlayer(session.getOtherPlayer(player.getUniqueId()));
            if (other != null) {
                other.sendMessage(ChatColor.RED + player.getName() + "が切断したため取引はキャンセルされました。");
            }
        });
        tradeService.forceCancelIfTrading(player.getUniqueId());
    }
}
