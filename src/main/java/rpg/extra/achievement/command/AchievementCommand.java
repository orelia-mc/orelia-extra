package rpg.extra.achievement.command;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import rpg.extra.achievement.model.AchievementDefinition;
import rpg.extra.achievement.service.AchievementService;

import java.util.Map;
import java.util.Set;

/**
 * {@code /achievement} lists every achievement and whether the player has unlocked it (SOW
 * AchievementModule).
 */
public final class AchievementCommand implements CommandExecutor {

    private final AchievementService achievementService;

    public AchievementCommand(AchievementService achievementService) {
        this.achievementService = achievementService;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Players only.");
            return true;
        }
        Map<String, AchievementDefinition> all = achievementService.getAllAchievements();
        Set<String> unlocked = achievementService.getUnlocked(player.getUniqueId());
        if (all.isEmpty()) {
            sender.sendMessage(ChatColor.YELLOW + "実績がありません。");
            return true;
        }
        sender.sendMessage(ChatColor.GREEN + "実績一覧:");
        all.values().forEach(achievement -> {
            boolean done = unlocked.contains(achievement.getId());
            sender.sendMessage((done ? ChatColor.GOLD + "[済] " : ChatColor.GRAY + "[未] ")
                    + achievement.getName() + ChatColor.GRAY + " - " + achievement.getDescription());
        });
        return true;
    }
}
