package madoku.craft.levels;

import madoku.craft.api.MadokuAPIManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

public final class MadokuCraftLevels implements ModInitializer {
	public static final String MOD_ID = "madoku-craft-levels";

	@Override
	public void onInitialize() {
		MadokuLevelsManager.initialize();

		ServerLifecycleEvents.SERVER_STARTED.register(server -> {
			MadokuLevelsManager.reset();
			MadokuLevelsManager.loadPersistedData(server);
		});

		ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
			MadokuLevelsManager.savePersistedData(server);
			MadokuAPIManager.savePersistedData(server);
		});

		ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
			MadokuLevelsManager.reset();
		});

		ServerTickEvents.END_SERVER_TICK.register(server -> {
			MadokuLevelsManager.autosavePersistedData(server);
			MadokuLevelsManager.flushDirtySyncs(server);
		});
	}
}
