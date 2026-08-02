package rpg.extra.trade.config;

import org.bukkit.configuration.file.YamlConfiguration;

/**
 * Timeout/item-cap settings for two-player trading (SOW TradeModule follow-up) - previously
 * a trade request or an open session could sit forever with nothing to force it closed.
 */
public final class TradeConfig {

    private int requestTimeoutSeconds = 30;
    private int sessionTimeoutSeconds = 300;
    private int maxItemsPerOffer = 9;

    public void load(YamlConfiguration config) {
        requestTimeoutSeconds = config.getInt("trade.request-timeout-seconds", 30);
        sessionTimeoutSeconds = config.getInt("trade.session-timeout-seconds", 300);
        maxItemsPerOffer = config.getInt("trade.max-items-per-offer", 9);
    }

    public int getRequestTimeoutSeconds() {
        return requestTimeoutSeconds;
    }

    public int getSessionTimeoutSeconds() {
        return sessionTimeoutSeconds;
    }

    public int getMaxItemsPerOffer() {
        return maxItemsPerOffer;
    }
}
