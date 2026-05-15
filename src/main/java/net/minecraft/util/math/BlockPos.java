package net.minecraft.util.math;

public class BlockPos extends net.minecraft.core.BlockPos {
	public BlockPos(int x, int y, int z) {
		super(x, y, z);
	}

	public BlockPos(double x, double y, double z) {
		super((int) x, (int) y, (int) z);
	}
}
