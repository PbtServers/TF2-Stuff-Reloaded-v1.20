package rafradek.tf2weapons.entity.building;

import net.minecraft.world.level.Level;

public class EntitySentry extends EntityBuilding {
	public boolean fromPDA;
	public float attackRateMult = 1f;
	private int ammo;
	private int rocketAmmo;

	public EntitySentry(Level world) { super(world); }

	public void setMini(boolean mini) {}
	public void setHeat(int heat) {}
	public void setTargetInfo(int info) {}
	public void setControlled(boolean controlled) {}
	public int getMaxAmmo() { return 0; }
	public int getAmmo() { return ammo; }
	public void setAmmo(int ammo) { this.ammo = ammo; }
	public int getRocketAmmo() { return rocketAmmo; }
	public void setRocketAmmo(int rocketAmmo) { this.rocketAmmo = rocketAmmo; }
}
