package rafradek.tf2weapons.item.crafting;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.NonNullList;
import net.minecraft.world.level.Level;
import rafradek.tf2weapons.TF2weapons;
import rafradek.tf2weapons.item.ItemFromData;
import rafradek.tf2weapons.util.PropertyType;
import rafradek.tf2weapons.util.TF2Class;

import java.util.ArrayList;
import java.util.HashSet;

public class RecipeToScrap extends net.minecraftforge.registries.IForgeRegistryEntry.Impl<IRecipe>
		implements IRecipe, IRecipeTF2 {

	public int token;

	public RecipeToScrap(int id) {
		this.token = id;
	}

	@Override
	public boolean matches(InventoryCrafting inv, Level world) {
		ArrayList<ItemStack> stacks = new ArrayList<>();

		HashSet<String> classnames = new HashSet<>();
		for (int x = 0; x < inv.getSizeInventory(); x++) {
			ItemStack stack = inv.getStackInSlot(x);
			if (!stack.isEmpty())
				if (stack.getItem() instanceof ItemFromData
						&& ItemFromData.getData(stack).getInt(PropertyType.COST) >= 6 && (token == -1 || ItemFromData
								.getData(stack).get(PropertyType.SLOT).containsKey(TF2Class.getClass(token).getName()))) {
					stacks.add(stack);
				} else
					return false;
		}
		// System.out.println("matches "+(australium&&stack2!=null));
		return stacks.size() == (token != -1 ? 3 : 2);
	}

	@Override
	public ItemStack getCraftingResult(InventoryCrafting inv) {
		if (token == -1)
			return new ItemStack(TF2weapons.itemTF2, 1, 3);
		else
			return new ItemStack(TF2weapons.itemToken, 1, token);
	}

	@Override
	public ItemStack getRecipeOutput() {
		if (token == -1)
			return new ItemStack(TF2weapons.itemTF2, 1, 3);
		else
			return new ItemStack(TF2weapons.itemToken, 1, token);
	}

	@Override
	public NonNullList<ItemStack> getRemainingItems(InventoryCrafting inv) {
		NonNullList<ItemStack> nonnulllist = NonNullList.<ItemStack>withSize(inv.getSizeInventory(), ItemStack.EMPTY);

		for (int i = 0; i < nonnulllist.size(); ++i) {
			ItemStack itemstack = inv.getStackInSlot(i);
			nonnulllist.set(i, net.minecraftforge.common.ForgeHooks.getContainerItem(itemstack));
		}

		return nonnulllist;
	}

	@Override
	public boolean canFit(int width, int height) {
		return width * height >= 2;
	}

	@Override
	public ItemStack getSuggestion(int slot) {
		if (slot < 2 && this.token == -1) {
			return new ItemStack(TF2weapons.itemTF2, 1, 9);
		} else if (slot < 3 && token != -1) {
			ItemStack stack = new ItemStack(TF2weapons.itemTF2, 1, 9);
			stack.setTagCompound(new CompoundTag());
			stack.getTagCompound().setByte("Token", (byte) this.token);
			return stack;
		}
		return ItemStack.EMPTY;
	}
}
