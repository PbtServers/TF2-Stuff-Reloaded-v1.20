package rafradek.tf2weapons.tileentity;

import net.minecraft.nbt.CompoundTag;

public interface IEntityConfigurable {
	CompoundTag writeConfig(CompoundTag tag);

	void readConfig(CompoundTag tag);

	EntityOutputManager getOutputManager();

	default void activateOutput(String output) {
		this.activateOutput(output, 1f, 1);
	}

	default void activateOutput(String output, float power, int minTime) {
		this.getOutputManager().activateOutput(output, power, minTime);
	}

	default String getLinkName() {
		return this.getOutputManager().name;
	}

	String[] getOutputs();

	default String[] getAllowedValues(String attribute) {
		return null;
	}
}
