package rpg.extra.party.service;

import org.bukkit.entity.Player;
import rpg.extra.party.manager.PartyManager;
import rpg.extra.party.model.Party;

import java.util.Optional;
import java.util.UUID;

/**
 * Party business rules on top of {@link PartyManager}'s plain data operations: who may
 * invite/kick/disband, and the player-facing outcomes.
 */
public final class PartyService {

    public enum ActionResult {
        OK, ALREADY_IN_PARTY, NOT_IN_PARTY, NOT_LEADER, PARTY_FULL, TARGET_ALREADY_IN_PARTY, NO_PENDING_INVITE, CANNOT_TARGET_SELF
    }

    private final PartyManager manager;
    private final int maxPartySize;

    public PartyService(PartyManager manager, int maxPartySize) {
        this.manager = manager;
        this.maxPartySize = maxPartySize;
    }

    public ActionResult create(Player leader) {
        if (manager.getByPlayer(leader.getUniqueId()).isPresent()) {
            return ActionResult.ALREADY_IN_PARTY;
        }
        manager.create(leader.getUniqueId(), maxPartySize);
        return ActionResult.OK;
    }

    public ActionResult invite(Player inviter, Player invitee) {
        if (inviter.getUniqueId().equals(invitee.getUniqueId())) {
            return ActionResult.CANNOT_TARGET_SELF;
        }
        Party party = manager.getByPlayer(inviter.getUniqueId()).orElse(null);
        if (party == null) {
            return ActionResult.NOT_IN_PARTY;
        }
        if (!party.getLeaderId().equals(inviter.getUniqueId())) {
            return ActionResult.NOT_LEADER;
        }
        if (manager.getByPlayer(invitee.getUniqueId()).isPresent()) {
            return ActionResult.TARGET_ALREADY_IN_PARTY;
        }
        if (party.isFull()) {
            return ActionResult.PARTY_FULL;
        }
        manager.invite(inviter.getUniqueId(), invitee.getUniqueId());
        return ActionResult.OK;
    }

    public ActionResult accept(Player invitee) {
        Optional<Party> party = manager.consumeInvite(invitee.getUniqueId());
        if (party.isEmpty()) {
            return ActionResult.NO_PENDING_INVITE;
        }
        if (party.get().isFull()) {
            return ActionResult.PARTY_FULL;
        }
        manager.joinParty(party.get(), invitee.getUniqueId());
        return ActionResult.OK;
    }

    public ActionResult leave(Player player) {
        if (manager.getByPlayer(player.getUniqueId()).isEmpty()) {
            return ActionResult.NOT_IN_PARTY;
        }
        manager.leaveParty(player.getUniqueId());
        return ActionResult.OK;
    }

    public ActionResult kick(Player leader, UUID targetId) {
        Party party = manager.getByPlayer(leader.getUniqueId()).orElse(null);
        if (party == null) {
            return ActionResult.NOT_IN_PARTY;
        }
        if (!party.getLeaderId().equals(leader.getUniqueId())) {
            return ActionResult.NOT_LEADER;
        }
        if (!party.getMembers().contains(targetId)) {
            return ActionResult.NOT_IN_PARTY;
        }
        manager.leaveParty(targetId);
        return ActionResult.OK;
    }

    public ActionResult disband(Player leader) {
        Party party = manager.getByPlayer(leader.getUniqueId()).orElse(null);
        if (party == null) {
            return ActionResult.NOT_IN_PARTY;
        }
        if (!party.getLeaderId().equals(leader.getUniqueId())) {
            return ActionResult.NOT_LEADER;
        }
        manager.disband(party);
        return ActionResult.OK;
    }

    public Optional<Party> getParty(UUID playerId) {
        return manager.getByPlayer(playerId);
    }
}
