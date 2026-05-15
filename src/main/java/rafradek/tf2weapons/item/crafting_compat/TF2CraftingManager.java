package rafradek.tf2weapons.item.crafting;

import java.util.Collections;
import java.util.List;

import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.oredict.ShapelessOreRecipe;

public class TF2CraftingManager {
	public static final TF2CraftingManager INSTANCE = new TF2CraftingManager();
	public static final ShapelessOreRecipe[] AMMO_RECIPES = new ShapelessOreRecipe[14];

	public List<Object> getRecipeList() {
		return Collections.emptyList();
	}

	public ItemStack findMatchingRecipe(Object craftMatrix, Level world, Player player) {
		return ItemStack.EMPTY;
	}

	public NonNullList<ItemStack> getRemainingItems(Object craftMatrix, Level world) {
		return NonNullList.create();
	}
}
