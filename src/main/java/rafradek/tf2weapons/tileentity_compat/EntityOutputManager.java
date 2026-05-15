package rafradek.tf2weapons.tileentity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;

public class EntityOutputManager {
	public Level world;
	public String name = "";
	public final IEntityConfigurable entity;

	public EntityOutputManager(IEntityConfigurable entity) {
		this.entity = entity;
	}

	public void setWorld(Level world) {
		this.world = world;
	}

	public void loadOutputs(CompoundTag tag) {}

	public CompoundTag saveOutputs(CompoundTag tag) {
		return tag;
	}

	public void readConfig(CompoundTag tag) {}

	public CompoundTag writeConfig(CompoundTag tag) {
		return tag;
	}

	public void activateOutput(String output, float power, int minTime) {}
}
