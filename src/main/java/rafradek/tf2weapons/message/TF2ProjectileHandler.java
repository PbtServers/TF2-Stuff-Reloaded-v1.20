package rafradek.tf2weapons.message;


import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.server.level.ServerLevel;
import rafradek.tf2weapons.message.TF2Packet;
import rafradek.tf2weapons.message.TF2MessageHandler;
import rafradek.tf2weapons.message.MessageContext;
import rafradek.tf2weapons.TF2weapons;
import rafradek.tf2weapons.message.TF2Message.PredictionMessage;

import java.util.ArrayList;
import java.util.Deque;

public class TF2ProjectileHandler implements TF2MessageHandler<TF2Message.PredictionMessage, TF2Packet> {

	// public static HashMap<Entity, ArrayList<PredictionMessage>> nextShotPos=
	// new HashMap<Entity, ArrayList<PredictionMessage>>();

	@Override
	public TF2Packet onMessage(final PredictionMessage message, MessageContext ctx) {
		final Player shooter = ctx.getServerHandler().player;
		// ItemStack stack=shooter.getHeldItem(InteractionHand.MAIN_HAND);
		((ServerLevel) shooter.world).addScheduledTask(() -> {
			// shooter.getCapability(TF2weapons.WEAPONS_CAP, null).predictionList.poll();
			message.target = new ArrayList<>();
			if (message.readData != null)
				for (Object[] obj : message.readData) {
					HitResult result;

					if (obj[0] != null) {
						Entity entity = shooter.world.getEntityByID((int) obj[0]);
						Vec3 hit1 = new Vec3((Byte) obj[6] / 16D + entity.posX, (Byte) obj[7] / 16D + entity.posY,
								(Byte) obj[8] / 16D + entity.posZ);
						result = new HitResult(entity, hit1);
						result.hitInfo = new float[] { (Boolean) obj[1] ? 1f : 0f, (Float) obj[2] };
					} else {
						BlockPos pos = new BlockPos((Integer) obj[3], (Integer) obj[4], (Integer) obj[5]);
						Vec3 hit2 = new Vec3((Byte) obj[6] / 16D + pos.getX(), (Byte) obj[7] / 16D + pos.getY(),
								(Byte) obj[8] / 16D + pos.getZ());
						result = new HitResult(hit2, Direction.getFront((Byte) obj[1]), pos);
						result.hitInfo = new float[] { (Float) obj[2] };
					}
					message.target.add(result);
				}
			Deque<PredictionMessage> deque = shooter.getCapability(TF2weapons.WEAPONS_CAP,
					null).predictionList[(message.state == 1 ? 0 : 2) + message.hand.ordinal()];
			deque.addLast(message);
			message.time = shooter.world.getTotalWorldTime();
		});
		return null;
	}

}
