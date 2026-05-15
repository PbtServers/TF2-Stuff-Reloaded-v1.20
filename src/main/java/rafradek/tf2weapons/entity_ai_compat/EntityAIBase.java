package rafradek.tf2weapons.entity.ai;

import java.util.EnumSet;

import net.minecraft.world.entity.ai.goal.Goal;

public class EntityAIBase extends Goal {
	public EntityAIBase() {
		this.setFlags(EnumSet.noneOf(Goal.Flag.class));
	}

	public void setMutexBits(int bits) {}

	public boolean shouldExecute() { return false; }
	public boolean shouldContinueExecuting() { return false; }
	public void startExecuting() {}
	public void resetTask() {}
	public void updateTask() {}

	@Override
	public boolean canUse() {
		return shouldExecute();
	}

	@Override
	public boolean canContinueToUse() {
		return shouldContinueExecuting();
	}

	@Override
	public void start() {
		startExecuting();
	}

	@Override
	public void stop() {
		resetTask();
	}

	@Override
	public void tick() {
		updateTask();
	}
}
