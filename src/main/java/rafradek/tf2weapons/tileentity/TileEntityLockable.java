package rafradek.tf2weapons.tileentity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntityLockable {
	protected TileEntityLockable(BlockEntityType<?> type, BlockPos pos, BlockState state) {}

	public Component getDisplayName() {
		return Component.empty();
	}
}
