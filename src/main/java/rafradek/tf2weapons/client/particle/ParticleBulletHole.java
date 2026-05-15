package rafradek.tf2weapons.client.particle;

import net.minecraft.client.particle.Particle;
import com.mojang.blaze3d.vertex.BufferBuilder;
import net.minecraft.world.entity.Entity;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.level.Level;

public class ParticleBulletHole extends Particle {

	HitResult block;

	public ParticleBulletHole(Level world, HitResult result) {
		super(world, result.hitVec.x + result.sideHit.getFrontOffsetX() * 0.012,
				result.hitVec.y + result.sideHit.getFrontOffsetY() * 0.012,
				result.hitVec.z + result.sideHit.getFrontOffsetZ() * 0.012);
		this.block = result;
		this.particleRed = 0.05F;
		this.particleGreen = 0.05F;
		this.particleBlue = 0.05F;
		this.particleAlpha = 1f;
		this.particleMaxAge = 280;
		this.particleScale *= 0.9F;
		this.setSize(0.1F, 0.1F);
		this.setParticleTextureIndex(0);
	}

	@Override
	public void onUpdate() {
		this.motionX = 0;
		this.motionY = 0;
		this.motionZ = 0;
		this.prevPosX = this.posX;
		this.prevPosY = this.posY;
		this.prevPosZ = this.posZ;
		// this.move(this.motionX, this.motionY, this.motionZ);

		if (this.world.getBlockState(block.getBlockPos()).getBlock()
				.isAir(this.world.getBlockState(block.getBlockPos()), world, block.getBlockPos())) {
			this.setExpired();
		}

		if (this.particleMaxAge-- <= 0) {
			this.setExpired();
		}
	}

	@Override
	public void renderParticle(BufferBuilder buffer, Entity entityIn, float partialTicks, float rotationX,
			float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {
		// super.renderParticle(buffer, entityIn, partialTicks, rotationX, rotationZ,
		// rotationYZ, rotationXY, rotationXZ);
		// System.out.println("rot: "+rotationX+" "+rotationZ+" "+rotationYZ+"
		// "+rotationXY+" "+rotationXZ);
		Direction face = this.block.sideHit;
		if (face == Direction.UP)
			super.renderParticle(buffer, entityIn, partialTicks, 1, 0, 0, 0, 1);
		else if (face == Direction.DOWN)
			super.renderParticle(buffer, entityIn, partialTicks, 1, 0, 0, 0, -1);
		else if (face == Direction.NORTH)
			super.renderParticle(buffer, entityIn, partialTicks, 1, 1, 0, 0, 0);
		else if (face == Direction.SOUTH)
			super.renderParticle(buffer, entityIn, partialTicks, -1, 1, 0, 0, 0);
		else if (face == Direction.EAST)
			super.renderParticle(buffer, entityIn, partialTicks, 0, 1, 1, 0, 0);
		else if (face == Direction.WEST)
			super.renderParticle(buffer, entityIn, partialTicks, 0, 1, -1, 0, 0);
	}
}
