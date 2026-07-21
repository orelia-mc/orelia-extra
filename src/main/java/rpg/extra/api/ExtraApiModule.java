package rpg.extra.api;

import org.bukkit.plugin.ServicePriority;
import rpg.extra.auction.AuctionModule;
import rpg.extra.core.OreliaExtraPlugin;
import rpg.extra.core.module.ExtraModule;
import rpg.extra.guild.GuildModule;
import rpg.extra.mail.MailModule;
import rpg.extra.party.PartyModule;
import rpg.extra.ranking.RankingModule;

/**
 * Publishes orelia-extra's own cross-plugin debug API ({@link ExtraDebugApi}) to Bukkit's
 * {@code ServicesManager}, mirroring orelia-core's {@code ApiModule} and orelia-world's
 * {@code WorldApiModule}. Registered last so every module it wraps is already enabled.
 */
public final class ExtraApiModule implements ExtraModule {

    @Override
    public String getName() {
        return "extra-api";
    }

    @Override
    public void onEnable(OreliaExtraPlugin plugin) {
        AuctionModule auctionModule = require(plugin, AuctionModule.class);
        MailModule mailModule = require(plugin, MailModule.class);
        RankingModule rankingModule = require(plugin, RankingModule.class);
        GuildModule guildModule = require(plugin, GuildModule.class);
        PartyModule partyModule = require(plugin, PartyModule.class);

        plugin.getServer().getServicesManager().register(
                ExtraDebugApi.class,
                new ExtraDebugApiImpl(plugin.getConfigManager(), auctionModule, mailModule, rankingModule),
                plugin, ServicePriority.Normal);
        plugin.getServer().getServicesManager().register(
                GuildApi.class, new GuildApiImpl(guildModule.getGuildService()), plugin, ServicePriority.Normal);
        plugin.getServer().getServicesManager().register(
                PartyApi.class, new PartyApiImpl(partyModule.getPartyService()), plugin, ServicePriority.Normal);
    }

    @Override
    public void onDisable() {
    }

    private <T extends ExtraModule> T require(OreliaExtraPlugin plugin, Class<T> type) {
        return plugin.getModuleManager().get(type)
                .orElseThrow(() -> new IllegalStateException("extra-api module requires " + type.getSimpleName()));
    }
}
