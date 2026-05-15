package rafradek.tf2weapons.entity.projectile;


import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import rafradek.tf2weapons.util.EnumParticleTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import rafradek.tf2weapons.client.ClientProxy;
import rafradek.tf2weapons.item.ItemFromData;
import rafradek.tf2weapons.item.ItemUsable;
import rafradek.tf2weapons.util.PropertyType;
import rafradek.tf2weapons.util.TF2Util;

public class EntityJar extends EntityProjectileBase {

	public int gasExplodeCooldown = 40;

	public EntityJar(Level p_i1756_1_) {
		super(p_i1756_1_);
	}

	@Override
	public void initProjectile(LivingEntity shooter, InteractionHand hand, ItemStack weapon) {
		String name = ItemFromData.getData(weapon).getString(PropertyType.PROJECTILE);
		if (name.equals("gas"))
			this.setType(1);
		super.initProjectile(shooter, hand, weapon);
	}

	@Override
	public void explode(double x, double y, double z, Entity direct, float power) {
		if (!this.world.isRemote) {
			int coatedCount = 0;
			for (LivingEntity living : this.world.getEntitiesWithinAABB(LivingEntity.class,
					this.getEntityBoundingBox().grow(5, 5, 5)))
				if (!this.hitEntities.contains(living)) {
					if (living.canBeHitWithPotion() && living.getDistanceSq(this) < 25 && living != this.shootingEntity
							&& !TF2Util.isOnSameTeam(this.shootingEntity, living)) {
						living.addPotionEffect(new MobEffectInstance(
								Potion.getPotionFromResourceLocation(
										ItemFromData.getData(this.usedWeapon).getString(PropertyType.EFFECT_TYPE)),
								300));
						if (TF2Util.isEnemy(this.shootingEntity, living))
							coatedCount++;
					} else
						living.extinguish();

					this.hitEntities.add(living);
				}
			/*
			 * if(coatedCount>=5 && this.shootingEntity instanceof Player)
			 * ((Player)
			 * this.shootingEntity).addStat(TF2Achievements.JARATE_MULTIPLE);
			 */
			if (!this.isGasExploded()) {
				this.playSound(ItemUsable.getSound(usedWeapon, PropertyType.EXPLOSION_SOUND), 1f, 1f);
				// this.playSound(TF2Sounds.JAR_EXPLODE, 1.5f, 1f);
				this.world.playEvent(2002, new BlockPos(this),
						Potion.getPotionFromResourceLocation(
								ItemFromData.getData(this.usedWeapon).getString(PropertyType.EFFECT_TYPE))
								.getLiquidColor());
			}
			if (this.isGas()) {
				this.motionX = 0.;
				this.motionZ = 0.;
				this.setType(2);
			} else
				this.setDead();
		}
	}

	@Override
	public void onHitGround(int x, int y, int z, HitResult mop) {
		this.explode(mop.hitVec.x + mop.sideHit.getFrontOffsetX() * 0.05,
				mop.hitVec.y + mop.sideHit.getFrontOffsetY() * 0.05,
				mop.hitVec.z + mop.sideHit.getFrontOffsetZ() * 0.05, null, 1f);
		if (this.isGas()) {
			this.motionX = 0;
			this.motionY = 0;
			this.motionZ = 0;
			this.posX = mop.hitVec.x;
			this.posY = mop.hitVec.y;
			this.posZ = mop.hitVec.z;
			if (mop.sideHit == Direction.UP) {
				this.setSticked(true);
			}

		}
	}

	@Override
	public void onHitMob(Entity entityHit, HitResult mop) {
		this.explode(mop.hitVec.x, mop.hitVec.y, mop.hitVec.z, mop.entityHit, 1f);
	}

	@Override
	public void onUpdate() {
		super.onUpdate();
		if (this.isGasExploded()) {
			this.explode(this.posX, this.posY, this.posZ, null, 1f);
			if (this.gasExplodeCooldown-- <= 0) {
				this.setDead();
			}
		}
	}

	public boolean isGas() {
		return this.getType() == 1 || this.getType() == 2;
	}

	public boolean isGasExploded() {
		return this.getType() == 2;
	}

	@Override
	public void spawnParticles(double x, double y, double z) {
		if (this.isInWater())
			this.world.spawnParticle(EnumParticleTypes.WATER_BUBBLE, x, y, z, this.motionX, this.motionY, this.motionZ);
		if (this.isGasExploded()) {
			for (int i = 0; i < 8; ++i) {
				Vec3 pos = TF2Util.radiusRandom2D(5f, this.rand);
				double xa = this.posX + pos.x;
				double ya = this.posY + 0.5;
				double za = this.posZ + pos.y;
				ClientProxy.spawnGasSmokeParticle(world, xa, ya, za, 0x2B3E00);
			}

		}
	}

	@Override
	protected float getSpeed() {
		return 1.04f;
	}

}
