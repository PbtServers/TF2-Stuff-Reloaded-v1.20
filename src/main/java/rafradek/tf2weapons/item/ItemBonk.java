package rafradek.tf2weapons.item;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import rafradek.tf2weapons.TF2ConfigVars;
import rafradek.tf2weapons.TF2weapons;
import rafradek.tf2weapons.util.PropertyType;

public class ItemBonk extends ItemFromData {
	public ItemBonk() {
		super();
		this.setMaxStackSize(64);
	}

	@Override
	public int getMaxItemUseDuration(ItemStack stack) {
		return 40;
	}

	@Override
	public EnumAction getItemUseAction(ItemStack stack) {
		return EnumAction.DRINK;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public boolean showDurabilityBar(ItemStack stack) {
		Integer value = Minecraft.getMinecraft().player.getCapability(TF2weapons.WEAPONS_CAP, null).effectsCool
				.get(getData(stack).getName());
		return value != null && value > 0;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public double getDurabilityForDisplay(ItemStack stack) {
		Integer value = Minecraft.getMinecraft().player.getCapability(TF2weapons.WEAPONS_CAP, null).effectsCool
				.get(getData(stack).getName());
		return (double) (value != null ? value : 0) / (double) ((int) (getData(stack).getInt(PropertyType.COOLDOWN)
				* (TF2ConfigVars.fastItemCooldown ? 1f : getData(stack).getFloat(PropertyType.COOLDOWN_LONG))));
	}

	@Override
	public InteractionResultHolder<ItemStack> onItemRightClick(Level world, Player player, InteractionHand hand) {
		ItemStack itemStackIn = player.getHeldItem(hand);
		Integer value = player.getCapability(TF2weapons.WEAPONS_CAP, null).effectsCool
				.get(getData(itemStackIn).getName());
		if (value == null || value <= 0) {
			player.setActiveHand(hand);
			return new InteractionResultHolder<>(InteractionResult.SUCCESS, itemStackIn);
		}
		return new InteractionResultHolder<>(InteractionResult.FAIL, itemStackIn);
	}

	@Override
	public ItemStack onItemUseFinish(ItemStack stack, Level world, LivingEntity entityLiving) {

		entityLiving.getCapability(TF2weapons.WEAPONS_CAP, null).effectsCool.put(getData(stack).getName(),
				(int) (getData(stack).getInt(PropertyType.COOLDOWN)
						* (TF2ConfigVars.fastItemCooldown ? 1f : getData(stack).getFloat(PropertyType.COOLDOWN_LONG))));
		entityLiving.addPotionEffect(new MobEffectInstance(
				Potion.getPotionFromResourceLocation(getData(stack).getString(PropertyType.EFFECT_TYPE)),
				TF2ConfigVars.longDurationBanner ? ItemFromData.getData(stack).getInt(PropertyType.DURATION) : 160));
		if (!TF2ConfigVars.freeUseItems
				&& !(entityLiving instanceof Player && ((Player) entityLiving).capabilities.isCreativeMode))
			stack.shrink(1);
		return stack;
	}
}
