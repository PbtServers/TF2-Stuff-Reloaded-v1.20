package rafradek.tf2weapons.potion;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.damagesource.DamageSource;
import rafradek.tf2weapons.util.EnumParticleTypes;
import net.minecraft.server.level.ServerLevel;
import rafradek.tf2weapons.TF2weapons;
import rafradek.tf2weapons.util.ReflectionAccess;

public class PotionTF2 extends Potion {

	public PotionTF2(boolean isBadEffectIn, int liquidColorIn, int x, int y) {
		super(isBadEffectIn, liquidColorIn);
		this.setIconIndex(x, y);

	}

	@Override
	public boolean isReady(int duration, int amplifier) {
		if (this == TF2weapons.bleeding)
			return duration % 10 == 0;
		return false;
	}

	@Override
	public void performEffect(LivingEntity entityLivingBaseIn, int amplifier) {
		if (this == TF2weapons.bleeding && !entityLivingBaseIn.world.isRemote) {
			((ServerLevel) entityLivingBaseIn.world).spawnParticle(EnumParticleTypes.REDSTONE, entityLivingBaseIn.posX,
					entityLivingBaseIn.posY + entityLivingBaseIn.height / 2, entityLivingBaseIn.posZ, 7,
					entityLivingBaseIn.width / 2, entityLivingBaseIn.height / 2, entityLivingBaseIn.width / 2, 0);
			if (entityLivingBaseIn.getRevengeTarget() instanceof Player) {
				try {
					ReflectionAccess.entityRecentlyHit.setInt(entityLivingBaseIn, 100);
				} catch (Exception e) {}
				entityLivingBaseIn.setRevengeTarget(entityLivingBaseIn.getRevengeTarget());
			}
			entityLivingBaseIn.attackEntityFrom(DamageSource.MAGIC, 0.41f * (amplifier + 1));
		}
	}
}
