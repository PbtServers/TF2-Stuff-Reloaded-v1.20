package rafradek.tf2weapons.item;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import rafradek.tf2weapons.TF2ConfigVars;
import rafradek.tf2weapons.TF2weapons;
import rafradek.tf2weapons.entity.mercenary.InvasionEvent;
import rafradek.tf2weapons.util.TF2Util;

public class ItemEventMaker extends Item {

	public ItemEventMaker() {
		this.setHasSubtypes(true);
		this.setUnlocalizedName("eventmaker");
	}

	@Override
	public String getUnlocalizedName(ItemStack stack) {
		return super.getUnlocalizedName(stack) + "." + stack.getMetadata();
	}

	@Override
	public InteractionResultHolder<ItemStack> onItemRightClick(Level world, Player living, InteractionHand hand) {
		ItemStack stack = living.getHeldItem(hand);
		if (!world.isRemote && !TF2ConfigVars.disableInvasionItems) {
			if (TF2Util.getTeam(living) == null && TF2ConfigVars.canJoin) {
				living.sendMessage(new Component("item.eventmaker.noteam"));
				return new InteractionResultHolder<>(InteractionResult.FAIL, stack);
			}
			if (!world.getCapability(TF2weapons.WORLD_CAP, null).startInvasion(living,
					stack.getMetadata() % InvasionEvent.DIFFICULTY.length, living.capabilities.isCreativeMode)) {
				living.sendMessage(new Component("item.eventmaker.fail"));
				return new InteractionResultHolder<>(InteractionResult.FAIL, stack);
			} else {
				stack.shrink(1);
			}
		}
		return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);

	}

	@Override
	public void getSubItems(CreativeModeTab par2CreativeTabs, NonNullList<ItemStack> par3List) {
		// System.out.println(this.getCreativeTab());
		if (!this.isInCreativeTab(par2CreativeTabs))
			return;
		for (int i = 0; i < InvasionEvent.DIFFICULTY.length; i++)
			par3List.add(new ItemStack(this, 1, i));
	}
}
