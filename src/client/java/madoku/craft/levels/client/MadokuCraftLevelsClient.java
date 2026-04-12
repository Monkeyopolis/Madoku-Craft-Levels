package madoku.craft.levels.client;

import madoku.craft.levels.MadokuLevelsClient;
import net.fabricmc.api.ClientModInitializer;

public final class MadokuCraftLevelsClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		MadokuLevelsClient.initialize();
	}
}
