package madoku.craft.levels;

import madoku.craft.config.StaticJsonSystem;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

public final class MadokuCraftLevels implements ModInitializer {
	public static final String MOD_ID = "madoku-craft-levels";

	@Override
	public void onInitialize() {
		StaticJsonSystem.initialize();
		MadokuLevels.initialize();

		ServerLifecycleEvents.SERVER_STARTED.register(server -> {
			MadokuLevels.reset();
			MadokuLevels.loadPersistedData(server);
		});

		ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
			MadokuLevels.savePersistedData(server);
			MadokuLevels.reset();
		});

		ServerTickEvents.END_SERVER_TICK.register(server -> {
			MadokuLevels.autosavePersistedData(server);
			MadokuLevels.flushDirtySyncs(server);
		});
	}
}
