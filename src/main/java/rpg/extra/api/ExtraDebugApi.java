package rpg.extra.api;

import org.bukkit.entity.Player;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Cross-plugin surface for debug/testplay tooling (orelia-debug) over orelia-extra: config
 * inspection/editing (same pattern as orelia-core's {@code rpg.api.DebugApi} and orelia-world's
 * {@code rpg.world.api.WorldDebugApi}) plus force-opening the few extra GUIs that exist
 * (Auction/Mail/Ranking) for an arbitrary player.
 */
public interface ExtraDebugApi {

    Set<String> listConfigFiles();

    Optional<String> getConfigValue(String fileName, String path);

    boolean setConfigValue(String fileName, String path, String rawValue);

    void saveConfig(String fileName);

    List<String> describeConfigKeys(String fileName);

    void openAuction(Player player);

    void openMail(Player player);

    void openRanking(Player player);
}
