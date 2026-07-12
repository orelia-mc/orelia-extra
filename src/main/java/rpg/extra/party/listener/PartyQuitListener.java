package rpg.extra.party.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import rpg.extra.party.manager.PartyManager;

/** Drops a disconnecting player's pending invite; party membership itself persists until they explicitly leave. */
public final class PartyQuitListener implements Listener {

    private final PartyManager manager;

    public PartyQuitListener(PartyManager manager) {
        this.manager = manager;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        manager.clearInvite(event.getPlayer().getUniqueId());
    }
}
