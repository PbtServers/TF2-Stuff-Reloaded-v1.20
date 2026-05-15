package rafradek.tf2weapons.tileentity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public abstract class TileEntityAbstractAmmoFurnace extends TileEntityLockable {
	protected TileEntityAbstractAmmoFurnace(BlockPos pos, BlockState state) {
		super(null, pos, state);
	}
}
