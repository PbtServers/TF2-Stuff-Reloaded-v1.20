package rafradek.tf2weapons.client.compat;

import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.world.item.ItemStack;

@FunctionalInterface
public interface ItemMeshDefinition {
	ModelResourceLocation getModelLocation(ItemStack stack);
}
