package madoku.craft.levels;

import com.mojang.blaze3d.platform.InputConstants;
import madoku.craft.network.MadokuLevelUpPayload;
import madoku.craft.network.MadokuLevelsPayload;
import madoku.craft.levels.MadokuLevelsManager.LevelStat;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

public final class MadokuLevelsClient {
	private static boolean initialized = false;
	private static final KeyMapping.Category MADOKU_LEVELS_CATEGORY = KeyMapping.Category.register(
		Identifier.fromNamespaceAndPath(MadokuCraftLevels.MOD_ID, "levels")
	);
	private static final KeyMapping OPEN_LEVELS_KEY = new KeyMapping(
		"key.madoku-craft-levels.open_levels",
		InputConstants.Type.KEYSYM,
		GLFW.GLFW_KEY_K,
		MADOKU_LEVELS_CATEGORY
	);

	private MadokuLevelsClient() {
	}

	public static void initialize() {
		if (initialized) {
			return;
		}

		KeyMappingHelper.registerKeyMapping(OPEN_LEVELS_KEY);
		ClientPlayNetworking.registerGlobalReceiver(MadokuLevelsPayload.TYPE, (payload, context) ->
			MadokuLevelsClientState.applyPayload(payload)
		);
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> MadokuLevelsClientState.clear());
		ClientTickEvents.END_CLIENT_TICK.register(MadokuLevelsClient::handleClientTick);
		initialized = true;
	}

	private static void handleClientTick(Minecraft client) {
		if (client == null || client.player == null) {
			return;
		}

		if (OPEN_LEVELS_KEY.consumeClick() && client.gui.screen() == null) {
			client.setScreenAndShow(new MadokuLevelsScreen());
		}
	}

	public static void requestStatUpgrade(LevelStat stat) {
		if (stat == null) {
			return;
		}

		ClientPlayNetworking.send(new MadokuLevelUpPayload(stat.id()));
	}
}
