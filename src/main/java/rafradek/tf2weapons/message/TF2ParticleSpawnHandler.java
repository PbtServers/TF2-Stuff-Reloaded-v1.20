package rafradek.tf2weapons.message;

import net.minecraft.client.Minecraft;
import rafradek.tf2weapons.message.TF2Packet;
import rafradek.tf2weapons.message.TF2MessageHandler;
import rafradek.tf2weapons.message.MessageContext;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import rafradek.tf2weapons.client.ClientHandler;
import rafradek.tf2weapons.message.TF2Message.ParticleSpawnMessage;

public class TF2ParticleSpawnHandler implements TF2MessageHandler<ParticleSpawnMessage, TF2Packet> {

	@Override
	@OnlyIn(Dist.CLIENT)
	public TF2Packet onMessage(final ParticleSpawnMessage message, MessageContext ctx) {
		Minecraft.getMinecraft().addScheduledTask(() -> {
			ClientHandler.processParticleSpawnMessage(message);
		});
		return null;
	}

}
