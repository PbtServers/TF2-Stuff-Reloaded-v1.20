package rafradek.tf2weapons.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

public class BlockUpgradeStation extends Block {
	public static final BooleanProperty HOLDER = BooleanProperty.create("holder");
	public static final BooleanProperty PLACED = BooleanProperty.create("placed");

	public BlockUpgradeStation() {
		super(BlockBehaviour.Properties.of());
	}
}
