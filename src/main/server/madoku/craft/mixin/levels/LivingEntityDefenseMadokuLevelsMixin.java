package madoku.craft.mixin.levels;

import madoku.craft.java.levels.LevelsPlayerAPIManager;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Applies the Levels Defense stat when the Attributes armor module is unavailable. */
@Mixin(LivingEntity.class)
public abstract class LivingEntityDefenseMadokuLevelsMixin {
	private static final double DAMAGE_REDUCTION_PER_DEFENSE_POINT = 0.02D;

	@Inject(method = "getDamageAfterArmorAbsorb", at = @At("RETURN"), cancellable = true)
	private void madokuCraft$applyDefenseLevel(
		DamageSource source,
		float amount,
		CallbackInfoReturnable<Float> callbackInfo
	) {
		if (isHandledByAttributesCompat() || !((Object) this instanceof ServerPlayer player)) return;

		double defensePoints = LevelsPlayerAPIManager.getPlayerDefensePoints(player);
		float damageAfterArmor = callbackInfo.getReturnValue();
		if (defensePoints <= 0.0D || damageAfterArmor <= 0.0F) return;

		double reduction = Math.min(1.0D, defensePoints * DAMAGE_REDUCTION_PER_DEFENSE_POINT);
		callbackInfo.setReturnValue((float) Math.max(0.0D, damageAfterArmor * (1.0D - reduction)));
	}

	private static boolean isHandledByAttributesCompat() {
		FabricLoader loader = FabricLoader.getInstance();
		if (loader.isModLoaded("madoku-craft")) return true;
		return loader.isModLoaded("madoku-craft-compat")
			&& loader.isModLoaded("madoku-craft-attributes")
			&& loader.isModLoaded("madoku-craft-levels");
	}
}
