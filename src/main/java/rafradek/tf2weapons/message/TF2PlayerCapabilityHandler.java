package rafradek.tf2weapons.message;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import rafradek.tf2weapons.message.TF2Packet;
import rafradek.tf2weapons.message.TF2MessageHandler;
import rafradek.tf2weapons.message.MessageContext;
import rafradek.tf2weapons.TF2PlayerCapability;
import rafradek.tf2weapons.TF2weapons;
import rafradek.tf2weapons.message.TF2Message.PlayerCapabilityMessage;

public class TF2PlayerCapabilityHandler implements TF2MessageHandler<PlayerCapabilityMessage, TF2Packet> {

	@Override
	public TF2Packet onMessage(final PlayerCapabilityMessage message, MessageContext ctx) {
		Minecraft.getMinecraft().addScheduledTask(() -> {
			Entity ent = Minecraft.getMinecraft().world.getEntityByID(message.entityID);
			if (ent != null && ent.hasCapability(TF2weapons.WEAPONS_CAP, null)) {
				TF2PlayerCapability cap = ent.getCapability(TF2weapons.PLAYER_CAP, null);
				if (message.entries != null) {
					// Legacy entity data sync is disabled until it is ported to 1.20.
				}
			}
		});
		return null;
	}

}
