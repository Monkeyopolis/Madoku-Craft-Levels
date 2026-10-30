package madoku.craft.java.levels;

import madoku.craft.java.core.attribute.PlayerAttributeProvider;
import madoku.craft.java.core.attribute.PlayerAttributeStat;
import net.minecraft.server.level.ServerPlayer;

/** Supplies the level-stat contribution to the shared player-stat resolver. */
public final class MadokuLevelsPlayerAttributeProvider implements PlayerAttributeProvider {
	@Override
	public String id() {
		return "levels";
	}

	@Override
	public double bonus(ServerPlayer player, PlayerAttributeStat stat) {
		if (player == null || stat == null || !MadokuLevelsManager.isEnabled()) return 0.0D;
		LevelsPlayerState state = LevelsPlayerManager.state(player);
		return switch (stat) {
			case HEALTH -> LevelsAttributesManager.valueAtLevel(player, LevelStat.HEALTH, state.statLevel(LevelStat.HEALTH));
			case STRENGTH -> LevelsAttributesManager.valueAtLevel(player, LevelStat.STRENGTH, state.statLevel(LevelStat.STRENGTH));
			case DEFENSE -> LevelsAttributesManager.valueAtLevel(player, LevelStat.DEFENSE, state.statLevel(LevelStat.DEFENSE));
			case LUCK -> LevelsAttributesManager.valueAtLevel(player, LevelStat.LUCK, state.statLevel(LevelStat.LUCK));
			case MOVEMENT_SPEED -> LevelsAttributesManager.valueAtLevel(player, LevelStat.MOVEMENT_SPEED, state.statLevel(LevelStat.MOVEMENT_SPEED));
			case HUNGER -> LevelsAttributesManager.valueAtLevel(player, LevelStat.HUNGER, state.statLevel(LevelStat.HUNGER));
			case OXYGEN -> LevelsAttributesManager.valueAtLevel(player, LevelStat.OXYGEN, state.statLevel(LevelStat.OXYGEN));
			case MINING -> 0.0D;
		};
	}
}
