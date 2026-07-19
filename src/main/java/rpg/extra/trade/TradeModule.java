package rpg.extra.trade;

import rpg.extra.core.OreliaExtraPlugin;
import rpg.extra.core.module.ExtraModule;
import rpg.extra.trade.command.TradeCommand;
import rpg.extra.trade.listener.TradeQuitListener;
import rpg.extra.trade.manager.TradeManager;
import rpg.extra.trade.service.TradeService;

/**
 * Trade module: two-player item trading with a confirm/confirm handshake (SOW TradeModule).
 */
public final class TradeModule implements ExtraModule {

    private final TradeManager manager = new TradeManager();
    private TradeService tradeService;

    @Override
    public String getName() {
        return "trade";
    }

    @Override
    public void onEnable(OreliaExtraPlugin plugin) {
        this.tradeService = new TradeService(manager);
        plugin.getServer().getPluginManager().registerEvents(new TradeQuitListener(tradeService, plugin.getMessageManager()), plugin);
        plugin.getPlayerCommandRegistry().register("trade", new TradeCommand(tradeService, plugin.getMessageManager()),
                "他プレイヤーとアイテムを取引します。", "trade <player>|accept|add|remove <index>|confirm|cancel|view");
    }

    @Override
    public void onDisable() {
    }

    public TradeService getTradeService() {
        return tradeService;
    }
}
