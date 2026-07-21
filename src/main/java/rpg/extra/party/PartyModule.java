package rpg.extra.party;

import rpg.core.command.CommandAliasUtil;
import rpg.extra.core.OreliaExtraPlugin;
import rpg.extra.core.module.ExtraModule;
import rpg.extra.party.command.PartyCommand;
import rpg.extra.party.listener.PartyQuitListener;
import rpg.extra.party.manager.PartyManager;
import rpg.extra.party.service.PartyService;

/**
 * Party module: runtime (not persisted) player groups - create/invite/accept/leave/kick/
 * disband (SOW PartyModule).
 */
public final class PartyModule implements ExtraModule {

    private final PartyManager manager = new PartyManager();
    private PartyService partyService;

    @Override
    public String getName() {
        return "party";
    }

    @Override
    public void onEnable(OreliaExtraPlugin plugin) {
        int maxPartySize = plugin.getConfigManager().get("config.yml").get().getInt("party.max-size", 6);
        this.partyService = new PartyService(manager, maxPartySize);

        plugin.getServer().getPluginManager().registerEvents(new PartyQuitListener(manager), plugin);
        PartyCommand partyCommand = new PartyCommand(partyService, plugin.getMessageManager());
        String description = "パーティーを管理します。";
        String usage = "party <create|invite|accept|leave|kick|disband|list|chat <message>>";
        plugin.getPlayerCommandRegistry().register("party", partyCommand, description, usage);
        CommandAliasUtil.registerAlias(plugin, "party", partyCommand, description,
                "<create|invite|accept|leave|kick|disband|list|chat <message>>");
    }

    @Override
    public void onDisable() {
    }

    public PartyService getPartyService() {
        return partyService;
    }
}
