package net.minecraftforge.oredict;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

public class ShapelessOreRecipe {
	public ItemStack getRecipeOutput() {
		return ItemStack.EMPTY;
	}

	public NonNullList<Ingredient> getIngredients() {
		return NonNullList.create();
	}
}
