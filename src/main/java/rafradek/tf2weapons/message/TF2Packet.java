package rafradek.tf2weapons.message;

import io.netty.buffer.ByteBuf;

public interface TF2Packet {
	void fromBytes(ByteBuf buf);

	void toBytes(ByteBuf buf);
}
