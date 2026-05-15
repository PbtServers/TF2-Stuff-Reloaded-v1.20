package rafradek.tf2weapons.entity.projectile;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import rafradek.tf2weapons.entity.building.EntitySentry;

import java.util.HashSet;
import java.util.function.Predicate;

public class EntityProjectileBase extends Entity {
	public HashSet<Entity> hitEntities = new HashSet<>();
	public LivingEntity shootingEntity;
	public ItemStack usedWeapon = ItemStack.EMPTY;
	public ItemStack usedWeaponOrig = ItemStack.EMPTY;
	public double gravity = 0.05;
	public float health = 4f;
	public float distanceTravelled;
	public EntitySentry sentry;
	public boolean reflected;
	public boolean infinite;
	public double cachedGravity = -1;
	public float damageModifier = 1f;
	public float chargeLevel;
	public Entity homingTarget;
	public float homingAngle;
	public float homingSpeed;
	private int critical;
	private int type;
	private boolean sticked;
	private boolean penetrate;

	public EntityProjectileBase(Level level) {
		super(EntityType.SNOWBALL, level);
	}

	public void initProjectile(LivingEntity shooter, net.minecraft.world.InteractionHand hand, ItemStack weapon) {
		this.shootingEntity = shooter;
		this.usedWeapon = weapon;
		this.usedWeaponOrig = weapon.copy();
	}

	@Override
	protected void defineSynchedData() {
	}

	@Override
	protected void readAdditionalSaveData(CompoundTag tag) {
	}

	@Override
	protected void addAdditionalSaveData(CompoundTag tag) {
	}

	public float getPitchAddition() { return 0; }
	public boolean isImmuneToExplosions() { return true; }
	public void shoot(double x, double y, double z, float speed, float spread) {}
	public void face(double x, double y, double z, float speedmult) {}
	public void face(LivingEntity target, float speedmult) {}
	public void setVelocity(double x, double y, double z) { setDeltaMovement(x, y, z); }
	public void explode(double x, double y, double z, Entity direct, float damageMult) {}
	public SoundEvent getExplosionSound() { return null; }
	public void addDamageTypes(DamageSource source) {}
	public boolean attackDirect(Entity target, double pushForce, boolean headshot, Vec3 hitPos) { return false; }
	public float getDistanceToTarget(Entity target, double x, double y, double z) { return distanceTo(target); }
	public Entity changeDimension(int dimensionId) { return this; }
	public float getExplosionSize() { return 0; }
	public void trace() {}
	protected Predicate<Entity> getCollisionPredicate() { return entity -> true; }
	public void onUpdate() { tick(); }
	public void setPositionAndRotationDirect(double x, double y, double z, float yaw, float pitch, int increments, boolean teleport) {
		moveTo(x, y, z, yaw, pitch);
	}
	public void move(MoverType type, double x, double y, double z) { move(type, new Vec3(x, y, z)); }
	public void writeEntityToNBT(CompoundTag tag) {}
	public void readEntityFromNBT(CompoundTag tag) {}
	public void onHitGround(int x, int y, int z, HitResult mop) {}
	public void onHitMob(Entity entityHit, HitResult mop) {}
	protected boolean canTriggerWalking() { return false; }
	public boolean moveable() { return true; }
	public float getHomingAngle() { return homingAngle; }
	public float getHomingTurnSpeed() { return homingSpeed; }
	public void setCritical(int critical) { this.critical = critical; }
	public void setSticked(boolean stick) { this.sticked = stick; }
	public int getCritical() { return critical; }
	public boolean isSticked() { return sticked; }
	public boolean canPenetrate() { return penetrate; }
	protected float getSpeed() { return 1; }
	public double getGravity() { return gravity; }
	public double getGravityOverride() { return cachedGravity; }
	public Entity getThrower() { return shootingEntity; }
	public void setThrower(Entity entity) { if (entity instanceof LivingEntity living) this.shootingEntity = living; }
	public void setType(int type) { this.type = type; }
	public void setPenetrate() { this.penetrate = true; }
	public boolean isSticky() { return false; }
	public void onHitBlockX() {}
	public void onHitBlockY(Block block) {}
	public void onHitBlockZ() {}
	public void spawnParticles(double x, double y, double z) {}
	public int getMaxTime() { return 200; }
	@Override
	public boolean saveAsPassenger(CompoundTag tag) { return true; }
	public boolean writeToNBTOptional(CompoundTag tag) { return true; }
	public boolean useCollisionBox() { return false; }
	public float getCollisionSize() { return 0.25f; }
	@Override
	public boolean isPushable() { return false; }
	public boolean attackEntityFrom(DamageSource source, float damage) { return hurt(source, damage); }
	@Override
	public boolean shouldRenderAtSqrDistance(double distance) { return true; }
	public boolean isInRangeToRenderDist(double distance) { return true; }
	public void makeLit() {}
	public Entity getAttachmentEntity() { return null; }
	public int getLightLevel() { return 0; }
	public void writeSpawnData(io.netty.buffer.ByteBuf buffer) {}
	public void readSpawnData(io.netty.buffer.ByteBuf buffer) {}
}
