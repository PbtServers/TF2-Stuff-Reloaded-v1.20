package rafradek.tf2weapons.inventory;

import net.minecraft.inventory.InventoryBasic;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import rafradek.tf2weapons.TF2weapons;

public class InventoryAmmoBelt extends InventoryBasic implements ICapabilityProvider, INBTSerializable<ListTag> {

	public InventoryAmmoBelt() {
		super("AmmoBelt", false, 9);
	}

	@Override
	public boolean isEmpty() {
		for (int i = 0; i < this.getSizeInventory(); i++)
			if (this.getStackInSlot(0) != null)
				return false;
		return true;
	}

	@Override
	public boolean hasCapability(Capability<?> capability, Direction facing) {
		return TF2weapons.INVENTORY_BELT_CAP != null && capability == TF2weapons.INVENTORY_BELT_CAP;
	}

	@Override
	public <T> T getCapability(Capability<T> capability, Direction facing) {
		if (TF2weapons.INVENTORY_BELT_CAP != null && capability == TF2weapons.INVENTORY_BELT_CAP)
			return TF2weapons.INVENTORY_BELT_CAP.cast(this);
		return null;
	}

	@Override
	public ListTag serializeNBT() {
		ListTag list = new ListTag();
		for (int i = 0; i < this.getSizeInventory(); i++) {
			ItemStack itemstack = this.getStackInSlot(i);

			if (!itemstack.isEmpty()) {
				CompoundTag CompoundTag = new CompoundTag();
				CompoundTag.setByte("Slot", (byte) i);
				itemstack.writeToNBT(CompoundTag);
				list.appendTag(CompoundTag);
			}
		}
		// System.out.println("Saving ");
		return list;
	}

	@Override
	public void deserializeNBT(ListTag nbt) {

		for (int i = 0; i < nbt.tagCount(); ++i) {
			CompoundTag CompoundTag = nbt.getCompoundTagAt(i);
			int j = CompoundTag.getByte("Slot");
			this.setInventorySlotContents(j, new ItemStack(CompoundTag));
		}
		// System.out.println("Reading ");
	}
}
