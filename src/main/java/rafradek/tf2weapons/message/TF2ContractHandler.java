package rafradek.tf2weapons.message;

import net.minecraft.client.Minecraft;
import rafradek.tf2weapons.message.TF2Packet;
import rafradek.tf2weapons.message.TF2MessageHandler;
import rafradek.tf2weapons.message.MessageContext;
import rafradek.tf2weapons.TF2weapons;
import rafradek.tf2weapons.message.TF2Message.ContractMessage;
import rafradek.tf2weapons.util.Contract;

import java.util.ArrayList;

public class TF2ContractHandler implements TF2MessageHandler<TF2Message.ContractMessage, TF2Packet> {

	public static int size;

	@Override
	public TF2Packet onMessage(final ContractMessage message, MessageContext ctx) {

		if (Minecraft.getMinecraft().player == null)
			return null;
		ArrayList<Contract> contracts = Minecraft.getMinecraft().player.getCapability(TF2weapons.PLAYER_CAP,
				null).contracts;
		if (message.id == -1) {
			contracts.add(message.contract);
			Minecraft.getMinecraft().player.getCapability(TF2weapons.PLAYER_CAP, null).newContracts = true;
		} else if (contracts.size() <= message.id) {
			contracts.add(message.id, message.contract);
			if (message.contract.rewards > 0) {
				Minecraft.getMinecraft().player.getCapability(TF2weapons.PLAYER_CAP, null).newRewards = true;
			}
		} else {
			Contract prev = contracts.set(message.id, message.contract);
			if (prev.rewards == 0 && message.contract.rewards > 0) {
				Minecraft.getMinecraft().player.getCapability(TF2weapons.PLAYER_CAP, null).newRewards = true;
			}
		}
		/*
		 * Minecraft.getMinecraft().addScheduledTask(new Runnable(){
		 * 
		 * @Override public void run() { //System.out.println("Wep drop "+message.name);
		 * ItemStack stack=ItemFromData.getNewStack(message);
		 * ((ItemUsable)stack.getItem()).holster(Minecraft.getMinecraft().player.
		 * getCapability(TF2weapons.WEAPONS_CAP, null), stack,
		 * Minecraft.getMinecraft().player, Minecraft.getMinecraft().world);
		 * 
		 * }
		 * 
		 * });
		 */

		return null;
	}

}
