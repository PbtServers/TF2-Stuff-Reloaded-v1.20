package rafradek.tf2weapons.inventory;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import rafradek.tf2weapons.TF2weapons;
import rafradek.tf2weapons.common.WeaponsCapability;
import rafradek.tf2weapons.item.ItemToken;
import rafradek.tf2weapons.message.TF2Message;
import rafradek.tf2weapons.util.TF2Util;

public class InventoryWearables extends InventoryBasic implements ICapabilityProvider, INBTSerializable<ListTag> {

	public LivingEntity owner;
	public static final int USED_SLOTS = 8;
	private final NonNullList<ItemStack> inventoryContentsOld;
	public ItemStack origHead = ItemStack.EMPTY;

	public InventoryWearables(LivingEntity ply) {
		super("Wearables", false, 13);
		this.inventoryContentsOld = NonNullList.withSize(13, ItemStack.EMPTY);
		owner = ply;
	}

	@Override
	public boolean isEmpty() {
		for (int i = 0; i < USED_SLOTS; i++)
			if (!this.getStackInSlot(i).isEmpty())
				return false;
		return true;
	}

	public void updateSlots() {
		for (int i = 0; i < USED_SLOTS; i++) {
			ItemStack stack = this.getStackInSlot(i);
			ItemStack old = inventoryContentsOld.get(i);
			if (!ItemStack.areItemStacksEqual(stack, old)) {
				inventoryContentsOld.set(i, old);
				TF2Util.sendTracking(new TF2Message.WearableChangeMessage(owner, i, stack), owner);
			}
		}
	}

	@Override
	public boolean hasCapability(Capability<?> capability, Direction facing) {
		return TF2weapons.INVENTORY_CAP != null && capability == TF2weapons.INVENTORY_CAP;
	}

	@Override
	public <T> T getCapability(Capability<T> capability, Direction facing) {
		if (TF2weapons.INVENTORY_CAP != null && capability == TF2weapons.INVENTORY_CAP)
			return TF2weapons.INVENTORY_CAP.cast(this);
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
		if (!WeaponsCapability.get(owner).forcedClass) {
			ItemStack token = this.getStackInSlot(4);
			((ItemToken) TF2weapons.itemToken).updateAttributes(token, owner);
		}
		// System.out.println("Reading ");
	}

}
