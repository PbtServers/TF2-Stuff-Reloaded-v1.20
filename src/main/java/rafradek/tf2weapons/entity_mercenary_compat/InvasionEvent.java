package rafradek.tf2weapons.entity.mercenary;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class InvasionEvent {
	public static final int[] DIFFICULTY = { 0, 1, 2 };

	public InvasionEvent(Level world, BlockPos pos, int difficulty) {}

	public void finish() {}
	public void tick() {}
}
