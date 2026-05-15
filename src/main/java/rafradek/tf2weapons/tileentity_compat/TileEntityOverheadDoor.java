package rafradek.tf2weapons.tileentity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntityOverheadDoor extends BlockEntity {
	public enum Allow {
		PLAYERS,
		MOBS,
		RED,
		BLU
	}

	public boolean powered;
	public boolean master;
	public boolean entitySome;
	public float amountScrolled;
	public BlockPos minBounds = BlockPos.ZERO;
	public BlockPos maxBounds = BlockPos.ZERO;

	public TileEntityOverheadDoor(BlockPos pos, BlockState state) {
		super(null, pos, state);
	}

	public void setController(String controller) {}
	public void updateMasterStatus() {}
	public boolean isPowered() { return powered; }
}
