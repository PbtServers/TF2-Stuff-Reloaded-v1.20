package rafradek.tf2weapons.entity.projectile;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.level.Level;

public class EntityCleaver extends EntityProjectileSimple {

	public EntityCleaver(Level world) {
		super(world);
		this.setType(2);
	}

	@Override
	public void initProjectile(LivingEntity shooter, InteractionHand hand, ItemStack weapon) {
		super.initProjectile(shooter, hand, weapon);
		this.usedWeapon.setCount(1);
	}

	@Override
	public void onHitGround(int x, int y, int z, HitResult mop) {
		super.onHitGround(x, y, z, mop);
		if (!this.world.isRemote && this.damage <= 0 && !this.infinite) {
			this.entityDropItem(this.usedWeapon, 0f);
		}
	}

	@Override
	public void onHitMob(Entity entityHit, HitResult mop) {
		super.onHitMob(entityHit, mop);
		if (!this.world.isRemote && this.isDead && !this.infinite) {
			if (entityHit.isEntityAlive()) {
				ListTag list = entityHit.getEntityData().getTagList("Cleavers", 10);
				list.appendTag(this.usedWeapon.serializeNBT());
				if (!entityHit.getEntityData().hasKey("Cleavers"))
					entityHit.getEntityData().setTag("Cleavers", list);
			} else
				this.entityDropItem(this.usedWeapon, 0f);
		}
	}

	@Override
	public double getGravity() {
		return 0.05f;
	}
}
