package rafradek.tf2weapons.item;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

public class Potion extends MobEffect {
	public Potion() {
		this(false, 0);
	}

	public Potion(boolean badEffect, int color) {
		super(badEffect ? MobEffectCategory.HARMFUL : MobEffectCategory.BENEFICIAL, color);
	}
}
