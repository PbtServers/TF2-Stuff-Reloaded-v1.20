package rafradek.tf2weapons.client.particle;

import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.client.particle.Particle;
import com.mojang.blaze3d.vertex.BufferBuilder;
import net.minecraft.world.entity.Entity;
import rafradek.tf2weapons.util.EnumParticleTypes;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import rafradek.tf2weapons.util.TF2Util;

public class ParticleExplosion extends Particle {

	private int timeSinceStart;
	/** the maximum time for the explosion */
	private final int maximumTime = 5;

	private float size = 4;

	public ParticleExplosion(Level par1World, double startX, double startY, double startZ, double x, double y,
			double z) {
		super(par1World, startX, startY, startZ, 0, 0, 0);
		this.size = (float) (x - 0.5);
	}

	@Override
	public void onUpdate() {
		++this.timeSinceStart;
		float scale = ((float) timeSinceStart / (float) this.maximumTime) * size;
		float count = 0.33f * scale * size * scale;
		for (int i = 0; i < count; ++i) {
			Vec3 pos = TF2Util.rangeRandom3D(scale, this.rand);
			double d0 = this.posX + pos.x;
			double d1 = this.posY + pos.y;
			double d2 = this.posZ + pos.z;
			this.world.spawnParticle(EnumParticleTypes.EXPLOSION_LARGE, d0, d1, d2,
					(float) this.timeSinceStart / (float) this.maximumTime, 0.0D, 0.0D);
		}

		if (this.timeSinceStart == this.maximumTime) {
			this.setExpired();
		}
	}

	@Override
	public void renderParticle(BufferBuilder buffer, Entity entityIn, float partialTicks, float rotationX,
			float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {}

	@Override
	public int getFXLayer() {
		return 1;
	}

	public static class Factory implements IParticleFactory {

		@Override
		public Particle createParticle(int particleID, Level world, double xCoordIn, double yCoordIn, double zCoordIn,
				double xSpeedIn, double ySpeedIn, double zSpeedIn, int... p_178902_15_) {
			return new ParticleExplosion(world, xCoordIn, yCoordIn, zCoordIn, xSpeedIn, ySpeedIn, zSpeedIn);
		}

	}
}
