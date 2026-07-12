package rpg.extra.core.module;

import rpg.extra.core.OreliaExtraPlugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;

/**
 * Registration-order lifecycle registry for {@link ExtraModule}s, mirroring orelia-core's
 * {@code ModuleManager} and orelia-world's {@code WorldModuleManager}.
 */
public final class ExtraModuleManager {

    private final OreliaExtraPlugin plugin;
    private final List<ExtraModule> registrationOrder = new ArrayList<>();
    private final Map<Class<? extends ExtraModule>, ExtraModule> byType = new LinkedHashMap<>();

    public ExtraModuleManager(OreliaExtraPlugin plugin) {
        this.plugin = plugin;
    }

    public void register(ExtraModule module) {
        registrationOrder.add(module);
        byType.put(module.getClass(), module);
    }

    public void enableAll() {
        for (ExtraModule module : registrationOrder) {
            try {
                module.onEnable(plugin);
                plugin.getLogger().info("Module enabled: " + module.getName());
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to enable module: " + module.getName(), e);
            }
        }
    }

    public void disableAll() {
        List<ExtraModule> reversed = new ArrayList<>(registrationOrder);
        Collections.reverse(reversed);
        for (ExtraModule module : reversed) {
            try {
                module.onDisable();
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to disable module: " + module.getName(), e);
            }
        }
    }

    public void reloadAll() {
        for (ExtraModule module : registrationOrder) {
            try {
                module.onReload();
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to reload module: " + module.getName(), e);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends ExtraModule> Optional<T> get(Class<T> type) {
        return Optional.ofNullable((T) byType.get(type));
    }
}
