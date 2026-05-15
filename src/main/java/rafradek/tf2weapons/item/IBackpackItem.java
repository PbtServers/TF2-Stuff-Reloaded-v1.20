package rafradek.tf2weapons.item;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public interface IBackpackItem {

	default void checkItem(ItemStack par1ItemStack, Level par2World, Entity par3Entity, int par4, boolean par5) {
		if (!par5) {
			if (((Player) par3Entity).inventory.getStackInSlot(par4) == par1ItemStack) {
				((Player) par3Entity).inventory.setInventorySlotContents(par4, ItemStack.EMPTY);
			} else if (((Player) par3Entity).getHeldItemOffhand() == par1ItemStack) {
				((Player) par3Entity).inventory.offHandInventory.set(0, ItemStack.EMPTY);
			}
		}
	}
}
