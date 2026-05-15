package rafradek.tf2weapons.entity.ai;

import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.phys.Vec3;

public final class RandomPositionGenerator {
	private RandomPositionGenerator() {
	}

	public static Vec3 findRandomTarget(PathfinderMob mob, int horizontalRange, int verticalRange) {
		return DefaultRandomPos.getPos(mob, horizontalRange, verticalRange);
	}

	public static Vec3 findRandomTargetBlockTowards(PathfinderMob mob, int horizontalRange, int verticalRange, Vec3 target) {
		return DefaultRandomPos.getPosTowards(mob, horizontalRange, verticalRange, target, Math.PI / 2D);
	}
}
