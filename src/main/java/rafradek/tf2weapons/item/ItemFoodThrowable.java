package rafradek.tf2weapons.item;


import net.minecraft.nbt.CompoundTag;
import com.google.common.collect.Iterables;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.Level;
import rafradek.tf2weapons.TF2ConfigVars;

public class ItemFoodThrowable extends ItemFood {

	public int waitTime;

	public ItemFoodThrowable(int amount, float saturation, boolean isWolfFood, int waitTime) {
		super(amount, saturation, isWolfFood);
		this.waitTime = waitTime;
		this.setAlwaysEdible();
	}

	@Override
	public boolean onEntityItemUpdate(ItemEntity entityItem) {
		if (entityItem.getItem().hasTagCompound() && entityItem.getItem().getTagCompound().getBoolean("IsHealing")) {
			LivingEntity living = Iterables.getFirst(entityItem.world.getEntitiesWithinAABB(LivingEntity.class,
					entityItem.getEntityBoundingBox(), test -> (!(test instanceof Player)
							&& test.getHealth() < test.getMaxHealth() && test.isNonBoss() && test.isEntityAlive())),
					null);

			if (living != null) {
				living.heal(living.getMaxHealth() * this.getHealAmount(entityItem.getItem()) / 28f);
				entityItem.getItem().shrink(1);
				if (entityItem.getItem().getTagCompound().getBoolean("IsHealing"))
					entityItem.getItem().setTagCompound(null);
			}
		}
		return false;
	}

	@Override
	public boolean onDroppedByPlayer(ItemStack item, Player player) {

		if (!TF2ConfigVars.fastItemCooldown) {
			/*
			 * if (!player.getCooldownTracker().hasCooldown(this)) {
			 * player.getCooldownTracker().setCooldown(this, waitTime); if
			 * (!item.hasTagCompound()) item.setTagCompound(new CompoundTag());
			 * item.getTagCompound().setBoolean("IsHealing", true); }
			 */
		}
		return true;
	}

	@Override
	protected void onFoodEaten(ItemStack stack, Level world, Player player) {
		super.onFoodEaten(stack, world, player);
		if (!world.isRemote) {
			player.getCooldownTracker().setCooldown(this, TF2ConfigVars.fastItemCooldown ? waitTime / 2 : waitTime);
		}
	}

	@Override
	public InteractionResultHolder<ItemStack> onItemRightClick(Level world, Player player, InteractionHand handIn) {
		ItemStack previous = player.getHeldItem(handIn);
		InteractionResultHolder<ItemStack> result = super.onItemRightClick(world, player, handIn);
		if (TF2ConfigVars.freeUseItems)
			result.getResult().setCount(previous.getCount());
		return result;
	}
}
