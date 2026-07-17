package rpg.extra.mail;

import rpg.database.manager.DatabaseManager;
import rpg.extra.core.OreliaExtraPlugin;
import rpg.extra.core.module.ExtraModule;
import rpg.extra.mail.command.MailCommand;
import rpg.extra.mail.gui.MailGuiScreen;
import rpg.extra.mail.repository.MailRepository;
import rpg.extra.mail.service.MailService;
import rpg.gui.framework.GuiManager;

import java.util.logging.Level;

/**
 * Mail module: persisted mailbox with item attachments (SOW MailModule).
 */
public final class MailModule implements ExtraModule {

    private MailService mailService;
    private MailGuiScreen guiScreen;

    @Override
    public String getName() {
        return "mail";
    }

    @Override
    public void onEnable(OreliaExtraPlugin plugin) {
        DatabaseManager databaseManager = plugin.getServer().getServicesManager().load(DatabaseManager.class);
        if (databaseManager == null) {
            throw new IllegalStateException("mail module requires OreliaCore's DatabaseManager");
        }

        MailRepository repository = new MailRepository(databaseManager);
        try {
            repository.createSchemaIfNotExists();
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to initialize mail schema", e);
        }

        this.mailService = new MailService(repository);
        this.guiScreen = new MailGuiScreen(mailService);

        plugin.getPlayerCommandRegistry().register("mail",
                new MailCommand(mailService, guiScreen, new GuiManager(), plugin.getMessageManager()),
                "郵便受けを開きます。", "mail [unread]");
    }

    @Override
    public void onDisable() {
    }

    public MailService getMailService() {
        return mailService;
    }

    public MailGuiScreen getGuiScreen() {
        return guiScreen;
    }
}
