package rafradek.tf2weapons.item;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.InteractionResult;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import rafradek.tf2weapons.util.TF2GuiOpener;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.ItemStackHandler;
import rafradek.tf2weapons.TF2weapons;

public class ItemAmmoBelt extends Item {

	@Override
	public InteractionResultHolder<ItemStack> onItemRightClick(Level world, Player living, InteractionHand hand) {
		if (!world.isRemote)
			TF2GuiOpener.openGui(living, TF2weapons.instance, 0, world, 0, 0, 0);
		return new InteractionResultHolder<>(InteractionResult.SUCCESS, living.getHeldItem(hand));
	}

	@Override
	public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
		return TF2weapons.MOD_ID + ":textures/models/tf2/ammo_belt.png";
	}

	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack, CompoundTag nbt) {
		return new Provider();
	}

	public static class Provider extends ItemStackHandler implements ICapabilityProvider {

		public Provider() {
			super(9);
		}

		@Override
		public boolean hasCapability(Capability<?> capability, Direction facing) {
			return capability == ForgeCapabilities.ITEM_HANDLER;
		}

		@Override
		public <T> T getCapability(Capability<T> capability, Direction facing) {
			if (capability == ForgeCapabilities.ITEM_HANDLER)
				return ForgeCapabilities.ITEM_HANDLER.cast(this);
			return null;
		}

	}
}
