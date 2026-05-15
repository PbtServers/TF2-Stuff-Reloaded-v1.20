package rafradek.tf2weapons.message;

import net.minecraft.world.entity.Entity;
import net.minecraft.util.Mth;
import rafradek.tf2weapons.message.TF2Packet;
import rafradek.tf2weapons.message.TF2MessageHandler;
import rafradek.tf2weapons.message.MessageContext;
import rafradek.tf2weapons.TF2weapons;
import rafradek.tf2weapons.entity.building.EntityBuilding;
import rafradek.tf2weapons.entity.building.EntitySentry;
import rafradek.tf2weapons.entity.building.EntityTeleporter;
import rafradek.tf2weapons.message.TF2Message.BuildingConfigMessage;

import java.util.HashMap;

public class TF2BuildingConfigHandler implements TF2MessageHandler<TF2Message.BuildingConfigMessage, TF2Packet> {

	public static HashMap<Entity, float[]> shotInfo = new HashMap<>();

	@Override
	public TF2Packet onMessage(final BuildingConfigMessage message, final MessageContext ctx) {

		TF2weapons.server.addScheduledTask(() -> {
			Entity ent = ctx.getServerHandler().player.world.getEntityByID(message.entityid);
			if (ent != null && ent instanceof EntityBuilding
					&& ((EntityBuilding) ent).getOwner() == ctx.getServerHandler().player) {
				if (message.id == 127) {
					((EntityBuilding) ent).grab();
					return;
				}

				if (ent instanceof EntityTeleporter) {
					if (message.id == 0)
						((EntityTeleporter) ent)
								.setID(Mth.clamp(message.value, 0, EntityTeleporter.TP_PER_PLAYER - 1));
					else if (message.id == 1)
						((EntityTeleporter) ent).setExit(message.value == 1);
				} else if (ent instanceof EntitySentry)
					if (message.id == 0)
						((EntitySentry) ent).setTargetInfo(message.value);
			}
		});

		return null;
	}

}
