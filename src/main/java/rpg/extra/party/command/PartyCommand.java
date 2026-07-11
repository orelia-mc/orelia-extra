package rpg.extra.party.command;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import rpg.extra.party.model.Party;
import rpg.extra.party.service.PartyService;

/**
 * {@code /ol party create|invite|accept|leave|kick|disband|list} (SOW PartyModule).
 */
public final class PartyCommand implements CommandExecutor {

    private final PartyService partyService;

    public PartyCommand(PartyService partyService) {
        this.partyService = partyService;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Players only.");
            return true;
        }
        if (args.length == 0) {
            sender.sendMessage(ChatColor.YELLOW + "Usage: /ol party <create|invite|accept|leave|kick|disband|list>");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "create" -> report(sender, partyService.create(player), "パーティーを作成しました。");
            case "invite" -> {
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.YELLOW + "Usage: /ol party invite <player>");
                    return true;
                }
                Player target = Bukkit.getPlayerExact(args[1]);
                if (target == null) {
                    sender.sendMessage(ChatColor.RED + "Player not found: " + args[1]);
                    return true;
                }
                PartyService.ActionResult result = partyService.invite(player, target);
                report(sender, result, "招待を送りました。");
                if (result == PartyService.ActionResult.OK) {
                    target.sendMessage(ChatColor.GREEN + player.getName() + "からパーティー招待が届きました。/ol party accept で参加できます。");
                }
            }
            case "accept" -> report(sender, partyService.accept(player), "パーティーに参加しました。");
            case "leave" -> report(sender, partyService.leave(player), "パーティーを抜けました。");
            case "kick" -> {
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.YELLOW + "Usage: /ol party kick <player>");
                    return true;
                }
                Player target = Bukkit.getPlayerExact(args[1]);
                if (target == null) {
                    sender.sendMessage(ChatColor.RED + "Player not found: " + args[1]);
                    return true;
                }
                report(sender, partyService.kick(player, target.getUniqueId()), "追放しました。");
            }
            case "disband" -> report(sender, partyService.disband(player), "パーティーを解散しました。");
            case "list" -> listMembers(sender, player);
            default -> sender.sendMessage(ChatColor.YELLOW + "Usage: /ol party <create|invite|accept|leave|kick|disband|list>");
        }
        return true;
    }

    private void listMembers(CommandSender sender, Player player) {
        Party party = partyService.getParty(player.getUniqueId()).orElse(null);
        if (party == null) {
            sender.sendMessage(ChatColor.YELLOW + "パーティーに所属していません。");
            return;
        }
        sender.sendMessage(ChatColor.GREEN + "パーティーメンバー:");
        for (var memberId : party.getMembers()) {
            String name = Bukkit.getOfflinePlayer(memberId).getName();
            boolean isLeader = memberId.equals(party.getLeaderId());
            sender.sendMessage(ChatColor.GRAY + "- " + name + (isLeader ? ChatColor.GOLD + " [リーダー]" : ""));
        }
    }

    private void report(CommandSender sender, PartyService.ActionResult result, String successMessage) {
        if (result == PartyService.ActionResult.OK) {
            sender.sendMessage(ChatColor.GREEN + successMessage);
            return;
        }
        String message = switch (result) {
            case ALREADY_IN_PARTY -> "既にパーティーに所属しています。";
            case NOT_IN_PARTY -> "パーティーに所属していません。";
            case NOT_LEADER -> "リーダーのみ実行できます。";
            case PARTY_FULL -> "パーティーが満員です。";
            case TARGET_ALREADY_IN_PARTY -> "対象は既に別のパーティーに所属しています。";
            case NO_PENDING_INVITE -> "招待が届いていません。";
            case CANNOT_TARGET_SELF -> "自分自身は対象にできません。";
            case OK -> successMessage;
        };
        sender.sendMessage(ChatColor.RED + message);
    }
}
