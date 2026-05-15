package rafradek.tf2weapons.potion;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

public class Potion extends MobEffect {
	public Potion() {
		this(false, 0);
	}

	public Potion(boolean isBadEffect, int color) {
		super(isBadEffect ? MobEffectCategory.HARMFUL : MobEffectCategory.BENEFICIAL, color);
	}

	public Potion setIconIndex(int x, int y) {
		return this;
	}
}
