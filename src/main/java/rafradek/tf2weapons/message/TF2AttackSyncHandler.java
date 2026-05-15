package rafradek.tf2weapons.message;

import net.minecraft.world.entity.player.Player;
import rafradek.tf2weapons.message.TF2Packet;
import rafradek.tf2weapons.message.TF2MessageHandler;
import rafradek.tf2weapons.message.MessageContext;
import rafradek.tf2weapons.TF2weapons;

public class TF2AttackSyncHandler implements TF2MessageHandler<TF2Message.AttackSyncMessage, TF2Packet> {

	@Override
	public TF2Packet onMessage(TF2Message.AttackSyncMessage message, MessageContext ctx) {
		Player player = TF2weapons.proxy.getPlayerForSide(ctx);
		// System.out.println("Time: "+(System.currentTimeMillis() - message.time));
		/*
		 * if (player != null){ System.out.println("Delay: "+message.time);
		 * if(message.time>1) {
		 * WeaponsCapability.get(player).fire1Cool+=50*(message.time-1);
		 * WeaponsCapability.get(player).fire2Cool+=50*(message.time-1); } }
		 */
		// System.out.println("setting "+message.value);
		// TF2weapons.proxy.playReloadSound(player,stack);
		return null;
	}
}
