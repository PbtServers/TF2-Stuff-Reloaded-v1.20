package rafradek.tf2weapons.message;

import net.minecraft.client.Minecraft;
import rafradek.tf2weapons.message.TF2Packet;
import rafradek.tf2weapons.message.TF2MessageHandler;
import rafradek.tf2weapons.message.MessageContext;
import rafradek.tf2weapons.client.ClientHandler;
import rafradek.tf2weapons.message.TF2Message.WeaponDroppedMessage;

public class TF2WeaponDropHandler implements TF2MessageHandler<TF2Message.WeaponDroppedMessage, TF2Packet> {

	@Override
	public TF2Packet onMessage(final WeaponDroppedMessage message, MessageContext ctx) {
		Minecraft.getMinecraft().addScheduledTask(() -> ClientHandler.playerDropWeapon(message.name));
		return null;
	}

}
