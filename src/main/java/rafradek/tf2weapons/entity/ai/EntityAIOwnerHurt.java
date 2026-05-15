package rafradek.tf2weapons.entity.ai;

import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import rafradek.tf2weapons.entity.ai.EntityAITarget;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.scores.Team;
import rafradek.tf2weapons.entity.mercenary.EntityMedic;
import rafradek.tf2weapons.entity.mercenary.EntityTF2Character;

public class EntityAIOwnerHurt extends EntityAITarget {

	public EntityAIOwnerHurt(PathfinderMob creature) {
		super(creature, false, false);
	}

	@Override
	public boolean shouldExecute() {
		Player owner=(Player) ((EntityTF2Character) this.taskOwner).getOwner();
		return owner != null && (owner.getRevengeTarget() != null || owner.getLastAttackedEntity() != null);
	}

	public void startExecuting()
    {
		Player owner=(Player) ((EntityTF2Character) this.taskOwner).getOwner();
		if(owner.getRevengeTarget() != null && this.isSuitableTarget(owner.getRevengeTarget(), false))
			this.taskOwner.setAttackTarget(owner.getRevengeTarget());
		else if(owner.getLastAttackedEntity() != null && this.isSuitableTarget(owner.getLastAttackedEntity(), false))
			this.taskOwner.setAttackTarget(owner.getLastAttackedEntity());
    }
	
	@Override
	protected boolean isSuitableTarget(LivingEntity target, boolean includeInvincibles) {
		if (target == null)
			return false;
		else if (target == this.taskOwner)
			return false;
		else if (!target.isEntityAlive())
			return false;
		else if (!this.taskOwner.canAttackClass(target.getClass()))
			return false;
		else {
			Team team = this.taskOwner.getTeam();
			Team team1 = target.getTeam();

			if ((team != null && team1 == team) && !(this.taskOwner instanceof EntityMedic))
				return false;
			else {
				if (this.taskOwner instanceof OwnableEntity
						&& ((OwnableEntity) this.taskOwner).getOwnerId() != null) {
					if (target instanceof OwnableEntity && ((OwnableEntity) this.taskOwner).getOwnerId()
							.equals(((OwnableEntity) target).getOwnerId()))
						return false;

					if (target == ((OwnableEntity) this.taskOwner).getOwner())
						return false;
				} else if (target instanceof Player && !includeInvincibles
						&& ((Player) target).capabilities.disableDamage)
					return false;

				return !this.shouldCheckSight || this.taskOwner.getEntitySenses().canSee(target);
			}
		}
	}
}
