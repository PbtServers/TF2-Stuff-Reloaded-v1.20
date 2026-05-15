package rafradek.tf2weapons.message;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

public class TF2NetworkWrapper {
	public boolean useUdp;
	public void sendToAll(TF2Packet message) {}
	public void sendTo(TF2Packet message, ServerPlayer player) {}
	public void sendToServer(TF2Packet message) {}
	public void sendToAllAround(TF2Packet message, TargetPoint point) {}
	public void sendToDimension(TF2Packet message, ServerLevel level) {}
	public void sendToTracking(TF2Packet message, Entity entity) {}
}
