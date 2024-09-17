package xyz.mashtoolz.custom;

import java.util.List;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import xyz.mashtoolz.FaceLift;
import xyz.mashtoolz.config.FaceConfig;

public enum FaceStatus {

	WELL_RESTED(12000, StatusEffectCategory.BENEFICIAL),
	ESCAPE_COOLDOWN(6000, StatusEffectCategory.NEUTRAL),
	CURSE_STACK(-1, StatusEffectCategory.HARMFUL);

	private static FaceLift INSTANCE = FaceLift.getInstance();

	private static final List<FaceStatus> EFFECTS = new ArrayList<>();

	private final int duration;
	private final StatusEffect effect;

	FaceStatus(int duration, StatusEffectCategory category) {
		this.duration = duration;
		this.effect = new FaceStatusEffect(category, 0x000000, this);
	}

	public StatusEffect getEffect() {
		return effect;
	}

	public void applyEffect() {
		applyEffect(this, this.duration);
		INSTANCE.CONFIG.general.statusEffects.put(this, System.currentTimeMillis());
		FaceConfig.save();
	}

	private static void applyEffect(FaceStatus status, int duration) {
		if (status.equals(FaceStatus.CURSE_STACK))
			duration = -1;
		var effect = new StatusEffectInstance(status.effect, duration, 0, false, false, true);
		INSTANCE.CLIENT.player.addStatusEffect(effect);
		FaceLift.info(false, "Applying effect: " + status.name() + " for " + duration + " ticks");
	}

	public void removeEffect() {
		INSTANCE.CLIENT.player.removeStatusEffect(this.effect);
		INSTANCE.CONFIG.general.statusEffects.remove(this);
		FaceConfig.save();
		FaceLift.info(false, "Removed effect: " + this.name());
	}

	public static void getDescription(StatusEffectInstance statusEffect, CallbackInfoReturnable<Text> cir) {
		var status = statusEffect.getEffectType();
		if (!(status instanceof FaceStatusEffect))
			return;

		var effect = (FaceStatusEffect) status;
		switch (effect.getFaceStatus()) {
			case CURSE_STACK -> cir.setReturnValue(Text.of("Curse Stacks: " + INSTANCE.CONFIG.general.curseStacks));
			default -> {
			}
		}
	}

	public static void registerEffects() {
		for (FaceStatus status : FaceStatus.values()) {
			EFFECTS.add(status);
			Registry.register(Registries.STATUS_EFFECT, new Identifier("facelift", status.name().toLowerCase()), status.effect);
		}
	}

	public static void update() {
		var player = INSTANCE.CLIENT.player;
		if (player == null)
			return;

		for (var entry : INSTANCE.CONFIG.general.statusEffects.entrySet()) {
			var status = entry.getKey();
			var startTime = entry.getValue();
			var elapsedTime = (System.currentTimeMillis() - startTime) / 1000;
			var remainingTicks = (int) ((status.duration / 20) - elapsedTime) * 20;
			var statusEffect = player.getStatusEffect(status.getEffect());
			if (statusEffect == null)
				applyEffect(status, remainingTicks);
		}

		for (var faceStatus : EFFECTS) {

			var statusEffect = player.getStatusEffect(faceStatus.getEffect());
			if (statusEffect == null) {
				if (faceStatus.equals(FaceStatus.CURSE_STACK) && INSTANCE.CONFIG.general.curseStacks > 0)
					faceStatus.applyEffect();
				continue;
			}

			var effect = (FaceStatusEffect) statusEffect.getEffectType();
			switch (effect.getFaceStatus()) {
				case CURSE_STACK -> {
					if (INSTANCE.CONFIG.general.curseStacks <= 0) {
						faceStatus.removeEffect();
						continue;
					}
				}

				default -> {
					if (statusEffect.getDuration() <= 0)
						faceStatus.removeEffect();
				}
			}
		}
	}

	public static class FaceStatusEffect extends StatusEffect {

		private final FaceStatus status;

		protected FaceStatusEffect(StatusEffectCategory category, int color, FaceStatus status) {
			super(category, color);
			this.status = status;
		}

		@Override
		public boolean canApplyUpdateEffect(int duration, int amplifier) {
			return false;
		}

		public FaceStatus getFaceStatus() {
			return status;
		}
	}
}
