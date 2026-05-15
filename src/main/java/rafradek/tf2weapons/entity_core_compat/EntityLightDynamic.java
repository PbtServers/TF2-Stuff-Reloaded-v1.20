package rafradek.tf2weapons.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class EntityLightDynamic extends Entity {
	public Entity parent;
	public int timeToLive;

	public EntityLightDynamic(Level level) {
		super(EntityType.MARKER, level);
	}

	public EntityLightDynamic(Level level, Entity parent, int timeToLive) {
		this(level);
		this.parent = parent;
		this.timeToLive = timeToLive;
	}

	public EntityLightDynamic(Level level, BlockPos pos, int timeToLive) {
		this(level);
		this.timeToLive = timeToLive;
		moveTo(pos.getX(), pos.getY(), pos.getZ());
	}

	@Override
	protected void defineSynchedData() {}

	@Override
	protected void readAdditionalSaveData(CompoundTag tag) {}

	@Override
	protected void addAdditionalSaveData(CompoundTag tag) {}
}
