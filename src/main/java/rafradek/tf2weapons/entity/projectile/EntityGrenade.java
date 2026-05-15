package rafradek.tf2weapons.entity.projectile;


import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.level.Level;
import rafradek.tf2weapons.client.audio.TF2Sounds;
import rafradek.tf2weapons.common.TF2Attribute;
import rafradek.tf2weapons.item.ItemWeapon;

public class EntityGrenade extends EntityProjectileBase {

	public boolean hitGround;

	public int fuse = 46;
	private static final EntityDataAccessor<Byte> BOMB = SynchedEntityData.createKey(EntityGrenade.class,
			EntityDataSerializers.BYTE);
	private static final EntityDataAccessor<Boolean> BURST = SynchedEntityData.createKey(EntityGrenade.class,
			EntityDataSerializers.BOOLEAN);

	public EntityGrenade(Level p_i1756_1_) {
		super(p_i1756_1_);
		this.setSize(0.3f, 0.3f);
	}

	@Override
	public void initProjectile(LivingEntity shooter, InteractionHand hand, ItemStack weapon) {
		super.initProjectile(shooter, hand, weapon);
		this.setSize(0.3f, 0.3f);
		int weaponmode = (int) TF2Attribute.getModifier("Weapon Mode", this.usedWeapon, 0, shooter);
		if (weaponmode == 1) {
			this.setBomb(1);
			this.setSize(0.7f, 0.7f);
			this.fuse = 26 + this.rand.nextInt(20);
			double motion = 0.8f + this.rand.nextDouble() * 0.55;
			this.motionX *= motion;
			this.motionY *= motion;
			this.motionZ *= motion;
		} else if (weaponmode == 2) {
			this.setBomb(2);
			this.fuse = (int) (20 - ((ItemWeapon) weapon.getItem()).getCharge(shooter, weapon) * 20);
		} else {
			this.fuse = (int) (TF2Attribute.getModifier("Fuse Time", weapon, 2.3f, shooter) * 20);
		}
	}

	@Override
	protected void entityInit() {
		super.entityInit();
		this.dataManager.register(BOMB, (byte) 0);
		this.dataManager.register(BURST, false);
	}

	public void setBomb(int val) {
		this.dataManager.set(BOMB, (byte) val);
	}

	public int getBomb() {
		return this.dataManager.get(BOMB);
	}

	public void setBurst(boolean burst) {
		this.dataManager.set(BURST, burst);
	}

	public boolean isBurst() {
		return this.dataManager.get(BURST);
	}

	@Override
	public float getPitchAddition() {
		return -3;
	}

	@Override
	public float getExplosionSize() {
		return 3.05f;
	}

	@Override
	public void onHitGround(int x, int y, int z, HitResult mop) {

	}

	@Override
	public SoundEvent getExplosionSound() {
		return this.isBurst() ? TF2Sounds.GRENADE_EXPLODESPECIAL : super.getExplosionSound();
	}

	@Override
	public void explode(double x, double y, double z, Entity direct, float damageMult) {
		super.explode(x, y, z, direct, damageMult);
		if (world.isRemote || this.shootingEntity == null)
			return;
		if (!this.isBurst()) {
			int grenadeSpecialist = (int) TF2Attribute.getModifier("Grenade Specialist", this.usedWeapon, 0f,
					shootingEntity);
			int grenadeMult = (int) (2 * (direct == null ? 1.5f : 1));
			double rndSpread = direct == null ? 0.4f : 0.1f;
			for (int i = 0; i < grenadeSpecialist * grenadeMult; i++) {
				EntityGrenade burst = new EntityGrenade(world);
				burst.initProjectile(this.shootingEntity, InteractionHand.MAIN_HAND, usedWeaponOrig);
				burst.setPosition(x, y, z);
				double motionmult = 1D + (this.rand.nextDouble() * 0.3 * grenadeSpecialist) - 0.15 * grenadeSpecialist;
				if (direct == null)
					motionmult += 0.25D + (this.rand.nextDouble() * 0.1 * grenadeSpecialist);
				burst.shoot(-this.motionX * motionmult * 0.07 + this.rand.nextDouble() * rndSpread * 2 - rndSpread, 0.2,
						-this.motionZ * motionmult * 0.07 + this.rand.nextDouble() * rndSpread * 2 - rndSpread,
						(float) motionmult * 0.3f, 0f);
				burst.setBurst(true);
				burst.damageModifier = this.damageModifier * 0.5f;
				this.world.spawnEntity(burst);
			}
		}

	}

	@Override
	public void onHitMob(Entity entityHit, HitResult mop) {
		if (!this.hitGround && !this.isBurst()) {
			if (getBomb() == 0) {
				this.explode(mop.hitVec.x, mop.hitVec.y, mop.hitVec.z, mop.entityHit, 1);
			} else if (!this.hitEntities.contains(entityHit)) {

				if (this.attackDirect(entityHit, 1, mop.hitInfo instanceof Boolean ? (Boolean) mop.hitInfo : false,
						mop.hitVec) && entityHit instanceof LivingEntity) {
					((LivingEntity) entityHit).addPotionEffect(new MobEffectInstance(MobEffects.SLOWNESS, 20, 3));

				}

				/*
				 * if(mop.sideHit==Direction.EAST || mop.sideHit==Direction.WEST)
				 * this.onHitBlockX(); else if(mop.sideHit==Direction.NORTH ||
				 * mop.sideHit==Direction.SOUTH) this.onHitBlockZ(); else
				 * this.onHitBlockY(null);
				 */
				this.motionX *= 0.6;
				this.motionY *= 0.65;
				this.motionZ *= 0.6;
			}
		}
	}

	public double maxMotion() {
		return Math.max(this.motionX, Math.max(this.motionY, this.motionZ));
	}

	@Override
	public void onUpdate() {
		super.onUpdate();
		this.fuse--;
		if (this.fuse <= 0)
			this.explode(this.posX, this.posY + this.height / 2, this.posZ, null, this.getBomb() > 0 ? 1 : 0.64f);
		if (this.collided && !this.hitGround) {
			this.hitGround = true;
			if (!this.world.isRemote) {
				int attr = (int) TF2Attribute.getModifier("Coll Remove", this.usedWeapon, 0, this.shootingEntity);
				if (attr == 2 || this.isBurst())
					this.explode(this.posX, this.posY, this.posZ, null, this.getBomb() > 0 ? 1 : 0.64f);
				if (attr == 1)
					this.setDead();
			}
		}
	}

	@Override
	public void spawnParticles(double x, double y, double z) {

	}

	@Override
	protected float getSpeed() {
		return 1.16205f;
	}

	@Override
	public double getGravity() {
		return 0.0381f;
	}

	@Override
	public boolean useCollisionBox() {
		return true;
	}

	@Override
	public void onHitBlockX() {
		this.motionX = -this.motionX * 0.18;
		this.motionY = this.motionY * 0.8;
		this.motionZ = this.motionZ * 0.8;
	}

	@Override
	public void onHitBlockY(Block block) {
		this.motionX = this.motionX * 0.8;
		this.motionY = -this.motionY * 0.18;
		this.motionZ = this.motionZ * 0.8;
	}

	@Override
	public void onHitBlockZ() {
		this.motionX = this.motionX * 0.8;
		this.motionY = this.motionY * 0.8;
		this.motionZ = -this.motionZ * 0.18;
	}

	@Override
	public void notifyDataManagerChange(EntityDataAccessor<?> key) {
		if (BOMB.equals(key) && this.getBomb() == 1) {
			this.setSize(0.7f, 0.7f);
		}
	}
}
