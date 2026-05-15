package rafradek.tf2weapons.message;

import net.minecraft.client.Minecraft;
import rafradek.tf2weapons.message.TF2Packet;
import rafradek.tf2weapons.message.TF2MessageHandler;
import rafradek.tf2weapons.message.MessageContext;
import rafradek.tf2weapons.TF2weapons;
import rafradek.tf2weapons.common.WeaponsCapability;
import rafradek.tf2weapons.message.TF2Message.EffectCooldownMessage;

public class TF2EffectCooldownHandler implements TF2MessageHandler<TF2Message.EffectCooldownMessage, TF2Packet> {

	@Override
	public TF2Packet onMessage(final EffectCooldownMessage message, MessageContext ctx) {
		if (ctx.side.isClient())
			Minecraft.getMinecraft().addScheduledTask(() -> {
				WeaponsCapability cap = Minecraft.getMinecraft().player.getCapability(TF2weapons.WEAPONS_CAP, null);
				cap.effectsCool.put(message.name, message.time);
				// cap.critTime = message.critTime;
				// cap.collectedHeads = message.heads;
			});
		return null;
	}

}
