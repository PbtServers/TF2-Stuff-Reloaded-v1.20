package rafradek.tf2weapons.entity.ai;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import java.util.Comparator;

public final class EntityAINearestAttackableTarget {
	private EntityAINearestAttackableTarget() {
	}

	public static class Sorter implements Comparator<LivingEntity> {
		private final Entity entity;

		public Sorter(Entity entity) {
			this.entity = entity;
		}

		@Override
		public int compare(LivingEntity first, LivingEntity second) {
			double firstDistance = this.entity.distanceToSqr(first);
			double secondDistance = this.entity.distanceToSqr(second);
			return Double.compare(firstDistance, secondDistance);
		}
	}
}
