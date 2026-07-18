package madoku.craft.levels;

import madoku.craft.levels.MadokuLevelsManager.LevelStat;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/** Applies only the attributes owned by the Levels mod. */
public final class LevelsAttributesManager {
	private static final Identifier HEALTH_MODIFIER = id("levels_health");
	private static final Identifier DAMAGE_MODIFIER = id("levels_damage");
	private static final Identifier ARMOR_MODIFIER = id("levels_armor");
	private static final Identifier MOVEMENT_SPEED_MODIFIER = id("levels_movement_speed");
	private LevelsAttributesManager() { }
	public static void initialize() { }
	public static void reset() { }

	public static void applyPlayerAttributes(ServerPlayer player) {
		if (player == null) return;
		LevelsPlayerManager.PlayerState state = LevelsPlayerManager.state(player);
		apply(player.getAttribute(Attributes.MAX_HEALTH), HEALTH_MODIFIER, valueAtLevel(player, LevelStat.HEALTH, state.statLevel(LevelStat.HEALTH)));
		apply(player.getAttribute(Attributes.ATTACK_DAMAGE), DAMAGE_MODIFIER, valueAtLevel(player, LevelStat.PLAYER_DAMAGE, state.statLevel(LevelStat.PLAYER_DAMAGE)));
		apply(player.getAttribute(Attributes.ARMOR), ARMOR_MODIFIER, valueAtLevel(player, LevelStat.PLAYER_ARMOR, state.statLevel(LevelStat.PLAYER_ARMOR)));
		apply(player.getAttribute(Attributes.MOVEMENT_SPEED), MOVEMENT_SPEED_MODIFIER, valueAtLevel(player, LevelStat.PLAYER_MOVEMENT_SPEED, state.statLevel(LevelStat.PLAYER_MOVEMENT_SPEED)));
		if (player.getHealth() > player.getMaxHealth()) player.setHealth(player.getMaxHealth());
	}

	public static double valueAtLevel(ServerPlayer player, LevelStat stat, int level) {
		if (stat == null || !MadokuLevelsManager.isEnabled()) return 0.0d;
		LevelsConfigManager.StatSettings settings = LevelsConfigManager.stat(stat);
		double increment = settings.value();
		if (settings.type() == LevelsConfigManager.IncrementType.PERCENTAGE) increment *= baseValue(player, stat);
		return Math.max(0.0d, increment * Math.max(0, stat.clampLevel(level)));
	}

	private static double baseValue(ServerPlayer player, LevelStat stat) {
		if (player == null) return switch (stat) {
			case HEALTH -> 20.0d;
			case PLAYER_DAMAGE -> 1.0d;
			case PLAYER_ARMOR -> 0.0d;
			case PLAYER_MOVEMENT_SPEED -> 0.1d;
		};
		return switch (stat) {
			case HEALTH -> baseAttribute(player, Attributes.MAX_HEALTH, 20.0d);
			case PLAYER_DAMAGE -> baseAttribute(player, Attributes.ATTACK_DAMAGE, 1.0d);
			case PLAYER_ARMOR -> baseAttribute(player, Attributes.ARMOR, 0.0d);
			case PLAYER_MOVEMENT_SPEED -> baseAttribute(player, Attributes.MOVEMENT_SPEED, 0.1d);
		};
	}

	private static double baseAttribute(ServerPlayer player, net.minecraft.core.Holder<net.minecraft.world.entity.ai.attributes.Attribute> attribute, double fallback) {
		AttributeInstance instance = player.getAttribute(attribute);
		return instance == null ? fallback : instance.getBaseValue();
	}
	private static void apply(AttributeInstance attribute, Identifier id, double amount) {
		if (attribute == null) return;
		attribute.removeModifier(id);
		if (amount > 0.0d) attribute.addOrUpdateTransientModifier(new AttributeModifier(id, amount, AttributeModifier.Operation.ADD_VALUE));
	}
	private static Identifier id(String path) { return Identifier.fromNamespaceAndPath(MadokuCraftLevels.MOD_ID, path); }
}
