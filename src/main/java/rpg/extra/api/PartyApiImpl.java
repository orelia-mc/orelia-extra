package rpg.extra.api;

import rpg.extra.party.service.PartyService;

import java.util.UUID;

final class PartyApiImpl implements PartyApi {

    private final PartyService partyService;

    PartyApiImpl(PartyService partyService) {
        this.partyService = partyService;
    }

    @Override
    public boolean isInParty(UUID playerId) {
        return partyService.getParty(playerId).isPresent();
    }
}
