package rafradek.tf2weapons.entity.building;

import java.util.UUID;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class EntityBuilding extends PathfinderMob {
	public static final UUID UPGRADE_HEALTH_UUID = UUID.fromString("00000000-0000-0000-0000-000000000001");
	public boolean redeploy;
	protected Player owner;
	protected int team;

	@SuppressWarnings("unchecked")
	public EntityBuilding(Level world) {
		super((EntityType<? extends PathfinderMob>) (EntityType<?>) EntityType.ZOMBIE, world);
	}

	public static int getCost(int slot, ItemStack stack) { return 0; }
	public void setOwner(Player owner) { this.owner = owner; }
	public Player getOwner() { return owner; }
	public void setConstructing(boolean constructing) {}
	public void setEntTeam(int team) { this.team = team; }
	public int getEntTeam() { return team; }
	public boolean isSapped() { return false; }
	public void setSapped(Player attacker, ItemStack stack) {}
	public boolean isDisabled() { return false; }
	public void grab() {}
	public void heal(float amount) {}
	public void readEntityFromNBT(CompoundTag tag) {}
}
