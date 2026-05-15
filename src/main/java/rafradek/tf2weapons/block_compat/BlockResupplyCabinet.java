package rafradek.tf2weapons.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

public class BlockResupplyCabinet extends Block {
	public static final BooleanProperty HOLDER = BooleanProperty.create("holder");

	public BlockResupplyCabinet() {
		super(BlockBehaviour.Properties.of());
	}
}
