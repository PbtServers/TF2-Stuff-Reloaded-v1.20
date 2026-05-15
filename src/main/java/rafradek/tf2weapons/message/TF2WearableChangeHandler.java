package rafradek.tf2weapons.message;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import rafradek.tf2weapons.message.TF2Packet;
import rafradek.tf2weapons.message.TF2MessageHandler;
import rafradek.tf2weapons.message.MessageContext;
import rafradek.tf2weapons.TF2weapons;
import rafradek.tf2weapons.entity.mercenary.EntityTF2Character;
import rafradek.tf2weapons.message.TF2Message.WearableChangeMessage;

public class TF2WearableChangeHandler implements TF2MessageHandler<TF2Message.WearableChangeMessage, TF2Packet> {

	@Override
	public TF2Packet onMessage(final WearableChangeMessage message, MessageContext ctx) {
		Minecraft.getMinecraft().addScheduledTask(() -> {
			Entity entity = Minecraft.getMinecraft().world.getEntityByID(message.entityID);
			if (entity != null) {
				if (message.slot < 20)
					entity.getCapability(TF2weapons.INVENTORY_CAP, null).setInventorySlotContents(message.slot,
							message.stack);
				else if (entity instanceof EntityTF2Character) {
					((EntityTF2Character) entity).loadout.setStackInSlot(message.slot - 20, message.stack);
				}
			}

		});
		return null;
	}

}
