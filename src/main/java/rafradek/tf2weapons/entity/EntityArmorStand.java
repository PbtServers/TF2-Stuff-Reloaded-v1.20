package rafradek.tf2weapons.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.level.Level;

public class EntityArmorStand extends ArmorStand {
	public boolean creative;

	public EntityArmorStand(Level world) {
		super(EntityType.ARMOR_STAND, world);
	}

	public EntityArmorStand(Level world, double x, double y, double z) {
		this(world);
		setPos(x, y, z);
	}

	public void setSize(float width, float height) {}
}
