package rafradek.tf2weapons.entity.boss;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.Level;

public class EntityTF2Boss extends PathfinderMob {
	public boolean summoned;
	public int level = 1;
	public float desiredHealth = 1f;

	@SuppressWarnings("unchecked")
	public EntityTF2Boss(Level world) {
		super((EntityType<? extends PathfinderMob>) (EntityType<?>) EntityType.ZOMBIE, world);
	}

	public void setLocationAndAngles(double x, double y, double z, float yaw, float pitch) {
		this.setPos(x, y, z);
		this.setYRot(yaw);
		this.setXRot(pitch);
	}
}
