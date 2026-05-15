package rafradek.tf2weapons.item;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.InteractionResult;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.HitResult;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.level.Level;
import rafradek.tf2weapons.TF2weapons;
import rafradek.tf2weapons.entity.EntityPickup;
import rafradek.tf2weapons.message.TF2Message;
import rafradek.tf2weapons.tileentity.IEntityConfigurable;

public class ItemConfigure extends Item {

	public ItemConfigure() {
		this.setUnlocalizedName(TF2weapons.MOD_ID + ".configurator");
	}

	@Override
	public InteractionResult onItemUse(Player player, Level world, BlockPos pos, InteractionHand hand,
			Direction facing, float hitX, float hitY, float hitZ) {
		if (!world.isRemote) {
			BlockEntity ent = world.getTileEntity(pos);
			if (ent instanceof IEntityConfigurable) {
				// TF2GuiOpener.openGui(player, TF2weapons.instance, 7, world,
				// pos.getX(),
				// pos.getY(), pos.getZ());
				TF2weapons.network.sendTo(new TF2Message.GuiConfigMessage(
						((IEntityConfigurable) ent).writeConfig(new CompoundTag()), pos), (ServerPlayer) player);
			} else {
				BlockPos forwardpos = pos.offset(facing);
				for (EntityPickup pickup : world.getEntitiesWithinAABB(EntityPickup.class,
						new AABB(forwardpos))) {
					pickup.setDead();
				}
			}
		} else {
			BlockEntity ent = world.getTileEntity(pos);
			if (!(ent instanceof IEntityConfigurable)) {
				GuiScreen.setClipboardString(pos.getX() + " " + pos.getY() + " " + pos.getZ());
				player.sendMessage(
						new TextComponentString("Copied coordinates to clipboard: " + GuiScreen.getClipboardString()));
			}
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	public InteractionResultHolder<ItemStack> onItemRightClick(Level world, Player player, InteractionHand handIn) {
		if (world.isRemote) {
			HitResult ray = world.rayTraceBlocks(player.getPositionEyes(1f),
					player.getPositionEyes(1f).add(player.getLookVec().scale(256)), false);
			if (ray != null && ray.getBlockPos() != null) {
				BlockPos pos = ray.getBlockPos();
				BlockEntity ent = world.getTileEntity(pos);
				if (!(ent instanceof IEntityConfigurable)) {
					GuiScreen.setClipboardString(pos.getX() + " " + pos.getY() + " " + pos.getZ());
					player.sendMessage(new TextComponentString(
							"Copied coordinates to clipboard: " + GuiScreen.getClipboardString()));
				}
			}
		}
		return new InteractionResultHolder<>(InteractionResult.SUCCESS, player.getHeldItem(handIn));
	}
}
