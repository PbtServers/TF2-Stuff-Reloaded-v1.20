package rafradek.tf2weapons.item;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.InteractionHand;
import rafradek.tf2weapons.TF2ConfigVars;
import rafradek.tf2weapons.TF2weapons;
import rafradek.tf2weapons.client.audio.TF2Sounds;
import rafradek.tf2weapons.common.TF2Attribute;
import rafradek.tf2weapons.entity.building.EntityBuilding;
import rafradek.tf2weapons.entity.mercenary.EntityTF2Character;
import rafradek.tf2weapons.util.TF2Util;

public class ItemSapper extends ItemBulletWeapon {

	public ItemSapper() {
		super();
		this.setMaxStackSize(64);
	}

	@Override
	public boolean onHit(ItemStack stack, LivingEntity attacker, Entity target, float damage, int critical,
			boolean simulate) {
		// System.out.println("Can hit: " + TF2weapons.canHit(attacker,
		// target));
		if (target instanceof EntityBuilding && !((EntityBuilding) target).isSapped()
				&& TF2Util.canHit(attacker, target)) {
			((EntityBuilding) target).setSapped(attacker, stack);
			if (attacker instanceof Player) {
				attacker.getCapability(TF2weapons.PLAYER_CAP, null).sapperTime = 100;
				attacker.getCapability(TF2weapons.PLAYER_CAP, null).buildingOwnerKill = ((EntityBuilding) target)
						.getOwner();
			}
			((EntityBuilding) target).playSound(TF2Sounds.MOB_SAPPER_PLANT, 1.3f, 1);
			if (((EntityBuilding) target).getOwner() != null)
				((EntityBuilding) target).getOwner().setRevengeTarget(attacker);
			if (!TF2ConfigVars.freeUseItems)
				stack.shrink(1);
			if (stack.getCount() <= 0 && attacker instanceof Player)
				((Player) attacker).inventory.deleteStack(stack);
		} else if (target instanceof EntityTF2Character && !TF2Util.isOnSameTeam(attacker, target)
				&& ((EntityTF2Character) target).isRobot()
				&& ((EntityTF2Character) target).getActivePotionEffect(TF2weapons.sapped) == null) {

			if (attacker instanceof Player && ((Player) attacker).getCooldownTracker().hasCooldown(this))
				return false;
			((EntityTF2Character) target).addPotionEffect(new MobEffectInstance(TF2weapons.stun, 140, 1));
			((EntityTF2Character) target).addPotionEffect(new MobEffectInstance(TF2weapons.sapped, 140, 0));
			float range = TF2Attribute.getModifier("Sapper Strength", stack, 0f, attacker) * 3 + 2;
			for (EntityTF2Character entity : attacker.world.getEntitiesWithinAABB(EntityTF2Character.class,
					target.getEntityBoundingBox().grow(range), entityl -> (!TF2Util.isOnSameTeam(attacker, entityl)
							&& entityl.isRobot() && entityl.getDistanceSq(target) < range * range))) {
				entity.addPotionEffect(new MobEffectInstance(TF2weapons.stun, 140, 1));
				entity.addPotionEffect(new MobEffectInstance(TF2weapons.sapped, 140, 0));
			}
			if (!TF2ConfigVars.freeUseItems)
				stack.shrink(1);
			if (attacker instanceof Player)
				((Player) attacker).getCooldownTracker().setCooldown(this, 300);
			if (stack.getCount() <= 0 && attacker instanceof Player)
				((Player) attacker).inventory.deleteStack(stack);
		}
		return false;
	}

	@Override
	public float getMaxRange(ItemStack stack) {
		return 2.4f;
	}

	public float getBulletSize() {
		return 0.35f;
	}

	@Override
	public boolean showTracer(ItemStack stack) {
		return false;
	}

	@Override
	public boolean doMuzzleFlash(ItemStack stack, LivingEntity attacker, InteractionHand hand) {
		return false;
	}
}
