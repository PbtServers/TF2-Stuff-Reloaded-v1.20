package rafradek.tf2weapons.item;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.NonNullList;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.opengl.GL11;
import rafradek.tf2weapons.TF2ConfigVars;
import rafradek.tf2weapons.TF2EventsCommon;
import rafradek.tf2weapons.TF2weapons;
import rafradek.tf2weapons.client.ClientProxy;
import rafradek.tf2weapons.client.ClientRender;
import rafradek.tf2weapons.common.TF2Attribute;
import rafradek.tf2weapons.common.WeaponsCapability;
import rafradek.tf2weapons.entity.IEntityTF2;
import rafradek.tf2weapons.entity.building.EntityBuilding;
import rafradek.tf2weapons.entity.mercenary.EntityTF2Character;
import rafradek.tf2weapons.message.TF2Message.PredictionMessage;
import rafradek.tf2weapons.util.Contract.Objective;
import rafradek.tf2weapons.util.PropertyType;
import rafradek.tf2weapons.util.ReflectionAccess;
import rafradek.tf2weapons.util.TF2Util;

import java.util.ArrayList;
import java.util.List;

public class ItemMedigun extends ItemUsable {

	@Override
	public boolean use(ItemStack stack, LivingEntity living, Level world, InteractionHand hand,
			PredictionMessage message) {
		// if(!world.isRemote||living !=
		// Minecraft.getMinecraft().player!(TF2weapons.medigunLock&&living.getCapability(TF2weapons.WEAPONS_CAP,
		// null).healTarget>0)) return false;
		// System.out.println("View: "+var4+" "+startX+" "+startY+" "+startZ+"
		// "+startX+endX+" "+endY+" "+endZ);
		if (world.isRemote && living == Minecraft.getMinecraft().player) {
			HitResult trace = this.trace(stack, living, world);
			if (world.getEntityByID(living.getCapability(TF2weapons.WEAPONS_CAP, null).getHealTarget()) == null
					&& trace != null && trace.entityHit != null && trace.entityHit instanceof LivingEntity
					&& !(trace.entityHit instanceof EntityBuilding)) {
				List<HitResult> list = new ArrayList<>();
				trace.hitInfo = new float[] { 0, 0 };
				list.add(trace);
				message.target = list;
			}
		} else if (!world.isRemote && message != null && message.readData != null) {
			living.getCapability(TF2weapons.WEAPONS_CAP, null).setHealTarget((int) message.readData.get(0)[0]);
		}
		return true;
	}

	public void heal(ItemStack stack, LivingEntity living, Level world, LivingEntity target) {

		if (!(living instanceof Player && ((Player) living).capabilities.isCreativeMode)) {
			// ItemStack stackAmmo = this.searchForAmmo(living, stack);
			if (!this.isAmmoSufficient(stack, living, true))
				return;
			int use = 16;
			if (target.getHealth() >= target.getMaxHealth())
				use /= 2;
			if (target.getAbsorptionAmount() >= target.getMaxHealth() * this.getMaxOverheal(stack, living, target)
					* 0.9f) {
				use = use * 2 / 5;
				if (stack.getTagCompound().getFloat("ubercharge") >= 1)
					if (living instanceof Player)
						use /= 3;
					else
						use = 0;
			} else if (stack.getTagCompound().getFloat("ubercharge") >= 1)
				use = use * 2 / 3;
			if (use != 0)
				this.consumeAmmoGlobal(living, stack, use);
			// ((ItemAmmo) stackAmmo.getItem()).consumeAmmo(living, stackAmmo,
			// this.getActualAmmoUse(stack, living, use));
		}
		int lastHitTime = target.ticksExisted - target.getEntityData().getInteger("lasthit");
		float heal = this.getHealAmount(stack, living, target);
		if (!(target instanceof IEntityTF2))
			heal *= TF2ConfigVars.damageMultiplier;
		if (lastHitTime > 200)
			heal *= 1 + Math.min(2, (lastHitTime - 200f) / 50f);
		float overheal = heal + target.getMaxHealth() * 0.001666f;
		float ubercharge = TF2Attribute.getModifier("Uber Rate", stack, 0.00125f, living);
		if (TF2Attribute.getModifier("Life Steal", stack, 0, living) != 0 && !TF2Util.isOnSameTeam(living, target)) {
			heal = heal * (0.8f + TF2Attribute.getModifier("Life Steal", stack, 0, living) * 0.2f) * (1f
					- (float) target.getEntityAttribute(SharedMonsterAttributes.ARMOR).getAttributeValue() * 0.03f);
			TF2Util.dealDamage(target, world, living, stack, 0, heal,
					TF2Util.causeDirectDamage(stack, living).setCritical(0).setDamageBypassesArmor());
			float lifesteal = TF2Util.getReducedHealing(living, target,
					heal * TF2Attribute.getModifier("Life Steal", stack, 0, living) * 0.15f);
			if (!TF2Util.isEnemy(living, target))
				lifesteal *= 0.25f;
			if (target instanceof Player) {
				lifesteal *= 2f;
			}
			living.heal(lifesteal);

		} else {
			if (target.getHealth() < target.getMaxHealth()) {
				overheal = (target.getHealth() + heal) - target.getMaxHealth() + 0.04f;
				target.heal(heal);
				if (living instanceof ServerPlayer
						&& (target instanceof EntityTF2Character || target instanceof Player)) {
					if ((living.getCapability(TF2weapons.PLAYER_CAP, null).healed += heal) >= 20) {
						living.getCapability(TF2weapons.PLAYER_CAP, null).healed -= 20;
						living.getCapability(TF2weapons.PLAYER_CAP, null).completeObjective(Objective.HEAL_20, stack);
					}
				}
			} else {
				if (target.getAbsorptionAmount() < target.getMaxHealth() * this.getMaxOverheal(stack, living, target)) {
					target.setAbsorptionAmount(Math.min(target.getAbsorptionAmount() + overheal,
							target.getMaxHealth() * this.getMaxOverheal(stack, living, null)));
					target.getDataManager().set(TF2EventsCommon.ENTITY_OVERHEAL, target.getAbsorptionAmount());
				}
				ubercharge = TF2Attribute.getModifier("Uber Overheal Rate", stack, ubercharge, target);
				if (target.getAbsorptionAmount() >= target.getMaxHealth()
						* (this.getMaxOverheal(stack, living, target) - 0.075)) {
					ubercharge /= 2;
				}
			}

		}
		if (!stack.getTagCompound().getBoolean("Activated") && stack.getTagCompound().getFloat("ubercharge") < 1) {
			float preUber = stack.getTagCompound().getFloat("ubercharge");
			stack.getTagCompound().setFloat("ubercharge", Math.min(1, preUber + ubercharge));
			if (Mth.floor(stack.getTagCompound().getFloat("ubercharge")
					* this.getUbers(stack, living)) > Mth.floor(preUber * this.getUbers(stack, living)))
				TF2Util.playSound(living, ItemFromData.getSound(stack, PropertyType.CHARGED_SOUND), 1.25f, 1);
		}
	}

	public HitResult trace(ItemStack stack, LivingEntity living, Level world) {
		double startX = 0;
		double startY = 0;
		double startZ = 0;

		double endX = 0;
		double endY = 0;
		double endZ = 0;

		// double[]
		// rand=TF2weapons.radiusRandom3D(this.getWeaponSpread(stack,living),
		// world.rand);

		startX = living.posX;// - (double)(Mth.cos(living.rotationYaw /
								// 180.0F * (float)Math.PI) * 0.16F);
		startY = living.posY + living.getEyeHeight();
		startZ = living.posZ;// - (double)(Mth.sin(living.rotationYaw /
								// 180.0F * (float)Math.PI) * 0.16F);

		// double[] rand=TF2weapons.radiusRandom2D(this.getWeaponSpread(stack),
		// world.rand);

		// float spreadPitch = (float) (living.rotationPitch / 180 + rand[1]);
		// float spreadYaw = (float) (living.rotationYaw / 180 +
		// rand[0]*(90/Math.max(90-Math.abs(spreadPitch*180),0.0001f)));
		// System.out.println("Rot: "+living.rotationYawHead+"
		// "+living.rotationPitch);
		float spreadPitch = living.rotationPitch / 180;
		float spreadYaw = living.rotationYawHead / 180;

		endX = -Mth.sin(spreadYaw * (float) Math.PI) * Mth.cos(spreadPitch * (float) Math.PI);
		endY = (-Mth.sin(spreadPitch * (float) Math.PI));
		endZ = Mth.cos(spreadYaw * (float) Math.PI) * Mth.cos(spreadPitch * (float) Math.PI);

		float var9 = Mth.sqrt(endX * endX + endY * endY + endZ * endZ);
		// float[] ratioX= this.calculateRatioX(living.rotationYaw,
		// living.rotationPitch);
		// float[] ratioY= this.calculateRatioY(living.rotationYaw,
		// living.rotationPitch);
		// float
		// wrapAngledYaw=Mth.wrapAngleTo180_float(living.rotationYaw);
		// float
		// fixedYaw=Math.max(Math.abs(wrapAngledYaw),90)-Math.min(Math.abs(wrapAngledYaw),90);

		double range = getData(stack).getFloat(PropertyType.RANGE);
		endX = (endX / var9) * range;
		endY = (endY / var9) * range;
		endZ = (endZ / var9) * range;
		List<HitResult> list = TF2Util.pierce(world, living, startX, startY, startZ, startX + endX, startY + endY,
				startZ + endZ, false, 0.2f, false);
		return !list.isEmpty() && list.get(0).entityHit != null ? list.get(0) : null;

	}

	@SuppressWarnings("unchecked")
	@Override
	public void onUpdate(ItemStack par1ItemStack, Level par2World, Entity par3Entity, int par4, boolean par5) {
		super.onUpdate(par1ItemStack, par2World, par3Entity, par4, par5);
		LivingEntity living = (LivingEntity) par3Entity;
		if (par1ItemStack.isEmpty())
			return;
		if (!par2World.isRemote && par5 && !this.canFire(par2World, living, par1ItemStack)) {
			par3Entity.getCapability(TF2weapons.WEAPONS_CAP, null).setHealTarget(-1);
		}

		if (!par2World.isRemote) {

			Potion effect = this.getPotion(par1ItemStack, living);
			boolean shield = this.isShieldResist(par1ItemStack, living);
			boolean activated = par1ItemStack.getTagCompound().getBoolean("Activated");

			if (activated && !shield) {
				float uber = par1ItemStack.getTagCompound().getFloat("ubercharge")
						- 1f / this.getUberTime(par1ItemStack, living);
				par1ItemStack.getTagCompound().setFloat("ubercharge", Math.max(0, uber));

				if (par5 && effect != null && par3Entity.ticksExisted % 4 == 0)
					TF2Util.addAndSendEffect(living, new MobEffectInstance(effect, 15));
				if (par1ItemStack.getTagCompound().getFloat("ubercharge") == 0) {

					par1ItemStack.getTagCompound().setBoolean("Activated", false);
					TF2Util.playSound(par3Entity, ItemFromData.getSound(par1ItemStack, PropertyType.UBER_STOP_SOUND),
							1.5f, 1);
					living.removePotionEffect(effect);

					// TF2weapons.sendTracking(new
					// TF2Message.PropertyMessage("UberCharged",
					// (byte)0,par3Entity),par3Entity);
				}
			}
			if (par5) {
				WeaponsCapability cap = par3Entity.getCapability(TF2weapons.WEAPONS_CAP, null);
				cap.setUberView(par1ItemStack.getTagCompound().getFloat("ubercharge"));
				if (shield && (cap.state & 4) == 4 && cap.getSecondaryCooldown() <= 0) {
					cap.setSecondaryCooldown(250);
					TF2Util.playSound(living, ItemFromData.getSound(par1ItemStack, PropertyType.SPECIAL_1_SOUND), 0.75f,
							1);
					par1ItemStack.getTagCompound().setByte("ResType",
							(byte) (par1ItemStack.getTagCompound().getByte("ResType") + 1));
					if (par1ItemStack.getTagCompound().getByte("ResType") > 2) {
						par1ItemStack.getTagCompound().setByte("ResType", (byte) 0);
					}
				}
				if (this.getPotion(par1ItemStack, living) == TF2weapons.quickFix
						&& this.isAlreadyUbered(par1ItemStack, living)) {
					this.heal(par1ItemStack, living, par2World, living);
				}
				Entity healTargetEnt = par2World.getEntityByID(cap.getHealTarget());
				if (healTargetEnt != null && healTargetEnt instanceof LivingEntity) {
					LivingEntity healTarget = (LivingEntity) healTargetEnt;
					// System.out.println("healing:
					// "+ItemUsable.itemProperties.server.get(par3Entity).getInteger("HealTarget"));
					double range = getData(par1ItemStack).getFloat(PropertyType.RANGE) + 1.6;
					if (!par2World.isRemote && healTarget != null
							&& par3Entity.getDistanceSq(healTarget) > range * range) {
						par3Entity.getCapability(TF2weapons.WEAPONS_CAP, null).setHealTarget(-1);
						// TF2weapons.sendTracking(new
						// TF2Message.PropertyMessage("HealTarget",
						// -1,par3Entity),par3Entity);
					} else if (healTarget != null && healTarget instanceof LivingEntity) {

						this.heal(par1ItemStack, living, par2World, healTarget);
						if (effect != null && (healTarget.ticksExisted % 4 == 0
								|| healTarget.getActivePotionEffect(effect) == null) && (activated || shield)) {
							TF2Util.addAndSendEffect(healTarget, new MobEffectInstance(effect, shield ? 5 : 15));
							if (shield) {
								TF2Util.addAndSendEffect(living, new MobEffectInstance(effect, 5));
							}
						}
					}
				}
			}
			if (par5 || par4 == 40) {
				try {
					((NonNullList<ItemStack>) ReflectionAccess.entityHandInv.get(par3Entity)).get(par5 ? 0 : 1)
							.getTagCompound()
							.setFloat("ubercharge", par1ItemStack.getTagCompound().getFloat("ubercharge"));
				} catch (Exception e) {}
			}
		}
	}

	public boolean shouldActivateCharge(ItemStack stack, EntityTF2Character living, LivingEntity target) {
		if (this.isShieldResist(stack, living)) {
			// System.out.println("reload: "+!living.isReloadPressed()+"
			// "+(living.getLastDamageSource() != null || target.getLastDamageSource() !=
			// null));
			return (living.getHealth() < living.getMaxHealth() * 0.82 || target.getHealth() < target.getMaxHealth())
					&& (living.getLastDamageSource() != null || target.getLastDamageSource() != null)
					&& !living.isReloadPressed();
		} else {
			if (this.getPotion(stack, living) == TF2weapons.critBoost) {
				if (target instanceof EntityTF2Character
						&& ((EntityTF2Character) target).getAttackTarget() instanceof Player
						&& ((EntityTF2Character) target).attack.getRangeSq() >= ((EntityTF2Character) target)
								.getAttackTarget().getDistanceSq(target)) {
					return true;
				}
				return target.getHealth() / target.getMaxHealth() < 0.35F
						&& (target.ticksExisted - target.getRevengeTimer()) < 25;
			} else {
				return target.getHealth() / target.getMaxHealth() < 0.35F
						&& (target.ticksExisted - target.getRevengeTimer()) < 25;
			}
		}
	}

	@Override
	public void holster(WeaponsCapability cap, ItemStack stack, LivingEntity living, Level world) {
		cap.setHealTarget(-1);
		living.removePotionEffect(TF2weapons.uber);
		super.holster(cap, stack, living, world);
	}

	@Override
	public boolean canFire(Level world, LivingEntity living, ItemStack stack) {
		return !(living instanceof Player) || (((Player) living).capabilities.isCreativeMode
				|| !this.searchForAmmo(living, stack).isEmpty());
	}

	@Override
	public boolean fireTick(ItemStack stack, LivingEntity living, Level world) {
		return false;
	}

	@Override
	public boolean altFireTick(ItemStack stack, LivingEntity living, Level world) {
		return false;
	}

	@Override
	public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
		return true;
	}

	@Override
	public boolean onEntitySwing(LivingEntity entityLiving, ItemStack stack) {
		return true;
	}

	public boolean isAlreadyUbered(ItemStack stack, LivingEntity living) {
		Potion potion = this.getPotion(stack, living);
		return living.isPotionActive(potion)
				&& (living.getActivePotionEffect(potion).getAmplifier() > 0 || !this.isShieldResist(stack, living));
	}

	public Potion getPotion(ItemStack stack, LivingEntity living) {
		if (this.isShieldResist(stack, living)) {
			switch (stack.getTagCompound().getByte("ResType")) {
			case 0:
				return TF2weapons.shieldBullet;
			case 1:
				return TF2weapons.shieldExplosive;
			case 2:
				return TF2weapons.shieldFire;
			default:
				return TF2weapons.shieldBullet;
			}
		}
		return Potion.getPotionFromResourceLocation(ItemFromData.getData(stack).getString(PropertyType.EFFECT_TYPE));
	}

	public boolean isShieldResist(ItemStack stack, LivingEntity living) {
		return TF2Attribute.getModifier("Weapon Mode", stack, 0, living) == 1;
	}

	public float getHealAmount(ItemStack stack, LivingEntity living, LivingEntity target) {
		float mult = 1f;
		if (this.getPotion(stack, living) == TF2weapons.quickFix && this.isAlreadyUbered(stack, target)) {
			mult *= 3f;
		}
		return mult * TF2Attribute.getModifierGlobal("Medic Heal", TF2Attribute.getModifier("Heal", stack,
				ItemFromData.getData(stack).getFloat(PropertyType.HEAL), living), living)
				* TF2Attribute.getModifierGlobal("Healing From Medic", 1, target);
	}

	public int getUbers(ItemStack stack, LivingEntity living) {
		return this.isShieldResist(stack, living) ? 4 : 1;
	}

	public float getUberTime(ItemStack stack, LivingEntity living) {
		return TF2Attribute.getModifier("Uber Time", stack,
				ItemFromData.getData(stack).getFloat(PropertyType.UBER_TIME), living);
	}

	public float getMaxOverheal(ItemStack stack, LivingEntity living, LivingEntity target) {
		if (target instanceof EntityTF2Character && ((EntityTF2Character) target).isGiant())
			return 0;
		return TF2Attribute.getModifier("Overheal", stack,
				ItemFromData.getData(stack).getFloat(PropertyType.MAX_OVERHEAL), living);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public boolean isFull3D() {
		return true;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void addInformation(ItemStack stack, Level world, List<String> tooltip, TooltipFlag advanced) {
		super.addInformation(stack, world, tooltip, advanced);

		tooltip.add(
				"Charge: " + Integer.toString(Math.round((stack.getTagCompound().getFloat("ubercharge")) * 100)) + "%");
	}

	@Override
	public boolean startUse(ItemStack stack, LivingEntity living, Level world, int oldState, int newState) {
		if (((newState & 1) - (oldState & 1)) == 1) {
			if (world.isRemote) {
				HitResult trace = this.trace(stack, living, world);
				if (trace == null || trace.entityHit == null || !(trace.entityHit instanceof LivingEntity))
					ClientProxy.playWeaponSound(living, ItemFromData.getSound(stack, PropertyType.NO_TARGET_SOUND),
							false, 1, stack);
			} else {
				if (living.getCapability(TF2weapons.WEAPONS_CAP, null).getHealTarget() != -1) {
					living.getCapability(TF2weapons.WEAPONS_CAP, null).setHealTarget(-1);
					// TF2weapons.network.sendToServer(new TF2Message.CapabilityMessage(living,
					// false));
				}
			}
			// System.out.println("Stop heal");
		}
		if (!world.isRemote && ((newState & 2) - (oldState & 2)) == 2
				&& stack.getTagCompound().getFloat("ubercharge") >= 1f / this.getUbers(stack, living)
				&& !this.isAlreadyUbered(stack, living)) {
			stack.getTagCompound().setBoolean("Activated", true);
			TF2Util.playSound(living, ItemFromData.getSound(stack, PropertyType.UBER_START_SOUND), 0.75f, 1);
			Entity healTargetEnt = world
					.getEntityByID(living.getCapability(TF2weapons.WEAPONS_CAP, null).getHealTarget());
			if (healTargetEnt instanceof LivingEntity) {
				((LivingEntity) healTargetEnt).addPotionEffect(new MobEffectInstance(this.getPotion(stack, living), 1));
			}
			if (this.isShieldResist(stack, living)) {
				if (healTargetEnt instanceof LivingEntity)
					TF2Util.addAndSendEffect(((LivingEntity) healTargetEnt),
							new MobEffectInstance(this.getPotion(stack, living),
									(int) (this.getUberTime(stack, living) / this.getUbers(stack, living)), 1));
				TF2Util.addAndSendEffect(living, new MobEffectInstance(this.getPotion(stack, living),
						(int) (this.getUberTime(stack, living) / this.getUbers(stack, living)), 1));
				stack.getTagCompound().setFloat("ubercharge",
						stack.getTagCompound().getFloat("ubercharge") - 1f / this.getUbers(stack, living));
				stack.getTagCompound().setBoolean("Activated", false);
			}
			if (stack.getTagCompound().getBoolean("Strange")) {
				stack.getTagCompound().setInteger("Ubercharges", stack.getTagCompound().getInteger("Ubercharges") + 1);
				TF2EventsCommon.onStrangeUpdate(stack, living);
			}
			// TF2weapons.sendTracking(new
			// TF2Message.PropertyMessage("UberCharged",
			// (byte)1,living),living);
		}
		return false;
	}

	@Override
	public boolean endUse(ItemStack stack, LivingEntity living, Level world, int oldState, int newState) {
		if (!world.isRemote && !TF2ConfigVars.medigunLock && (oldState & 1 - newState & 1) == 1) {
			living.getCapability(TF2weapons.WEAPONS_CAP, null).setHealTarget(-1);
			// TF2weapons.network.sendToServer(new TF2Message.CapabilityMessage(living,
			// false));
		}
		return false;
	}

	@Override
	public void drawOverlay(ItemStack stack, Player player, Tesselator tessellator, BufferBuilder renderer,
			Window resolution) {
		Minecraft.getMinecraft().getTextureManager().bindTexture(ClientProxy.healingTexture);
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		GL11.glDepthMask(false);
		OpenGlHelper.glBlendFunc(770, 771, 1, 0);
		GL11.glDisable(GL11.GL_ALPHA_TEST);

		ClientProxy.setColor(TF2Util.getTeamColor(player), 0.7f, 0, 0.25f, 0.8f);

		renderer.begin(7, DefaultVertexFormat.POSITION_TEX);
		renderer.pos(resolution.getScaledWidth() - 138, resolution.getScaledHeight() - 20, 0.0D).tex(0.0D, 1D)
				.endVertex();
		renderer.pos(resolution.getScaledWidth() - 14, resolution.getScaledHeight() - 20, 0.0D).tex(0.01D, 1D)
				.endVertex();
		renderer.pos(resolution.getScaledWidth() - 14, resolution.getScaledHeight() - 50, 0.0D).tex(0.01D, 0.99D)
				.endVertex();
		renderer.pos(resolution.getScaledWidth() - 138, resolution.getScaledHeight() - 50, 0.0D).tex(0.0D, 0.99D)
				.endVertex();
		tessellator.draw();

		GlStateManager.color(1.0F, 1.0F, 1.0F, 0.7F);
		renderer.begin(7, DefaultVertexFormat.POSITION_TEX);
		renderer.pos(resolution.getScaledWidth() - 140, resolution.getScaledHeight() - 18, 0.0D).tex(0.0D, 0.265625D)
				.endVertex();
		renderer.pos(resolution.getScaledWidth() - 12, resolution.getScaledHeight() - 18, 0.0D).tex(1.0D, 0.265625D)
				.endVertex();
		renderer.pos(resolution.getScaledWidth() - 12, resolution.getScaledHeight() - 52, 0.0D).tex(1.0D, 0.0D)
				.endVertex();
		renderer.pos(resolution.getScaledWidth() - 140, resolution.getScaledHeight() - 52, 0.0D).tex(0.0D, 0.0D)
				.endVertex();
		tessellator.draw();

		float uber = stack.getTagCompound().getFloat("ubercharge");
		String text;
		if (this.getUbers(stack, player) == 1)
			text = "UBERCHARGE: " + Math.round(uber * 100f) + "%";
		else
			text = "UBERCHARGES: " + Mth.floor(uber / (1f / this.getUbers(stack, player)));
		Minecraft.getMinecraft().ingameGUI.drawString(Minecraft.getMinecraft().ingameGUI.getFontRenderer(), text,
				resolution.getScaledWidth() - 130, resolution.getScaledHeight() - 48, 16777215);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GlStateManager.color(1.0F, 1.0F, 1.0F, 0.33F);
		renderer.begin(7, DefaultVertexFormat.POSITION);
		renderer.pos(resolution.getScaledWidth() - 132, resolution.getScaledHeight() - 22, 0.0D).endVertex();
		renderer.pos(resolution.getScaledWidth() - 20, resolution.getScaledHeight() - 22, 0.0D).endVertex();
		renderer.pos(resolution.getScaledWidth() - 20, resolution.getScaledHeight() - 36, 0.0D).endVertex();
		renderer.pos(resolution.getScaledWidth() - 132, resolution.getScaledHeight() - 36, 0.0D).endVertex();
		tessellator.draw();

		GlStateManager.color(1.0F, 1.0F, 1.0F, 0.85F);
		renderer.begin(7, DefaultVertexFormat.POSITION);
		renderer.pos(resolution.getScaledWidth() - 132, resolution.getScaledHeight() - 22, 0.0D).endVertex();
		renderer.pos(resolution.getScaledWidth() - 132 + 112 * uber, resolution.getScaledHeight() - 22, 0.0D)
				.endVertex();
		renderer.pos(resolution.getScaledWidth() - 132 + 112 * uber, resolution.getScaledHeight() - 36, 0.0D)
				.endVertex();
		renderer.pos(resolution.getScaledWidth() - 132, resolution.getScaledHeight() - 36, 0.0D).endVertex();
		tessellator.draw();
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glDepthMask(true);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glEnable(GL11.GL_ALPHA_TEST);
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

		if (this.isShieldResist(stack, player)) {
			float offset = 0f;
			if (TF2Util.getTeamForDisplay(player) != 0)
				offset = 96f;
			Minecraft.getMinecraft().getTextureManager().bindTexture(ClientRender.VAC_EFFECT);
			switch (stack.getTagCompound().getByte("ResType")) {
			case 0:
				offset += 0f;
				break;
			case 1:
				offset += 32f;
				break;
			case 2:
				offset += 64f;
				break;
			default:
				offset += 0f;
				break;
			}
			Gui.drawModalRectWithCustomSizedTexture(resolution.getScaledWidth() - 95, resolution.getScaledHeight() - 90,
					offset, 0, 32, 32, 256, 32);
		}
	}
}
