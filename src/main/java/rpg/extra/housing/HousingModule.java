package rpg.extra.housing;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.configuration.file.YamlConfiguration;
import rpg.database.manager.DatabaseManager;
import rpg.extra.core.OreliaExtraPlugin;
import rpg.extra.core.module.ExtraModule;
import rpg.extra.housing.command.HousingCommand;
import rpg.extra.housing.repository.HouseOwnershipRepository;
import rpg.extra.housing.repository.HousePlotRepository;
import rpg.extra.housing.service.HousingService;

import java.util.logging.Level;

/**
 * Housing module: config-driven purchasable house plots with a {@code /house home} teleport
 * (SOW HousingModule). Money settles through Vault's {@link Economy}.
 */
public final class HousingModule implements ExtraModule {

    private final HousePlotRepository plotRepository = new HousePlotRepository();
    private HousingService housingService;
    private OreliaExtraPlugin plugin;

    @Override
    public String getName() {
        return "housing";
    }

    @Override
    public void onEnable(OreliaExtraPlugin plugin) {
        this.plugin = plugin;
        DatabaseManager databaseManager = plugin.getServer().getServicesManager().load(DatabaseManager.class);
        if (databaseManager == null) {
            throw new IllegalStateException("housing module requires OreliaCore's DatabaseManager");
        }
        Economy economy = plugin.getServer().getServicesManager().load(Economy.class);
        if (economy == null) {
            throw new IllegalStateException("housing module requires Vault's Economy service");
        }

        reloadPlots();

        HouseOwnershipRepository ownershipRepository = new HouseOwnershipRepository(databaseManager);
        try {
            ownershipRepository.createSchemaIfNotExists();
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to initialize housing schema", e);
        }

        this.housingService = new HousingService(plotRepository, ownershipRepository, economy);
        housingService.loadAll();

        plugin.getCommand("house").setExecutor(new HousingCommand(housingService));
    }

    @Override
    public void onDisable() {
    }

    @Override
    public void onReload() {
        reloadPlots();
    }

    private void reloadPlots() {
        plugin.getConfigManager().register("housing.yml");
        YamlConfiguration config = plugin.getConfigManager().get("housing.yml").get();
        plotRepository.load(config);
    }

    public HousingService getHousingService() {
        return housingService;
    }
}
