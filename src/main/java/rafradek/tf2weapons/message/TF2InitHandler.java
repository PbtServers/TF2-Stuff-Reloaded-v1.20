package rafradek.tf2weapons.message;

import io.netty.util.internal.SocketUtils;
import rafradek.tf2weapons.config.Property;
import rafradek.tf2weapons.message.TF2Packet;
import rafradek.tf2weapons.message.TF2MessageHandler;
import rafradek.tf2weapons.message.MessageContext;
import rafradek.tf2weapons.TF2ConfigVars;
import rafradek.tf2weapons.TF2weapons;
import rafradek.tf2weapons.client.ClientProxy;
import rafradek.tf2weapons.message.udp.TF2UdpClient;

import java.util.Map.Entry;

public class TF2InitHandler implements TF2MessageHandler<TF2Message.InitMessage, TF2Packet> {

	@Override
	public TF2Packet onMessage(TF2Message.InitMessage message, MessageContext ctx) {
		try {
			if (message.port != -1) {
				TF2UdpClient.instance = new TF2UdpClient(
						SocketUtils.socketAddress(TF2UdpClient.addressToUse, message.port));
				TF2UdpClient.playerId = message.id;
				TF2weapons.network.useUdp = false;
			}
			for (Entry<String, Property> entry : message.property.entries()) {
				TF2weapons.conf.getCategory(entry.getKey()).get(entry.getValue().getName())
						.set(entry.getValue().getString());
				TF2ConfigVars.createConfig(false);
			}
			ClientProxy.buildingsUseEnergy = message.energyUse;

		} catch (Exception e) {
			e.printStackTrace();
		}
		// System.out.println("setting "+message.value);
		// TF2weapons.proxy.playReloadSound(player,stack);
		return null;
	}
}
