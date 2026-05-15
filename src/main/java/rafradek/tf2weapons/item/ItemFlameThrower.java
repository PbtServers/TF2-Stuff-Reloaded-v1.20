package rafradek.tf2weapons.item;




import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.util.*;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import rafradek.tf2weapons.TF2weapons;
import rafradek.tf2weapons.client.ClientProxy;
import rafradek.tf2weapons.common.TF2Attribute;
import rafradek.tf2weapons.common.WeaponsCapability;
import rafradek.tf2weapons.common.WeaponsCapability.RageType;
import rafradek.tf2weapons.util.PropertyType;
import rafradek.tf2weapons.util.TF2Util;

public class ItemFlameThrower extends ItemAirblast {

	@Override
	public boolean canAltFire(Level worldObj, LivingEntity player, ItemStack item) {
		return super.canAltFire(worldObj, player, item) || (TF2Attribute.getModifier("Rage Crit", item, 0, player) != 0
				&& this.getRage(item, player) >= this.getMaxRage(item, player));
	}

	@Override
	public boolean canFire(Level world, LivingEntity living, ItemStack stack) {
		return super.canFire(world, living, stack);
	}

	@Override
	public boolean startUse(ItemStack stack, LivingEntity living, Level world, int action, int newState) {
		if (world.isRemote && (newState & 1) - (action & 1) == 1 && this.canFire(world, living, stack)) {
			SoundEvent playSound = ItemFromData.getSound(stack, PropertyType.FIRE_START_SOUND);
			ClientProxy.playWeaponSound(living, playSound, false, 2, stack);
		}
		return false;
	}

	@Override
	public boolean endUse(ItemStack stack, LivingEntity living, Level world, int action, int newState) {
		if ((action & 1) == 1) {
			if (world.isRemote)
				// System.out.println("called"+ClientProxy.fireSounds.get(living));
				if (ClientProxy.fireSounds.get(living) != null)
					// System.out.println("called2"+ClientProxy.fireSounds.get(living).type);
					ClientProxy.fireSounds.get(living).setDone();
			// Minecraft.getMinecraft().getSoundHandler().stopSound(ClientProxy.fireSounds.get(living));
			living.playSound(ItemFromData.getSound(stack, PropertyType.FIRE_STOP_SOUND), 1f, 1f);
		}
		return false;
	}

	@Override
	public boolean fireTick(ItemStack stack, LivingEntity living, Level world) {
		if (world.isRemote && living.getCapability(TF2weapons.WEAPONS_CAP, null).getPrimaryCooldown() <= 50
				&& this.canFire(world, living, stack)) {
			if (living.getCapability(TF2weapons.WEAPONS_CAP, null).startedPress()) {
				SoundEvent playSound = ItemFromData.getSound(stack, PropertyType.FIRE_START_SOUND);
				ClientProxy.playWeaponSound(living, playSound, false, 2, stack);
			}
			if (living.isInsideOfMaterial(Material.WATER)) {
				Vec3 look = living.getLookVec();
				Vec3 tangent = new Vec3(-look.z, 0, look.x);
				Vec3 pos = living.getPositionVector().addVector(tangent.x*0.3, living.getEyeHeight(), tangent.z*0.3)
						.add(look.scale(0.5));
				world.spawnParticle(EnumParticleTypes.WATER_BUBBLE, pos.x,
						pos.y, pos.z, living.motionX, 0.2D + living.motionY,
						living.motionZ, new int[0]);
			} else {
				ClientProxy.spawnFlameParticle(world, living, 0f, false);
				ClientProxy.spawnFlameParticle(world, living, 0.5f, false);
				if (TF2Attribute.getModifier("Spawns Bubbles", stack, 0, living) > 0 && living.ticksExisted%3==0) {
					Vec3 look = living.getLookVec();
					Vec3 tangent = new Vec3(-look.z, 0, look.x);
					Vec3 motion = new Vec3(living.motionX, 0.02, living.motionZ);
					ClientProxy.spawnBubbleParticle(world, living.getPositionVector()
							.addVector(tangent.x*0.25, living.getEyeHeight(), tangent.z*0.2)
							.add(look.scale(0.5)), motion.add(tangent.scale(-0.02)));
					ClientProxy.spawnBubbleParticle(world, living.getPositionVector()
							.addVector(tangent.x*0.35, living.getEyeHeight(), tangent.z*0.3)
							.add(look.scale(0.5)), motion.add(tangent.scale(0.02)));
				}
			}
			if (TF2Util.calculateCritPre(stack, living) != 2 && (!ClientProxy.fireSounds.containsKey(living)
					|| !Minecraft.getMinecraft().getSoundHandler().isSoundPlaying(ClientProxy.fireSounds.get(living))
					|| (ClientProxy.fireSounds.get(living).type != 0 && ClientProxy.fireSounds.get(living).type != 2)))
				ClientProxy.playWeaponSound(living, ItemFromData.getSound(stack, PropertyType.FIRE_LOOP_SOUND), true, 0,
						stack);
			else if (TF2Util.calculateCritPre(stack, living) == 2 && (!ClientProxy.fireSounds.containsKey(living)
					|| !Minecraft.getMinecraft().getSoundHandler().isSoundPlaying(ClientProxy.fireSounds.get(living))
					|| (ClientProxy.fireSounds.get(living).type != 1))) {
				ResourceLocation playSoundCrit = new ResourceLocation(
						ItemFromData.getData(stack).getString(PropertyType.FIRE_LOOP_SOUND) + ".crit");

				ClientProxy.playWeaponSound(living, SoundEvent.REGISTRY.getObject(playSoundCrit), true, 1, stack);
			}
		}
		// System.out.println("nie");
		return false;
	}

	@Override
	public float getProjectileSpeed(ItemStack stack, LivingEntity living) {
		float speed = super.getProjectileSpeed(stack, living);
		return speed * 0.6f + TF2Attribute.getModifier("Flame Range", stack, speed * 0.4f, living);
	}

	@Override
	public RageType getRageType(ItemStack stack, LivingEntity living) {
		return TF2Attribute.getModifier("Rage Crit", stack, 0, null) == 1f ? RageType.PHLOG
				: super.getRageType(stack, living);
	}

	@Override
	public float getMaxRage(ItemStack stack, LivingEntity living) {
		return TF2Attribute.getModifier("Rage Crit", stack, 0, null) == 1f ? 20f : super.getMaxRage(stack, living);
	}

	@Override
	public void onDealDamage(ItemStack stack, LivingEntity attacker, Entity target, DamageSource source,
			float amount) {
		super.onDealDamage(stack, attacker, target, source, amount);

		if (target instanceof LivingEntity && TF2Attribute.getModifier("Rage Crit", stack, 0, attacker) != 0
				&& !WeaponsCapability.get(attacker).isRageActive(RageType.PHLOG)) {
			float mult = 1f;
			if (attacker instanceof Player) {
				if (target instanceof Player)
					mult = 1f;
				else if (TF2Util.isEnemy(attacker, (LivingEntity) target))
					mult = 0.4f;
				else
					mult = 0.1f;
			} else {
				if (target instanceof Player)
					mult = 4f;
			}
			this.addRage(stack, attacker, amount * mult);
		}
	}

	@Override
	public void onUpdate(ItemStack stack, Level par2World, Entity par3Entity, int par4, boolean par5) {
		super.onUpdate(stack, par2World, par3Entity, par4, par5);
		if (WeaponsCapability.get(par3Entity).isRageActive(RageType.PHLOG)) {
			if (par5 && par3Entity.ticksExisted % 5 == 0) {
				((LivingEntity) par3Entity).addPotionEffect(new MobEffectInstance(TF2weapons.critBoost, 5));
			}
		}

	}

	@Override
	public int getMaxItemUseDuration(ItemStack stack) {
		return 40;
	}

	@Override
	public EnumAction getItemUseAction(ItemStack stack) {
		return EnumAction.BLOCK;
	}

	@Override
	public ItemStack onItemUseFinish(ItemStack stack, Level world, LivingEntity entityLiving) {
		// stack.getTagCompound().setFloat("Rage", 0f);
		// stack.getTagCompound().setBoolean("RageActive", true);
		return stack;
	}

	@Override
	public boolean showInfoBox(ItemStack stack, Player player) {
		return super.showInfoBox(stack, player) || TF2Attribute.getModifier("Rage Crit", stack, 0, player) != 0;
	}

	/*
	 * public String[] getInfoBoxLines(ItemStack stack, Player player){
	 * if(TF2Attribute.getModifier("Rage Crit", stack, 0, player)==0) return
	 * super.getInfoBoxLines(stack, player); else { String[] result=new String[2];
	 * result[0]="MMMPH"; int focus=(int) TF2Attribute.getModifier("Focus", stack,
	 * 0, player); if(focus!=0){ result[0]=result[0]+" "; int progress=(int)
	 * (((float)player.getCapability(TF2weapons.WEAPONS_CAP,
	 * null).focusShotTicks/(float)(70-focus*23+((ItemUsable)stack.getItem()).
	 * getFiringSpeed(stack, player)/50))*3f); for(int i=0;i<progress && i<3;i++){
	 * result[0]=result[0]+"\u2588"; } }
	 * result[1]=(int)((stack.getTagCompound().getFloat("Rage")/20f)*100)+"%";
	 * return result; } }
	 */

	@Override
	public InteractionResultHolder<ItemStack> onItemRightClick(Level world, Player player, InteractionHand hand) {
		ItemStack itemStackIn = player.getHeldItem(hand);
		/*
		 * if (TF2Attribute.getModifier("Rage Crit", itemStackIn, 0, player)!=0
		 * &&player.getCapability(TF2weapons.WEAPONS_CAP, null).getPhlogRage()>=20f) {
		 * player.setActiveHand(hand); player.addPotionEffect(new
		 * MobEffectInstance(TF2weapons.stun,40,1)); TF2Util.addAndSendEffect(player, new
		 * MobEffectInstance(TF2weapons.uber,40,0)); player.addPotionEffect(new
		 * MobEffectInstance(TF2weapons.noKnockback,40,0));
		 * player.playSound(ItemFromData.getSound(itemStackIn,
		 * PropertyType.CHARGE_SOUND), 1f, 1f);
		 * itemStackIn.getTagCompound().setBoolean("RageActive", true); return new
		 * InteractionResultHolder<ItemStack>(InteractionResult.SUCCESS, itemStackIn); }
		 */
		return new InteractionResultHolder<>(InteractionResult.FAIL, itemStackIn);
	}

	@Override
	public void altUse(ItemStack stack, LivingEntity living, Level world) {
		if (TF2Attribute.getModifier("Rage Crit", stack, 0, living) != 0
				&& this.getRage(stack, living) >= this.getMaxRage(stack, living)) {
			living.setActiveHand(InteractionHand.MAIN_HAND);
			living.addPotionEffect(new MobEffectInstance(TF2weapons.stun, 40, 1));
			TF2Util.addAndSendEffect(living, new MobEffectInstance(TF2weapons.uber, 40, 0));
			living.addPotionEffect(new MobEffectInstance(TF2weapons.noKnockback, 40, 0));
			living.playSound(ItemFromData.getSound(stack, PropertyType.CHARGE_SOUND), 1f, 1f);
			WeaponsCapability.get(living).setRageActive(RageType.PHLOG, true, 2f);
		} else
			super.altUse(stack, living, world);
	}
}
