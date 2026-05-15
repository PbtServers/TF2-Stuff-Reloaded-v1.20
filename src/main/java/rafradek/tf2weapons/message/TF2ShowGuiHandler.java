package rafradek.tf2weapons.message;

import rafradek.tf2weapons.util.TF2GuiOpener;
import rafradek.tf2weapons.message.TF2Packet;
import rafradek.tf2weapons.message.TF2MessageHandler;
import rafradek.tf2weapons.message.MessageContext;
import net.minecraftforge.api.distmarker.Dist;
import rafradek.tf2weapons.TF2weapons;
import rafradek.tf2weapons.client.ClientHandler;
import rafradek.tf2weapons.message.TF2Message.ShowGuiMessage;

public class TF2ShowGuiHandler implements TF2MessageHandler<TF2Message.ShowGuiMessage, TF2Packet> {

	@Override
	public TF2Packet onMessage(final ShowGuiMessage message, MessageContext ctx) {
		if (ctx.side == Dist.SERVER) {
			if (message.id != 99)
				TF2GuiOpener.openGui(ctx.getServerHandler().player, TF2weapons.instance, message.id,
						ctx.getServerHandler().player.world, 0, 0, 0);
			else {

			}
		} else {
			if (message.id == 100) {
				ClientHandler.createGui(message);
			}
		}
		return null;
	}

}
