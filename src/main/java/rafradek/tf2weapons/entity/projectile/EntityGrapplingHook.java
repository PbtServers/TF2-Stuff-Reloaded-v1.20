package rafradek.tf2weapons.entity.projectile;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.level.Level;
import rafradek.tf2weapons.TF2weapons;

public class EntityGrapplingHook extends EntityProjectileSimple {

	public boolean sticked;

	public Entity stickedEntity;

	public double relativeX;
	public double relativeY;
	public double relativeZ;

	public EntityGrapplingHook(Level world) {
		super(world);
	}

	@Override
	public void onHitGround(int x, int y, int z, HitResult mop) {
		if (!sticked && !this.world.isAirBlock(mop.getBlockPos())) {
			this.setPosition(mop.hitVec.x + mop.sideHit.getFrontOffsetX() * 0.1,
					mop.hitVec.y + mop.sideHit.getFrontOffsetY() * 0.1f,
					mop.hitVec.z + mop.sideHit.getFrontOffsetZ() * 0.1);
			this.sticked = true;
			this.stickedBlock = mop.getBlockPos();
		}
	}

	@Override
	public void onHitMob(Entity entityHit, HitResult mop) {
		if (!sticked) {
			super.onHitMob(entityHit, mop);
			this.setPosition(mop.hitVec.x + mop.sideHit.getFrontOffsetX() * 0.1,
					mop.hitVec.y + mop.sideHit.getFrontOffsetY() * 0.1f,
					mop.hitVec.z + mop.sideHit.getFrontOffsetZ() * 0.1);
			this.relativeX = this.posX - entityHit.posX;
			this.relativeY = this.posY - entityHit.posY;
			this.relativeZ = this.posZ - entityHit.posZ;
			this.sticked = true;
			this.stickedEntity = entityHit;
		}
	}

	@Override
	public void onUpdate() {
		if (this.ticksExisted > this.getMaxTime() || (!this.world.isRemote && this.sticked
				&& (stickedBlock == null || this.world.isAirBlock(stickedBlock))
				&& (this.stickedEntity == null || !this.stickedEntity.isEntityAlive()))) {
			this.setDead();
			return;
		} else if (this.stickedEntity != null) {
			if (!this.world.isRemote && this.stickedEntity instanceof LivingEntity && this.ticksExisted % 10 == 0) {
				((LivingEntity) this.stickedEntity).addPotionEffect(new MobEffectInstance(TF2weapons.bleeding, 10));
			}
			this.setPosition(relativeX + this.stickedEntity.posX, relativeY + this.stickedEntity.posY,
					relativeZ + this.stickedEntity.posZ);
		} else if (!this.sticked)
			super.onUpdate();
	}

	@Override
	public boolean canPenetrate() {
		return true;
	}

	@Override
	public boolean isPushable() {
		return false;
	}

	@Override
	public double getGravity() {
		return 0;
	}

}
