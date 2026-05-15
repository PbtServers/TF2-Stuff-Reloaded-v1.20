package rafradek.tf2weapons.item;



import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.InteractionHand;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import rafradek.tf2weapons.entity.ai.EntityAITarget;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.protocol.game.ClientboundExplodePacket;
import net.minecraft.util.*;
import net.minecraft.ChatFormatting;
import net.minecraft.world.level.Level;
import rafradek.tf2weapons.util.TF2GuiOpener;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import rafradek.tf2weapons.TF2weapons;
import rafradek.tf2weapons.client.ClientProxy;
import rafradek.tf2weapons.common.MapList;
import rafradek.tf2weapons.common.TF2Attribute;
import rafradek.tf2weapons.common.WeaponsCapability;
import rafradek.tf2weapons.entity.projectile.EntityProjectileBase;
import rafradek.tf2weapons.util.PropertyType;
import rafradek.tf2weapons.util.TF2Explosion;
import rafradek.tf2weapons.util.TF2Util;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class ItemWearable extends ItemFromData {

	public static int usedModel;

	@SuppressWarnings("unchecked")
	public static final Tuple<String, AttributeModifier>[] EFFECT_MODIFIERS = new Tuple[] {
			new Tuple<>(SharedMonsterAttributes.ATTACK_DAMAGE.getName(), new AttributeModifier("Mod e1", 0.1, 2)),
			new Tuple<>(SharedMonsterAttributes.MAX_HEALTH.getName(), new AttributeModifier("Mod e2", 1, 0)),
			new Tuple<>(SharedMonsterAttributes.MOVEMENT_SPEED.getName(), new AttributeModifier("Mod e3", 0.12, 1)),
			new Tuple<>(SharedMonsterAttributes.ARMOR_TOUGHNESS.getName(), new AttributeModifier("Mod e4", 3, 0)),
			new Tuple<>(SharedMonsterAttributes.ATTACK_SPEED.getName(), new AttributeModifier("Mod e5", 0.14, 2)),
			new Tuple<>(SharedMonsterAttributes.ATTACK_DAMAGE.getName(), new AttributeModifier("Mod e6", 1, 0)),
			new Tuple<>(SharedMonsterAttributes.ATTACK_SPEED.getName(), new AttributeModifier("Mod e7", 0.08, 2)),
			new Tuple<>(SharedMonsterAttributes.MAX_HEALTH.getName(), new AttributeModifier("Mod e8", 2, 0)),
			new Tuple<>(SharedMonsterAttributes.ARMOR_TOUGHNESS.getName(), new AttributeModifier("Mod e9", 4, 0)),
			new Tuple<>(SharedMonsterAttributes.ATTACK_DAMAGE.getName(), new AttributeModifier("Mod e10", 0.08, 2)),
			new Tuple<>(SharedMonsterAttributes.MOVEMENT_SPEED.getName(), new AttributeModifier("Mod e11", 0.16, 1)),
			new Tuple<>(SharedMonsterAttributes.ATTACK_SPEED.getName(), new AttributeModifier("Mod e12", 0.12, 2)),
			new Tuple<>(SharedMonsterAttributes.MOVEMENT_SPEED.getName(), new AttributeModifier("Mod e13", 0.1, 1)),
			new Tuple<>(SharedMonsterAttributes.MAX_HEALTH.getName(), new AttributeModifier("Mod e14", 0.1, 2)),
			new Tuple<>(SharedMonsterAttributes.ARMOR_TOUGHNESS.getName(), new AttributeModifier("Mod e15", 5, 0)) };

	public ItemWearable() {
		super();
		this.addPropertyOverride(new ResourceLocation("bodyModel"), (ItemStack stack, @Nullable Level world,
				@Nullable LivingEntity entityIn) -> (ItemWearable.usedModel == 1) ? 1 : 0);
		this.addPropertyOverride(new ResourceLocation("headModel"), (ItemStack stack, @Nullable Level world,
				@Nullable LivingEntity entityIn) -> (ItemWearable.usedModel == 2) ? 1 : 0);
	}

	@Override
	public boolean isValidArmor(ItemStack stack, EquipmentSlot slot, Entity player) {
		return slot == (isHat(stack) ? EquipmentSlot.HEAD : EquipmentSlot.CHEST);
	}

	@Override
	public EquipmentSlot getEquipmentSlot(ItemStack stack) {
		return isHat(stack) ? EquipmentSlot.HEAD : EquipmentSlot.CHEST;
	}

	@Override
	public InteractionResultHolder<ItemStack> onItemRightClick(Level world, Player living, InteractionHand hand) {
		if (!world.isRemote)
			TF2GuiOpener.openGui(living, TF2weapons.instance, 0, world, 0, 0, 0);
		return new InteractionResultHolder<>(InteractionResult.SUCCESS, living.getHeldItem(hand));
	}

	public boolean isHat(ItemStack stack) {
		return (getData(stack).getInt(PropertyType.WEAR) & 1) == 1;
	}

	@Override
	public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
		return getData(stack).getString(PropertyType.ARMOR_IMAGE);
	}

	@Override
	public void onArmorTick(Level world, Player player, ItemStack itemStack) {
		onUpdateWearing(itemStack, world, player);
	}

	public void applyRandomEffect(ItemStack stack, Random rand) {
		stack.getTagCompound().setByte("UEffect", (byte) rand.nextInt(11));
	}

	@Override
	public String getItemStackDisplayName(ItemStack stack) {
		String name = super.getItemStackDisplayName(stack);
		if (stack.hasTagCompound() && stack.getTagCompound().hasKey("UEffect"))
			name = ChatFormatting.DARK_PURPLE + "Unusual " + name;
		return name;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void addInformation(ItemStack stack, Level world, List<String> tooltip, TooltipFlag advanced) {
		super.addInformation(stack, world, tooltip, advanced);
		if (stack.hasTagCompound()) {

			if (stack.getTagCompound().hasKey("UEffect")) {
				tooltip.add("");
				String str = I18n.format("item.wearable.effect." + stack.getTagCompound().getByte("UEffect"));
				tooltip.add(I18n.format("item.wearable.effect") + " " + str);
			}
		}
	}

	@Override
	public Multimap<String, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
		Multimap<String, AttributeModifier> multimap = super.getAttributeModifiers(slot, stack);

		if (slot == EquipmentSlot.HEAD && stack.getTagCompound().hasKey("UEffect")) {
			Tuple<String, AttributeModifier> tuple = EFFECT_MODIFIERS[stack.getTagCompound().getByte("UEffect")
					% EFFECT_MODIFIERS.length];
			multimap.put(tuple.getFirst(), tuple.getSecond());
		}

		return multimap;
	}

	public void onUpdateWearing(ItemStack stack, Level par2World, LivingEntity living) {
		if (!living.world.isRemote && living.deathTime == 18
				&& TF2Attribute.getModifier("Explode Death", stack, 0, living) != 0) {
			TF2Explosion explosion = new TF2Explosion(living.world, living, living.posX, living.posY + 0.5, living.posZ,
					5, null, 0, 3, SoundEvents.ENTITY_GENERIC_EXPLODE);
			// System.out.println("ticks: "+this.ticksExisted);
			explosion.isFlaming = false;
			explosion.isSmoking = true;
			explosion.doExplosionA();
			explosion.doExplosionB(true);
			Iterator<Entity> affectedIterator = explosion.affectedEntities.keySet().iterator();
			while (affectedIterator.hasNext()) {
				Entity ent = affectedIterator.next();
				TF2Util.dealDamage(ent, living.world, living, ItemStack.EMPTY, 2,
						explosion.affectedEntities.get(ent) * 26,
						TF2Util.causeDirectDamage(stack, living).setCritical(2).setExplosion());
			}
			Iterator<Player> iterator = living.world.playerEntities.iterator();

			while (iterator.hasNext()) {
				Player Player = iterator.next();

				if (Player.getDistanceSq(living.posX, living.posY + 0.5, living.posZ) < 4096.0D) {
					((ServerPlayer) Player).connection
							.sendPacket(new ClientboundExplodePacket(living.posX, living.posY + 0.5, living.posZ, 4,
									explosion.affectedBlockPositions, explosion.getKnockbackMap().get(Player)));
				}
			}
		}

		if (WeaponsCapability.get(living).isDisguised())
			return;
		if (!living.world.isRemote && living.ticksExisted % 13 == 0
				&& TF2Attribute.getModifier("Bomb Enemy", stack, 0, living) != 0) {
			LivingEntity target = WeaponsCapability.get(living).lastAttacked;
			if (target == null || !target.isEntityAlive() || target.getDistanceSq(living) > 200) {
				target = Iterables
						.getFirst(
								living.Level
										.getEntitiesWithinAABB(Mob.class,
												living.getEntityBoundingBox().grow(11),
												ent -> ent.getAttackTarget() == living
														&& EntityAITarget.isSuitableTarget(ent, living, false, true)),
								null);
			}
			if (target != null && living.canEntityBeSeen(target) && target.isEntityAlive()
					&& target.getDistanceSq(living) < 144) {
				ItemStack stackW = ItemFromData.getNewStack("bombinomiconbomb");
				// ((ItemWeapon)stackW.getItem()).shoot(stackW, living, living.world, 0,
				// InteractionHand.OFF_HAND);

				try {
					EntityProjectileBase proj = MapList.projectileClasses
							.get(ItemFromData.getData(stackW).getString(PropertyType.PROJECTILE))
							.getConstructor(world.class).newInstance(living.world);
					proj.damageModifier = TF2Attribute.getModifier("Bomb Enemy", stack, 0, living);
					proj.initProjectile(living, InteractionHand.MAIN_HAND, stackW);
					double x = target.posX;
					double y = target.posY + target.getEyeHeight();
					double z = target.posZ;
					// float speed = TF2Attribute.getModifier("Proj Speed", stackW,
					// ItemFromData.getData(stackW).getFloat(PropertyType.PROJECTILE_SPEED),
					// living);
					proj.face(x, y, z, 1);
					living.world.spawnEntity(proj);

				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		}
		if (living.world.isRemote
				&& (living != ClientProxy.getLocalPlayer()
						|| Minecraft.getMinecraft().gameSettings.thirdPersonView != 0)
				&& stack.getTagCompound().hasKey("UEffect")) {
			net.minecraft.core.particles.ParticleOptions type;
			switch (stack.getTagCompound().getByte("UEffect")) {
			case 0:
				type = EnumParticleTypes.FLAME;
				break;
			case 1:
				type = EnumParticleTypes.HEART;
				break;
			case 2:
				type = EnumParticleTypes.SMOKE_LARGE;
				break;
			case 3:
				type = EnumParticleTypes.WATER_BUBBLE;
				break;
			case 4:
				type = EnumParticleTypes.VILLAGER_ANGRY;
				break;
			case 5:
				type = EnumParticleTypes.VILLAGER_HAPPY;
				break;
			case 6:
				type = EnumParticleTypes.SUSPENDED_DEPTH;
				break;
			case 7:
				type = EnumParticleTypes.SLIME;
				break;
			case 8:
				type = EnumParticleTypes.CRIT_MAGIC;
				break;
			case 9:
				type = EnumParticleTypes.DRAGON_BREATH;
				break;
			case 10:
				type = EnumParticleTypes.NOTE;
				break;
			case 11:
				type = EnumParticleTypes.FIREWORKS_SPARK;
				break;
			case 12:
				type = EnumParticleTypes.EXPLOSION_NORMAL;
				break;
			case 13:
				type = EnumParticleTypes.SPELL_MOB;
				break;
			default:
				type = EnumParticleTypes.END_ROD;
				break;
			}
			par2World.spawnParticle(type,
					living.posX + living.getRNG().nextDouble() * living.width - (living.width / 2),
					living.posY + living.height + living.getRNG().nextDouble() * 0.2,
					living.posZ + living.getRNG().nextDouble() * living.width - (living.width / 2), living.motionX,
					living.getRNG().nextDouble() * 0.02, living.motionZ, new int[0]);
		}
	}
}
