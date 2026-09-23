package madoku.craft.java.levels;

import madoku.craft.java.core.menu.LevelsMenuAPIManager;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public final class MadokuLevelsClient {
	private static boolean initialized = false;

	private MadokuLevelsClient() {
	}

	public static void initialize() {
		if (initialized) {
			return;
		}

		LevelsMenuAPIManager.registerProvider(new MadokuLevelsMenuProvider());
		ClientPlayNetworking.registerGlobalReceiver(LevelsPayloadAPIManager.Payload.TYPE, (payload, context) ->
			MadokuLevelsClientState.applyPayload(payload)
		);
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> MadokuLevelsClientState.clear());
		initialized = true;
	}

	public static void requestStatUpgrade(LevelStat stat) {
		if (stat == null) {
			return;
		}

		ClientPlayNetworking.send(new LevelsPayloadAPIManager.LevelUpPayload(stat.id()));
	}
}
