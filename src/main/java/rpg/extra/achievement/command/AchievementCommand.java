package rpg.extra.achievement.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import rpg.core.message.MessageManager;
import rpg.extra.achievement.model.AchievementDefinition;
import rpg.extra.achievement.service.AchievementService;

import java.util.Map;
import java.util.Set;

/**
 * {@code /ol achievement} lists every achievement and whether the player has unlocked it (SOW
 * AchievementModule).
 */
public final class AchievementCommand implements CommandExecutor {

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
        Set<String> unlocked = achievementService.getUnlocked(player.getUniqueId());
        if (all.isEmpty()) {
            messages.send(sender, "achievement.none");
            return true;
        }
        messages.send(sender, "achievement.list-header");
        all.values().forEach(achievement -> {
            boolean done = unlocked.contains(achievement.getId());
            String status = messages.format(done ? "achievement.status-done" : "achievement.status-pending");
            messages.sendRaw(sender, "achievement.list-entry",
                    "status", status, "name", achievement.getName(), "description", achievement.getDescription());
        });
        return true;
    }
}
