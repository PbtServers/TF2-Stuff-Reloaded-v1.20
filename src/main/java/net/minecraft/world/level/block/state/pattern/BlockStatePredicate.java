package net.minecraft.world.level.block.state.pattern;

public class BlockStatePredicate {
	public static BlockStatePredicate forBlock(Object block) {
		return new BlockStatePredicate();
	}
}
