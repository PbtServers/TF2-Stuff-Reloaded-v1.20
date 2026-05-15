package rafradek.tf2weapons.entity.mercenary;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.ItemStackHandler;
import rafradek.tf2weapons.util.TF2Class;

public class EntityTF2Character extends PathfinderMob {
	public enum Order {
		FOLLOW,
		HOLD,
		WANDER
	}

	public final ItemStackHandler loadout = new ItemStackHandler(13);
	public final AttackHelper attack = new AttackHelper();
	public final double[] targetPrevPos = new double[6];
	public final float[] lastRotation = new float[8];
	public int usedSlot;
	public int ballCooldown;
	public BlockPos spawnPos = BlockPos.ZERO;

	private int entTeam;
	private LivingEntity attackTarget;
	private LivingEntity owner;

	@SuppressWarnings("unchecked")
	public EntityTF2Character(Level world) {
		super((EntityType<? extends PathfinderMob>) (EntityType<?>) EntityType.ZOMBIE, world);
	}

	public TF2Class getTF2Class() { return TF2Class.NONE; }
	public int getEntTeam() { return entTeam; }
	public void setEntTeam(int team) { this.entTeam = team; }
	public boolean isRobot() { return false; }
	public boolean isGiant() { return false; }
	public boolean isSharing() { return false; }
	public LivingEntity getOwner() { return owner; }
	public LivingEntity getAttackTarget() { return attackTarget; }
	public void setAttackTarget(@Nullable LivingEntity target) { this.attackTarget = target; this.setTarget(target); }
	public float getAttributeModifier(String effect) { return 1f; }
	public void onShot() {}
	public void useAmmo(int amount) {}
	public int getAmmo() { return 0; }
	public int getAmmo(int slot) { return 0; }
	public void restoreAmmo(float amount) {}
	public int scaleWithDifficulty(int hard, int easy) { return easy; }
	public float scaleWithDifficulty(float hard, float easy) { return easy; }
	public float getMotionSensitivity() { return 1f; }
	public void addPotionEffect(MobEffectInstance effect) { this.addEffect(effect); }
	public MobEffectInstance getActivePotionEffect(MobEffect effect) { return this.getEffect(effect); }

	public static class AttackHelper {
		public double getRangeSq() { return 0; }
		public boolean lookingAt(Entity entity, double tolerance) { return false; }
	}
}
