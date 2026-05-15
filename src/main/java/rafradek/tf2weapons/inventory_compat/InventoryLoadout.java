package rafradek.tf2weapons.inventory;

import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.items.ItemStackHandler;

public class InventoryLoadout extends ItemStackHandler {
	public InventoryLoadout(int size, LivingEntity entity) {
		super(size);
	}

	public void updateSlots() {}
}
