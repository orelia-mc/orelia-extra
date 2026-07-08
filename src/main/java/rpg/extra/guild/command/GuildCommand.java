package rpg.extra.guild.command;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import rpg.extra.guild.model.Guild;
import rpg.extra.guild.model.GuildRole;
import rpg.extra.guild.service.GuildService;

/**
 * {@code /guild create|invite|accept|leave|kick|promote|demote|disband|info} (SOW GuildModule).
 */
public final class GuildCommand implements CommandExecutor {

    private final GuildService guildService;

    public GuildCommand(GuildService guildService) {
        this.guildService = guildService;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Players only.");
            return true;
        }
        if (args.length == 0) {
            sender.sendMessage(ChatColor.YELLOW + "Usage: /guild <create|invite|accept|leave|kick|promote|demote|disband|info>");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "create" -> {
                if (args.length < 3) {
                    sender.sendMessage(ChatColor.YELLOW + "Usage: /guild create <name> <tag>");
                    return true;
                }
                report(sender, guildService.create(player, args[1], args[2]), "ギルドを作成しました。");
            }
            case "invite" -> withTarget(sender, player, args, target -> {
                GuildService.ActionResult result = guildService.invite(player, target);
                report(sender, result, "招待を送りました。");
                if (result == GuildService.ActionResult.OK) {
                    target.sendMessage(ChatColor.GREEN + player.getName() + "からギルド招待が届きました。/guild accept で参加できます。");
                }
            });
            case "accept" -> report(sender, guildService.accept(player), "ギルドに参加しました。");
            case "leave" -> report(sender, guildService.leave(player), "ギルドを抜けました。");
            case "kick" -> withTarget(sender, player, args, target ->
                    report(sender, guildService.kick(player, target.getUniqueId()), "追放しました。"));
            case "promote" -> withTarget(sender, player, args, target ->
                    report(sender, guildService.setRole(player, target.getUniqueId(), GuildRole.OFFICER), "昇格させました。"));
            case "demote" -> withTarget(sender, player, args, target ->
                    report(sender, guildService.setRole(player, target.getUniqueId(), GuildRole.MEMBER), "降格させました。"));
            case "disband" -> report(sender, guildService.disband(player), "ギルドを解散しました。");
            case "info" -> showInfo(sender, player);
            default -> sender.sendMessage(ChatColor.YELLOW + "Usage: /guild <create|invite|accept|leave|kick|promote|demote|disband|info>");
        }
        return true;
    }

    private void withTarget(CommandSender sender, Player player, String[] args, java.util.function.Consumer<Player> action) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.YELLOW + "Usage: /guild " + args[0] + " <player>");
            return;
        }
        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player not found: " + args[1]);
            return;
        }
        action.accept(target);
    }

    private void showInfo(CommandSender sender, Player player) {
        Guild guild = guildService.getGuild(player.getUniqueId()).orElse(null);
        if (guild == null) {
            sender.sendMessage(ChatColor.YELLOW + "ギルドに所属していません。");
            return;
        }
        sender.sendMessage(ChatColor.GREEN + "[" + guild.getTag() + "] " + guild.getName());
        for (var entry : guild.getMembers().entrySet()) {
            String name = Bukkit.getOfflinePlayer(entry.getKey()).getName();
            sender.sendMessage(ChatColor.GRAY + "- " + name + " (" + entry.getValue() + ")");
        }
    }

    private void report(CommandSender sender, GuildService.ActionResult result, String successMessage) {
        if (result == GuildService.ActionResult.OK) {
            sender.sendMessage(ChatColor.GREEN + successMessage);
            return;
        }
        String message = switch (result) {
            case ALREADY_IN_GUILD -> "既にギルドに所属しています。";
            case NOT_IN_GUILD -> "ギルドに所属していません。";
            case INSUFFICIENT_ROLE -> "権限が足りません。";
            case TARGET_ALREADY_IN_GUILD -> "対象は既に別のギルドに所属しています。";
            case NO_PENDING_INVITE -> "招待が届いていません。";
            case CANNOT_TARGET_SELF -> "自分自身は対象にできません。";
            case CANNOT_TARGET_LEADER -> "リーダーを対象にはできません。";
            case OK -> successMessage;
        };
        sender.sendMessage(ChatColor.RED + message);
    }
}
