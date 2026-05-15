package net.minecraft.util;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;

public class EntityDamageSource extends DamageSource {
	public final Entity damageSourceEntity;

	public EntityDamageSource(String type, Entity entity) {
		super(null);
		this.damageSourceEntity = entity;
	}
}
