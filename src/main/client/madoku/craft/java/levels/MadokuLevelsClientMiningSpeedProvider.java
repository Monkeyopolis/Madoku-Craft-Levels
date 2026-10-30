package madoku.craft.java.levels;

import madoku.craft.java.core.attribute.PlayerAttributeProvider;
import madoku.craft.java.core.attribute.PlayerAttributeStat;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;

/** Provides the client-side synchronized Madoku Levels Mining bonus. */
public final class MadokuLevelsClientMiningSpeedProvider implements PlayerAttributeProvider {
	@Override public String id() { return "madoku-levels-client"; }

	@Override
	public double bonus(Player player, PlayerAttributeStat stat, BlockState state) {
		return stat == PlayerAttributeStat.MINING && state != null && player instanceof LocalPlayer
			? LevelsAttributesManager.valueAtLevel(null, LevelStat.MINING, MadokuLevelsClientState.miningLevel())
			: 0.0D;
	}
}
