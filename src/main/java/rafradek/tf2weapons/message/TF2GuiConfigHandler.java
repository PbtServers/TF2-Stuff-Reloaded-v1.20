package rafradek.tf2weapons.message;

import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.server.level.ServerLevel;
import rafradek.tf2weapons.message.TF2Packet;
import rafradek.tf2weapons.message.TF2MessageHandler;
import rafradek.tf2weapons.message.MessageContext;
import net.minecraftforge.api.distmarker.Dist;
import rafradek.tf2weapons.client.ClientHandler;
import rafradek.tf2weapons.message.TF2Message.GuiConfigMessage;
import rafradek.tf2weapons.tileentity.IEntityConfigurable;

import java.util.HashMap;

public class TF2GuiConfigHandler implements TF2MessageHandler<TF2Message.GuiConfigMessage, TF2Packet> {

	public static HashMap<Entity, float[]> shotInfo = new HashMap<>();

	@Override
	public TF2Packet onMessage(final GuiConfigMessage message, final MessageContext ctx) {

		if (ctx.side == Dist.SERVER) {
			final ServerPlayer player = ctx.getServerHandler().player;
			((ServerLevel) player.world).addScheduledTask(() -> {
				BlockEntity ent = player.world.getTileEntity(message.getPos());
				if (ent instanceof IEntityConfigurable) {
					((IEntityConfigurable) ent).readConfig(message.getTag());
				}
				/*
				 * if (player.openContainer instanceof ContainerConfigurable) {
				 * ((ContainerConfigurable)player.openContainer).config.readConfig(message.tag);
				 * }
				 */
			});
		} else ClientHandler.createConfigGui(message);

		return null;
	}

}
