package rafradek.tf2weapons.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;

public class BlockOverheadDoor extends Block {
	public static final DirectionProperty FACING = DirectionProperty.create("facing");
	public static final BooleanProperty HOLDER = BooleanProperty.create("holder");
	public static final BooleanProperty SLIDING = BooleanProperty.create("sliding");

	public BlockOverheadDoor() {
		super(BlockBehaviour.Properties.of());
	}
}
