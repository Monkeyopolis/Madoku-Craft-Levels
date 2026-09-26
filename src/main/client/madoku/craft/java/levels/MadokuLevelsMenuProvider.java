package madoku.craft.java.levels;

import madoku.craft.java.core.menu.LevelsMenuProvider;
import net.minecraft.resources.Identifier;

import java.util.List;

/** Supplies synchronized Madoku Levels data to the Core-owned Levels menu. */
public final class MadokuLevelsMenuProvider implements LevelsMenuProvider {
	@Override
	public int version() {
		return MadokuLevelsClientState.version();
	}

	@Override
	public Snapshot snapshot() {
		MadokuLevelsClientState.Snapshot source = MadokuLevelsClientState.snapshot();
		List<Stat> stats = source.visibleStats().stream()
			.map(stat -> new Stat(
				stat.id(),
				stat.label(),
				rowTexture(stat),
				source.statLevel(stat),
				source.maxStatLevel(stat)
			))
			.toList();
		return new Snapshot(
			source.username(),
			source.level(),
			source.currentXp(),
			source.requiredXp(),
			source.availablePoints(),
			stats
		);
	}

	@Override
	public void requestUpgrade(String statId) {
		LevelStat stat = LevelStat.fromId(statId);
		if (stat != null) MadokuLevelsClient.requestStatUpgrade(stat);
	}

	private Identifier rowTexture(LevelStat stat) {
		String textureName = switch (stat) {
			case MOVEMENT_SPEED -> "speed";
		default -> stat.id();
		};
		return Identifier.fromNamespaceAndPath("madoku-craft", "textures/madoku-menu/levels-menu/" + textureName + "-attribute.png");
	}
}
