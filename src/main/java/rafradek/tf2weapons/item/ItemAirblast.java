package rafradek.tf2weapons.item;


import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.play.server.ClientboundMoveEntityPacketTeleport;
import net.minecraft.network.play.server.ClientboundMoveEntityPacketVelocity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.fml.common.registry.IThrowableEntity;
import rafradek.tf2weapons.TF2weapons;
import rafradek.tf2weapons.client.ClientProxy;
import rafradek.tf2weapons.common.TF2Achievements;
import rafradek.tf2weapons.common.TF2Attribute;
import rafradek.tf2weapons.common.WeaponsCapability;
import rafradek.tf2weapons.entity.building.EntityBuilding;
import rafradek.tf2weapons.entity.projectile.EntityProjectileBase;
import rafradek.tf2weapons.entity.projectile.EntityRocket;
import rafradek.tf2weapons.entity.projectile.EntityStickybomb;
import rafradek.tf2weapons.util.PropertyType;
import rafradek.tf2weapons.util.TF2Util;

import java.util.List;

public class ItemAirblast extends ItemProjectileWeapon {

	@Override
	public boolean canAltFire(Level worldObj, LivingEntity player, ItemStack item) {
		return super.canAltFire(worldObj, player, item) && WeaponsCapability.get(player).getPrimaryCooldown() <= 50
				&& TF2Attribute.getModifier("Cannot Airblast", item, 0, player) == 0;
	}

	@Override
	public short getAltFiringSpeed(ItemStack item, LivingEntity player) {
		return (short) TF2Attribute.getModifier("Airblast Rate", item, 750, player);
	}

	public static boolean isPushable(LivingEntity living, Entity target) {
		return !(target instanceof EntityBuilding)
				&& !(target instanceof EntityProjectileBase && !((EntityProjectileBase) target).isPushable())
				&& !(target instanceof EntityArrow && target.onGround)
				&& !(target instanceof IThrowableEntity && ((IThrowableEntity) target).getThrower() == living)
				&& !TF2Util.isOnSameTeam(living, target);
	}

	@Override
	public void playHitSound(ItemStack stack, LivingEntity living, Entity target) {

		if (target.isBurning() && getData(stack).hasProperty(PropertyType.SPECIAL_1_SOUND))
			TF2Util.playSound(target, ItemFromData.getSound(stack, PropertyType.SPECIAL_1_SOUND), 0.7F, 1F);
		else
			super.playHitSound(stack, living, target);

	}

	@Override
	public void altUse(ItemStack stack, LivingEntity living, Level world) {
		living.getCapability(TF2weapons.WEAPONS_CAP, null).setPrimaryCooldown(this.getAltFiringSpeed(stack, living));
		if (world.isRemote) {
			if (ClientProxy.fireSounds.get(living) != null)
				ClientProxy.fireSounds.get(living).setDone();
			// Minecraft.getMinecraft().getSoundHandler().stopSound(ClientProxy.fireSounds.get(living));
			return;
		}
		int ammoUse = 15;
		if (!(living instanceof Player && ((Player) living).capabilities.isCreativeMode)
				&& this.getAmmoAmount(living, stack) < ammoUse)
			return;
		this.consumeAmmoGlobal(living, stack, ammoUse);
		// String airblastSound=getData(stack).get("Airblast
		// Sound").getString();
		TF2Util.playSound(living, ItemFromData.getSound(stack, PropertyType.AIRBLAST_SOUND), 1f, 1f);

		Vec3 lookVec = living.getLook(1f);
		Vec3 eyeVec = new Vec3(living.posX, living.posY + living.getEyeHeight(), living.posZ);
		eyeVec.add(lookVec);
		float size = TF2Attribute.getModifier("Flame Range", stack, 5, living);
		List<Entity> list = world.getEntitiesWithinAABB(Entity.class, new AABB(eyeVec.x - size,
				eyeVec.y - size, eyeVec.z - size, eyeVec.x + size, eyeVec.y + size, eyeVec.z + size));
		// System.out.println("aiming: "+lookVec+" "+eyeVec+" "+centerVec);
		for (Entity entity : list) {
			// System.out.println("dist: "+entity.getDistanceSq(living.posX,
			// living.posY + (double)living.getEyeHeight(), living.posZ));
			if (!ItemAirblast.isPushable(living, entity)
					|| entity.getDistanceSq(living.posX, living.posY + living.getEyeHeight(), living.posZ) > size * size
					|| !TF2Util.lookingAt(living, 60, entity.posX, entity.posY + entity.height / 2, entity.posZ))
				continue;
			if (entity instanceof IThrowableEntity && !(entity instanceof EntityStickybomb))
				((IThrowableEntity) entity).setThrower(living);
			else if (entity instanceof EntityStickybomb)
				((EntityStickybomb) entity).addStickCooldown();
			else if (entity instanceof EntityArrow) {
				((EntityArrow) entity).shootingEntity = living;
				((EntityArrow) entity).setDamage(((EntityArrow) entity).getDamage() * 1.35);
			}
			if (entity instanceof IProjectile) {
				IProjectile proj = (IProjectile) entity;
				float speed = (float) Math.sqrt(entity.motionX * entity.motionX + entity.motionY * entity.motionY
						+ entity.motionZ * entity.motionZ)
						* (0.65f + TF2Attribute.getModifier("Flame Range", stack, 0.5f, living));
				List<HitResult> rayTraces = TF2Util.pierce(world, living, eyeVec.x, eyeVec.y, eyeVec.z,
						eyeVec.x + lookVec.x * 256, eyeVec.y + lookVec.y * 256, eyeVec.z + lookVec.z * 256, false,
						0.08f, false);
				if (!rayTraces.isEmpty() && rayTraces.get(0).hitVec != null)
					// System.out.println("hit: "+mop.hitVec);
					proj.shoot(rayTraces.get(0).hitVec.x - entity.posX,
							rayTraces.get(0).hitVec.y - entity.posY - entity.height / 2,
							rayTraces.get(0).hitVec.z - entity.posZ, speed, 0);
				else
					proj.shoot(eyeVec.x + lookVec.x * 256 - entity.posX, eyeVec.y + lookVec.y * 256 - entity.posY,
							eyeVec.z + lookVec.z * 256 - entity.posZ, speed, 0);
			} else {
				double mult = (entity instanceof LivingEntity
						? 1 - ((LivingEntity) entity)
								.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).getAttributeValue()
						: 0.2) + TF2Attribute.getModifier("Flame Range", stack, 0.8f, living);
				entity.motionX = lookVec.x * 0.6 * mult;
				entity.motionY = (lookVec.y * 0.2 + 0.36) * mult;
				entity.motionZ = lookVec.z * 0.6 * mult;
			}
			if (entity instanceof EntityProjectileBase) {
				((EntityProjectileBase) entity).reflected = true;
				((EntityProjectileBase) entity).setCritical(Math.max(((EntityProjectileBase) entity).getCritical(), 1));
				if (entity instanceof EntityRocket && ((EntityRocket) entity).shootingEntity instanceof Player) {
					living.getCapability(TF2weapons.WEAPONS_CAP, null).tickAirblasted = living.ticksExisted;
				}
			}
			if (!(entity instanceof LivingEntity)) {
				// String throwObjectSound=getData(stack).get("Airblast Rocket
				// Sound").getString();
				entity.playSound(ItemFromData.getSound(stack, PropertyType.AIRBLAST_ROCKET_SOUND), 1.5f, 1f);
				// System.out.println("class: " + entity.getName());
			}
			if (living instanceof ServerPlayer) {
				((ServerPlayer) living).addStat(TF2Achievements.PROJECTILES_REFLECTED);
				/*
				 * if(((ServerPlayer)living).getStatFile().readStat(TF2Achievements.
				 * PROJECTILES_REFLECTED)>=100){
				 * ((ServerPlayer)living).addStat(TF2Achievements.HOT_POTATO); }
				 */
			}
			EntityTracker tracker = ((ServerLevel) world).getEntityTracker();
			tracker.sendToTrackingAndSelf(entity, new ClientboundMoveEntityPacketVelocity(entity));
			tracker.sendToTrackingAndSelf(entity, new ClientboundMoveEntityPacketTeleport(entity));
		}
	}
}
