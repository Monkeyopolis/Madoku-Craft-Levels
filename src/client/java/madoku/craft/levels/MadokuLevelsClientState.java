package madoku.craft.levels;

import madoku.craft.network.MadokuLevelsPayload;
import madoku.craft.levels.MadokuLevelsManager.LevelStat;

import java.util.EnumMap;
import java.util.List;

public final class MadokuLevelsClientState {
	private static Snapshot snapshot = Snapshot.empty();
	private static int version = 0;

	private MadokuLevelsClientState() {
	}

	public static void applyPayload(MadokuLevelsPayload payload) {
		if (payload == null) {
			return;
		}

		snapshot = new Snapshot(
			payload.username(),
			Math.max(1, payload.level()),
			Math.max(0, payload.currentXp()),
			Math.max(1, payload.requiredXp()),
			Math.max(0, payload.availablePoints()),
			decodeMaxStatLevels(payload.maxStatLevels()),
			payload.useAttributesContainer(),
			visibleStatsForPayload(payload),
			decodeStatLevels(payload.statLevels(), decodeMaxStatLevels(payload.maxStatLevels()))
		);
		version++;
	}

	public static Snapshot snapshot() {
		return snapshot;
	}

	public static int version() {
		return version;
	}

	public static void clear() {
		snapshot = Snapshot.empty();
		version++;
	}

	private static List<LevelStat> visibleStatsForPayload(MadokuLevelsPayload payload) {
		List<LevelStat> decoded = LevelStat.decodeVisibleStats(payload.visibleStats());
		if (!decoded.isEmpty()) {
			return List.copyOf(decoded);
		}
		return LevelStat.visibleStats();
	}

	private static EnumMap<LevelStat, Integer> decodeStatLevels(String encoded, EnumMap<LevelStat, Integer> maxLevels) {
		EnumMap<LevelStat, Integer> levels = LevelStat.createDefaultLevels();
		if (encoded == null || encoded.isBlank()) return levels;
		for (String entry : encoded.split(";")) {
			String[] pair = entry.split("=", 2);
			if (pair.length != 2) continue;
			LevelStat stat = LevelStat.fromId(pair[0]);
			if (stat == null) continue;
			try {
				int maximum = maxLevels.getOrDefault(stat, stat.maxLevel());
				levels.put(stat, Math.max(LevelStat.DEFAULT_LEVEL, Math.min(maximum, Integer.parseInt(pair[1].trim()))));
			} catch (NumberFormatException ignored) { }
		}
		return levels;
	}

	private static java.util.EnumMap<LevelStat, Integer> decodeMaxStatLevels(String encoded) {
		java.util.EnumMap<LevelStat, Integer> maxLevels = new java.util.EnumMap<>(LevelStat.class);
		for (LevelStat stat : LevelStat.values()) maxLevels.put(stat, stat.maxLevel());
		if (encoded != null) for (String entry : encoded.split(";")) {
			String[] pair = entry.split("=", 2);
			if (pair.length != 2) continue;
			LevelStat stat = LevelStat.fromId(pair[0]);
			if (stat == null) continue;
			try { maxLevels.put(stat, Math.max(1, Integer.parseInt(pair[1].trim()))); } catch (NumberFormatException ignored) { }
		}
		return maxLevels;
	}

	public record Snapshot(
		String username,
		int level,
		int currentXp,
		int requiredXp,
		int availablePoints,
		java.util.EnumMap<LevelStat, Integer> maxStatLevels,
		boolean useAttributesContainer,
		List<LevelStat> visibleStats,
		EnumMap<LevelStat, Integer> statLevels
	) {
		private static Snapshot empty() {
			return new Snapshot(
				"",
				1,
				0,
				1,
				0,
				decodeMaxStatLevels(""),
				false,
				LevelStat.visibleStats(),
				LevelStat.createDefaultLevels()
			);
		}

		public boolean hasData() {
			return username != null && !username.isBlank();
		}

		public int statLevel(LevelStat stat) {
			return statLevels.getOrDefault(stat, LevelStat.DEFAULT_LEVEL);
		}

		public int maxStatLevel(LevelStat stat) {
			return maxStatLevels.getOrDefault(stat, stat == null ? 1 : stat.maxLevel());
		}
	}
}
