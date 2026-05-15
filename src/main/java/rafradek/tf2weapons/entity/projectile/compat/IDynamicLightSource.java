package rafradek.tf2weapons.entity.projectile.compat;

import net.minecraft.world.entity.Entity;

public interface IDynamicLightSource {
	Entity getAttachmentEntity();

	int getLightLevel();
}
