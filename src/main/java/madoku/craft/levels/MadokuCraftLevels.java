package madoku.craft.levels;

import madoku.craft.clock.MadokuTicks;
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
			MadokuTicks.reset();
			MadokuLevels.reset();
			MadokuLevels.loadPersistedData(server);
		});

		ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
			MadokuLevels.savePersistedData(server);
			MadokuLevels.reset();
			MadokuTicks.reset();
		});

		ServerTickEvents.END_SERVER_TICK.register(server -> {
			MadokuTicks.advance(server, 1L);
			MadokuLevels.autosavePersistedData(server);
			MadokuLevels.flushDirtySyncs(server);
		});
	}
}
