package madoku.craft.java.levels;

import madoku.craft.java.core.module.MadokuClientRuntime;
import madoku.craft.java.core.module.MadokuModule;
import madoku.craft.java.core.module.MadokuModuleIds;
import net.fabricmc.api.ClientModInitializer;

/** Fabric client entrypoint for the standalone Levels jar. */
public final class MadokuLevelsClientInitializer implements ClientModInitializer, MadokuModule {
	@Override public net.minecraft.resources.Identifier id() { return MadokuModuleIds.LEVELS; }
	@Override public void onInitializeClient() { MadokuClientRuntime.initialize(this); }
	@Override public void initialize() { MadokuLevelsClient.initialize(); }
}
