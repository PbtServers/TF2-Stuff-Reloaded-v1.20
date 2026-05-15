package rafradek.tf2weapons.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import rafradek.tf2weapons.common.WeaponsCapability;

public class EntityDummy extends Mob {
	public WeaponsCapability cap;

	@SuppressWarnings("unchecked")
	public EntityDummy(Level world) {
		super((EntityType<? extends Mob>) (EntityType<?>) EntityType.ZOMBIE, world);
		this.cap = new WeaponsCapability(this);
	}
}
