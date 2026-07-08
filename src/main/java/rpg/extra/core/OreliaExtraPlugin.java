package rpg.extra.core;

import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import rpg.core.config.ConfigManager;
import rpg.core.player.PlayerDataManager;
import rpg.core.scheduler.SchedulerService;
import rpg.extra.core.command.ExtraAdminCommand;
import rpg.extra.core.module.ExtraModuleManager;

/**
 * Plugin entry point for the orelia-extra repo/jar: later MMORPG features (Party, Guild,
 * Trade, Mail, Auction, Housing, Pet, Mount, Ranking, Achievement). Requires OreliaCore;
 * OreliaWorld is a soft dependency for modules that end up needing quest/story/dialogue
 * integration once they exist.
 *
 * <p>No modules are registered yet - this is intentionally just the bootstrap, ready for
 * the first orelia-extra module to be added the same way orelia-core/orelia-world modules
 * are (implement {@code ExtraModule}, register it in {@link #onEnable()}).
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

        // No modules registered yet - see class Javadoc.
        moduleManager.enableAll();

        getLogger().info("OreliaExtra is enabled but currently has no modules registered. "
                + "Add Party/Guild/Trade/... modules here as they are implemented.");
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
