package rafradek.tf2weapons.item;


import net.minecraft.world.InteractionHand;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import rafradek.tf2weapons.NBTLiterals;
import rafradek.tf2weapons.common.TF2Achievements;
import rafradek.tf2weapons.common.TF2Attribute;
import rafradek.tf2weapons.common.WeaponsCapability;
import rafradek.tf2weapons.entity.IEntityTF2;
import rafradek.tf2weapons.entity.mercenary.EntityTF2Character;
import rafradek.tf2weapons.util.TF2DamageSource;
import rafradek.tf2weapons.util.TF2Util;

import javax.annotation.Nullable;
import java.util.HashMap;

public class ItemKnife extends ItemMeleeWeapon {

	public ItemKnife() {
		super();
		this.addPropertyOverride(new ResourceLocation("backstab"), new IItemPropertyGetter() {
			@Override
			@OnlyIn(Dist.CLIENT)
			public float apply(ItemStack stack, @Nullable Level world, @Nullable LivingEntity entityIn) {
				if (entityIn == Minecraft.getMinecraft().player && Minecraft.getMinecraft().objectMouseOver != null
						&& Minecraft.getMinecraft().objectMouseOver.entityHit != null
						&& TF2Util.getDistanceSqBox(Minecraft.getMinecraft().objectMouseOver.entityHit, entityIn.posX,
								entityIn.posY, entityIn.posZ, entityIn.width,
								entityIn.height) <= getMaxRange(stack) * getMaxRange(stack)
						&& isBackstab(entityIn, Minecraft.getMinecraft().objectMouseOver.entityHit, stack))
					return 1;
				return 0;
			}
		});
	}

	@Override
	public void handleShoot(LivingEntity living, ItemStack stack, Level world, HashMap<Entity, float[]> map,
			int critical, int flags) {
		for (Entity target : map.keySet()) {
			if (this.isBackstab(living, target, stack)) {
				flags += TF2DamageSource.BACKSTAB;
				break;
			}
		}
		super.handleShoot(living, stack, world, map, critical, flags);
	}

	public boolean isBackstab(LivingEntity living, Entity target, ItemStack stack) {
		if (target != null && target instanceof LivingEntity
				&& !(target instanceof IEntityTF2 && !((IEntityTF2) target).isBackStabbable(living, stack))) {
			float ourAngle = 180 + Mth.wrapDegrees(living.rotationYawHead);
			float angle2 = (float) (Mth.atan2(living.posX - target.posX, living.posZ - target.posZ) * 180.0D
					/ Math.PI);
			// System.out.println(angle2);
			if (angle2 >= 0)
				angle2 = 180 - angle2;
			else
				angle2 = -180 - angle2;
			angle2 += 180;
			float enemyAngle = 180 + Mth.wrapDegrees(target.getRotationYawHead());
			float difference = 180 - Math.abs(Math.abs(ourAngle - enemyAngle) - 180);
			float difference2 = 180 - Math.abs(Math.abs(angle2 - enemyAngle) - 180);
			// System.out.println(angle2+" "+difference2+" "+difference);
			if (difference < 90 && difference2 < 90)
				return true;
		}
		return false;
	}

	public float getBackstabBonusDamage(ItemStack stack, LivingEntity living, Entity target) {
		float base = 4f;
		if (living instanceof Player && ((Player) living).getCooldownTracker().hasCooldown(this)) {
			base -= (base - 1) * (((Player) living).getCooldownTracker().getCooldown(this, 0));
		}
		base *= Math.pow(TF2Attribute.getModifier("Backstab Damage", stack, 1, living), 2.0f);
		if (target instanceof IEntityTF2) {
			base = ((IEntityTF2) target).getBackstabDamageReduction(living, stack, base);
		}
		if (target.getEntityData().hasKey(NBTLiterals.BACKSTAB_MULT))
			base *= target.getEntityData().getFloat(NBTLiterals.BACKSTAB_MULT);
		return Math.max(1f, base);
	}

	@Override
	public float getWeaponDamage(ItemStack stack, LivingEntity living, Entity target) {
		return super.getWeaponDamage(stack, living, target)
				* (this.isBackstab(living, target, stack) ? this.getBackstabBonusDamage(stack, living, target) : 1);
	}

	@Override
	public int setCritical(ItemStack stack, LivingEntity shooter, Entity target, int old, DamageSource source) {
		return super.setCritical(stack, shooter, target, this.isBackstab(shooter, target, stack) ? 2 : old, source);
	}

	/*
	 * @OnlyIn(Dist.CLIENT) public ModelResourceLocation getModel(ItemStack stack,
	 * Player player, int useRemaining) { if (
	 * Minecraft.getMinecraft().player!=null&&Minecraft.getMinecraft(
	 * ).player.getHeldItem(InteractionHand.MAIN_HAND)==stack&& stack.getItem() instanceof
	 * ItemKnife&& /Minecraft.getMinecraft().objectMouseOver.entityHit != null &&
	 * this.isBackstab(player, Minecraft.getMinecraft().objectMouseOver.entityHit))
	 * return ClientProxy.nameToModel.get(stack.getTagCompound().getString("Type") +
	 * "/b"); return null; }
	 */
	@Override
	public void onDealDamage(ItemStack stack, LivingEntity attacker, Entity target, DamageSource source,
			float amount) {
		super.onDealDamage(stack, attacker, target, source, amount);
		if (attacker instanceof Player && isBackstab(attacker, target, stack) && target.isEntityAlive()
				&& !(target instanceof EntityTF2Character && ((EntityTF2Character) target).isGiant())) {
			if (target instanceof Mob) {
				if (target.getEntityData().hasKey(NBTLiterals.BACKSTAB_MULT)) {
					target.getEntityData().setFloat(NBTLiterals.BACKSTAB_MULT,
							Math.max(0.5f, target.getEntityData().getFloat(NBTLiterals.BACKSTAB_MULT) * 0.9f));
				} else
					target.getEntityData().setFloat(NBTLiterals.BACKSTAB_MULT, 0.9f);
			}
			((Player) attacker).getCooldownTracker().setCooldown(this, this.getFiringSpeed(stack, attacker) / 12);
		}
		boolean isBackstab = isBackstab(attacker, target, stack);

		if (attacker instanceof ServerPlayer && isBackstab && target instanceof LivingEntity
				&& !target.isEntityAlive() && TF2Util.isEnemy(attacker, (LivingEntity) target)) {
			((ServerPlayer) attacker).addStat(TF2Achievements.KILLED_BACKSTAB);
			if (TF2Attribute.getModifier("Disguise Backstab", stack, 0, attacker) != 0) {

				WeaponsCapability.get(attacker).stabbedDisguise = true;
				if (!(target instanceof Player))
					WeaponsCapability.get(attacker).setDisguiseType("M:" + EntityList.getKey(target).toString());
				else
					WeaponsCapability.get(attacker).setDisguiseType("P:" + target.getName());
				((LivingEntity) target).deathTime = 15;
				// TF2Util.sendTracking(new TF2Message.ActionMessage(19, (LivingEntity)
				// target), target);
				WeaponsCapability.get(attacker).setDisguised(true);
			}
			/*
			 * if(((ServerPlayer)
			 * attacker).getStatFile().readStat(TF2Achievements.KILLED_BACKSTAB)>=400)
			 * ((ServerPlayer) attacker).addStat(TF2Achievements.SPYMASTER);
			 * if(attacker.getCapability(TF2weapons.PLAYER_CAP, null).sapperTime>0 &&
			 * attacker.getCapability(TF2weapons.PLAYER_CAP,
			 * null).buildingOwnerKill==target) ((ServerPlayer)
			 * attacker).addStat(TF2Achievements.SAP_STAB);
			 */
		}
	}
}
