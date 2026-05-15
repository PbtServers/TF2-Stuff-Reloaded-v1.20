package rafradek.tf2weapons.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ItemFood extends Item {
	public ItemFood(int amount, float saturation, boolean isWolfFood) {
		super(new Item.Properties().food(new net.minecraft.world.food.FoodProperties.Builder().nutrition(amount).saturationMod(saturation).build()));
	}

	public void setAlwaysEdible() {}

	public int getHealAmount(ItemStack stack) {
		return 1;
	}
}
