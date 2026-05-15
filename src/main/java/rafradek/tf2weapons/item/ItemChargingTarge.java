package rafradek.tf2weapons.item;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import rafradek.tf2weapons.TF2weapons;
import rafradek.tf2weapons.common.TF2Attribute;
import rafradek.tf2weapons.util.TF2Class;
import rafradek.tf2weapons.util.TF2Util;

import javax.annotation.Nullable;

public class ItemChargingTarge extends ItemFromData {

	public ItemChargingTarge() {
		super();
		this.setMaxDamage(600);
	}

	@Override
	public InteractionResultHolder<ItemStack> onItemRightClick(Level world, Player living, InteractionHand hand) {
		// if (!living.getCapability(TF2weapons.WEAPONS_CAP,
		// null).effectsCool.containsKey("Charging")) {
		ItemStack stack = living.getHeldItem(hand);
		if (ItemToken.allowUse(living, TF2Class.DEMOMAN)) {

			if (!world.isRemote)
				living.addPotionEffect(new MobEffectInstance(TF2weapons.charging,
						(int) TF2Attribute.getModifier("Effect Duration", stack, 40, living),
						(int) TF2Attribute.getModifier("Charge Step", stack, 0, living)));
			living.getCooldownTracker().setCooldown(this,
					(int) (280f / TF2Attribute.getModifier("Charge", stack, 1, living)));
		}
		// living.getCapability(TF2weapons.WEAPONS_CAP,
		// null).effectsCool.put("Charging", 280);
		// }
		return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
	}

	public static ItemStack getChargingShield(LivingEntity living) {
		if (living.getHeldItemMainhand() != null && living.getHeldItemMainhand().getItem() instanceof ItemChargingTarge)
			return living.getHeldItemMainhand();
		else if (living.getHeldItemOffhand() != null
				&& living.getHeldItemOffhand().getItem() instanceof ItemChargingTarge)
			return living.getHeldItemOffhand();
		else
			return ItemStack.EMPTY;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public boolean showDurabilityBar(ItemStack stack) {

		return super.showDurabilityBar(stack)
				|| Minecraft.getMinecraft().player.getActivePotionEffect(TF2weapons.charging) != null;
		/*
		 * Integer value =
		 * Minecraft.getMinecraft().player.getCapability(TF2weapons.WEAPONS_CAP,
		 * null).effectsCool .get("Charging"); return value != null && value > 0;
		 */
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public double getDurabilityForDisplay(ItemStack stack) {
		if (Minecraft.getMinecraft().player.getActivePotionEffect(TF2weapons.charging) != null)
			return 1 - ((double) Minecraft.getMinecraft().player.getActivePotionEffect(TF2weapons.charging)
					.getDuration() / (double) 40);
		return super.getDurabilityForDisplay(stack);
		/*
		 * Integer value =
		 * Minecraft.getMinecraft().player.getCapability(TF2weapons.WEAPONS_CAP,
		 * null).effectsCool .get("Charging"); return (double) (value != null ? value :
		 * 0) / (double) 280;
		 */
	}

	@Override
	public boolean getIsRepairable(ItemStack toRepair, ItemStack repair) {
		if (TF2Util.isOre("plankWood", repair))
			return true;
		return super.getIsRepairable(toRepair, repair);
	}

	@Override
	public boolean isShield(ItemStack stack, @Nullable LivingEntity entity) {
		return true;
	}
}
