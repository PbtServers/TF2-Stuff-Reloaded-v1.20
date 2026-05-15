package rafradek.tf2weapons.entity.ai;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;

public class EntityAITarget extends EntityAIBase {
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
	}

	public static boolean isSuitableTarget(PathfinderMob owner, LivingEntity target, boolean includeInvincibles,
			boolean checkSight) {
		return target != null && target.isAlive();
	}
}
