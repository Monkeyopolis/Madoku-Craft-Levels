package madoku.craft.java.levels;

import madoku.craft.java.core.attribute.PlayerAttributeAPIManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;

/** Applies configured level-stat increments to a player's runtime attributes. */
public final class LevelsAttributesManager {
	private LevelsAttributesManager() { }

	public static void initialize() { }

	public static void reset() { }

	public static void applyPlayerAttributes(ServerPlayer player) {
		if (player == null) return;
		PlayerAttributeAPIManager.apply(player);
		if (player.getHealth() > player.getMaxHealth()) player.setHealth(player.getMaxHealth());
	}

	public static double valueAtLevel(ServerPlayer player, LevelStat stat, int level) {
		if (stat == null || !MadokuLevelsManager.isEnabled()) return 0.0d;
		LevelsConfigManager.StatSettings settings = LevelsConfigManager.stat(stat);
		double increment = settings.value();
		if (settings.type() == LevelsConfigManager.IncrementType.PERCENTAGE) {
			increment *= baseValue(player, stat);
		}
		return Math.max(0.0d, increment * Math.max(0, stat.clampLevel(level)));
	}

	private static double baseValue(ServerPlayer player, LevelStat stat) {
		if (player == null) {
			return switch (stat) {
				case HEALTH -> 20.0d;
				case HUNGER -> 20.0d;
				case MOVEMENT_SPEED -> 0.1d;
				case STRENGTH -> 1.0d;
				case DEFENSE, LUCK, OXYGEN, MINING -> 1.0d;
			};
		}
		return switch (stat) {
			case HEALTH -> baseAttribute(player, Attributes.MAX_HEALTH, 20.0d);
			case STRENGTH -> baseAttribute(player, Attributes.ATTACK_DAMAGE, 1.0d);
			case DEFENSE -> 1.0d;
			case LUCK -> baseAttribute(player, Attributes.LUCK, 0.0d);
			case MOVEMENT_SPEED -> baseAttribute(player, Attributes.MOVEMENT_SPEED, 0.1d);
			case HUNGER -> 20.0d;
			case OXYGEN, MINING -> 1.0d;
		};
	}

	private static double baseAttribute(ServerPlayer player, net.minecraft.core.Holder<net.minecraft.world.entity.ai.attributes.Attribute> attribute, double fallback) {
		AttributeInstance instance = player.getAttribute(attribute);
		return instance == null ? fallback : instance.getBaseValue();
	}

}
