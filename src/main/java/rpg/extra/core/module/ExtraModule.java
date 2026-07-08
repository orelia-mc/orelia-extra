package rpg.extra.core.module;

import rpg.extra.core.OreliaExtraPlugin;

/**
 * Lifecycle contract for orelia-extra's future top-level modules (Party, Guild, Trade,
 * Mail, Auction, Housing, Pet, Mount, Ranking, Achievement - SOW section
 * "後から追加するMMORPG向け機能"). None are implemented yet; this interface exists so the
 * first one added later slots into the same registration-order pattern as orelia-core and
 * orelia-world instead of inventing a new convention.
 */
public interface ExtraModule {

    String getName();

    void onEnable(OreliaExtraPlugin plugin);

    void onDisable();

    default void onReload() {
    }
}
