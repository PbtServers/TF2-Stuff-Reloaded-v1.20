package rafradek.tf2weapons.entity.ai;

import net.minecraft.world.entity.PathfinderMob;
import rafradek.tf2weapons.entity.ai.EntityAIBase;
import rafradek.tf2weapons.entity.ai.RandomPositionGenerator;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

public class EntityAIMoveTowardsRestriction2 extends EntityAIBase {

	private final PathfinderMob creature;
	private double movePosX;
	private double movePosY;
	private double movePosZ;
	private final double movementSpeed;

	public EntityAIMoveTowardsRestriction2(PathfinderMob creatureIn, double speedIn) {
		this.creature = creatureIn;
		this.movementSpeed = speedIn;
		this.setMutexBits(1);
	}

	/**
	 * Returns whether the EntityAIBase should begin execution.
	 */
	@Override
	public boolean shouldExecute() {

		if (this.creature.isWithinHomeDistanceCurrentPosition()) {
			return false;
		} else {

			BlockPos blockpos = this.creature.getHomePosition();

			Vec3 Vec3 = this.creature.getMaximumHomeDistance() == 0 ? new Vec3(blockpos)
					: RandomPositionGenerator.findRandomTargetBlockTowards(this.creature,
							(int) Math.min(this.creature.getMaximumHomeDistance(), 16), 7,
							new Vec3(blockpos.getX(), blockpos.getY(), blockpos.getZ()));

			if (Vec3 == null) {

				return false;
			} else {
				this.movePosX = Vec3.x;
				this.movePosY = Vec3.y;
				this.movePosZ = Vec3.z;
				return true;
			}
		}
	}

	/**
	 * Returns whether an in-progress EntityAIBase should continue executing
	 */
	@Override
	public boolean shouldContinueExecuting() {
		return !this.creature.getNavigator().noPath();
	}

	/**
	 * Execute a one shot task or start executing a continuous task
	 */
	@Override
	public void startExecuting() {
		this.creature.getNavigator().tryMoveToXYZ(this.movePosX, this.movePosY, this.movePosZ, this.movementSpeed);
	}

}
