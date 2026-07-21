package rpg.extra.party.command;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import rpg.core.message.MessageManager;
import rpg.extra.chat.ChatBroadcast;
import rpg.extra.party.model.Party;
import rpg.extra.party.service.PartyService;
import rpg.util.ColorUtil;

import java.util.Arrays;

/**
 * {@code /ol party create|invite|accept|leave|kick|disband|list|chat} (SOW PartyModule).
 */
public final class PartyCommand implements CommandExecutor {

    private final PartyService partyService;
    private final MessageManager messages;

    public PartyCommand(PartyService partyService, MessageManager messages) {
        this.partyService = partyService;
        this.messages = messages;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            messages.send(sender, "command.player-only");
            return true;
        }
        if (args.length == 0) {
            messages.send(sender, "usage.party");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "create" -> report(sender, partyService.create(player), "party.created");
            case "invite" -> {
                if (args.length < 2) {
                    messages.send(sender, "usage.party-invite");
                    return true;
                }
                Player target = Bukkit.getPlayerExact(args[1]);
                if (target == null) {
                    messages.send(sender, "command.player-not-found", "player", args[1]);
                    return true;
                }
                PartyService.ActionResult result = partyService.invite(player, target);
                report(sender, result, "party.invited");
                if (result == PartyService.ActionResult.OK) {
                    messages.send(target, "party.invite-received", "player", player.getName());
                }
            }
            case "accept" -> report(sender, partyService.accept(player), "party.accepted");
            case "leave" -> report(sender, partyService.leave(player), "party.left");
            case "kick" -> {
                if (args.length < 2) {
                    messages.send(sender, "usage.party-kick");
                    return true;
                }
                Player target = Bukkit.getPlayerExact(args[1]);
                if (target == null) {
                    messages.send(sender, "command.player-not-found", "player", args[1]);
                    return true;
                }
                report(sender, partyService.kick(player, target.getUniqueId()), "party.kicked");
            }
            case "disband" -> report(sender, partyService.disband(player), "party.disbanded");
            case "list" -> listMembers(sender, player);
            case "chat" -> partyChat(sender, player, args);
            default -> messages.send(sender, "usage.party");
        }
        return true;
    }

    private void partyChat(CommandSender sender, Player player, String[] args) {
        if (args.length < 2) {
            messages.send(sender, "chat.usage-party-chat");
            return;
        }
        Party party = partyService.getParty(player.getUniqueId()).orElse(null);
        if (party == null) {
            messages.send(sender, "chat.not-in-party");
            return;
        }
        String message = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        ChatBroadcast.toParty(party, ColorUtil.component(
                messages.format("chat.party-format", "sender", player.getName(), "message", message)));
    }

    private void listMembers(CommandSender sender, Player player) {
        Party party = partyService.getParty(player.getUniqueId()).orElse(null);
        if (party == null) {
            messages.send(sender, "party.not-in-party");
            return;
        }
        messages.send(sender, "party.members-header");
        for (var memberId : party.getMembers()) {
            String name = Bukkit.getOfflinePlayer(memberId).getName();
            boolean isLeader = memberId.equals(party.getLeaderId());
            String leaderTag = isLeader ? messages.format("party.member-leader-tag") : "";
            messages.sendRaw(sender, "party.member-entry", "name", name, "leader", leaderTag);
        }
    }

    private void report(CommandSender sender, PartyService.ActionResult result, String successKey) {
        if (result == PartyService.ActionResult.OK) {
            messages.send(sender, successKey);
            return;
        }
        String key = switch (result) {
            case ALREADY_IN_PARTY -> "party.already-in-party";
            case NOT_IN_PARTY -> "party.not-in-party";
            case NOT_LEADER -> "party.not-leader";
            case PARTY_FULL -> "party.party-full";
            case TARGET_ALREADY_IN_PARTY -> "party.target-already-in-party";
            case NO_PENDING_INVITE -> "party.no-pending-invite";
            case CANNOT_TARGET_SELF -> "party.cannot-target-self";
            case OK -> successKey;
        };
        messages.send(sender, key);
    }
}
