package rafradek.tf2weapons.block;

import net.minecraft.world.level.block.Block;

public class BlockEventReceiver extends Block {

	public BlockEventReceiver() {
		super(BlockBehaviour.Properties.of().strength(0.3F).lightLevel(state -> 15));
	}

}
