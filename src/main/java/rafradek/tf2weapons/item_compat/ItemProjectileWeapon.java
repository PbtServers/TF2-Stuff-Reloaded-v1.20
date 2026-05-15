package rafradek.tf2weapons.item;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import rafradek.tf2weapons.entity.projectile.EntityProjectileBase;

public class ItemProjectileWeapon extends ItemWeapon {
	public void onProjectileShoot(ItemStack stack, EntityProjectileBase projectile, LivingEntity living, Level level, int critical) {}
}
