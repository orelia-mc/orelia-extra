package rpg.extra.ranking;

import rpg.api.StatusApi;
import rpg.extra.core.OreliaExtraPlugin;
import rpg.extra.core.module.ExtraModule;
import rpg.extra.ranking.command.RankingCommand;
import rpg.extra.ranking.gui.RankingGuiScreen;
import rpg.gui.framework.GuiManager;

/**
 * Ranking module: level leaderboard GUI/command (SOW RankingModule), backed entirely by
 * orelia-core's {@link StatusApi} - this module owns no data of its own.
 */
public final class RankingModule implements ExtraModule {

    private RankingGuiScreen guiScreen;

    @Override
    public String getName() {
        return "ranking";
    }

    @Override
    public void onEnable(OreliaExtraPlugin plugin) {
        StatusApi statusApi = plugin.getServer().getServicesManager().load(StatusApi.class);
        if (statusApi == null) {
            throw new IllegalStateException("ranking module requires OreliaCore's StatusApi");
        }

        this.guiScreen = new RankingGuiScreen(statusApi);
        plugin.getPlayerCommandRegistry().register("ranking",
                new RankingCommand(guiScreen, new GuiManager(), plugin.getMessageManager()),
                "レベルランキングを表示します。", "ranking");
    }

    @Override
    public void onDisable() {
    }

    public RankingGuiScreen getGuiScreen() {
        return guiScreen;
    }
}
