package net.minecraft.entity.monster;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.Level;

public abstract class EntityMob extends PathfinderMob {
	protected EntityMob(EntityType<? extends PathfinderMob> type, Level level) {
		super(type, level);
	}
}
