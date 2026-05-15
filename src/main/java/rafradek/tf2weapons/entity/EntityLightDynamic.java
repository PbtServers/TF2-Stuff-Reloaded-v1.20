package rafradek.tf2weapons.entity;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class EntityLightDynamic extends Entity {

	public int timeToLive;
	public Entity parent;

	public EntityLightDynamic(Level world) {
		super(world);
	}

	public EntityLightDynamic(Level world, Entity parent, int timeToLive) {
		super(world);
		this.setPosition(parent.posX, parent.posY + parent.getEyeHeight(), parent.posZ);
		this.parent = parent;
		this.timeToLive = timeToLive;
	}

	public EntityLightDynamic(Level world, BlockPos pos, int timeToLive) {
		super(world);
		this.setPosition(pos.getX(), pos.getY(), pos.getZ());
		this.timeToLive = timeToLive;
	}

	@Override
	protected void entityInit() {}

	@Override
	public void onUpdate() {
		super.onUpdate();
		if (this.parent != null)
			this.setPosition(this.parent.posX, this.parent.posY + this.parent.getEyeHeight(), this.parent.posZ);
		this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);
		if (this.ticksExisted >= timeToLive || (this.parent != null && this.parent.isDead)) {
			this.setDead();
		}
	}

	@Override
	protected void readEntityFromNBT(CompoundTag compound) {}

	@Override
	protected void writeEntityToNBT(CompoundTag compound) {}

	@Override
	@OnlyIn(Dist.CLIENT)
	public boolean isInRangeToRender3d(double x, double y, double z) {
		return false;
	}
}
