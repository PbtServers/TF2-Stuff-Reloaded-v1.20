package rafradek.tf2weapons.entity.building;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public class EntityDispenser extends EntityBuilding {
	public EntityDispenser(Level world) { super(world); }
	public void setRange(float range) {}
	public static boolean isNearDispenser(Level world, LivingEntity living) { return false; }
}
