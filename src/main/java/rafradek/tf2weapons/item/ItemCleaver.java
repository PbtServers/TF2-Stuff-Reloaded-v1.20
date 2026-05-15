package rafradek.tf2weapons.item;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.Level;
import rafradek.tf2weapons.TF2ConfigVars;
import rafradek.tf2weapons.entity.EntityDummy;
import rafradek.tf2weapons.message.TF2Message.PredictionMessage;
import rafradek.tf2weapons.util.PropertyType;

public class ItemCleaver extends ItemProjectileWeapon {

	public ItemCleaver() {
		super();
		this.setMaxStackSize(16);
	}

	@Override
	public boolean doMuzzleFlash(ItemStack stack, LivingEntity attacker, InteractionHand hand) {
		return false;
	}

	@Override
	public boolean use(ItemStack stack, LivingEntity living, Level world, InteractionHand hand,
			PredictionMessage message) {
		if (super.use(stack, living, world, hand, message) && !world.isRemote) {
			if (living instanceof Player)
				((Player) living).getCooldownTracker().setCooldown(this, (int) (this.getFiringSpeed(stack, living)
						/ 50
						* (TF2ConfigVars.fastItemCooldown ? 1f : getData(stack).getFloat(PropertyType.COOLDOWN_LONG))));
			if (living instanceof Player && !((Player) living).capabilities.isCreativeMode
					&& !TF2ConfigVars.freeUseItems)
				stack.shrink(1);

		}
		return true;
	}

	@Override
	public boolean canFire(Level world, LivingEntity living, ItemStack stack) {
		return super.canFire(world, living, stack)
				&& !(living instanceof Player && ((Player) living).getCooldownTracker().hasCooldown(this));
	}

	@Override
	public boolean isProjectileInfinite(LivingEntity living, ItemStack stack) {
		return !(living instanceof EntityDummy) && (TF2ConfigVars.freeUseItems || !(living instanceof Player)
				|| ((Player) living).capabilities.isCreativeMode);
	}
}
