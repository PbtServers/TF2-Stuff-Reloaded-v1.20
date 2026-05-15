package rafradek.tf2weapons.entity.projectile;


import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import rafradek.tf2weapons.TF2weapons;

public class EntityBall extends EntityProjectileSimple {

	public Vec3 throwPos;
	public boolean canBePickedUp;

	public EntityBall(Level world) {
		super(world);
		this.throwPos = new Vec3(0, 0, 0);
	}

	@Override
	public void initProjectile(LivingEntity shooter, InteractionHand hand, ItemStack weapon) {
		super.initProjectile(shooter, hand, weapon);
		this.throwPos = this.getPositionVector();
	}

	/*
	 * @Override public void onHitGround(int x, int y, int z, HitResult mop) {
	 * if(!this.canBePickedUp){ super.onHitGround(x, y, z, mop); } }
	 */
	@Override
	public boolean useCollisionBox() {
		return true;
	}

	@Override
	public void onHitBlockX() {
		this.canBePickedUp = true;
		this.motionX = -this.motionX * 0.12;
		this.motionY = this.motionY * 0.3;
		this.motionZ = this.motionZ * 0.3;
	}

	@Override
	public void onHitBlockY(Block block) {
		this.canBePickedUp = true;
		this.motionX = this.motionX * 0.3;
		this.motionY = -this.motionY * 0.12;
		this.motionZ = this.motionZ * 0.3;
	}

	@Override
	public void onHitBlockZ() {
		this.canBePickedUp = true;
		this.motionX = this.motionX * 0.3;
		this.motionY = this.motionY * 0.3;
		this.motionZ = -this.motionZ * 0.12;
	}

	@Override
	public void onCollideWithPlayer(Player entityIn) {
		if (!this.world.isRemote && this.canBePickedUp) {
			if (!this.infinite && entityIn.inventory.addItemStackToInventory(new ItemStack(TF2weapons.itemAmmo, 1, 14)))
				this.setDead();

		}
	}

	/*
	 * @Override public void onHitMob(Entity entityHit, HitResult mop) {
	 * super.onHitMob(entityHit, mop); if(!this.world.isRemote){
	 *
	 * } }
	 */
	@Override
	public void setDead() {
		if (this.impact) {
			this.impact = false;
			this.canBePickedUp = true;
		} else
			super.setDead();
	}
}
