package madoku.craft.java.levels;

import madoku.craft.java.core.menu.MenuAPIManager;
import madoku.craft.java.core.menu.MenuEntry;
import madoku.craft.java.core.menu.LevelsMenuAPIManager;
import madoku.craft.java.core.menu.LevelsMenuClientAPIManager;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.resources.Identifier;

public final class MadokuLevelsClient {
	private static boolean initialized = false;

	private MadokuLevelsClient() {
	}

	public static void initialize() {
		if (initialized) {
			return;
		}

		LevelsMenuAPIManager.registerProvider(new MadokuLevelsMenuProvider());
		MenuAPIManager.registerEntry(new MenuEntry(
			"levels",
			"menu.madoku-craft.levels",
			texture("madoku-menu/main-menu/levels-button.png"),
			texture("madoku-menu/main-menu/levels-button-highlighted.png"),
			30,
			client -> LevelsMenuClientAPIManager.open()
		));
		ClientPlayNetworking.registerGlobalReceiver(LevelsPayloadAPIManager.Payload.TYPE, (payload, context) ->
			MadokuLevelsClientState.applyPayload(payload)
		);
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> MadokuLevelsClientState.clear());
		initialized = true;
	}

	private static Identifier texture(String path) {
		return Identifier.fromNamespaceAndPath("madoku-craft", "textures/" + path);
	}

	public static void requestStatUpgrade(LevelStat stat) {
		if (stat == null) {
			return;
		}

		ClientPlayNetworking.send(new LevelsPayloadAPIManager.LevelUpPayload(stat.id()));
	}
}
