package rafradek.tf2weapons.tileentity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntityRobotDeploy extends BlockEntity {
	public static final int[] NORMAL_REQUIRE = { 1, 1, 1, 1 };
	public static final int[] GIANT_REQUIRE = { 2, 2, 2, 2 };
	public boolean joined;

	public TileEntityRobotDeploy(BlockPos pos, BlockState state) {
		super(null, pos, state);
	}

	public void dropInventory() {}
	public void readFromNBT(CompoundTag tag) {}
	public CompoundTag writeToNBT(CompoundTag tag) { return tag; }
	public void setOwner(Component name, Object uuid) {}
}
