package rafradek.tf2weapons.message;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.InteractionHand;
import net.minecraft.sounds.SoundEvent;
import rafradek.tf2weapons.message.TF2Packet;
import rafradek.tf2weapons.message.TF2MessageHandler;
import rafradek.tf2weapons.message.MessageContext;
import rafradek.tf2weapons.TF2weapons;
import rafradek.tf2weapons.client.ClientProxy;
import rafradek.tf2weapons.common.WeaponsCapability;
import rafradek.tf2weapons.item.ItemFromData;
import rafradek.tf2weapons.message.TF2Message.CapabilityMessage;
import rafradek.tf2weapons.util.PropertyType;

public class TF2CapabilityHandler implements TF2MessageHandler<TF2Message.CapabilityMessage, TF2Packet> {

	@Override
	public TF2Packet onMessage(final CapabilityMessage message, MessageContext ctx) {
		if (ctx.side.isClient())
			Minecraft.getMinecraft().addScheduledTask(() -> {
				Entity ent = Minecraft.getMinecraft().world.getEntityByID(message.entityID);
				if (ent != null && ent.hasCapability(TF2weapons.WEAPONS_CAP, null)) {
					WeaponsCapability cap = ent.getCapability(TF2weapons.WEAPONS_CAP, null);
					int prevHealTarget = cap.getHealTarget();
					if (message.entries != null) {
						// Legacy entity data sync is disabled until it is ported to 1.20.
					}
					if (prevHealTarget != cap.getHealTarget() && cap.getHealTarget() > 0) {
						SoundEvent sound = ItemFromData.getSound(
								((LivingEntity) ent).getHeldItem(InteractionHand.MAIN_HAND),
								PropertyType.HEAL_START_SOUND);
						ClientProxy.playWeaponSound((LivingEntity) ent, sound, false, 0,
								((LivingEntity) ent).getHeldItem(InteractionHand.MAIN_HAND));
					}
					cap.ticksTotal = message.totalTime;
					// cap.critTime = message.critTime;
					// cap.collectedHeads = message.heads;
				}
			});
		/*
		 * if(ent !=null){ ent.getEntityData().setTag("TF2", message.tag); }
		 */
		else {
			/*
			 * ctx.getServerHandler().player.getCapability(TF2weapons.WEAPONS_CAP,
			 * null).setHealTarget(message.healTarget);
			 */
			/*
			 * message.entityID = ctx.getServerHandler().player.getEntityId();
			 * TF2weapons.sendTracking(message, ctx.getServerHandler().player);
			 */
		}
		return null;
	}

}
