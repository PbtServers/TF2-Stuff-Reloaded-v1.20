package rafradek.tf2weapons.tileentity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntityCapturePoint extends BlockEntity implements IEntityConfigurable {
	public float captureProgress;
	private final EntityOutputManager outputManager = new EntityOutputManager(this);

	public TileEntityCapturePoint(BlockPos pos, BlockState state) {
		super(null, pos, state);
	}

	public CompoundTag writeConfig(CompoundTag tag) { return tag; }
	public void readConfig(CompoundTag tag) {}
	public EntityOutputManager getOutputManager() { return outputManager; }
	public String[] getOutputs() { return new String[0]; }
}
