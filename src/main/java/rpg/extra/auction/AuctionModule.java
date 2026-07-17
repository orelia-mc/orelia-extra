package rpg.extra.auction;

import net.milkbowl.vault.economy.Economy;
import rpg.database.manager.DatabaseManager;
import rpg.extra.auction.command.AuctionCommand;
import rpg.extra.auction.gui.AuctionGuiScreen;
import rpg.extra.auction.repository.AuctionRepository;
import rpg.extra.auction.service.AuctionService;
import rpg.extra.core.OreliaExtraPlugin;
import rpg.extra.core.module.ExtraModule;
import rpg.gui.framework.GuiManager;

import java.util.logging.Level;

/**
 * Auction module: player-run auction house with timed listings (SOW AuctionModule). Money
 * settles through Vault's {@link Economy}.
 */
public final class AuctionModule implements ExtraModule {

    private static final long EXPIRY_CHECK_PERIOD_TICKS = 20L * 60;

    private AuctionService auctionService;
    private AuctionGuiScreen guiScreen;

    @Override
    public String getName() {
        return "auction";
    }

    @Override
    public void onEnable(OreliaExtraPlugin plugin) {
        DatabaseManager databaseManager = plugin.getServer().getServicesManager().load(DatabaseManager.class);
        if (databaseManager == null) {
            throw new IllegalStateException("auction module requires OreliaCore's DatabaseManager");
        }
        Economy economy = plugin.getServer().getServicesManager().load(Economy.class);
        if (economy == null) {
            throw new IllegalStateException("auction module requires Vault's Economy service");
        }

        AuctionRepository repository = new AuctionRepository(databaseManager);
        try {
            repository.createSchemaIfNotExists();
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to initialize auction schema", e);
        }

        this.auctionService = new AuctionService(repository, economy);
        auctionService.loadAll();

        this.guiScreen = new AuctionGuiScreen(auctionService);
        plugin.getPlayerCommandRegistry().register("auction",
                new AuctionCommand(auctionService, guiScreen, new GuiManager(), plugin.getMessageManager()),
                "オークションを利用します。", "auction [list|sell <price>|collect]");

        plugin.getSchedulerService().runTimer(auctionService::expireOverdueListings,
                EXPIRY_CHECK_PERIOD_TICKS, EXPIRY_CHECK_PERIOD_TICKS);
    }

    @Override
    public void onDisable() {
    }

    public AuctionService getAuctionService() {
        return auctionService;
    }

    public AuctionGuiScreen getGuiScreen() {
        return guiScreen;
    }
}
