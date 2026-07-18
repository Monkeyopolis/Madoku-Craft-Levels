package madoku.craft.levels;

import madoku.craft.api.sync.SyncPlayerManager;
import madoku.craft.levels.MadokuLevelsManager.LevelStat;
import madoku.craft.network.MadokuLevelUpPayload;
import madoku.craft.network.MadokuLevelsPayload;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;

/** Registers Levels payloads and uses the API transport for dirty-player sync. */
public final class LevelsPayloadManager {
	private LevelsPayloadManager() { }

	public static void initialize() {
		PayloadTypeRegistry.playS2C().register(MadokuLevelsPayload.TYPE, MadokuLevelsPayload.CODEC);
		PayloadTypeRegistry.playC2S().register(MadokuLevelUpPayload.TYPE, MadokuLevelUpPayload.CODEC);
		ServerPlayNetworking.registerGlobalReceiver(MadokuLevelUpPayload.TYPE,
			(payload, context) -> MadokuLevelsManager.handleLevelUpRequest(context.player(), payload.statId()));
	}
	public static void reset() { }

	public static MadokuLevelsPayload createPayload(ServerPlayer player) {
		LevelsPlayerManager.PlayerState state = LevelsPlayerManager.state(player);
		return new MadokuLevelsPayload(
			player.getName().getString(),
			Math.min(state.level(), LevelsPlayerManager.maxPlayerLevel()),
			state.level() >= LevelsPlayerManager.maxPlayerLevel() ? 0 : state.currentXp(),
			LevelsPlayerManager.requiredXpForLevel(state.level()),
			state.availablePoints(),
			encodeMaxStatLevels(),
			false,
			LevelStat.encodeVisibleStats(LevelStat.visibleStats()),
			LevelStat.encodeLevels(stateLevels(state))
		);
	}

	private static java.util.Map<LevelStat, Integer> stateLevels(LevelsPlayerManager.PlayerState state) {
		java.util.EnumMap<LevelStat, Integer> levels = LevelStat.createDefaultLevels();
		for (LevelStat stat : LevelStat.values()) levels.put(stat, state.statLevel(stat));
		return levels;
	}
	private static String encodeMaxStatLevels() {
		StringBuilder builder = new StringBuilder();
		for (LevelStat stat : LevelStat.values()) {
			if (builder.length() > 0) builder.append(';');
			builder.append(stat.id()).append('=').append(stat.maxLevel());
		}
		return builder.toString();
	}
	public static boolean canSend(ServerPlayer player, MadokuLevelsPayload payload) { return SyncPlayerManager.canSend(player, payload); }
	public static boolean send(ServerPlayer player, MadokuLevelsPayload payload) { return SyncPlayerManager.send(player, payload); }
}
