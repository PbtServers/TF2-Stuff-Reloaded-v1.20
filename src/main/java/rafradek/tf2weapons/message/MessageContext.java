package rafradek.tf2weapons.message;

import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraftforge.api.distmarker.Dist;

public class MessageContext {
	public final Dist side;
	private final ServerGamePacketListenerImpl serverHandler;

	public MessageContext(ServerGamePacketListenerImpl serverHandler, Dist side) {
		this.serverHandler = serverHandler;
		this.side = side;
	}

	public ServerGamePacketListenerImpl getServerHandler() {
		return this.serverHandler;
	}
}
