package rpg.extra.mail.command;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import rpg.extra.mail.gui.MailGuiScreen;
import rpg.extra.mail.service.MailService;
import rpg.gui.framework.GuiManager;

/**
 * {@code /ol mail} opens the mailbox GUI (SOW MailModule).
 */
public final class MailCommand implements CommandExecutor {

    private final MailService mailService;
    private final MailGuiScreen guiScreen;
    private final GuiManager guiManager;

    public MailCommand(MailService mailService, MailGuiScreen guiScreen, GuiManager guiManager) {
        this.mailService = mailService;
        this.guiScreen = guiScreen;
        this.guiManager = guiManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Players only.");
            return true;
        }
        if (args.length > 0 && args[0].equalsIgnoreCase("unread")) {
            long unread = mailService.getInbox(player.getUniqueId()).stream().filter(m -> !m.isRead()).count();
            player.sendMessage(ChatColor.YELLOW + "未読メール: " + unread + "件");
            return true;
        }
        guiManager.open(player, guiScreen.build(player));
        return true;
    }
}
