package madoku.craft.levels;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import madoku.craft.api.json.JSONFormatManager;
import madoku.craft.api.json.MadokuJSONManager;
import madoku.craft.levels.MadokuLevelsManager.LevelStat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.EnumMap;

/** Owns the Levels configuration in the API managed JSON format. */
public final class LevelsConfigManager {
	public static final String CONFIG_FOLDER_NAME = "madoku-craft-levels";
	public static final String CONFIG_FILE_NAME = "madoku-levels";
	private static final Logger LOGGER = LoggerFactory.getLogger(LevelsConfigManager.class);
	private static volatile Settings settings = Settings.defaults();

	private LevelsConfigManager() { }
	public static void initialize() { load(); }
	public static void reload() { load(); }
	public static void reset() { settings = Settings.defaults(); }
	public static Settings settings() { return settings; }
	public static boolean isEnabled() { return settings.enabled(); }
	public static PlayerSettings player() { return settings.player(); }
	public static StatSettings stat(LevelStat stat) {
		return stat == null ? StatSettings.defaults() : settings.stats().getOrDefault(stat, StatSettings.defaults());
	}

	private static void load() {
		Settings fallback = Settings.defaults();
		try {
			Path directory = MadokuJSONManager.getOrCreateGlobalSystemDirectory(CONFIG_FOLDER_NAME);
			Path file = directory.resolve(CONFIG_FILE_NAME + ".json");
			JsonObject source = JSONFormatManager.readManagedDocument(file).data();
			JsonObject normalized = JSONFormatManager.ensureManagedFile(file, fallback.toJson());
			Settings loaded = Settings.fromJson(normalized, source);
			JSONFormatManager.writeManagedFile(file, loaded.toJson(), fallback.toJson());
			settings = loaded;
		} catch (IOException | RuntimeException exception) {
			settings = fallback;
			LOGGER.warn("Using default Madoku Levels settings after configuration load failure.", exception);
		}
	}

	public enum IncrementType {
		FLAT("flat"), PERCENTAGE("percentage");
		private final String id;
		IncrementType(String id) { this.id = id; }
		public String id() { return id; }
		private static IncrementType fromId(String id) {
			for (IncrementType type : values()) if (type.id.equalsIgnoreCase(id == null ? "" : id.trim())) return type;
			return FLAT;
		}
	}

	public record Settings(boolean enabled, PlayerSettings player, EnumMap<LevelStat, StatSettings> stats) {
		private static Settings defaults() {
			EnumMap<LevelStat, StatSettings> stats = new EnumMap<>(LevelStat.class);
			for (LevelStat stat : LevelStat.values()) stats.put(stat, StatSettings.defaultsFor(stat));
			return new Settings(true, PlayerSettings.defaults(), stats);
		}
		private static Settings fromJson(JsonObject source, JsonObject legacySource) {
			Settings defaults = defaults();
			JsonObject legacyMain = object(legacySource, "main");
			boolean legacyFormat = !legacyMain.isEmpty() && object(legacySource, "levels").isEmpty();
			JsonObject levels = object(source, "levels");
			if (levels.isEmpty()) levels = object(legacySource, "levels");
			JsonObject player = object(levels, "player");
			EnumMap<LevelStat, StatSettings> stats = new EnumMap<>(LevelStat.class);
			JsonObject statsObject = object(source, "stats");
			if (statsObject.isEmpty()) statsObject = object(legacySource, "stats");
			for (LevelStat stat : LevelStat.values()) {
				JsonObject statSource = legacyFormat ? legacyStat(legacyMain, stat) : object(statsObject, stat.id());
				stats.put(stat, StatSettings.fromJson(statSource, defaults.stats().get(stat)));
			}
			boolean enabled = legacyFormat
				? readBoolean(object(legacySource, "general"), "enabled", true)
				: readBoolean(source, "enabled", defaults.enabled());
			return new Settings(
				enabled,
				new PlayerSettings(
					legacyFormat ? readPositiveInt(legacyMain, "max-player-level", defaults.player().maxLevel()) : readPositiveInt(player, "max-level", defaults.player().maxLevel()),
					legacyFormat ? readNonNegativeDouble(legacyMain, "base-xp-requirement", defaults.player().baseXpRequirement()) : readNonNegativeDouble(player, "base-xp-requirement", defaults.player().baseXpRequirement()),
					legacyFormat ? readNonNegativeDouble(legacyMain, "base-xp-multiplier", defaults.player().baseXpMultiplier()) : readNonNegativeDouble(player, "base-xp-multiplier", defaults.player().baseXpMultiplier())
				),
				stats
			);
		}
		private JsonObject toJson() {
			JsonObject statsJson = new JsonObject();
			for (LevelStat stat : LevelStat.values()) statsJson.add(stat.id(), stats.getOrDefault(stat, StatSettings.defaultsFor(stat)).toJson());
			return JSONFormatManager.object().put("enabled", enabled)
				.object("levels", levels -> levels.object("player", player -> player
					.put("max-level", this.player.maxLevel()).put("base-xp-requirement", this.player.baseXpRequirement())
					.put("base-xp-multiplier", this.player.baseXpMultiplier())))
				.put("stats", statsJson).build();
		}
	}

	public record PlayerSettings(int maxLevel, double baseXpRequirement, double baseXpMultiplier) {
		private static PlayerSettings defaults() { return new PlayerSettings(40, 5.0d, 0.1d); }
	}

	public record StatSettings(int maxLevel, IncrementType type, double value) {
		private static StatSettings defaults() { return new StatSettings(10, IncrementType.FLAT, 0.0d); }
		private static StatSettings defaultsFor(LevelStat stat) { return new StatSettings(10, IncrementType.FLAT, stat == null ? 0.0d : stat.defaultIncrement()); }
		private static StatSettings fromJson(JsonObject source, StatSettings fallback) {
			StatSettings defaults = fallback == null ? defaults() : fallback;
			JsonObject increment = object(source, "level-increment");
			return new StatSettings(readPositiveInt(source, "max-level", defaults.maxLevel()),
				IncrementType.fromId(readString(increment, "type", defaults.type().id())),
				readNonNegativeDouble(increment, "value", readLegacyValue(source, defaults.value())));
		}
		private JsonObject toJson() { return JSONFormatManager.object().put("max-level", maxLevel)
			.object("level-increment", increment -> increment.put("type", type.id()).put("value", value)).build(); }
	}

	private static JsonObject legacyStat(JsonObject main, LevelStat stat) {
		String key = switch (stat) {
			case HEALTH -> "health-per-level";
			case PLAYER_DAMAGE -> "player-damage-per-level";
			case PLAYER_ARMOR -> "player-armor-per-level";
			case PLAYER_MOVEMENT_SPEED -> "player-movement-speed-per-level";
		};
		JsonObject result = new JsonObject();
		if (main != null && main.has(key)) result.add("level-increment", JSONFormatManager.object().put("value", main.get(key)).put("type", "flat").build());
		return result;
	}
	private static double readLegacyValue(JsonObject source, double fallback) { return readNonNegativeDouble(source, "value", fallback); }
	private static JsonObject object(JsonObject source, String key) { JsonElement e = source == null ? null : source.get(key); return e != null && e.isJsonObject() ? e.getAsJsonObject() : new JsonObject(); }
	private static boolean readBoolean(JsonObject source, String key, boolean fallback) { try { return source != null && source.has(key) ? source.get(key).getAsBoolean() : fallback; } catch (RuntimeException e) { return fallback; } }
	private static String readString(JsonObject source, String key, String fallback) { try { return source != null && source.has(key) ? source.get(key).getAsString() : fallback; } catch (RuntimeException e) { return fallback; } }
	private static int readPositiveInt(JsonObject source, String key, int fallback) { try { return source != null && source.has(key) ? Math.max(1, source.get(key).getAsInt()) : fallback; } catch (RuntimeException e) { return fallback; } }
	private static double readNonNegativeDouble(JsonObject source, String key, double fallback) { try { double value = source != null && source.has(key) ? source.get(key).getAsDouble() : fallback; return Double.isFinite(value) ? Math.max(0.0d, value) : fallback; } catch (RuntimeException e) { return fallback; } }
}

