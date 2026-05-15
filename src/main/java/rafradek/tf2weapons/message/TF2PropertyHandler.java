package rafradek.tf2weapons.message;


import net.minecraft.world.level.Level;
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
import rafradek.tf2weapons.item.ItemFromData;
import rafradek.tf2weapons.item.ItemMedigun;
import rafradek.tf2weapons.message.TF2Message.PropertyMessage;
import rafradek.tf2weapons.util.PropertyType;
import rafradek.tf2weapons.util.TF2Util;

public class TF2PropertyHandler implements TF2MessageHandler<TF2Message.PropertyMessage, TF2Packet> {

	@Override
	public TF2Packet onMessage(final PropertyMessage message, MessageContext ctx) {

		if (ctx.side.isClient())
			Minecraft.getMinecraft().addScheduledTask(() -> {
				if (Minecraft.getMinecraft().Level == null)
					return;
				Entity ent = Minecraft.getMinecraft().world.getEntityByID(message.entityID);
				if (ent != null && ent.hasCapability(TF2weapons.WEAPONS_CAP, null)) {
					if (ent instanceof LivingEntity
							&& ((LivingEntity) ent).getHeldItem(InteractionHand.MAIN_HAND) != null
							&& ((LivingEntity) ent).getHeldItem(InteractionHand.MAIN_HAND)
									.getItem() instanceof ItemMedigun)
						if (message.name.equals("HealTarget")
								&& ent.getEntityData().getInteger("HealTarget") != message.intValue
								&& message.intValue > 0) {
							SoundEvent sound = ItemFromData.getSound(
									((LivingEntity) ent).getHeldItem(InteractionHand.MAIN_HAND),
									PropertyType.HEAL_START_SOUND);
							ClientProxy.playWeaponSound((LivingEntity) ent, sound, false, 0,
									((LivingEntity) ent).getHeldItem(InteractionHand.MAIN_HAND));
						}

					if (message.type == 0)
						ent.getEntityData().setInteger(message.name, message.intValue);
					else if (message.type == 1)
						ent.getEntityData().setFloat(message.name, message.floatValue);
					else if (message.type == 2)
						ent.getEntityData().setByte(message.name, message.byteValue);
					else if (message.type == 3)
						ent.getEntityData().setString(message.name, message.stringValue);
				}
			});
		else {
			if (message.type == 0)
				ctx.getServerHandler().player.getEntityData().setInteger(message.name, message.intValue);
			else if (message.type == 1)
				ctx.getServerHandler().player.getEntityData().setFloat(message.name, message.floatValue);
			else if (message.type == 2)
				ctx.getServerHandler().player.getEntityData().setByte(message.name, message.byteValue);
			else if (message.type == 3)
				ctx.getServerHandler().player.getEntityData().setString(message.name, message.stringValue);
			// System.out.println("send: "+message.name+" "+message.intValue+"
			// "+message.floatValue);
			message.entityID = ctx.getServerHandler().player.getEntityId();
			TF2Util.sendTracking(message, ctx.getServerHandler().player);
		}
		return null;
	}

}
