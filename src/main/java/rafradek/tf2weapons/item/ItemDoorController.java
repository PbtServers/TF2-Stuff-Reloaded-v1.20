package rafradek.tf2weapons.item;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.InteractionResult;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.NonNullList;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import rafradek.tf2weapons.tileentity.TileEntityOverheadDoor;

public class ItemDoorController extends Item {

	public static final String[] NAMES = { "players", "mobs", "RED", "BLU" };

	public ItemDoorController() {
		this.setHasSubtypes(true);
	}

	@Override
	public String getUnlocalizedName(ItemStack stack) {
		return super.getUnlocalizedName(stack) + "." + NAMES[stack.getItemDamage() % NAMES.length];
	}

	@Override
	public void getSubItems(CreativeModeTab par2CreativeTabs, NonNullList<ItemStack> par3List) {
		// System.out.println(this.getCreativeTab());
		if (!this.isInCreativeTab(par2CreativeTabs))
			return;
		for (int i = 0; i < NAMES.length; i++)
			par3List.add(new ItemStack(this, 1, i));
	}

	@Override
	public InteractionResult onItemUse(Player player, Level world, BlockPos pos, InteractionHand hand, Direction facing,
			float hitX, float hitY, float hitZ) {
		if (!world.isRemote) {
			BlockEntity ent = world.getTileEntity(pos);
			if (ent instanceof TileEntityOverheadDoor) {
				((TileEntityOverheadDoor) ent)
						.setController(NAMES[player.getHeldItem(hand).getItemDamage() % NAMES.length]);
				if (!player.capabilities.isCreativeMode)
					player.getHeldItem(hand).shrink(1);
			} else
				return InteractionResult.PASS;
		}
		return InteractionResult.SUCCESS;
	}
}
