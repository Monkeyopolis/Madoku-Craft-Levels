package madoku.craft.levels;

import net.minecraft.resources.ResourceLocation;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public enum MadokuLevelStat {
	HEALTH("health", "Health", "health", 0xFF6B6B),
	PLAYER_DAMAGE("player_damage", "Strength", "strength", 0xF4A261),
	PLAYER_ARMOR("player_armor", "Defense", "defense", 0x4D96FF),
	PLAYER_MOVEMENT_SPEED("player_movement_speed", "Speed", "speed", 0x43AA8B);

	public static final int DEFAULT_STAT_LEVEL = 0;

	private final String id;
	private final String label;
	private final String iconTextureName;
	private final int accentColor;

	MadokuLevelStat(String id, String label, String iconTextureName, int accentColor) {
		this.id = id;
		this.label = label;
		this.iconTextureName = iconTextureName;
		this.accentColor = accentColor;
	}

	public String id() {
		return id;
	}

	public String label() {
		return label;
	}

	public static int maxStatLevel() {
		return MadokuLevels.maxStatLevel();
	}

	public ResourceLocation iconTexture() {
		return ResourceLocation.fromNamespaceAndPath(MadokuCraftLevels.MOD_ID, "textures/icons/" + iconTextureName + ".png");
	}

	public int accentColor() {
		return accentColor;
	}

	public double valueAtLevel(int level) {
		int normalizedLevel = clampLevel(level);
		return switch (this) {
			case HEALTH -> normalizedLevel * MadokuLevels.healthPerLevel();
			case PLAYER_DAMAGE -> normalizedLevel * MadokuLevels.playerDamagePerLevel();
			case PLAYER_ARMOR -> normalizedLevel * MadokuLevels.playerArmorPerLevel();
			case PLAYER_MOVEMENT_SPEED -> normalizedLevel * MadokuLevels.playerMovementSpeedPerLevel();
		};
	}

	public String formattedValue(int level) {
		return "+" + BigDecimal.valueOf(valueAtLevel(level)).stripTrailingZeros().toPlainString();
	}

	public static int clampLevel(int level) {
		return Math.max(0, Math.min(maxStatLevel(), level));
	}

	public static MadokuLevelStat fromId(String id) {
		if (id == null || id.isBlank()) {
			return null;
		}

		for (MadokuLevelStat stat : values()) {
			if (stat.id.equalsIgnoreCase(id.trim())) {
				return stat;
			}
		}

		return null;
	}

	public static EnumMap<MadokuLevelStat, Integer> createDefaultLevels() {
		EnumMap<MadokuLevelStat, Integer> levels = new EnumMap<>(MadokuLevelStat.class);
		for (MadokuLevelStat stat : values()) {
			levels.put(stat, DEFAULT_STAT_LEVEL);
		}
		return levels;
	}

	public static List<MadokuLevelStat> visibleStats() {
		return List.of(HEALTH, PLAYER_DAMAGE, PLAYER_ARMOR, PLAYER_MOVEMENT_SPEED);
	}

	public static String encodeLevels(Map<MadokuLevelStat, Integer> levels) {
		StringBuilder builder = new StringBuilder();
		for (MadokuLevelStat stat : values()) {
			if (builder.length() > 0) {
				builder.append(';');
			}
			int level = clampLevel(levels == null ? DEFAULT_STAT_LEVEL : levels.getOrDefault(stat, DEFAULT_STAT_LEVEL));
			builder.append(stat.id).append('=').append(level);
		}
		return builder.toString();
	}

	public static EnumMap<MadokuLevelStat, Integer> decodeLevels(String encodedLevels) {
		EnumMap<MadokuLevelStat, Integer> decoded = createDefaultLevels();
		if (encodedLevels == null || encodedLevels.isBlank()) {
			return decoded;
		}

		String[] entries = encodedLevels.split(";");
		for (String entry : entries) {
			String[] pair = entry.split("=", 2);
			if (pair.length != 2) {
				continue;
			}

			MadokuLevelStat stat = fromId(pair[0]);
			if (stat == null) {
				continue;
			}

			try {
				decoded.put(stat, clampLevel(Integer.parseInt(pair[1].trim())));
			} catch (NumberFormatException ignored) {
				decoded.put(stat, DEFAULT_STAT_LEVEL);
			}
		}

		return decoded;
	}
}
