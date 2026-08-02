package rpg.extra.mail.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.entity.Player;
import rpg.core.message.MessageManager;
import rpg.core.scheduler.SchedulerService;
import rpg.extra.mail.service.MailService;
import rpg.util.ColorUtil;

/**
 * Tells a player about unread mail shortly after they join, rather than requiring them to
 * think to open {@code /ol mail}. The notice is clickable ({@code /ol mail} runs on click,
 * see {@link rpg.util.ColorUtil#componentWithCommand}) instead of being plain text - one of
 * the few "make chat a bit less static" changes bundled with this mail work.
 */
public final class MailUnreadJoinListener implements Listener {

    private static final long NOTICE_DELAY_TICKS = 40L;

    private final MailService mailService;
    private final MessageManager messages;
    private final SchedulerService schedulerService;

    public MailUnreadJoinListener(MailService mailService, MessageManager messages, SchedulerService schedulerService) {
        this.mailService = mailService;
        this.messages = messages;
        this.schedulerService = schedulerService;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        schedulerService.runLater(() -> {
            if (!player.isOnline()) {
                return;
            }
            long unread = mailService.getInbox(player.getUniqueId()).stream().filter(m -> !m.isRead()).count();
            if (unread <= 0) {
                return;
            }
            String text = messages.getPrefix() + messages.format("mail.unread-count", "count", unread);
            player.sendMessage(ColorUtil.componentWithCommand(text, "/ol mail"));
        }, NOTICE_DELAY_TICKS);
    }
}
