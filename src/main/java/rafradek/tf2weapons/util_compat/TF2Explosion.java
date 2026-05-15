package rafradek.tf2weapons.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.Explosion;

import java.util.Collections;
import java.util.List;

public class TF2Explosion extends Explosion {
	public Level world;
	public Entity exploder;
	public double x;
	public double y;
	public double z;
	public float explosionSize;

	public TF2Explosion(Level world, Entity exploder, double x, double y, double z, float size) {
		super(world, exploder, x, y, z, size, false, Explosion.BlockInteraction.KEEP);
		this.world = world;
		this.exploder = exploder;
		this.x = x;
		this.y = y;
		this.z = z;
		this.explosionSize = size;
	}

	public void doExplosionA() {}
	public void doExplosionB(boolean spawnParticles) {}
	public List<BlockPos> getAffectedBlockPositions() { return Collections.emptyList(); }
	public LivingEntity getExplosivePlacedBy() { return exploder instanceof LivingEntity living ? living : null; }
}
