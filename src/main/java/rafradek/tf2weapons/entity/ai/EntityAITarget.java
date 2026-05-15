package rafradek.tf2weapons.entity.ai;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;

public abstract class EntityAITarget extends EntityAIBase {
	protected final PathfinderMob taskOwner;
	protected final boolean shouldCheckSight;
	protected final boolean nearbyOnly;

	public EntityAITarget(PathfinderMob taskOwner, boolean shouldCheckSight) {
		this(taskOwner, shouldCheckSight, false);
	}

	public EntityAITarget(PathfinderMob taskOwner, boolean shouldCheckSight, boolean nearbyOnly) {
		this.taskOwner = taskOwner;
		this.shouldCheckSight = shouldCheckSight;
		this.nearbyOnly = nearbyOnly;
		this.setMutexBits(1);
	}

	protected double getTargetDistance() {
		return 16.0D;
	}

	protected boolean isSuitableTarget(LivingEntity target, boolean includeInvincibles) {
		return target != null && target != this.taskOwner && target.isAlive();
	}
}
