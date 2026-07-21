package rpg.extra.guild.command;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import rpg.core.command.Pagination;
import rpg.core.message.MessageManager;
import rpg.util.ColorUtil;
import rpg.extra.guild.model.Guild;
import rpg.extra.guild.model.GuildRole;
import rpg.extra.guild.service.GuildService;

import java.util.ArrayList;
import java.util.List;

/**
 * {@code /ol guild create|invite|accept|leave|kick|promote|demote|disband|transfer|list|info}
 * (SOW GuildModule).
 */
public final class GuildCommand implements CommandExecutor {

    private static final int LIST_PAGE_SIZE = 15;

    private final GuildService guildService;
    private final MessageManager messages;

    public GuildCommand(GuildService guildService, MessageManager messages) {
        this.guildService = guildService;
        this.messages = messages;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            messages.send(sender, "command.player-only");
            return true;
        }
        if (args.length == 0) {
            messages.send(sender, "usage.guild");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "create" -> {
                if (args.length < 3) {
                    messages.send(sender, "usage.guild-create");
                    return true;
                }
                report(sender, guildService.create(player, args[1], args[2]), "guild.created");
            }
            case "invite" -> withTarget(sender, player, args, target -> {
                GuildService.ActionResult result = guildService.invite(player, target);
                report(sender, result, "guild.invited");
                if (result == GuildService.ActionResult.OK) {
                    messages.send(target, "guild.invite-received", "player", player.getName());
                }
            });
            case "accept" -> report(sender, guildService.accept(player), "guild.accepted");
            case "leave" -> report(sender, guildService.leave(player), "guild.left");
            case "kick" -> withTarget(sender, player, args, target ->
                    report(sender, guildService.kick(player, target.getUniqueId()), "guild.kicked"));
            case "promote" -> withTarget(sender, player, args, target ->
                    report(sender, guildService.setRole(player, target.getUniqueId(), GuildRole.OFFICER), "guild.promoted"));
            case "demote" -> withTarget(sender, player, args, target ->
                    report(sender, guildService.setRole(player, target.getUniqueId(), GuildRole.MEMBER), "guild.demoted"));
            case "disband" -> report(sender, guildService.disband(player), "guild.disbanded");
            case "transfer" -> withTarget(sender, player, args, target ->
                    report(sender, guildService.transferLeadership(player, target.getUniqueId()), "guild.leadership-transferred"));
            case "list" -> showList(sender, args);
            case "info" -> showInfo(sender, player);
            default -> messages.send(sender, "usage.guild");
        }
        return true;
    }

    private void withTarget(CommandSender sender, Player player, String[] args, java.util.function.Consumer<Player> action) {
        if (args.length < 2) {
            messages.send(sender, "usage.guild-target", "action", args[0]);
            return;
        }
        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            messages.send(sender, "command.player-not-found", "player", args[1]);
            return;
        }
        action.accept(target);
    }

    private void showInfo(CommandSender sender, Player player) {
        Guild guild = guildService.getGuild(player.getUniqueId()).orElse(null);
        if (guild == null) {
            messages.send(sender, "guild.not-in-guild");
            return;
        }
        messages.sendRaw(sender, "guild.info-header", "tag", guild.getTag(), "name", guild.getName());
        for (var entry : guild.getMembers().entrySet()) {
            String name = Bukkit.getOfflinePlayer(entry.getKey()).getName();
            messages.sendRaw(sender, "guild.member-entry", "name", name, "role", entry.getValue().getDisplayName());
        }
    }

    private void showList(CommandSender sender, String[] args) {
        int page = args.length >= 2 ? parsePageOrDefault(args[1]) : 1;
        List<Component> lines = new ArrayList<>();
        for (Guild guild : guildService.getAllGuilds()) {
            lines.add(ColorUtil.component(messages.format("guild.list-entry",
                    "tag", guild.getTag(), "name", guild.getName(), "members", guild.getMembers().size())));
        }
        Pagination.send(sender, "&%6&lギルド一覧&%7 ({page}/{total}ページ)", lines, LIST_PAGE_SIZE, page,
                "/ol guild list", "&%7登録されているギルドはありません。");
    }

    private int parsePageOrDefault(String raw) {
        try {
            return Integer.parseInt(raw);
        } catch (NumberFormatException e) {
            return 1;
        }
    }

    private void report(CommandSender sender, GuildService.ActionResult result, String successKey) {
        if (result == GuildService.ActionResult.OK) {
            messages.send(sender, successKey);
            return;
        }
        String key = switch (result) {
            case ALREADY_IN_GUILD -> "guild.already-in-guild";
            case NOT_IN_GUILD -> "guild.not-in-guild";
            case INSUFFICIENT_ROLE -> "guild.insufficient-role";
            case TARGET_ALREADY_IN_GUILD -> "guild.target-already-in-guild";
            case NO_PENDING_INVITE -> "guild.no-pending-invite";
            case CANNOT_TARGET_SELF -> "guild.cannot-target-self";
            case CANNOT_TARGET_LEADER -> "guild.cannot-target-leader";
            case LEADER_MUST_DISBAND -> "guild.leader-must-disband";
            case NAME_TAKEN -> "guild.name-taken";
            case TAG_TAKEN -> "guild.tag-taken";
            case TARGET_NOT_MEMBER -> "guild.target-not-member";
            case OK -> successKey;
        };
        messages.send(sender, key);
    }
}
