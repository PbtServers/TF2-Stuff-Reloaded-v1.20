package rafradek.tf2weapons.item;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public interface IItemOverlay {
	default boolean showInfoBox(ItemStack stack, Player player) { return false; }
	default String[] getInfoBoxLines(ItemStack stack, Player player) { return new String[0]; }
	default void drawOverlay(ItemStack stack, Player player, Tesselator tessellator, BufferBuilder buffer, Window resolution) {}
}
