package rafradek.tf2weapons.entity.ai;

import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

/**
 * Temporary bridge for legacy 1.12 AI tasks while porting to 1.20 goals.
 */
public abstract class EntityAIBase extends Goal {
	public void setMutexBits(int mutexBits) {
		EnumSet<Flag> flags = EnumSet.noneOf(Flag.class);
		if ((mutexBits & 1) != 0) {
			flags.add(Flag.MOVE);
		}
		if ((mutexBits & 2) != 0) {
			flags.add(Flag.LOOK);
		}
		if ((mutexBits & 4) != 0) {
			flags.add(Flag.JUMP);
		}
		if ((mutexBits & 8) != 0) {
			flags.add(Flag.TARGET);
		}
		this.setFlags(flags);
	}

	public boolean shouldExecute() {
		return false;
	}

	public boolean shouldContinueExecuting() {
		return this.shouldExecute();
	}

	public void startExecuting() {
	}

	public void resetTask() {
	}

	public void updateTask() {
	}

	@Override
	public boolean canUse() {
		return this.shouldExecute();
	}

	@Override
	public boolean canContinueToUse() {
		return this.shouldContinueExecuting();
	}

	@Override
	public void start() {
		this.startExecuting();
	}

	@Override
	public void stop() {
		this.resetTask();
	}

	@Override
	public void tick() {
		this.updateTask();
	}
}
