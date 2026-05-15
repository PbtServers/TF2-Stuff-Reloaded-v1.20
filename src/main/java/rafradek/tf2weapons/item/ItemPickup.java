package rafradek.tf2weapons.item;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionResult;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.NonNullList;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import rafradek.tf2weapons.TF2weapons;
import rafradek.tf2weapons.entity.EntityPickup;

public class ItemPickup extends Item {

	public ItemPickup() {
		this.setHasSubtypes(true);
		this.setUnlocalizedName(TF2weapons.MOD_ID + ".tf2pickup");
	}

	@Override
	public String getUnlocalizedName(ItemStack stack) {
		return super.getUnlocalizedName(stack) + "." + stack.getMetadata();
	}

	@Override
	public InteractionResult onItemUse(Player player, Level world, BlockPos pos, InteractionHand hand,
			Direction facing, float hitX, float hitY, float hitZ) {
		if (!world.isRemote) {
			BlockPos forwardpos = pos.offset(facing);
			EntityPickup pickup = new EntityPickup(world,
					EntityPickup.Type.values()[player.getHeldItem(hand).getMetadata()], true);
			if (!player.isCreative())
				player.getHeldItem(hand).shrink(1);
			pickup.setPosition(forwardpos.getX() + 0.5, forwardpos.getY(), forwardpos.getZ() + 0.5);
			world.spawnEntity(pickup);
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	public void getSubItems(CreativeModeTab par2CreativeTabs, NonNullList<ItemStack> par3List) {
		// System.out.println(this.getCreativeTab());
		if (!this.isInCreativeTab(par2CreativeTabs))
			return;
		for (int i = 0; i < EntityPickup.Type.values().length; i++) {
			if (EntityPickup.Type.values()[i].visible)
				par3List.add(new ItemStack(this, 1, i));
		}
	}
}
