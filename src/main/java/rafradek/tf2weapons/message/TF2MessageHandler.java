package rafradek.tf2weapons.message;

public interface TF2MessageHandler<REQ extends TF2Packet, REPLY extends TF2Packet> {
	REPLY onMessage(REQ message, MessageContext ctx);
}
