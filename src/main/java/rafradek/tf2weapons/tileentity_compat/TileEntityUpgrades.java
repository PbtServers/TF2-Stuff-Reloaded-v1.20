package rafradek.tf2weapons.tileentity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import rafradek.tf2weapons.TF2weapons;

public class TileEntityUpgrades extends BlockEntity {
	public static final int UPGRADES_COUNT = 0;

	public TileEntityUpgrades() {
		this(BlockPos.ZERO, TF2weapons.blockUpgradeStation.defaultBlockState());
	}

	public TileEntityUpgrades(Level world) {
		this();
	}

	public TileEntityUpgrades(BlockPos pos, BlockState state) {
		super(null, pos, state);
	}

	public void readFromNBT(CompoundTag tag) {}
	public CompoundTag writeToNBT(CompoundTag tag) { return tag; }
}
