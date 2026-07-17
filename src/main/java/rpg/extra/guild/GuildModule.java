package rpg.extra.guild;

import rpg.database.manager.DatabaseManager;
import rpg.extra.core.OreliaExtraPlugin;
import rpg.extra.core.module.ExtraModule;
import rpg.extra.guild.command.GuildCommand;
import rpg.extra.guild.listener.GuildQuitListener;
import rpg.extra.guild.manager.GuildManager;
import rpg.extra.guild.repository.GuildRepository;
import rpg.extra.guild.service.GuildService;

import java.util.logging.Level;

/**
 * Guild module: persistent player organizations with leader/officer/member roles (SOW
 * GuildModule).
 */
public final class GuildModule implements ExtraModule {

    private GuildService guildService;

    @Override
    public String getName() {
        return "guild";
    }

    @Override
    public void onEnable(OreliaExtraPlugin plugin) {
        DatabaseManager databaseManager = plugin.getServer().getServicesManager().load(DatabaseManager.class);
        if (databaseManager == null) {
            throw new IllegalStateException("guild module requires OreliaCore's DatabaseManager");
        }

        GuildRepository repository = new GuildRepository(databaseManager);
        try {
            repository.createSchemaIfNotExists();
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to initialize guild schema", e);
        }

        GuildManager manager = new GuildManager(repository);
        manager.loadAll();

        this.guildService = new GuildService(manager);

        plugin.getServer().getPluginManager().registerEvents(new GuildQuitListener(manager), plugin);
        plugin.getPlayerCommandRegistry().register("guild", new GuildCommand(guildService, plugin.getMessageManager()),
                "ギルドを管理します。", "guild <create|invite|accept|leave|kick|promote|demote|disband|info>");
    }

    @Override
    public void onDisable() {
    }

    public GuildService getGuildService() {
        return guildService;
    }
}
