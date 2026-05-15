package rafradek.tf2weapons.message.udp;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import net.minecraftforge.api.distmarker.Dist;
import rafradek.tf2weapons.TF2weapons;
import rafradek.tf2weapons.message.MessageContext;
import rafradek.tf2weapons.message.TF2MessageHandler;
import rafradek.tf2weapons.message.TF2Packet;

public class UdpChannelHandlerClient extends SimpleChannelInboundHandler<DatagramPacket> {

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket msg) throws Exception {
		ByteBuf buffer = msg.content();

		int seq = buffer.readUnsignedShort();
		int msgid = buffer.readByte();

		TF2Packet message = TF2weapons.network.messages[msgid].newInstance();
		// buffer.discardReadBytes();
		message.fromBytes(buffer);
		TF2MessageHandler<TF2Packet, TF2Packet> handler = TF2weapons.network.handlerList.get(message.getClass());
		MessageContext context = new MessageContext(null, Dist.CLIENT);
		handler.onMessage(message, context);
		// System.out.println("PacketFrom: "+msg.sender().getAddress()+ "
		// "+msg.sender().getPort()+" ");

	}

}
