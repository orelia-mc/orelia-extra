package rpg.extra.core.module;

import rpg.extra.core.OreliaExtraPlugin;

/**
 * Lifecycle contract for orelia-extra's top-level modules (Party, Guild, Trade, Mail,
 * Auction, Housing, Pet, Mount, Ranking, Achievement - SOW section
 * "後から追加するMMORPG向け機能"), mirroring orelia-core's {@code RpgModule} and
 * orelia-world's {@code WorldModule}.
 */
public interface ExtraModule {

    String getName();

    void onEnable(OreliaExtraPlugin plugin);

    void onDisable();

    default void onReload() {
    }
}
