package rpg.extra.achievement.command;

import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import rpg.core.command.Pagination;
import rpg.core.message.MessageManager;
import rpg.extra.achievement.model.AchievementDefinition;
import rpg.extra.achievement.service.AchievementService;
import rpg.util.ColorUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * {@code /ol achievement [page]} lists every achievement and whether the player has unlocked
 * it (SOW AchievementModule), paginated 15 per page with clickable prev/next navigation.
 */
public final class AchievementCommand implements CommandExecutor {

    private static final int PAGE_SIZE = 15;

    private final AchievementService achievementService;
    private final MessageManager messages;

    public AchievementCommand(AchievementService achievementService, MessageManager messages) {
        this.achievementService = achievementService;
        this.messages = messages;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            messages.send(sender, "command.player-only");
            return true;
        }
        Map<String, AchievementDefinition> all = achievementService.getAllAchievements();
        if (all.isEmpty()) {
            messages.send(sender, "achievement.none");
            return true;
        }
        Set<String> unlocked = achievementService.getUnlocked(player.getUniqueId());
        int page = args.length >= 1 ? parsePageOrDefault(args[0]) : 1;

        List<Component> lines = new ArrayList<>();
        for (AchievementDefinition achievement : all.values()) {
            boolean done = unlocked.contains(achievement.getId());
            String status = messages.format(done ? "achievement.status-done" : "achievement.status-pending");
            lines.add(ColorUtil.component(messages.format("achievement.list-entry",
                    "status", status, "name", achievement.getName(), "description", achievement.getDescription())));
        }
        Pagination.send(sender, "&%6&l実績一覧&%7 ({page}/{total}ページ)", lines, PAGE_SIZE, page, "/ol achievement");
        return true;
    }

    private int parsePageOrDefault(String raw) {
        try {
            return Integer.parseInt(raw);
        } catch (NumberFormatException e) {
            return 1;
        }
    }
}
