package rafradek.tf2weapons.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;

public class BlockRobotDeploy extends Block {
	public static final DirectionProperty FACING = DirectionProperty.create("facing");
	public static final BooleanProperty HOLDER = BooleanProperty.create("holder");
	public static final BooleanProperty JOINED = BooleanProperty.create("joined");

	public BlockRobotDeploy() {
		super(BlockBehaviour.Properties.of());
	}
}
