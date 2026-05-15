package net.minecraftforge.common;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public interface ISpecialArmor {
	ArmorProperties getProperties(LivingEntity player, ItemStack armor, DamageSource source, double damage, int slot);

	int getArmorDisplay(LivingEntity player, ItemStack armor, int slot);

	void damageArmor(LivingEntity entity, ItemStack stack, DamageSource source, int damage, int slot);

	class ArmorProperties {
		public int priority;
		public double ratio;
		public int max;

		public ArmorProperties(int priority, double ratio, int max) {
			this.priority = priority;
			this.ratio = ratio;
			this.max = max;
		}
	}
}
