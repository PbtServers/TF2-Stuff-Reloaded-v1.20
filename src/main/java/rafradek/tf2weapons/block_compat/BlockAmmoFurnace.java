package rafradek.tf2weapons.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class BlockAmmoFurnace extends Block {
	public BlockAmmoFurnace() {
		this(false);
	}

	public BlockAmmoFurnace(boolean electric) {
		super(BlockBehaviour.Properties.of());
	}

	public static void setState(boolean active, Level world, BlockPos pos) {}
}
