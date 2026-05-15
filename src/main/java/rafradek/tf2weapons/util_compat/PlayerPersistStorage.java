package rafradek.tf2weapons.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.NonNullList;
import net.minecraft.world.level.Level;
import rafradek.tf2weapons.entity.building.EntityBuilding;

import java.util.UUID;

public class PlayerPersistStorage {
	public NonNullList<ItemStack> itemsToGive = NonNullList.create();

	public CompoundTag serializeNBT() {
		return new CompoundTag();
	}

	public void deserializeNBT(CompoundTag nbt) {
	}

	public void storeBuilding(EntityBuilding building) {
	}

	public boolean isOwner(EntityBuilding building) {
		return true;
	}

	public static PlayerPersistStorage get(Player player) {
		return new PlayerPersistStorage();
	}

	public static PlayerPersistStorage get(Level level, UUID uuid) {
		return new PlayerPersistStorage();
	}
}
