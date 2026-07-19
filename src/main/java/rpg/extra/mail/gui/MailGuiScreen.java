package rpg.extra.mail.gui;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import rpg.extra.mail.model.MailMessage;
import rpg.extra.mail.service.MailService;
import rpg.gui.framework.Gui;
import rpg.gui.framework.GuiButton;
import rpg.util.ColorUtil;
import rpg.util.ItemBuilder;

import java.util.List;

/**
 * Mailbox screen (SOW MailModule). Reuses orelia-core's generic {@code Gui}/{@code
 * GuiButton} framework classes directly - pure UI plumbing, not gameplay logic - the same
 * way orelia-world's QuestGuiScreen does; orelia-core's already-registered
 * {@code GuiListener} handles the clicks.
 */
public final class MailGuiScreen {

    private final MailService mailService;

    public MailGuiScreen(MailService mailService) {
        this.mailService = mailService;
    }

    public Gui build(Player player) {
        Gui gui = new Gui(ColorUtil.colorize("&%8メール"), 54);
        List<MailMessage> inbox = mailService.getInbox(player.getUniqueId());

        int slot = 0;
        for (MailMessage message : inbox) {
            if (slot >= 54) {
                break;
            }
            Material icon = message.hasAttachments() ? Material.CHEST : Material.PAPER;
            gui.set(slot++, new GuiButton(new ItemBuilder(icon)
                    .name((message.isRead() ? "&%7" : "&%e&l") + message.getSubject())
                    .lore(List.of(
                            "&%7差出人: &%f" + message.getSenderName(),
                            "&%7" + message.getBody(),
                            message.hasAttachments() ? (message.isClaimed() ? "&%7添付物: 受取済み" : "&%a添付物あり - クリックで受け取り") : "&%7添付物なし",
                            "&%8クリックで既読にする"))
                    .build(), (clicker, clickType) -> {
                mailService.markRead(message);
                if (message.hasAttachments() && !message.isClaimed()) {
                    boolean claimed = mailService.claim(clicker, message);
                    clicker.sendMessage(claimed ? ChatColor.GREEN + "添付物を受け取りました。" : ChatColor.RED + "受け取りに失敗しました。");
                }
            }));
        }
        return gui;
    }
}
