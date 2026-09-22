package madoku.craft.mixin.levels;

import madoku.craft.java.levels.LevelsPlayerAPIManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Adds the player's Levels Mining bonus to the final block-breaking speed. */
@Mixin(Player.class)
public abstract class PlayerMiningSpeedMadokuLevelsMixin {
	@Inject(method = "getDestroySpeed", at = @At("RETURN"), cancellable = true)
	private void madokuCraft$addMiningLevelBonus(BlockState state, CallbackInfoReturnable<Float> callbackInfo) {
		if (!((Object) this instanceof ServerPlayer player)) return;
		double bonus = LevelsPlayerAPIManager.getPlayerMiningSpeedBonus(player);
		if (bonus <= 0.0d) return;
		callbackInfo.setReturnValue(callbackInfo.getReturnValue() + (float) bonus);
	}
}
