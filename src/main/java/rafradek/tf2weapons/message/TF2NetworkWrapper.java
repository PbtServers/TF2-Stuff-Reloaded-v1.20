package rafradek.tf2weapons.message;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.socket.DatagramPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import rafradek.tf2weapons.TF2PlayerCapability;
import rafradek.tf2weapons.TF2weapons;
import rafradek.tf2weapons.message.udp.TF2UdpClient;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.HashSet;
import java.util.function.Supplier;

public class TF2NetworkWrapper {

	private static final String PROTOCOL_VERSION = "1";

	public boolean useUdp;
	private final SimpleChannel channel;
	public HashMap<Class<? extends TF2Packet>, TF2MessageHandler<TF2Packet, TF2Packet>> handlerList;
	public HashSet<TF2MessageHandler<TF2Packet, TF2Packet>> udpEnabled;
	public HashMap<Class<? extends TF2Packet>, Byte> discriminators;

	@SuppressWarnings("unchecked")
	public Class<? extends TF2Packet>[] messages = new Class[256];

	public TF2NetworkWrapper(String channelName) {
		this.channel = NetworkRegistry.ChannelBuilder
				.named(new ResourceLocation(TF2weapons.MOD_ID, channelName))
				.networkProtocolVersion(() -> PROTOCOL_VERSION)
				.clientAcceptedVersions(PROTOCOL_VERSION::equals)
				.serverAcceptedVersions(PROTOCOL_VERSION::equals)
				.simpleChannel();
		udpEnabled = new HashSet<>();
		handlerList = new HashMap<>();
		discriminators = new HashMap<>();
	}

	public <REQ extends TF2Packet, REPLY extends TF2Packet> void registerMessage(
			Class<? extends TF2MessageHandler<REQ, REPLY>> messageHandler, Class<REQ> requestMessageType,
			int discriminator, Dist side, boolean useUdp) {
		try {
			registerMessage(messageHandler.getDeclaredConstructor().newInstance(), requestMessageType, discriminator,
					side, useUdp);
		} catch (Exception e) {
			TF2weapons.LOGGER.error("Failed to register network message {}", requestMessageType.getName(), e);
		}
	}

	@SuppressWarnings("unchecked")
	public <REQ extends TF2Packet, REPLY extends TF2Packet> void registerMessage(
			TF2MessageHandler<? super REQ, ? extends REPLY> messageHandler, Class<REQ> requestMessageType,
			int discriminator, Dist side, boolean useUdp) {
		this.channel.registerMessage(discriminator, requestMessageType, TF2NetworkWrapper::encode,
				buf -> decode(requestMessageType, buf), (message, ctx) -> handle(messageHandler, message, ctx, side));
		if (useUdp) {
			handlerList.put(requestMessageType, (TF2MessageHandler<TF2Packet, TF2Packet>) messageHandler);
			messages[discriminator] = requestMessageType;
			discriminators.put(requestMessageType, (byte) discriminator);
		}
	}

	private static <REQ extends TF2Packet> void encode(REQ message, FriendlyByteBuf buf) {
		message.toBytes(buf);
	}

	private static <REQ extends TF2Packet> REQ decode(Class<REQ> requestMessageType, FriendlyByteBuf buf) {
		try {
			REQ message = requestMessageType.getDeclaredConstructor().newInstance();
			message.fromBytes(buf);
			return message;
		} catch (Exception e) {
			throw new IllegalStateException("Failed to decode network message " + requestMessageType.getName(), e);
		}
	}

	private static <REQ extends TF2Packet, REPLY extends TF2Packet> void handle(
			TF2MessageHandler<? super REQ, ? extends REPLY> messageHandler, REQ message,
			Supplier<NetworkEvent.Context> ctxSupplier, Dist side) {
		NetworkEvent.Context forgeCtx = ctxSupplier.get();
		forgeCtx.enqueueWork(() -> {
			ServerPlayer sender = forgeCtx.getSender();
			messageHandler.onMessage(message,
					new MessageContext(sender == null ? null : sender.connection, side));
		});
		forgeCtx.setPacketHandled(true);
	}

	public void sendToAll(TF2Packet message) {
		if (useUdp && discriminators.containsKey(message.getClass())) {
			for (Player player : TF2weapons.server.getPlayerList().getPlayers()) {
				InetSocketAddress address = TF2weapons.udpServer.outboundTargets
						.get(TF2PlayerCapability.get(player).udpServerId);
				if (address != null) {
					sendUdp(message, address, false);
				} else {
					this.sendTo(message, (ServerPlayer) player);
				}
			}
		} else {
			this.channel.send(PacketDistributor.ALL.noArg(), message);
		}
	}

	public void sendTo(TF2Packet message, ServerPlayer player) {
		if (useUdp && discriminators.containsKey(message.getClass())) {
			InetSocketAddress address = TF2weapons.udpServer.outboundTargets
					.get(TF2PlayerCapability.get(player).udpServerId);
			if (address != null) {
				sendUdp(message, address, false);
				return;
			}
		}
		this.channel.send(PacketDistributor.PLAYER.with(() -> player), message);
	}

	public void sendToServer(TF2Packet message) {
		if (useUdp && TF2UdpClient.instance != null && discriminators.containsKey(message.getClass())) {
			InetSocketAddress address = TF2UdpClient.instance.address;
			if (address != null) {
				sendUdp(message, address, true);
				return;
			}
		}
		this.channel.sendToServer(message);
	}

	public void sendToAllAround(TF2Packet message, TargetPoint point) {
		this.channel.send(PacketDistributor.NEAR.with(() -> point.toForgeTargetPoint()), message);
	}

	public void sendToDimension(TF2Packet message, ServerLevel level) {
		this.channel.send(PacketDistributor.DIMENSION.with(level::dimension), message);
	}

	public void sendToTracking(TF2Packet message, Entity entity) {
		this.channel.send(PacketDistributor.TRACKING_ENTITY.with(() -> entity), message);
	}

	private void sendUdp(TF2Packet message, InetSocketAddress address, boolean clientToServer) {
		ByteBuf buffer = Unpooled.buffer();
		if (clientToServer) {
			buffer.writeShort(TF2UdpClient.playerId);
		}
		buffer.writeShort(0);
		buffer.writeByte(discriminators.get(message.getClass()));
		message.toBytes(buffer);
		DatagramPacket packet = new DatagramPacket(buffer, address);
		if (clientToServer) {
			TF2UdpClient.instance.channel.writeAndFlush(packet);
		} else {
			TF2weapons.udpServer.channel.writeAndFlush(packet);
		}
	}
}
