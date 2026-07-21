package rpg.extra.api;

import java.util.UUID;

/**
 * Cross-plugin surface exposing a player's party membership (e.g. for orelia-serverutil's
 * placeholder system). Published via Bukkit's {@code ServicesManager} by {@link ExtraApiModule}.
 */
public interface PartyApi {

    boolean isInParty(UUID playerId);
}
