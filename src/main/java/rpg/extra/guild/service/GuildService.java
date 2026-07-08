package rpg.extra.guild.service;

import org.bukkit.entity.Player;
import rpg.extra.guild.manager.GuildManager;
import rpg.extra.guild.model.Guild;
import rpg.extra.guild.model.GuildRole;

import java.util.Optional;
import java.util.UUID;

/**
 * Guild business rules on top of {@link GuildManager}'s plain data operations.
 */
public final class GuildService {

    public enum ActionResult {
        OK, ALREADY_IN_GUILD, NOT_IN_GUILD, INSUFFICIENT_ROLE, TARGET_ALREADY_IN_GUILD,
        NO_PENDING_INVITE, CANNOT_TARGET_SELF, CANNOT_TARGET_LEADER
    }

    private final GuildManager manager;

    public GuildService(GuildManager manager) {
        this.manager = manager;
    }

    public ActionResult create(Player leader, String name, String tag) {
        if (manager.getByPlayer(leader.getUniqueId()).isPresent()) {
            return ActionResult.ALREADY_IN_GUILD;
        }
        manager.create(name, tag, leader.getUniqueId());
        return ActionResult.OK;
    }

    public ActionResult invite(Player inviter, Player invitee) {
        if (inviter.getUniqueId().equals(invitee.getUniqueId())) {
            return ActionResult.CANNOT_TARGET_SELF;
        }
        Guild guild = manager.getByPlayer(inviter.getUniqueId()).orElse(null);
        if (guild == null) {
            return ActionResult.NOT_IN_GUILD;
        }
        if (!isOfficerOrAbove(guild, inviter.getUniqueId())) {
            return ActionResult.INSUFFICIENT_ROLE;
        }
        if (manager.getByPlayer(invitee.getUniqueId()).isPresent()) {
            return ActionResult.TARGET_ALREADY_IN_GUILD;
        }
        manager.invite(guild.getId(), invitee.getUniqueId());
        return ActionResult.OK;
    }

    public ActionResult accept(Player invitee) {
        Optional<Guild> guild = manager.consumeInvite(invitee.getUniqueId());
        if (guild.isEmpty()) {
            return ActionResult.NO_PENDING_INVITE;
        }
        manager.addMember(guild.get(), invitee.getUniqueId(), GuildRole.MEMBER);
        return ActionResult.OK;
    }

    public ActionResult leave(Player player) {
        Guild guild = manager.getByPlayer(player.getUniqueId()).orElse(null);
        if (guild == null) {
            return ActionResult.NOT_IN_GUILD;
        }
        manager.removeMember(guild, player.getUniqueId());
        return ActionResult.OK;
    }

    public ActionResult kick(Player actor, UUID targetId) {
        Guild guild = manager.getByPlayer(actor.getUniqueId()).orElse(null);
        if (guild == null) {
            return ActionResult.NOT_IN_GUILD;
        }
        if (!isOfficerOrAbove(guild, actor.getUniqueId())) {
            return ActionResult.INSUFFICIENT_ROLE;
        }
        if (targetId.equals(guild.getLeaderId())) {
            return ActionResult.CANNOT_TARGET_LEADER;
        }
        manager.removeMember(guild, targetId);
        return ActionResult.OK;
    }

    public ActionResult setRole(Player leader, UUID targetId, GuildRole role) {
        Guild guild = manager.getByPlayer(leader.getUniqueId()).orElse(null);
        if (guild == null) {
            return ActionResult.NOT_IN_GUILD;
        }
        if (!guild.getLeaderId().equals(leader.getUniqueId())) {
            return ActionResult.INSUFFICIENT_ROLE;
        }
        guild.setRole(targetId, role);
        manager.persist(guild);
        return ActionResult.OK;
    }

    public ActionResult disband(Player leader) {
        Guild guild = manager.getByPlayer(leader.getUniqueId()).orElse(null);
        if (guild == null) {
            return ActionResult.NOT_IN_GUILD;
        }
        if (!guild.getLeaderId().equals(leader.getUniqueId())) {
            return ActionResult.INSUFFICIENT_ROLE;
        }
        manager.disband(guild);
        return ActionResult.OK;
    }

    public Optional<Guild> getGuild(UUID playerId) {
        return manager.getByPlayer(playerId);
    }

    private boolean isOfficerOrAbove(Guild guild, UUID playerId) {
        GuildRole role = guild.roleOf(playerId);
        return role == GuildRole.LEADER || role == GuildRole.OFFICER;
    }
}
