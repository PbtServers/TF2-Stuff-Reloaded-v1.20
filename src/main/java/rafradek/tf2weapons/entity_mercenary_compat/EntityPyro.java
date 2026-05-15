package rafradek.tf2weapons.entity.mercenary;

import net.minecraft.world.level.Level;
import rafradek.tf2weapons.util.TF2Class;

public class EntityPyro extends EntityTF2Character {
	public EntityPyro(Level world) { super(world); }
	public TF2Class getTF2Class() { return TF2Class.PYRO; }
}
