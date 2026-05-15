package rafradek.tf2weapons.util;

import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public class DamageSourceProjectile extends DamageSource {
	public Entity damageSourceEntity;
	public Entity trueSource;
	public ItemStack weapon = ItemStack.EMPTY;
	public boolean selfdmg;
	public boolean notProjectile;

	public DamageSourceProjectile(String type, Entity projectile, Entity attacker, ItemStack weapon) {
		super(null);
		this.damageSourceEntity = projectile;
		this.trueSource = attacker;
		this.weapon = weapon == null ? ItemStack.EMPTY : weapon;
	}

	public Component getDeathMessage(LivingEntity living) {
		return Component.translatable("death.attack.generic", living.getDisplayName());
	}

	public Vec3 getDamageLocation() {
		return damageSourceEntity == null ? null : damageSourceEntity.position();
	}

	public boolean isProjectile() {
		return !notProjectile;
	}

	public Entity getTrueSource() {
		return trueSource;
	}

	public Entity getImmediateSource() {
		return damageSourceEntity;
	}

	public ItemStack getWeapon() {
		return weapon;
	}
}
