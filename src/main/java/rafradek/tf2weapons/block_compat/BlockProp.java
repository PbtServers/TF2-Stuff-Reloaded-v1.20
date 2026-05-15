package rafradek.tf2weapons.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public class BlockProp extends Block {
	public BlockProp() {
		super(BlockBehaviour.Properties.of());
	}

	public BlockProp(BlockBehaviour.Properties properties) {
		super(properties);
	}

	public BlockProp(BlockBehaviour.Properties properties, MapColor color) {
		super(properties);
	}
}
