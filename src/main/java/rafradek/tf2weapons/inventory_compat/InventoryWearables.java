package rafradek.tf2weapons.inventory;

import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import rafradek.tf2weapons.TF2weapons;

public class InventoryWearables implements ICapabilityProvider, INBTSerializable<ListTag> {
	public static final int USED_SLOTS = 8;
	public LivingEntity owner;
	public ItemStack origHead = ItemStack.EMPTY;
	private final NonNullList<ItemStack> stacks = NonNullList.withSize(13, ItemStack.EMPTY);

	public InventoryWearables(LivingEntity owner) {
		this.owner = owner;
	}

	public boolean isEmpty() {
		for (int i = 0; i < USED_SLOTS; i++) {
			if (!getStackInSlot(i).isEmpty()) return false;
		}
		return true;
	}

	public int getSizeInventory() {
		return stacks.size();
	}

	public ItemStack getStackInSlot(int slot) {
		return slot >= 0 && slot < stacks.size() ? stacks.get(slot) : ItemStack.EMPTY;
	}

	public void setInventorySlotContents(int slot, ItemStack stack) {
		if (slot >= 0 && slot < stacks.size()) stacks.set(slot, stack == null ? ItemStack.EMPTY : stack);
	}

	public void updateSlots() {}

	public boolean hasCapability(Capability<?> capability, Direction facing) {
		return TF2weapons.INVENTORY_CAP != null && capability == TF2weapons.INVENTORY_CAP;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> LazyOptional<T> getCapability(Capability<T> capability, Direction facing) {
		return hasCapability(capability, facing) ? LazyOptional.of(() -> (T) this) : LazyOptional.empty();
	}

	@Override
	public ListTag serializeNBT() {
		return new ListTag();
	}

	@Override
	public void deserializeNBT(ListTag nbt) {}
}
