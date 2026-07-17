package rpg.extra.api;

import org.bukkit.entity.Player;
import rpg.core.config.ConfigFile;
import rpg.core.config.ConfigManager;
import rpg.extra.auction.AuctionModule;
import rpg.extra.mail.MailModule;
import rpg.extra.ranking.RankingModule;
import rpg.gui.framework.GuiManager;

import java.util.List;
import java.util.Optional;
import java.util.Set;

final class ExtraDebugApiImpl implements ExtraDebugApi {

    private final ConfigManager configManager;
    private final AuctionModule auctionModule;
    private final MailModule mailModule;
    private final RankingModule rankingModule;
    private final GuiManager guiManager = new GuiManager();

    ExtraDebugApiImpl(ConfigManager configManager, AuctionModule auctionModule, MailModule mailModule,
                       RankingModule rankingModule) {
        this.configManager = configManager;
        this.auctionModule = auctionModule;
        this.mailModule = mailModule;
        this.rankingModule = rankingModule;
    }

    @Override
    public Set<String> listConfigFiles() {
        return configManager.getRegisteredFileNames();
    }

    @Override
    public Optional<String> getConfigValue(String fileName, String path) {
        ConfigFile file = tryGet(fileName);
        if (file == null || !file.get().contains(path)) {
            return Optional.empty();
        }
        return Optional.ofNullable(file.get().get(path)).map(String::valueOf);
    }

    @Override
    public boolean setConfigValue(String fileName, String path, String rawValue) {
        ConfigFile file = tryGet(fileName);
        if (file == null) {
            return false;
        }
        file.get().set(path, parseValue(rawValue));
        file.save();
        return true;
    }

    @Override
    public void saveConfig(String fileName) {
        ConfigFile file = tryGet(fileName);
        if (file != null) {
            file.save();
        }
    }

    @Override
    public List<String> describeConfigKeys(String fileName) {
        ConfigFile file = tryGet(fileName);
        if (file == null) {
            return List.of();
        }
        return file.get().getKeys(true).stream().sorted().toList();
    }

    @Override
    public void openAuction(Player player) {
        guiManager.open(player, auctionModule.getGuiScreen().build(player));
    }

    @Override
    public void openMail(Player player) {
        guiManager.open(player, mailModule.getGuiScreen().build(player));
    }

    @Override
    public void openRanking(Player player) {
        guiManager.open(player, rankingModule.getGuiScreen().build());
    }

    private ConfigFile tryGet(String fileName) {
        try {
            return configManager.get(fileName);
        } catch (IllegalStateException e) {
            return null;
        }
    }

    private Object parseValue(String rawValue) {
        if ("true".equalsIgnoreCase(rawValue) || "false".equalsIgnoreCase(rawValue)) {
            return Boolean.parseBoolean(rawValue);
        }
        try {
            return Long.parseLong(rawValue);
        } catch (NumberFormatException ignored) {
        }
        try {
            return Double.parseDouble(rawValue);
        } catch (NumberFormatException ignored) {
        }
        return rawValue;
    }
}
