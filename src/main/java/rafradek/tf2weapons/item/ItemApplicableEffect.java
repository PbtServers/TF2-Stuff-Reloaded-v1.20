package rafradek.tf2weapons.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ItemApplicableEffect extends Item {

	public boolean isApplicable(ItemStack stack, ItemStack weapon) {
		return stack.hasTagCompound()
				&& ItemFromData.getData(weapon).getName().equals(stack.getTagCompound().getString("Weapon"));
	}

	public void apply(ItemStack stack, ItemStack weapon) {}

}
