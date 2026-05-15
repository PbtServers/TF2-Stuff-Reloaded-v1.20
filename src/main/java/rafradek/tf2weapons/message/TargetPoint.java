package rafradek.tf2weapons.message;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.PacketDistributor;

public class TargetPoint {
	public final ResourceKey<Level> dimension;
	public final double x;
	public final double y;
	public final double z;
	public final double range;

	public TargetPoint(ResourceKey<Level> dimension, double x, double y, double z, double range) {
		this.dimension = dimension;
		this.x = x;
		this.y = y;
		this.z = z;
		this.range = range;
	}

	public PacketDistributor.TargetPoint toForgeTargetPoint() {
		return new PacketDistributor.TargetPoint(this.x, this.y, this.z, this.range, this.dimension);
	}
}
