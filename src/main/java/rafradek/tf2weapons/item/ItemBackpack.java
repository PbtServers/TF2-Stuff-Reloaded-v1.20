package rafradek.tf2weapons.item;

import com.google.common.collect.Multimap;
import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import rafradek.tf2weapons.util.TF2GuiOpener;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.items.ItemStackHandler;
import rafradek.tf2weapons.TF2weapons;
import rafradek.tf2weapons.common.TF2Attribute;
import rafradek.tf2weapons.entity.mercenary.EntityTF2Character;
import rafradek.tf2weapons.util.PropertyType;
import rafradek.tf2weapons.util.TF2Util;

import javax.annotation.Nullable;
import java.util.UUID;

public class ItemBackpack extends ItemFromData {

	private UUID ARMOR_MOD = UUID.fromString("9F3D476D-C118-4544-8365-64846904B48E");
	private UUID MAX_HEALTH_MOD = UUID.fromString("D8499B04-0E66-4726-AB29-64469D734E0D");
	private UUID ARMOR_TOUGHNESS_MOD = UUID.fromString("D8499B04-0E66-4726-AB29-64469D734E0D");

	public ItemBackpack() {
		this.addPropertyOverride(new ResourceLocation("bodyModel"), new IItemPropertyGetter() {
			@Override
			@OnlyIn(Dist.CLIENT)
			public float apply(ItemStack stack, @Nullable Level world, @Nullable LivingEntity entityIn) {
				if (ItemWearable.usedModel == 1)
					return 1;
				return 0;
			}
		});
		this.addPropertyOverride(new ResourceLocation("headModel"), new IItemPropertyGetter() {
			@Override
			@OnlyIn(Dist.CLIENT)
			public float apply(ItemStack stack, @Nullable Level world, @Nullable LivingEntity entityIn) {
				if (ItemWearable.usedModel == 2)
					return 1;
				return 0;
			}
		});
	}

	@Override
	public boolean showDurabilityBar(ItemStack stack) {
		if (stack.getTagCompound() == null) return false;
		return stack.getTagCompound().getShort("Cooldown") > 0 || super.showDurabilityBar(stack);
	}

	@Override
	public double getDurabilityForDisplay(ItemStack stack) {
		return stack.getTagCompound().getShort("Cooldown") > 0
				? stack.getTagCompound().getShort("Cooldown") / this.getCooldown(stack)
				: super.getDurabilityForDisplay(stack);
	}

	@Override
	public Multimap<String, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
		Multimap<String, AttributeModifier> multimap = super.getAttributeModifiers(slot, stack);

		if (slot == EquipmentSlot.CHEST) {
			multimap.put(SharedMonsterAttributes.ARMOR.getName(), new AttributeModifier(ARMOR_MOD, "Armor modifier",
					TF2Attribute.getModifier("Armor", stack, getData(stack).getFloat(PropertyType.ARMOR), null), 0));
			multimap.put(SharedMonsterAttributes.MAX_HEALTH.getName(), new AttributeModifier(MAX_HEALTH_MOD,
					"Health modifier", TF2Attribute.getModifier("Health", stack, 0, null), 0));
			multimap.put(SharedMonsterAttributes.ARMOR_TOUGHNESS.getName(),
					new AttributeModifier(ARMOR_TOUGHNESS_MOD, "Armor toughness modifier",
							TF2Attribute.getModifier("Armor", stack, 0, null) * 0.5f
									+ getData(stack).getFloat(PropertyType.ARMOR_TOUGHNESS),
							0));
		}

		return multimap;
	}

	@Override
	public boolean isValidArmor(ItemStack stack, EquipmentSlot armorType, Entity entity) {
		return armorType == EquipmentSlot.CHEST;
	}

	@Override
	public void onArmorTick(Level world, Player player, ItemStack itemStack) {
		this.onArmorTickAny(world, player, itemStack);
	}

	@Override
	public EquipmentSlot getEquipmentSlot(ItemStack stack) {
		return EquipmentSlot.CHEST;
	}

	@Override
	public int getVisibilityFlags(ItemStack stack, LivingEntity living) {
		return stack.getTagCompound().getShort("Cooldown") == 0 ? ItemFromData.getData(stack).getInt(PropertyType.WEAR)
				: 0;
	}

	public int getCooldown(ItemStack stack) {
		return 1200;
	}

	public ItemStack getBackpackItemToUse(ItemStack stack, LivingEntity player) {
		return ItemStack.EMPTY;
	}

	public void onArmorTickAny(Level world, LivingEntity player, ItemStack itemStack) {
		if (!world.isRemote) {
			if (player.ticksExisted % 20 == 0) {
				float heal = TF2Attribute.getModifier("Health Regen", itemStack, 0, player);
				if (heal > 0) {
					int lastHitTime = player.ticksExisted - player.getEntityData().getInteger("lasthit");
					if (lastHitTime >= 120)
						player.heal(heal);
					else if (lastHitTime >= 60)
						player.heal(TF2Util.lerp(heal, heal / 4f, (lastHitTime - 60) / 60f));
					else
						player.heal(heal / 4f);
				}
			}
			if (itemStack.getTagCompound().getShort("Cooldown") > 0) {
				itemStack.getTagCompound().setShort("Cooldown",
						(short) (itemStack.getTagCompound().getShort("Cooldown") - 1));
			}
		}
	}

	@Override
	public boolean canApplyAtEnchantingTable(ItemStack stack, net.minecraft.enchantment.Enchantment enchantment) {
		return super.canApplyAtEnchantingTable(stack, enchantment)
				|| enchantment.type == EnumEnchantmentType.ARMOR_CHEST || enchantment.type == EnumEnchantmentType.ARMOR
				|| enchantment.type == EnumEnchantmentType.WEARABLE;
	}

	@Override
	public InteractionResultHolder<ItemStack> onItemRightClick(Level world, Player living, InteractionHand hand) {
		if (!world.isRemote)
			TF2GuiOpener.openGui(living, TF2weapons.instance, 0, world, 0, 0, 0);
		return new InteractionResultHolder<>(InteractionResult.SUCCESS, living.getHeldItem(hand));
	}

	public static ItemStack getBackpack(LivingEntity living) {
		if (living.hasCapability(TF2weapons.INVENTORY_CAP, null) && living.getCapability(TF2weapons.INVENTORY_CAP, null)
				.getStackInSlot(2).getItem() instanceof ItemBackpack) {
			return living.getCapability(TF2weapons.INVENTORY_CAP, null).getStackInSlot(2);
		}
		if (living instanceof EntityTF2Character) {
			ItemStackHandler loadout = ((EntityTF2Character) living).loadout;
			for (int i = 0; i < loadout.getSlots(); i++) {
				if (loadout.getStackInSlot(i).getItem() instanceof ItemBackpack)
					return loadout.getStackInSlot(i);
			}
		}
		if (living.getItemStackFromSlot(EquipmentSlot.CHEST).getItem() instanceof ItemBackpack)
			return living.getItemStackFromSlot(EquipmentSlot.CHEST);
		else
			return ItemStack.EMPTY;
	}
}
