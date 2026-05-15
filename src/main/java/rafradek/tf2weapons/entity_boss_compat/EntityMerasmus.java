package rafradek.tf2weapons.entity.boss;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.level.Level;

public class EntityMerasmus extends EntityTF2Boss {
	public EntityMerasmus(Level world) { super(world); }
	public MobEffectInstance getActivePotionEffect(MobEffect effect) { return this.getEffect(effect); }
}
