package rafradek.tf2weapons.message;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.level.ServerLevel;
import rafradek.tf2weapons.message.TF2Packet;
import rafradek.tf2weapons.message.TF2MessageHandler;
import rafradek.tf2weapons.message.MessageContext;
import rafradek.tf2weapons.TF2ConfigVars;
import rafradek.tf2weapons.item.ItemDisguiseKit;
import rafradek.tf2weapons.message.TF2Message.DisguiseMessage;

public class TF2DisguiseHandler implements TF2MessageHandler<TF2Message.DisguiseMessage, TF2Packet> {

	@Override
	public TF2Packet onMessage(final DisguiseMessage message, MessageContext ctx) {
		final ServerPlayer player = ctx.getServerHandler().player;
		((ServerLevel) player.world).addScheduledTask(() -> {
			ItemStack stack;
			if (((stack = player.getHeldItemMainhand()) != null && stack.getItem() instanceof ItemDisguiseKit)
					|| ((stack = player.getHeldItemOffhand()) != null
							&& stack.getItem() instanceof ItemDisguiseKit)) {
				ItemDisguiseKit.startDisguise(player, player.world, message.value);
				if (!player.capabilities.isCreativeMode && !TF2ConfigVars.freeUseItems)
					stack.damageItem(1, player);
			}
		});
		return null;
	}

}
