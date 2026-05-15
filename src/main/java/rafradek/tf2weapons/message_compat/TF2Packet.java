package rafradek.tf2weapons.message;

import io.netty.buffer.ByteBuf;

public interface TF2Packet {
	default void fromBytes(ByteBuf buf) {}
	default void toBytes(ByteBuf buf) {}
}
