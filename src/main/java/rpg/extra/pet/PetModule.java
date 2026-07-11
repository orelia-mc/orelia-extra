package rpg.extra.pet;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.configuration.file.YamlConfiguration;
import rpg.database.manager.DatabaseManager;
import rpg.extra.core.OreliaExtraPlugin;
import rpg.extra.core.module.ExtraModule;
import rpg.extra.pet.command.PetCommand;
import rpg.extra.pet.listener.PetQuitListener;
import rpg.extra.pet.manager.PetManager;
import rpg.extra.pet.repository.PetConfigRepository;
import rpg.extra.pet.repository.PetOwnershipRepository;
import rpg.extra.pet.service.PetService;

import java.util.logging.Level;

/**
 * Pet module: config-driven follower pets the player unlocks and summons/dismisses (SOW
 * PetModule). Money settles through Vault's {@link Economy}.
 */
public final class PetModule implements ExtraModule {

    private static final long FOLLOW_TICK_PERIOD_TICKS = 10L;

    private final PetConfigRepository configRepository = new PetConfigRepository();
    private final PetManager petManager = new PetManager();
    private PetService petService;
    private OreliaExtraPlugin plugin;

    @Override
    public String getName() {
        return "pet";
    }

    @Override
    public void onEnable(OreliaExtraPlugin plugin) {
        this.plugin = plugin;
        DatabaseManager databaseManager = plugin.getServer().getServicesManager().load(DatabaseManager.class);
        if (databaseManager == null) {
            throw new IllegalStateException("pet module requires OreliaCore's DatabaseManager");
        }
        Economy economy = plugin.getServer().getServicesManager().load(Economy.class);
        if (economy == null) {
            throw new IllegalStateException("pet module requires Vault's Economy service");
        }

        reloadPets();

        PetOwnershipRepository ownershipRepository = new PetOwnershipRepository(databaseManager);
        try {
            ownershipRepository.createSchemaIfNotExists();
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to initialize pet schema", e);
        }

        this.petService = new PetService(configRepository, ownershipRepository, petManager, economy);
        petService.loadAll();

        plugin.getServer().getPluginManager().registerEvents(new PetQuitListener(petManager), plugin);
        plugin.getPlayerCommandRegistry().register("pet", new PetCommand(petService));

        plugin.getSchedulerService().runTimer(petManager::tickFollow, FOLLOW_TICK_PERIOD_TICKS, FOLLOW_TICK_PERIOD_TICKS);
    }

    @Override
    public void onDisable() {
        petManager.despawnAll();
    }

    @Override
    public void onReload() {
        reloadPets();
    }

    private void reloadPets() {
        plugin.getConfigManager().register("pets.yml");
        YamlConfiguration config = plugin.getConfigManager().get("pets.yml").get();
        configRepository.load(config);
    }

    public PetService getPetService() {
        return petService;
    }
}
