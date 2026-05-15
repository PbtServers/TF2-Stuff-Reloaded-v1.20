package rafradek.tf2weapons.item;

import net.minecraft.client.Minecraft;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import rafradek.tf2weapons.TF2ConfigVars;
import rafradek.tf2weapons.TF2weapons;
import rafradek.tf2weapons.common.WeaponsCapability;
import rafradek.tf2weapons.message.TF2Message.PredictionMessage;

public class ItemGas extends ItemProjectileWeapon {

	public ItemGas() {
		super();
		this.setMaxStackSize(64);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public CreativeModeTab getCreativeTab() {
		return TF2weapons.tabutilitytf2;
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
		return (double) (value != null ? value : 0) / (double) 1600;
	}

	@Override
	public boolean canFire(Level world, LivingEntity living, ItemStack stack) {
		Integer value = living.getCapability(TF2weapons.WEAPONS_CAP, null).effectsCool.get(getData(stack).getName());
		return (value == null || value <= 0) && super.canFire(world, living, stack)
				&& !(living instanceof Player && ((Player) living).getCooldownTracker().hasCooldown(this));
	}

	@Override
	public boolean doMuzzleFlash(ItemStack stack, LivingEntity attacker, InteractionHand hand) {
		return false;
	}

	@Override
	public boolean use(ItemStack stack, LivingEntity living, Level world, InteractionHand hand,
			PredictionMessage message) {
		if (super.use(stack, living, world, hand, message) && !world.isRemote) {
			if (living instanceof Player && !((Player) living).capabilities.isCreativeMode
					&& !TF2ConfigVars.freeUseItems)
				stack.shrink(1);
			if (living.hasCapability(TF2weapons.WEAPONS_CAP, null))
				WeaponsCapability.get(living).addEffectCooldown(getData(stack).getName(), 600);
			// ((Player) living).getCooldownTracker().setCooldown(this, (int)
			// (this.getFiringSpeed(stack, living)/50 *
			// (TF2ConfigVars.fastItemCooldown ? 1f:
			// getData(stack).getFloat(PropertyType.COOLDOWN_LONG))));
		}
		return true;
	}
}
