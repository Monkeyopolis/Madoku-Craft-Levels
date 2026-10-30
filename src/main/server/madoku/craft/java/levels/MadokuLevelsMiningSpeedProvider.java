package madoku.craft.java.levels;

import madoku.craft.java.core.attribute.PlayerAttributeProvider;
import madoku.craft.java.core.attribute.PlayerAttributeStat;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;

/** Provides the server-side Madoku Levels Mining bonus to the shared resolver. */
public final class MadokuLevelsMiningSpeedProvider implements PlayerAttributeProvider {
	@Override public String id() { return "madoku-levels-server"; }

	@Override
	public double bonus(Player player, PlayerAttributeStat stat, BlockState state) {
		if (stat != PlayerAttributeStat.MINING || state == null || !(player instanceof ServerPlayer serverPlayer)) return 0.0D;
		LevelsPlayerState playerState = LevelsPlayerManager.state(serverPlayer);
		return LevelsAttributesManager.valueAtLevel(serverPlayer, LevelStat.MINING, playerState.statLevel(LevelStat.MINING));
	}
}
