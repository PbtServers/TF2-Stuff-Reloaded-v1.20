package rafradek.tf2weapons.message;

import net.minecraft.client.Minecraft;
import rafradek.tf2weapons.message.TF2Packet;
import rafradek.tf2weapons.message.TF2MessageHandler;
import rafradek.tf2weapons.message.MessageContext;
import rafradek.tf2weapons.message.TF2Message.VelocityAddMessage;

public class TF2VelocityAddHandler implements TF2MessageHandler<TF2Message.VelocityAddMessage, TF2Packet> {

	@Override
	public TF2Packet onMessage(final VelocityAddMessage message, MessageContext ctx) {
		Minecraft.getMinecraft().player.addVelocity(message.x, message.y, message.z);
		Minecraft.getMinecraft().player.isAirBorne = message.airborne;

		return null;
	}

}
