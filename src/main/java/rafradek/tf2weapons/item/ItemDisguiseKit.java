package rafradek.tf2weapons.item;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import rafradek.tf2weapons.TF2ConfigVars;
import rafradek.tf2weapons.TF2weapons;
import rafradek.tf2weapons.client.ClientProxy;
import rafradek.tf2weapons.client.gui.GuiDisguiseKit;
import rafradek.tf2weapons.common.TF2Attribute;
import rafradek.tf2weapons.common.WeaponsCapability;
import rafradek.tf2weapons.entity.building.EntityBuilding;
import rafradek.tf2weapons.entity.mercenary.EntityTF2Character;
import rafradek.tf2weapons.message.TF2Message;
import rafradek.tf2weapons.util.TF2Class;
import rafradek.tf2weapons.util.TF2Util;

public class ItemDisguiseKit extends Item implements IItemSlotNumber, IItemOverlay {

	public ItemDisguiseKit() {
		this.setCreativeTab(TF2weapons.tabutilitytf2);
		this.setMaxStackSize(1);
		this.setMaxDamage(80);
	}

	public static void startDisguise(LivingEntity living, Level world, String type) {
		String type2 = type;
		if (!world.isRemote && type.startsWith("T:") && (living instanceof Player)) {
			Player player = world.getClosestPlayer(living.posX, living.posY, living.posZ, 512,
					playerl -> (!TF2Util.isOnSameTeam(living, playerl)
							&& WeaponsCapability.get(playerl).getUsedToken() >= 0 && type.substring(2).equalsIgnoreCase(
									TF2Class.getClass(WeaponsCapability.get(playerl).getUsedToken()).getName())));
			if (player != null) {
				type2 = "P:" + player.getName();
			}
		}
		WeaponsCapability.get(living).setDisguiseType(type2);
		if (living.getCapability(TF2weapons.WEAPONS_CAP, null).disguiseTicks == 0)
			// System.out.println("starting disguise");
			if (!world.isRemote)
				living.getCapability(TF2weapons.WEAPONS_CAP, null).disguiseTicks = 1;
	}

	@Override
	public InteractionResultHolder<ItemStack> onItemRightClick(Level world, Player living, InteractionHand hand) {
		if (world.isRemote && !TF2ConfigVars.limitedDisguise && ItemToken.allowUse(living, TF2Class.SPY)
				&& TF2Util.getFirstItem(living.inventory,
						stack -> TF2Attribute.getModifier("No Disguise Kit", stack, 0, living) != 0).isEmpty())
			ClientProxy.showGuiDisguise();
		return new InteractionResultHolder<>(InteractionResult.SUCCESS, living.getHeldItem(hand));
	}

	public static boolean isDisguised(LivingEntity living, LivingEntity view) {
		if (!living.hasCapability(TF2weapons.WEAPONS_CAP, null) || !WeaponsCapability.get(living).isDisguised()
				|| (living.getCapability(TF2weapons.WEAPONS_CAP, null).invisTicks != 0
						&& !(view instanceof EntityBuilding)))
			return false;
		String disguisetype = WeaponsCapability.get(living).getDisguiseType();
		if (disguisetype.startsWith("M:") || disguisetype.startsWith("T:"))
			return true;
		if (disguisetype.startsWith("P:")) {
			return living.world.getScoreboard().getPlayersTeam(disguisetype.substring(2)) == view.getTeam();
		}
		return false;
	}

	@Override
	public boolean getIsRepairable(ItemStack toRepair, ItemStack repair) {
		return repair.getItem() == TF2weapons.itemTF2 && repair.getMetadata() == 2;
	}

	@Override
	public boolean showInfoBox(ItemStack stack, Player player) {
		return false;
	}

	@Override
	public String[] getInfoBoxLines(ItemStack stack, Player player) {
		return null;
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public void drawOverlay(ItemStack stack, Player player, Tesselator tessellator, BufferBuilder buffer,
			Window resolution) {
		if (ItemToken.allowUse(player, TF2Class.SPY) && TF2ConfigVars.limitedDisguise
				&& TF2Util
						.getFirstItem(player.inventory,
								stackl -> TF2Attribute.getModifier("No Disguise Kit", stackl, 0, player) != 0)
						.isEmpty()) {
			Gui gui = Minecraft.getMinecraft().ingameGUI;
			int width = resolution.getScaledWidth();
			int height = resolution.getScaledHeight();
			for (int i = 0; i < 9; i++) {
				TF2Class clazz = TF2Class.getClass(i);
				gui.drawCenteredString(gui.getFontRenderer(), clazz.getLocalizedName().getFormattedText(), width / 2 - 225 + i * 50,
						height / 2 + 50, 0xFFFFFFFF);
				gui.drawCenteredString(gui.getFontRenderer(), String.valueOf(i + 1), width / 2 - 225 + i * 50,
						height / 2 + 60, 0xFFFFFFFF);
				EntityTF2Character entity = clazz.createEntity(player.world);
				entity.setEntTeam(TF2Util.getTeamForDisplay(player) == 0 ? 1 : 0);
				entity.getCapability(TF2weapons.WEAPONS_CAP, null).invisTicks = 0;
				GuiDisguiseKit.drawEntityOnScreen(width / 2 - 225 + i * 50, height / 2 + 40, 35, entity);
			}
		}
	}

	@Override
	public boolean catchSlotHotkey(ItemStack stack, Player player) {
		return TF2ConfigVars.limitedDisguise;
	}

	@Override
	public void onSlotSelection(ItemStack stack, Player player, int slot) {
		if (ItemToken.allowUse(player, TF2Class.SPY)
				&& TF2Util
						.getFirstItem(player.inventory,
								stackl -> TF2Attribute.getModifier("No Disguise Kit", stackl, 0, player) != 0)
						.isEmpty()) {
			TF2weapons.network.sendToServer(new TF2Message.DisguiseMessage("T:" + TF2Class.getClass(slot)));
		}
	}
}
