package rafradek.tf2weapons.message;

import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerLevel;
import rafradek.tf2weapons.message.TF2Packet;
import rafradek.tf2weapons.message.TF2MessageHandler;
import rafradek.tf2weapons.message.MessageContext;
import rafradek.tf2weapons.TF2PlayerCapability;
import rafradek.tf2weapons.common.WeaponsCapability;

public class TF2InitClientHandler implements TF2MessageHandler<TF2Message.InitClientMessage, TF2Packet> {

	@Override
	public TF2Packet onMessage(TF2Message.InitClientMessage message, MessageContext ctx) {
		Player player = ctx.getServerHandler().player;
		((ServerLevel) player.world).addScheduledTask(() -> {
			TF2PlayerCapability.get(player).breakBlocks = message.breakBlocks;
			WeaponsCapability.get(player).sentryTargets = message.sentryTargets;
			WeaponsCapability.get(player).dispenserPlayer = message.dispenserPlayer;
			WeaponsCapability.get(player).teleporterPlayer = message.teleporterPlayer;
			WeaponsCapability.get(player).teleporterEntity = message.teleporterEntity;
		});
		// System.out.println("setting "+message.value);
		// TF2weapons.proxy.playReloadSound(player,stack);
		return null;
	}
}
