package rafradek.tf2weapons.tileentity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntityAmmoFurnace extends TileEntityAbstractAmmoFurnace {
	public TileEntityAmmoFurnace(BlockPos pos, BlockState state) {
		super(pos, state);
	}

	public void setCustomInventoryName(Component name) {}
}
