package rpg.extra.core;

import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import rpg.core.config.ConfigManager;
import rpg.core.player.PlayerDataManager;
import rpg.core.scheduler.SchedulerService;
import rpg.extra.achievement.AchievementModule;
import rpg.extra.auction.AuctionModule;
import rpg.extra.core.command.ExtraAdminCommand;
import rpg.extra.core.module.ExtraModuleManager;
import rpg.extra.guild.GuildModule;
import rpg.extra.housing.HousingModule;
import rpg.extra.mail.MailModule;
import rpg.extra.mount.MountModule;
import rpg.extra.party.PartyModule;
import rpg.extra.pet.PetModule;
import rpg.extra.ranking.RankingModule;
import rpg.extra.trade.TradeModule;

/**
 * Plugin entry point for the orelia-extra repo/jar: later MMORPG features (Party, Guild,
 * Trade, Mail, Auction, Housing, Pet, Mount, Ranking, Achievement). Requires OreliaCore;
 * OreliaWorld is a soft dependency used only by AchievementModule's optional COMPLETE_QUEST
 * condition.
 *
 * <p>Modules with no dependency on each other register in roughly alphabetical order;
 * Ranking/Achievement register last since they read state produced by the others (or by
 * OreliaCore/OreliaWorld directly) rather than owning anything themselves.
 */
public final class OreliaExtraPlugin extends JavaPlugin {

    private static OreliaExtraPlugin instance;

    private ConfigManager configManager;
    private SchedulerService schedulerService;
    private PlayerDataManager playerDataManager;
    private ExtraModuleManager moduleManager;

    @Override
    public void onEnable() {
        instance = this;

        RegisteredServiceProvider<PlayerDataManager> registration =
                getServer().getServicesManager().getRegistration(PlayerDataManager.class);
        if (registration == null) {
            getLogger().severe("OreliaCore's PlayerDataManager service was not found. "
                    + "Is OreliaCore installed and enabled before OreliaExtra?");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        this.playerDataManager = registration.getProvider();

        this.configManager = new ConfigManager(this);
        this.configManager.register("config.yml");

        this.schedulerService = new SchedulerService(this);
        this.moduleManager = new ExtraModuleManager(this);

        getCommand("rpgextraadmin").setExecutor(new ExtraAdminCommand(this));

        moduleManager.register(new PartyModule());
        moduleManager.register(new GuildModule());
        moduleManager.register(new TradeModule());
        moduleManager.register(new MailModule());
        moduleManager.register(new AuctionModule());
        moduleManager.register(new HousingModule());
        moduleManager.register(new PetModule());
        moduleManager.register(new MountModule());
        moduleManager.register(new RankingModule());
        moduleManager.register(new AchievementModule());
        moduleManager.enableAll();
    }

    @Override
    public void onDisable() {
        if (moduleManager != null) {
            moduleManager.disableAll();
        }
        instance = null;
    }

    public void reload() {
        configManager.reloadAll();
        moduleManager.reloadAll();
    }

    public static OreliaExtraPlugin getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public SchedulerService getSchedulerService() {
        return schedulerService;
    }

    public PlayerDataManager getPlayerDataManager() {
        return playerDataManager;
    }

    public ExtraModuleManager getModuleManager() {
        return moduleManager;
    }
}
