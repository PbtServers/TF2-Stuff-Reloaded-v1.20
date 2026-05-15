package rafradek.tf2weapons.message;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public class TargetPoint {
	public ResourceKey<Level> dimension;
	public double x;
	public double y;
	public double z;
	public double range;
	public TargetPoint(ResourceKey<Level> dimension, double x, double y, double z, double range) {
		this.dimension = dimension;
		this.x = x;
		this.y = y;
		this.z = z;
		this.range = range;
	}
}
