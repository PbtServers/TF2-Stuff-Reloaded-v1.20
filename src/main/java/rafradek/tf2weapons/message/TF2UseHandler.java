package rafradek.tf2weapons.message;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import rafradek.tf2weapons.message.TF2Packet;
import rafradek.tf2weapons.message.TF2MessageHandler;
import rafradek.tf2weapons.message.MessageContext;
import rafradek.tf2weapons.TF2weapons;
import rafradek.tf2weapons.common.TF2Attribute;
import rafradek.tf2weapons.common.WeaponsCapability;
import rafradek.tf2weapons.item.ItemAmmo;
import rafradek.tf2weapons.item.ItemFromData;
import rafradek.tf2weapons.item.ItemUsable;
import rafradek.tf2weapons.item.ItemWeapon;

public class TF2UseHandler implements TF2MessageHandler<TF2Message.UseMessage, TF2Packet> {

	@Override
	public TF2Packet onMessage(TF2Message.UseMessage message, MessageContext ctx) {
		Player player = TF2weapons.proxy.getPlayerForSide(ctx);
		if (player != null) {
			ItemStack stack = player.getHeldItem(message.hand);
			if (!stack.isEmpty() && stack.getItem() instanceof ItemUsable) {
				if (message.newAmmo != -1) {
					if (TF2Attribute.getModifier("Ball Release", stack, 0, player) != 0)
						stack = ItemFromData.getNewStack("sandmanball");
					if (((ItemUsable) stack.getItem()).getAmmoType(stack) < ItemAmmo.AMMO_TYPES.length)
						player.getCapability(TF2weapons.PLAYER_CAP, null).cachedAmmoCount[((ItemUsable) stack.getItem())
								.getAmmoType(stack)] = message.newAmmo;
				}

				if (message.value != -1 && stack.getItem() instanceof ItemWeapon
						&& ((ItemWeapon) stack.getItem()).hasClip(stack)) {
					((ItemWeapon) stack.getItem()).setClip(stack, message.value);
					final ItemStack fstack = stack;
					if (message.reload) {
						WeaponsCapability.get(player).reloadCool = ((ItemWeapon) stack.getItem())
								.getWeaponFirstReloadTime(stack, player);
						WeaponsCapability.get(player).reloadingHand = message.hand;
						Minecraft.getMinecraft().addScheduledTask(() -> {
							TF2weapons.proxy.playReloadSound(player, fstack);
						});
					} else {
						WeaponsCapability.get(player).stopReload();
					}

				}
			}
		}
		return null;
	}
}
