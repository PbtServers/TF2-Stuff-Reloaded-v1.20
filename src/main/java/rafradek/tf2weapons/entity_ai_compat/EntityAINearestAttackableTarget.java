package rafradek.tf2weapons.entity.ai;

import java.util.Comparator;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;

public class EntityAINearestAttackableTarget<T extends LivingEntity> extends EntityAITarget {
	public EntityAINearestAttackableTarget(PathfinderMob owner, Class<T> targetClass, Object... args) {
		super(owner, false);
	}

	public static class Sorter implements Comparator<Entity> {
		public Sorter(Entity entity) {}

		@Override
		public int compare(Entity first, Entity second) {
			return 0;
		}
	}
}
