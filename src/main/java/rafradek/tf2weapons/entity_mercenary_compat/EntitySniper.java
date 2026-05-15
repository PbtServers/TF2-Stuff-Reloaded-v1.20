package rafradek.tf2weapons.entity.mercenary;

import net.minecraft.world.level.Level;
import rafradek.tf2weapons.util.TF2Class;

public class EntitySniper extends EntityTF2Character {
	public EntitySniper(Level world) { super(world); }
	public TF2Class getTF2Class() { return TF2Class.SNIPER; }
}
