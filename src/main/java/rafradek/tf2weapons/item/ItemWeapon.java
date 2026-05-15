package rafradek.tf2weapons.item;




import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.NonNullList;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.InteractionHand;
import atomicstryker.dynamiclights.client.DynamicLights;
import com.google.common.collect.Multimap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.Gui;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.util.*;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.opengl.GL11;
import rafradek.tf2weapons.NBTLiterals;
import rafradek.tf2weapons.TF2ConfigVars;
import rafradek.tf2weapons.TF2PlayerCapability;
import rafradek.tf2weapons.TF2weapons;
import rafradek.tf2weapons.client.ClientProxy;
import rafradek.tf2weapons.client.TF2EventsClient;
import rafradek.tf2weapons.common.TF2Attribute;
import rafradek.tf2weapons.common.WeaponsCapability;
import rafradek.tf2weapons.common.WeaponsCapability.RageType;
import rafradek.tf2weapons.entity.EntityLightDynamic;
import rafradek.tf2weapons.entity.IEntityTF2;
import rafradek.tf2weapons.entity.building.EntityBuilding;
import rafradek.tf2weapons.entity.mercenary.EntityTF2Character;
import rafradek.tf2weapons.lightsource.MuzzleFlashLightSource;
import rafradek.tf2weapons.message.TF2Message;
import rafradek.tf2weapons.message.TF2Message.PredictionMessage;
import rafradek.tf2weapons.util.*;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

public abstract class ItemWeapon extends ItemUsable implements IItemNoSwitch {
	/*
	 * public float damage; public float scatter; public int pellets; public float
	 * maxDamage; public int damageFalloff; public float minDamage; public int
	 * reload; public int clipSize; public boolean hasClip; public boolean clipType;
	 * public boolean randomCrits; public float criticalDamage; public int
	 * firstReload; public int knockback;
	 */
	public static boolean shouldSwing = false;
	public static int critical;
	protected static final UUID HEALTH_MODIFIER = UUID.fromString("FA233E1C-4180-4865-B01B-BCCE9785AAB6");
	protected static final UUID SPEED_MODIFIER = UUID.fromString("FA233E1C-4180-4865-B01B-BCCE97871BC2");

	public static final UUID HEADS_HEALTH = UUID.fromString("FA233E1C-4180-4865-B01B-BCCE9785FC3A");
	public static final UUID HEADS_SPEED = UUID.fromString("FA233E1C-4180-4865-B01B-B4A79785FC3A");
	public AttributeModifier headsHealthMod = new AttributeModifier(HEADS_HEALTH, "Heads modifier", 0, 0);
	public AttributeModifier headsSpeedMod = new AttributeModifier(HEADS_SPEED, "Heads modifier", 0, 2);
	public static boolean inHand;

	public ItemWeapon() {
		super();
		this.setCreativeTab(TF2weapons.tabweapontf2);
		this.addPropertyOverride(new ResourceLocation("inhand"), (ItemStack stack, @Nullable Level world,
				@Nullable LivingEntity entityIn) -> (entityIn != null && inHand) ? 1 : 0);
	}

	@Override
	public void onUpdate(ItemStack par1ItemStack, Level par2World, Entity par3Entity, int par4, boolean par5) {
		super.onUpdate(par1ItemStack, par2World, par3Entity, par4, par5);
		if (!par2World.isRemote
				&& TF2Attribute.getModifier("No Disguise Kit", par1ItemStack, 0, (LivingEntity) par3Entity) != 0) {
			if (par3Entity.hasCapability(TF2weapons.WEAPONS_CAP, null)
					&& !WeaponsCapability.get(par3Entity).stabbedDisguise)
				WeaponsCapability.get(par3Entity).setDisguised(false);
		}
		if (par5 && ((LivingEntity) par3Entity).getHeldItemMainhand() == par1ItemStack) {
			WeaponsCapability cap = par3Entity.getCapability(TF2weapons.WEAPONS_CAP, null);
			CompoundTag tag = par1ItemStack.getTagCompound();
			if (!par2World.isRemote && cap.critTimeCool <= 0) {
				cap.critTimeCool = 20;
				if (this.rapidFireCrits(par1ItemStack) && this.hasRandomCrits(par1ItemStack, par3Entity)
						&& ((LivingEntity) par3Entity).getRNG().nextFloat() <= this.critChance(par1ItemStack,
								par3Entity)) {
					cap.setCritTime(40);
					// System.out.println("Apply crits rapid");
				}
			}
			if (TF2Attribute.getModifier("Kill Count", par1ItemStack, 0, null) != 0) {
				par1ItemStack.getTagCompound().setInteger("Heads", cap.getHeads());
			}

			while (TF2ConfigVars.killstreakDrop && tag.getInteger(NBTLiterals.STREAK_KILLS) > 0
					&& cap.ticksTotal >= tag.getLong(NBTLiterals.STREAK_COOL)) {
				tag.setInteger(NBTLiterals.STREAK_KILLS, tag.getInteger(NBTLiterals.STREAK_KILLS) - 1);
				int red = tag.getShort(NBTLiterals.STREAK_REDUCTION) * 2 + 1;
				tag.setShort(NBTLiterals.STREAK_REDUCTION, red > Short.MAX_VALUE ? Short.MAX_VALUE : (short) red);
				tag.setLong(NBTLiterals.STREAK_COOL, tag.getLong(NBTLiterals.STREAK_LAST) + ItemKillstreakKit
						.getCooldown(tag.getByte(NBTLiterals.STREAK_LEVEL), tag.getInteger(NBTLiterals.STREAK_KILLS)));
				/*
				 * tag.setLong(NBTLiterals.STREAK_COOL, tag.getLong(NBTLiterals.STREAK_COOL) +
				 * Math.max(20,(ItemKillstreakKit.getCooldown(tag.getByte(NBTLiterals.
				 * STREAK_LEVEL))-250 -
				 * Mth.log2(tag.getInteger(NBTLiterals.STREAK_KILLS))*250) / red));
				 */
				par1ItemStack.getCapability(TF2weapons.WEAPONS_DATA_CAP, null).cached = false;
			}
			if (cap.getCritTime() > 0)
				cap.setCritTime(cap.getCritTime() - 1);
			cap.critTimeCool -= 1;
			/*
			 * if(par3Entity instanceof EntityTF2Character&&((EntityTF2Character)
			 * par3Entity).getAttackTarget()!=null){
			 * System.out.println(this.getWeaponSpreadBase(par1ItemStack, (LivingEntity)
			 * par3Entity));
			 * if(par1ItemStack.getTagCompound().getInteger("reload")<=100&&!((
			 * EntityTF2Character)par3Entity).attack.lookingAt(this.
			 * getWeaponSpreadBase(par1ItemStack, (LivingEntity) par3Entity)*100+1)){
			 * par1ItemStack.getTagCompound().setInteger("reload", 100); }
			 * //par1ItemStack.getTagCompound().setBoolean("WaitProper", true); }
			 */
		}
	}

	public boolean canPenetrate(ItemStack stack, LivingEntity shooter) {
		return getData(stack).getBoolean(PropertyType.PENETRATE)
				|| TF2Attribute.getModifier("Penetration", stack, 0, shooter) != 0;
	}

	@Override
	public boolean use(ItemStack stack, LivingEntity living, Level world, InteractionHand hand,
			PredictionMessage message) {
		// boolean mainHand=living instanceof
		// Player&&living.getEntityData().getCompoundTag("TF2").getBoolean("mainhand");
		WeaponsCapability cap = living.getCapability(TF2weapons.WEAPONS_CAP, null);
		if (this.holdingMode(stack, living) > 0 && !cap.isCharging()) {
			cap.setCharging(true);
			cap.chargeTicks = 0;
			if (world.isRemote)
				ClientProxy.playWeaponSound(living, ItemFromData.getSound(stack, PropertyType.CHARGE_SOUND), false, 0,
						stack);
			return true;
		}
		cap.autoFire = TF2Attribute.getModifier("Auto Fire", stack, 0, living) != 0;
		if (this.hasClip(stack))
			this.consumeClip(stack, 1, living, hand);

		if (living instanceof Player && hand == InteractionHand.MAIN_HAND)
			((Player) living).resetCooldown();
		else if (world.isRemote && Minecraft.getMinecraft().player == living)
			Minecraft.getMinecraft().getItemRenderer().resetEquippedProgress(InteractionHand.OFF_HAND);

		int thisCritical = TF2Util.calculateCritPre(stack, living);

		critical = thisCritical;

		if (cap.focusShotTicks > 0) {

			if (cap.focusedShot(stack))
				cap.focusShotRemaining = 8;
			cap.focusShotTicks = 0;
		}

		if (!world.isRemote && this.getAmmoType(stack) != 0 && !this.hasClip(stack)) {
			this.consumeAmmoGlobal(living, stack, 1);
		}
		if (!world.isRemote && living instanceof ServerPlayer)
			TF2weapons.network.sendTo(
					new TF2Message.UseMessage(this.getClip(stack), false,
							!this.hasClip(stack) ? this.getAmmoAmount(living, stack) : -1, hand),
					(ServerPlayer) living);

		this.doFireSound(stack, living, world, thisCritical);

		if (world.isRemote)
			this.doMuzzleFlash(stack, living, hand);

		if (!living.onGround && living.getCapability(TF2weapons.WEAPONS_CAP, null).fanCool <= 0
				&& TF2Attribute.getModifier("KnockbackFAN", stack, 0, living) != 0) {
			Vec3 look = living.getLook(1f);
			living.addVelocity(-look.x * 0.66, -look.y * 0.58, -look.z * 0.66);
		}

		if (living instanceof Player && !((Player) living).capabilities.isCreativeMode
				&& living.getCapability(TF2weapons.PLAYER_CAP, null).zombieHuntTicks <= 0
				&& (!(this instanceof ItemMeleeWeapon || this instanceof ItemJar)
						|| getData(stack).getName().equals("fryingpan"))) {
			living.getCapability(TF2weapons.PLAYER_CAP, null).zombieHuntTicks = 15;
			TF2Util.attractMobs(living, world);
		}

		for (int x = 0; x < this.getWeaponPelletCount(stack, living); x++)

			this.shoot(stack, living, world, thisCritical, hand);

		if (this.shootAllAtOnce(stack, living)) {
			while (this.getClip(stack) > 0) {
				this.consumeClip(stack, 1, living, hand);
				this.shoot(stack, living, world, thisCritical, hand);
			}
		}
		return true;
	}

	@Optional.Method(modid = "dynamiclights")
	public void doMuzzleFlashLight(ItemStack stack, LivingEntity living) {
		MuzzleFlashLightSource light = new MuzzleFlashLightSource(living);
		TF2EventsClient.muzzleFlashes.add(light);
		DynamicLights.addLightSource(light);
	}

	public abstract void shoot(ItemStack stack, LivingEntity living, Level world, int thisCritical, InteractionHand hand);

	public int getClip(ItemStack stack) {
		if (!this.hasClip(stack))
			return 0;
		if (!stack.getTagCompound().hasKey("Clip"))
			this.setClip(stack, this.getWeaponClipSize(stack, null));
		return stack.getTagCompound().getInteger("Clip");
	}

	public void setClip(ItemStack stack, int value) {
		if (this.hasClip(stack))
			stack.getTagCompound().setInteger("Clip", Mth.clamp(value, 0, this.getWeaponClipSize(stack, null)));

		// stack.setItemDamage(MathUtil.clamp(stack.getMaxDamage()-value,0,stack.getMaxDamage()));
	}

	@SuppressWarnings("unchecked")
	public void consumeClip(ItemStack stack, int value, LivingEntity living, InteractionHand hand) {
		if (!(value > 0 && !TF2ConfigVars.mustReload && living instanceof Player
				&& ((Player) living).capabilities.isCreativeMode)) {
			this.setClip(stack, this.getClip(stack) - value);
			try {
				((NonNullList<ItemStack>) ReflectionAccess.entityHandInv.get(living)).get(hand.ordinal())
						.getTagCompound().setInteger("Clip", this.getClip(stack));
			} catch (Exception e) {}
		}
	}

	public void doFireSound(ItemStack stack, LivingEntity living, Level world, int critical) {
		if (ItemFromData.getData(stack).hasProperty(PropertyType.FIRE_SOUND)) {
			SoundEvent soundToPlay = SoundEvent.REGISTRY.getObject(new ResourceLocation(
					ItemFromData.getData(stack).getString(PropertyType.FIRE_SOUND) + (critical == 2 ? ".crit" : "")));

			living.playSound(soundToPlay,
					living instanceof Mob
							&& !(((Mob) living).getAttackTarget() instanceof Player)
									? TF2ConfigVars.mercenaryVolume
									: TF2ConfigVars.gunVolume,
					1f);
			if (world.isRemote)
				ClientProxy.removeReloadSound(living);
		}
	}

	@Override
	public boolean canFire(Level world, LivingEntity living, ItemStack stack) {
		return super.canFire(world, living, stack)
				&& !(this.holdingMode(stack, living) > 0
						&& living.getCapability(TF2weapons.WEAPONS_CAP, null).isCharging())
				&& (this.isAmmoSufficient(stack, living, false));
	}

	@Override
	public boolean endUse(ItemStack stack, LivingEntity living, Level world, int action, int newState) {
		WeaponsCapability cap = living.getCapability(TF2weapons.WEAPONS_CAP, null);
		if (this.holdingMode(stack, living) > 0 && (newState & 1) == 0 && cap.isCharging()) {
			// System.out.println("stop charging "+newState);

			WeaponData.getCapability(stack).fire1Cool = this.getFiringSpeed(stack, living);

			if (world.isRemote && ClientProxy.fireSounds.get(living) != null)
				ClientProxy.fireSounds.get(living).setDone();
			// Minecraft.getMinecraft().getSoundHandler().stopSound(ClientProxy.fireSounds.get(living));
			this.use(stack, living, world, InteractionHand.MAIN_HAND, null);
			cap.setCharging(false);
			cap.lastFire = 1250;
			if (world.isRemote)
				sps++;
			cap.reloadCool = 0;
			if ((cap.state & 8) != 0)
				cap.state -= 8;
		}
		return false;
	}

	@Override
	public void addInformation(ItemStack stack, Level world, List<String> tooltip, TooltipFlag advanced) {
		super.addInformation(stack, world, tooltip, advanced);

		if (this.hasClip(stack))
			tooltip.add("Clip: " + (this.getClip(stack)) + "/" + this.getWeaponClipSize(stack, null));
	}

	@Override
	public Multimap<String, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
		Multimap<String, AttributeModifier> multimap = super.getAttributeModifiers(slot, stack);

		if (slot == EquipmentSlot.MAINHAND && getData(stack) != ItemFromData.BLANK_DATA && stack.hasTagCompound()
				&& (stack.getCapability(TF2weapons.WEAPONS_DATA_CAP, null).usedClass == -1
						|| getData(stack).getString(PropertyType.CLASS).isEmpty())
		/*
		 * && ItemToken.allowUse(stack.getCapability(TF2weapons.WEAPONS_DATA_CAP,
		 * null).usedClass, getData(stack).getString(PropertyType.CLASS))
		 */) {
			this.addModifiersWithToken(stack, multimap);
		}
		stack.getCapability(TF2weapons.WEAPONS_DATA_CAP, null).usedClass = -1;
		return multimap;
	}

	public void addModifiersWithToken(ItemStack stack, Multimap<String, AttributeModifier> multimap) {
		int heads = Math.min((int) TF2Attribute.getModifier("Kill Count", stack, 0, null),
				stack.getTagCompound().getInteger("Heads"));
		multimap.put(SharedMonsterAttributes.ATTACK_DAMAGE.getName(),
				new AttributeModifier(ATTACK_DAMAGE_MODIFIER, "Weapon modifier",
						this.getWeaponDamage(stack, null, null) * this.getWeaponPelletCount(stack, null) - 1, 0));
		multimap.put(SharedMonsterAttributes.ATTACK_SPEED.getName(), new AttributeModifier(ATTACK_SPEED_MODIFIER,
				"Weapon modifier", -4 + (1000D / this.getFiringSpeed(stack, null)), 0));
		float addHealth = TF2Attribute.getModifier("Health", stack, 0, null)
				+ heads * TF2Attribute.getModifier("Max Health Kill", stack, 0, null);
		if (addHealth != 0)
			multimap.put(SharedMonsterAttributes.MAX_HEALTH.getName(),
					new AttributeModifier(HEALTH_MODIFIER, "Weapon modifier", addHealth, 0));
		float addSpeed = TF2Attribute.getModifier("Speed", stack,
				1 + heads * TF2Attribute.getModifier("Speed Kill", stack, 0, null), null);
		if (addSpeed != 1)
			multimap.put(SharedMonsterAttributes.MOVEMENT_SPEED.getName(),
					new AttributeModifier(SPEED_MODIFIER, "Weapon modifier", addSpeed - 1, 2));
	}

	public float critChance(ItemStack stack, Entity entity) {
		float chance = 0.025f;
		if (entity instanceof Player)
			chance += TF2PlayerCapability.get((Player) entity).getTotalLastDamage() / 800;
		return Math.min(chance, 0.125f);
	}

	@Override
	public boolean fireTick(ItemStack stack, LivingEntity living, Level world) {
		WeaponsCapability cap = living.getCapability(TF2weapons.WEAPONS_CAP, null);
		if (this.holdingMode(stack, living) > 0 && cap.isCharging()) {
			// System.out.println("charging "+tag.getShort("chargeticks"));
			cap.chargeTicks += 1;
			if (cap.chargeTicks >= this.holdingMode(stack, living) && !this.shouldKeepCharged(stack, living))
				this.endUse(stack, living, world, 1, 0);

		}
		return false;
	}

	@Override
	public boolean isFull3D() {
		return true;
	}

	@Override
	public boolean altFireTick(ItemStack stack, LivingEntity living, Level world) {
		if (!world.isRemote && living instanceof Player && WeaponsCapability.get(living).hasMetal(100)
				&& TF2Attribute.getModifier("Pick Building", stack, 0, living) > 0) {
			Vec3 forward = living.getLook(1f).scale(120).add(living.getPositionEyes(1));
			HitResult result = TF2Util.pierce(world, living, living.posX, living.posY + living.getEyeHeight(),
					living.posZ, forward.x, forward.y, forward.z, false, 0.5f, false).get(0);
			if (result.entityHit != null && result.entityHit instanceof EntityBuilding
					&& !((EntityBuilding) result.entityHit).isDisabled()
					&& ((EntityBuilding) result.entityHit).getOwner() == living
					&& WeaponsCapability.get(living).consumeMetal(100, false) != 0) {
				result.entityHit.setPosition(living.posX, living.posY, living.posZ);
				((EntityBuilding) result.entityHit).grab();
			}
		}
		if (!world.isRemote && this.getRageType(stack, living) == RageType.MINICRIT
				&& this.getRage(stack, living) >= this.getMaxRage(stack, living)) {
			WeaponsCapability.get(living).setRageActive(RageType.MINICRIT, true,
					this.getMaxRage(stack, living) / TF2Attribute.getModifier("Minicrit Rage", stack, 0f, living));
			living.addPotionEffect(new MobEffectInstance(TF2weapons.buffbanner,
					(int) (20 * TF2Attribute.getModifier("Minicrit Rage", stack, 0f, living))));
		}
		return false;
	}

	public boolean shootAllAtOnce(ItemStack stack, LivingEntity living) {
		return TF2Attribute.getModifier("Shoot Once", stack, 0f, living) != 0f;
	}

	@Override
	public int getMaxDamage(ItemStack stack) {
		return stack.hasTagCompound() ? this.getWeaponClipSize(stack, null) : 0;
	}

	public float getDamageForArmor(ItemStack stack, LivingEntity living, Entity target) {
		float damage = ItemFromData.getData(stack).getFloat(PropertyType.DAMAGE)
				* this.getWeaponPelletCount(stack, living);
		if (ItemFromData.getData(stack).hasProperty(PropertyType.ARMOR_PEN_SCALE))
			damage *= ItemFromData.getData(stack).getFloat(PropertyType.ARMOR_PEN_SCALE);
		return damage;
	}

	public float getWeaponDamage(ItemStack stack, LivingEntity living, Entity target) {
		float damage = ItemFromData.getData(stack).getFloat(PropertyType.DAMAGE);
		if (living == null || living != target) {
			damage = TF2Attribute.getModifier("Damage", stack, damage, living);
			if (living != null && (isDoubleWielding(living) || living.isHandActive()))
				damage *= 0.85f;
			if (target != null) {
				if (!target.isBurning() && !(target instanceof IEntityTF2 && ((IEntityTF2) target).isBuilding()))
					damage = TF2Attribute.getModifier("Damage Non Burn", stack, damage, living);
				else if (target.isBurning())
					damage = TF2Attribute.getModifier("Damage Burning", stack, damage, living);
				if (target instanceof IEntityTF2 && ((IEntityTF2) target).isBuilding())
					damage = TF2Attribute.getModifier("Damage Building", stack, damage, living);
				else if (target instanceof LivingEntity)
					damage = TF2Attribute.getModifier("Damage Player", stack, damage, living);
			}
			if (living != null) {
				float damageHealth = TF2Attribute.getModifier("Damage Health", stack, 1f, living);
				if (damageHealth != 1) {
					damage = damage
							* (damageHealth - ((living.getHealth() / living.getMaxHealth()) * damageHealth - 0.5f));// *
																													// TF2Util.lerp(damageHealth,
																													// 1f
																													// -
																													// (damageHealth
																													// -
																													// 1f),
																													// TF2Util.position(living.getMaxHealth(),
																													// ,
																													// lerp));
				}
			}
			if (living != null && living.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE) != null) {
				// System.out.println("Pre "+damage);
				damage = (float) calculateModifiers(living.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE),
						ATTACK_DAMAGE_MODIFIER, damage, 1D / 9D);
				// System.out.println("Post "+damage);
			}

		}
		return damage;
	}

	public float getWeaponMaxDamage(ItemStack stack, LivingEntity living) {
		return ItemFromData.getData(stack).getFloat(PropertyType.MAX_DAMAGE);
	}

	public float getWeaponMinDamage(ItemStack stack, LivingEntity living) {
		return TF2Util.lerp(ItemFromData.getData(stack).getFloat(PropertyType.MIN_DAMAGE), 1,
				(TF2Attribute.getModifier("Accuracy", stack, 1f, living) - 1f) * 0.4f);
	}

	public float getWeaponSpread(ItemStack stack, LivingEntity living) {
		float base = this.getWeaponSpreadBase(stack, living);
		if (living instanceof EntityTF2Character && ((EntityTF2Character) living).getAttackTarget() != null) {
			float totalRotation = 0;
			for (int i = 0; i < 20; i++)
				totalRotation += ((EntityTF2Character) living).lastRotation[i];

			base += ((EntityTF2Character) living).getMotionSensitivity() * totalRotation * 0.001f;
		}
		return Math.abs(base);
	}

	public float getWeaponSpreadBase(ItemStack stack, LivingEntity living) {
		if (living != null && ItemFromData.getData(stack).getBoolean(PropertyType.SPREAD_RECOVERY)
				&& living.getCapability(TF2weapons.WEAPONS_CAP, null).lastFire <= 0)
			return 0;
		float value = TF2Attribute.getModifier("Spread", stack,
				ItemFromData.getData(stack).getFloat(PropertyType.SPREAD), living)
				/ TF2Attribute.getModifier("Accuracy", stack, 1, living);
		if (living != null && (isDoubleWielding(living) || living.isHandActive()))
			value *= 1.5f;
		if (TF2Attribute.getModifier("Spread Health", stack, 1f, living) != 1f)
			value *= this.getHealthBasedBonus(stack, living,
					TF2Attribute.getModifier("Spread Health", stack, 1f, living));
		return value;
	}

	public int getWeaponPelletCount(ItemStack stack, LivingEntity living) {
		return (int) (TF2Attribute.getModifier("Pellet Count", stack,
				ItemFromData.getData(stack).getInt(PropertyType.PELLETS), living));
	}

	public float getWeaponDamageFalloff(ItemStack stack) {
		return ItemFromData.getData(stack).getFloat(PropertyType.DAMAGE_FALOFF);
	}

	public float getWeaponDamageFalloffMaxRange(ItemStack stack, LivingEntity living) {
		return TF2Attribute.getModifier("Accuracy", stack, this.getWeaponDamageFalloff(stack) * 2, living);
	}

	public float getWeaponDamageFalloffSq(ItemStack stack) {
		float falloff = this.getWeaponDamageFalloff(stack);
		return falloff * falloff;
	}

	public int getWeaponReloadTime(ItemStack stack, LivingEntity living) {
		float speed = 1;
		if (living != null && living.getEntityAttribute(SharedMonsterAttributes.ATTACK_SPEED) != null) {
			double orig = living.getEntityAttribute(SharedMonsterAttributes.ATTACK_SPEED).getBaseValue();
			double modifiers = (calculateModifiers(living.getEntityAttribute(SharedMonsterAttributes.ATTACK_SPEED),
					ATTACK_SPEED_MODIFIER, orig, 1.4) - orig) * 2 + orig;
			speed = (float) ((living instanceof Player ? 4 : orig) / modifiers);
		}
		return (int) (TF2Attribute.getModifier("Reload Time", stack,
				ItemFromData.getData(stack).getInt(PropertyType.RELOAD_TIME) * speed, living));
	}

	public int getWeaponFirstReloadTime(ItemStack stack, LivingEntity living) {
		float speed = 1;
		if (living != null && living.getEntityAttribute(SharedMonsterAttributes.ATTACK_SPEED) != null) {
			double orig = living.getEntityAttribute(SharedMonsterAttributes.ATTACK_SPEED).getBaseValue();
			double modifiers = (calculateModifiers(living.getEntityAttribute(SharedMonsterAttributes.ATTACK_SPEED),
					ATTACK_SPEED_MODIFIER, orig, 1.4) - orig) * 2 + orig;
			speed = (float) ((living instanceof Player ? 4 : orig) / modifiers);
		}
		return (int) (TF2Attribute.getModifier("Reload Time", stack,
				ItemFromData.getData(stack).getInt(PropertyType.RELOAD_TIME_FIRST) * speed, living));
	}

	public boolean hasClip(ItemStack stack) {
		return ItemFromData.getData(stack).getBoolean(PropertyType.RELOADS_CLIP);
	}

	public int getWeaponClipSize(ItemStack stack, LivingEntity living) {
		int headsbonus = (int) (TF2Attribute.getModifier("Clip Kill", stack, 0, living) * Math.min(
				TF2Attribute.getModifier("Kill Count", stack, 0, living), stack.getTagCompound().getInteger("Heads")));
		return (int) (TF2Attribute.getModifier("Clip Size", stack,
				ItemFromData.getData(stack).getInt(PropertyType.CLIP_SIZE), living)) + headsbonus;
	}

	public boolean IsReloadingFullClip(ItemStack stack) {
		return ItemFromData.getData(stack).getBoolean(PropertyType.RELOADS_FULL_CLIP);
	}

	public boolean hasRandomCrits(ItemStack stack, Entity par3Entity) {
		return (TF2ConfigVars.randomCrits == 0
				|| (TF2ConfigVars.randomCrits == 1 && par3Entity instanceof Player)) && !par3Entity.world.isRemote
				&& ItemFromData.getData(stack).getBoolean(PropertyType.RANDOM_CRITS)
				&& TF2Attribute.getModifier("Random Crit", stack, 0, null) == 0;
	}

	public double getWeaponKnockback(ItemStack stack, LivingEntity living) {
		return TF2Attribute.getModifier("Knockback", stack, ItemFromData.getData(stack).getInt(PropertyType.KNOCKBACK),
				living)
				* (living.hasCapability(TF2weapons.WEAPONS_CAP, null)
						&& WeaponsCapability.get(living).isRageActive(RageType.KNOCKBACK)
						&& TF2Attribute.getModifier("Knockback Rage", stack, 0, living) != 0 ? 4.5 : 1);
	}

	public double getKnockbackForDamage(ItemStack stack, LivingEntity living, float damage, DamageSource src) {
		return this.getWeaponKnockback(stack, living) * damage * 0.01625D;
	}

	public double getZKnockback(ItemStack stack, LivingEntity living, float damage, DamageSource src) {
		return ItemFromData.getData(stack).getFloat(PropertyType.KNOCKBACK_Z);
	}

	public boolean rapidFireCrits(ItemStack stack) {
		return ItemFromData.getData(stack).getBoolean(PropertyType.RAPIDFIRE_CRITS);
	}

	@Override
	public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
		if (TF2PlayerCapability.get(player).breakBlocks && !(this instanceof ItemMeleeWeapon)) {
			player.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE)
					.removeModifier(Item.ATTACK_DAMAGE_MODIFIER);
			return false;
		} else
			return true;
	}

	@Override
	public boolean onEntitySwing(LivingEntity entityLiving, ItemStack stack) {
		return !shouldSwing;
	}

	@Override
	public void draw(WeaponsCapability cap, ItemStack stack, LivingEntity living, Level world) {
		cap.setCritTime(0);
		cap.stopReload();
		if (living instanceof ServerPlayer)
			TF2weapons.network.sendTo(new TF2Message.UseMessage(this.getClip(stack), false,
					this.getAmmoAmount(living, stack), InteractionHand.MAIN_HAND), (ServerPlayer) living);
		if (TF2Attribute.getModifier("Auto Fire", stack, 0, living) != 0) {
			this.setClip(stack, 0);
		}
		super.draw(cap, stack, living, world);
	}

	@Override
	public void holster(WeaponsCapability cap, ItemStack stack, LivingEntity living, Level world) {

		cap.stopReload();
		cap.focusShotRemaining = 0;
		cap.focusShotTicks = 0;
		cap.setCharging(false);
		float removeHealth = 0;
		for (Entry<String, AttributeModifier> entry : stack.getAttributeModifiers(EquipmentSlot.MAINHAND)
				.entries()) {
			if (entry.getValue().getID() == HEALTH_MODIFIER)
				removeHealth += entry.getValue().getAmount();
		}
		if (removeHealth != 0)
			living.setHealth((living.getMaxHealth() / (removeHealth + living.getMaxHealth()) * living.getHealth()));

		super.holster(cap, stack, living, world);
	}

	@Override
	public boolean stopSlotSwitch(ItemStack stack, LivingEntity living) {
		return TF2Attribute.getModifier("Auto Fire", stack, 0, living) != 0 && this.getClip(stack) > 0;
	}

	public boolean onHit(ItemStack stack, LivingEntity attacker, Entity target, float damage, int critical,
			boolean simulate) {
		if (target instanceof EntityBuilding && TF2Util.isOnSameTeam(attacker, target)
				&& !((EntityBuilding) target).isSapped()) {
			float repair = TF2Attribute.getModifier("Repair Building", stack, 0, attacker);
			if (repair > 0) {
				if (!simulate)
					((EntityBuilding) target).heal(repair);
				return false;
			}
		} else if (target instanceof LivingEntity && TF2Util.isOnSameTeam(attacker, target)
				&& !(target instanceof EntityBuilding)) {
			float heal = damage * TF2Attribute.getModifier("Heal Target", stack, 0, attacker);
			if (heal > 0) {
				if (!simulate)
					((LivingEntity) target).heal(heal);
				return false;
			}
		}
		if (target instanceof LivingEntity && !TF2Util.isEnemy(attacker, (LivingEntity) target)) {
			float speedtime = TF2Attribute.getModifier("Speed Hit", stack, 0, attacker);
			if (!simulate && speedtime > 0) {
				((LivingEntity) target)
						.addPotionEffect(new MobEffectInstance(MobEffects.SPEED, (int) (speedtime * 20), 1));
				attacker.addPotionEffect(new MobEffectInstance(MobEffects.SPEED, (int) (speedtime * 36), 1));
			}
		}
		if (TF2Attribute.getModifier("Silent Kill", stack, 0, attacker) != 0) {
			target.setSilent(true);
			if (target instanceof Player) {
				target.world.getCapability(TF2weapons.WORLD_CAP, null).silent = target.world.getGameRules()
						.getBoolean("showDeathMessages");
				target.world.getGameRules().setOrCreateGameRule("showDeathMessages", "false");
			}
		} else if (target instanceof LivingEntity && TF2Util.isOnSameTeam(attacker, target)
				&& !(target instanceof EntityBuilding)) {
			float heal = damage * TF2Attribute.getModifier("Heal Target", stack, 0, attacker);
			if (heal > 0) {
				if (!simulate)
					((LivingEntity) target).heal(heal);
				return false;
			}
		}
		return true;
	}

	public void onDealDamage(ItemStack stack, LivingEntity attacker, Entity target, DamageSource source,
			float amount) {
		if (TF2Attribute.getModifier("Burn Hit", stack, 0, attacker) > 0 && target != attacker)
			TF2Util.igniteAndAchievement(target, attacker,
					(int) TF2Attribute.getModifier("Burn Hit", stack, 0, attacker),
					(int) TF2Attribute.getModifier("Burn Time", stack, 1f, attacker));

		if (target instanceof LivingEntity && attacker.hasCapability(TF2weapons.WEAPONS_CAP, null)) {
			boolean enemy = TF2Util.isEnemy(attacker, (LivingEntity) target);
			LivingEntity targetliving = (LivingEntity) target;
			int stun = (int) TF2Attribute.getModifier("Stun On Hit", stack, 0, attacker);
			if (stun > 0 && !(attacker instanceof Player
					&& ((Player) attacker).getCooldownTracker().hasCooldown(this))) {
				TF2Util.stun(targetliving, 16, true);
				if (attacker instanceof Player) {
					((Player) attacker).getCooldownTracker().setCooldown(this, stun);
				}
			}
			int metalhit = (int) TF2Attribute.getModifier("Metal Hit", stack, 0, attacker);
			if (metalhit != 0) {
				int restore = (int) (((TF2DamageSource) source).getAttackPower() * metalhit
						/ TF2ConfigVars.damageMultiplier);
				if (attacker.getDistanceSq(target) > this.getWeaponDamageFalloffSq(stack))
					restore /= 2;
				int metaluse = this.getActualAmmoUse(stack, attacker,
						(int) TF2Attribute.getModifier("Metal Ammo", stack, 0, attacker));
				if (!enemy && restore > metaluse)
					restore = metaluse;
				attacker.getCapability(TF2weapons.WEAPONS_CAP, null)
						.setMetal(attacker.getCapability(TF2weapons.WEAPONS_CAP, null).getMetal() + restore);
			}
			if (!target.isEntityAlive() && !(target instanceof EntityBuilding)
					&& TF2Attribute.getModifier("Kill Count", stack, 0, attacker) != 0) {
				stack.getTagCompound().setInteger("Heads", stack.getTagCompound().getInteger("Heads") + 1);
			}
			float healthHit = TF2Attribute.getModifier("Health Hit", stack, 0, attacker);
			if (healthHit > 0)
				attacker.heal(TF2Util.getReducedHealing(attacker, targetliving, enemy ? healthHit : healthHit / 2f));
			float bleed = TF2Attribute.getModifier("Bleed", stack, 0, attacker);
			if (bleed > 0) {
				targetliving.addPotionEffect(new MobEffectInstance(TF2weapons.bleeding, (int) (bleed * 20f) + 10, 0));
			}

			int rage = (int) TF2Attribute.getModifier("Knockback Rage", stack, 0, attacker);
			if (enemy && rage > 0 && !WeaponsCapability.get(attacker).isRageActive(RageType.KNOCKBACK)) {
				this.addRage(stack, attacker, amount * (0.012f + rage * 0.008f));
			}

			int ragedamage = (int) TF2Attribute.getModifier("Build Rage Damage", stack, 0, attacker);
			if (enemy && ragedamage > 0
					&& !WeaponsCapability.get(attacker).isRageActive(this.getRageType(stack, attacker))) {
				this.addRage(stack, attacker, amount);
			}

			if (enemy && TF2Attribute.getModifier("Regen On Hit", stack, 0f, attacker) > 0f) {
				attacker.addPotionEffect(new MobEffectInstance(TF2weapons.regen, 10,
						(int) TF2Attribute.getModifier("Regen On Hit", stack, 0f, attacker) - 1));
			}

			if (TF2Attribute.getModifier("Uber Hit Remove", stack, 0f, attacker) > 0f) {
				ItemStack medigun = ItemStack.EMPTY;
				if (target instanceof Player) {
					medigun = TF2Util.getFirstItem(((Player) target).inventory,
							stackm -> stackm.getItem() instanceof ItemMedigun
									&& stackm.getTagCompound().getFloat("ubercharge") > 0);
				} else if (target instanceof EntityTF2Character) {
					medigun = TF2Util.getFirstItem(((EntityTF2Character) target).loadout,
							stackm -> stackm.getItem() instanceof ItemMedigun
									&& stackm.getTagCompound().getFloat("ubercharge") > 0);
				}
				if (!medigun.isEmpty())
					medigun.getTagCompound().setFloat("ubercharge",
							Math.max(0, medigun.getTagCompound().getFloat("ubercharge")
									- TF2Attribute.getModifier("Uber Hit Remove", stack, 0f, attacker) * 0.01f));
			}

			if (TF2Attribute.getModifier("Cloak Hit Remove", stack, 0f, attacker) > 0f) {
				ItemStack cloak = ItemStack.EMPTY;
				if (target instanceof Player) {
					cloak = TF2Util.getFirstItem(((Player) target).inventory,
							stackm -> stackm.getItem() instanceof ItemCloak
									&& stackm.getItemDamage() < stackm.getMaxDamage());
				} else if (target instanceof EntityTF2Character) {
					cloak = TF2Util.getFirstItem(((EntityTF2Character) target).loadout,
							stackm -> stackm.getItem() instanceof ItemMedigun
									&& stackm.getItemDamage() < stackm.getMaxDamage());
				}
				if (!cloak.isEmpty())
					cloak.setItemDamage(
							(int) Math.min(cloak.getMaxDamage(), cloak.getItemDamage() + cloak.getMaxDamage()
									* TF2Attribute.getModifier("Cloak Hit Remove", stack, 0f, attacker) * 0.01f));
			}
			if (TF2Attribute.getModifier("Rocket Specialist", stack, 0f, attacker) > 0f) {
				targetliving.addPotionEffect(new MobEffectInstance(MobEffects.SLOWNESS,
						(int) TF2Attribute.getModifier("Rocket Specialist", stack, 0f, attacker) * 8, 4));
				TF2Util.setVelocity(target, 0, 0, 0);
			}

			if (TF2Attribute.getModifier("Fire Specialist", stack, 0f, attacker) > 0f) {
				try {
					((LivingEntity) target).addPotionEffect(
							new MobEffectInstance(TF2weapons.viralfire, ReflectionAccess.entityFire.getInt(target),
									(int) TF2Attribute.getModifier("Fire Specialist", stack, 0f, attacker)));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			if (enemy && TF2Attribute.getModifier("Uber Hit", stack, 0, attacker) > 0)
				if (attacker instanceof Player)
					for (ItemStack medigun : ((Player) attacker).inventory.mainInventory)
						if (medigun != null && medigun.getItem() instanceof ItemMedigun) {
							medigun.getTagCompound().setFloat("ubercharge",
									Mth.clamp(
											medigun.getTagCompound().getFloat("ubercharge")
													+ TF2Attribute.getModifier("Uber Hit", stack, 0, attacker) / 100,
											0, 1));
							if (stack.getTagCompound().getFloat("ubercharge") >= 1)
								attacker.playSound(ItemFromData.getSound(stack, PropertyType.CHARGED_SOUND), 1.2f, 1);
							break;
						}
			if (TF2Attribute.getModifier("Fire Rate Hit", stack, 1, attacker) != 1
					&& !WeaponsCapability.get(attacker).fireCoolReduced) {
				stack.getCapability(TF2weapons.WEAPONS_DATA_CAP, null).fire1Cool -= this.getFiringSpeed(stack, attacker)
						* (1 - (1 / TF2Attribute.getModifier("Fire Rate Hit", stack, 1, attacker)));
				WeaponsCapability.get(attacker).fireCoolReduced = true;
				if (attacker instanceof ServerPlayer)
					TF2weapons.network.sendTo(new TF2Message.ActionMessage(27, attacker), (ServerPlayer) attacker);
			}
		}
	}

	public boolean doMuzzleFlash(ItemStack stack, LivingEntity attacker, InteractionHand hand) {

		ClientProxy.spawnFlashParticle(attacker.world, attacker, hand);
		attacker.world.spawnEntity(new EntityLightDynamic(attacker.world, attacker, 3));
		if (TF2ConfigVars.dynamicLights) {
			this.doMuzzleFlashLight(stack, attacker);

		}
		return true;
	}

	@Override
	public boolean showInfoBox(ItemStack stack, Player player) {
		return getAmmoType(stack) != 0;
	}

	@Override
	public String[] getInfoBoxLines(ItemStack stack, Player player) {
		String[] result = new String[2];
		boolean metalammo = TF2Attribute.getModifier("Metal Ammo", stack, 0, player) != 0;

		int holdTickMax = holdingMode(stack, player);
		WeaponsCapability cap = player.getCapability(TF2weapons.WEAPONS_CAP, null);
		if (holdTickMax > 0 && cap.isCharging()) {
			int chargeTicks = cap.chargeTicks;
			int progress = (int) ((float) chargeTicks / (float) holdTickMax * 20);
			result[0] = "";
			if (progress > 0) {
				for (int i = 0; i < 20; i++) {
					if (i < progress)
						result[0] = result[0] + "|";
					else
						result[0] = result[0] + ".";
				}
			}
		} else {
			if (metalammo)
				result[0] = "METAL";
			else
				result[0] = "AMMO";

			int focus = (int) TF2Attribute.getModifier("Focus", stack, 0, player);
			int progress = 0;
			if (focus != 0) {
				progress = (int) (((float) cap.focusShotTicks / (float) cap.focusShotTime(stack)) * 8f);
			} else if (TF2Attribute.getModifier("Headshot", stack, 0, player) > 0) {
				progress = (1250 - player.getCapability(TF2weapons.WEAPONS_CAP, null).lastFire) / 156;
			}
			if (progress > 0) {
				result[0] = result[0] + " ";
				for (int i = 0; i < 8; i++) {
					if (i < progress)
						result[0] = result[0] + "|";
					else
						result[0] = result[0] + ".";
					// result[0]=result[0]+"\u2588";
				}
			}
		}
		int ammoLeft = 0;
		if (getAmmoType(stack) < player.getCapability(TF2weapons.PLAYER_CAP, null).cachedAmmoCount.length)
			ammoLeft = player.getCapability(TF2weapons.PLAYER_CAP, null).cachedAmmoCount[getAmmoType(stack)];
		else
			ammoLeft = this.getAmmoAmount(player, stack);
		if (metalammo)
			ammoLeft = player.getCapability(TF2weapons.WEAPONS_CAP, null).getMetal();

		if (hasClip(stack)) {
			int inClip = this.getClip(stack);

			if (isDoubleWielding(player)) {
				inClip += ((ItemWeapon) player.getHeldItemOffhand().getItem()).getClip(player.getHeldItemOffhand());
			}
			result[1] = inClip + "/" + ammoLeft;

		} else {
			result[1] = Integer.toString(ammoLeft);
		}
		return result;
	}

	public int holdingMode(ItemStack stack, LivingEntity shooter) {
		return (int) (TF2Attribute.getModifier("Charged Grenades", stack, 0, shooter)
				* TF2Attribute.getModifier("Fire Rate", stack, 1f, shooter));
	}

	public boolean shouldKeepCharged(ItemStack stack, LivingEntity shooter) {
		return false;
	}

	public float getProjectileSpeed(ItemStack stack, LivingEntity living) {
		return TF2Attribute.getModifier("Proj Speed", stack,
				ItemFromData.getData(stack).getFloat(PropertyType.PROJECTILE_SPEED), living);
	}

	@Override
	public boolean isAmmoSufficient(ItemStack stack, LivingEntity living, boolean all) {

		// System.out.println((living.world.isRemote && living !=
		// ClientProxy.getLocalPlayer())+" "+ ((!this.hasClip(stack) || all) &&
		// !ItemAmmo.searchForAmmo(living, stack).isEmpty()) +" "+ (this.hasClip(stack)
		// && stack.getItemDamage() < stack.getMaxDamage()));
		return (living.world.isRemote && living != ClientProxy.getLocalPlayer()) || (((!this.hasClip(stack) || all)
				&& !this.searchForAmmo(living, stack).isEmpty())
				|| (this.hasClip(stack) && (this.getClip(stack) > 0 || (!TF2ConfigVars.mustReload
						&& (living instanceof Player && ((Player) living).capabilities.isCreativeMode)))));
	}

	public boolean isItemStackDamageable() {
		return false;
	}

	@Override
	public boolean hasEffect(ItemStack stack) {
		return super.hasEffect(stack) || (stack.hasTagCompound() && this.hasKillstreak(stack, 2)
				&& stack.getTagCompound().getInteger(NBTLiterals.STREAK_KILLS) > 0);
	}

	@Override
	public void setDamage(ItemStack stack, int damage) {
		super.setDamage(stack, damage);
		if (damage > this.getMaxDamage(stack))
			stack.setItemDamage(this.getMaxDamage(stack));
	}

	public boolean canHeadshot(LivingEntity living, ItemStack stack) {
		return TF2Attribute.getModifier("Headshot", stack, 0, living) > 0
				&& living.getCapability(TF2weapons.WEAPONS_CAP, null).lastFire <= 0;
	}

	public int getHeadshotCrit(LivingEntity living, ItemStack stack) {
		return 2;
	}

	public float getAdditionalGravity(LivingEntity living, ItemStack stack, double initial) {
		return TF2Attribute.getModifier("Gravity", stack, (float) initial, living);
	}

	@Override
	public RageType getRageType(ItemStack stack, LivingEntity living) {
		return TF2Attribute.getModifier("Knockback Rage", stack, 0f, living) != 0f ? RageType.KNOCKBACK
				: TF2Attribute.getModifier("Minicrit Rage", stack, 0f, living) != 0f ? RageType.MINICRIT : null;
	}

	@Override
	public float getMaxRage(ItemStack stack, LivingEntity living) {
		RageType type = this.getRageType(stack, living);
		if (type == null)
			return 0f;
		switch (type) {
		case KNOCKBACK:
			return 1f;
		default:
			return TF2Attribute.getModifier("Build Rage Damage", stack, 0f, living);
		}
	}

	public float getCharge(LivingEntity living, ItemStack stack) {
		if (living == null)
			return 0f;
		if (WeaponsCapability.get(living).lastHitCharge != 0)
			return WeaponsCapability.get(living).lastHitCharge;
		if (this.holdingMode(stack, living) == 0)
			return 0f;

		return Mth.clamp(
				(float) WeaponsCapability.get(living).chargeTicks / (float) this.holdingMode(stack, living), 0f, 1f);
	}

	public void playHitSound(ItemStack stack, LivingEntity living, Entity target) {
		SoundEvent sound;

		if (getData(stack).hasProperty(PropertyType.SPECIAL_1_SOUND) && WeaponsCapability.get(living).hitNoMiss > 0
				&& WeaponsCapability.get(living).hitNoMiss + 1 >= TF2Attribute.getModifier("Hit Crit", stack, 0,
						living))
			sound = ItemFromData.getSound(stack, PropertyType.SPECIAL_1_SOUND);
		else
			sound = ItemFromData.getSound(stack, PropertyType.HIT_SOUND);
		TF2Util.playSound(target, sound, ItemFromData.getData(stack).getName().equals("fryingpan") ? 2F : 0.7F, 1F);
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public boolean showDurabilityBar(ItemStack stack) {
		return (this.hasClip(stack)
				&& this.getClip(stack) != this.getWeaponClipSize(stack, Minecraft.getMinecraft().player))
				|| super.showDurabilityBar(stack);
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public double getDurabilityForDisplay(ItemStack stack) {
		if (super.showDurabilityBar(stack))
			return super.getDurabilityForDisplay(stack);
		else if (stack == Minecraft.getMinecraft().player.getHeldItemMainhand() && this.IsReloadingFullClip(stack)) {
			WeaponsCapability cap = WeaponsCapability.get(Minecraft.getMinecraft().player);
			if (cap.reloadingHand != null && cap.reloadCool > 0) {
				double ready = (double) this.getClip(stack)
						/ this.getWeaponClipSize(stack, Minecraft.getMinecraft().player);
				return (cap.reloadCool / (double) this.getWeaponReloadTime(stack, Minecraft.getMinecraft().player))
						* (1d - ready);
			}
		}
		return (1D - (double) this.getClip(stack) / this.getWeaponClipSize(stack, Minecraft.getMinecraft().player));
	}

	@Override
	public void drawOverlay(ItemStack stack, Player player, Tesselator tessellator, BufferBuilder buffer,
			Window resolution) {
		if (this.hasKillstreak(stack, 1)) {
			Minecraft.getMinecraft().getTextureManager().bindTexture(ClientProxy.healingTexture);

			GL11.glDisable(GL11.GL_DEPTH_TEST);
			GL11.glDepthMask(false);
			OpenGlHelper.glBlendFunc(770, 771, 1, 0);
			GL11.glDisable(GL11.GL_ALPHA_TEST);
			Gui gui = Minecraft.getMinecraft().ingameGUI;
			ClientProxy.setColor(TF2Util.getTeamColor(player), 0.7f, 0, 0.25f, 0.8f);

			buffer.begin(7, DefaultVertexFormat.POSITION_TEX);
			buffer.pos(30, resolution.getScaledHeight() - 20, 0.0D).tex(0.0D, 1D).endVertex();
			buffer.pos(71, resolution.getScaledHeight() - 20, 0.0D).tex(0.01D, 1D).endVertex();
			buffer.pos(71, resolution.getScaledHeight() - 46, 0.0D).tex(0.01D, 0.99D).endVertex();
			buffer.pos(30, resolution.getScaledHeight() - 46, 0.0D).tex(0.0D, 0.99D).endVertex();
			tessellator.draw();

			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
			long timestart = stack.getTagCompound().getInteger(NBTLiterals.STREAK_LAST);
			long timeend = stack.getTagCompound().getInteger(NBTLiterals.STREAK_COOL);
			float progress = Mth.clamp(
					1f - (WeaponsCapability.get(player).ticksTotal - timestart) / (float) (timeend - timestart), 0f,
					1f);
			buffer.begin(7, DefaultVertexFormat.POSITION_TEX);
			buffer.pos(30, resolution.getScaledHeight() - 20, 0.0D).tex(0.0D, 1D).endVertex();
			buffer.pos(30 + progress * 41f, resolution.getScaledHeight() - 20, 0.0D).tex(0.01D, 1D).endVertex();
			buffer.pos(30 + progress * 41f, resolution.getScaledHeight() - 21, 0.0D).tex(0.01D, 0.99D).endVertex();
			buffer.pos(30, resolution.getScaledHeight() - 21, 0.0D).tex(0.0D, 0.99D).endVertex();
			tessellator.draw();

			Gui.drawModalRectWithCustomSizedTexture(28, resolution.getScaledHeight() - 18, 83, 68, 45, 40, 128, 128);

			gui.drawCenteredString(gui.getFontRenderer(),
					Integer.toString(stack.getTagCompound().getInteger(NBTLiterals.STREAK_KILLS)), 50,
					resolution.getScaledHeight() - 44, 16777215);
			gui.drawCenteredString(gui.getFontRenderer(), "STREAK", 50, resolution.getScaledHeight() - 31, 16777215);

			GL11.glDepthMask(true);
			GL11.glEnable(GL11.GL_DEPTH_TEST);
			GL11.glEnable(GL11.GL_ALPHA_TEST);
			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		}
	}

	public void onHitFinal(ItemStack stack, LivingEntity attacker, Entity target, DamageSource source) {
		if (TF2Attribute.getModifier("Silent Kill", stack, 0, attacker) != 0) {
			target.setSilent(false);
			if (target instanceof Player) {
				target.world.getGameRules().setOrCreateGameRule("showDeathMessages",
						Boolean.toString(target.world.getCapability(TF2weapons.WORLD_CAP, null).silent));
			}
		}
	}

	public static ItemWeapon getWeapon(ItemStack stack) {
		return stack.getItem() instanceof ItemWeapon ? (ItemWeapon) stack.getItem() : null;
	}

	public void onOverload(ItemStack stack, LivingEntity owner, InteractionHand hand) {
		this.shoot(stack, owner, owner.world, 0, hand);
	}
}
