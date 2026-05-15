package net.minecraft.util;

import net.minecraft.world.entity.Entity;

public class EntityDamageSourceIndirect extends EntityDamageSource {
	public final Entity indirectEntity;

	public EntityDamageSourceIndirect(String type, Entity source, Entity indirect) {
		super(type, source);
		this.indirectEntity = indirect;
	}
}
