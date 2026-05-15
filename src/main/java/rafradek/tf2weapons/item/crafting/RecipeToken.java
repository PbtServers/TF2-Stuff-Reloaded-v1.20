package rafradek.tf2weapons.item.crafting;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.oredict.ShapelessOreRecipe;
import rafradek.tf2weapons.TF2weapons;
import rafradek.tf2weapons.util.TF2Class;

public class RecipeToken extends ShapelessOreRecipe {

	public RecipeToken(ResourceLocation group, ItemStack result, Object[] recipe, TF2Class clazz) {
		super(group, result, recipe);
		output.setTagCompound(new CompoundTag());
		output.getTagCompound().setByte("Token", (byte) clazz.getIndex());
		input.add(Ingredient.fromStacks(new ItemStack(TF2weapons.itemToken, 1, clazz.getIndex())));
	}

}
