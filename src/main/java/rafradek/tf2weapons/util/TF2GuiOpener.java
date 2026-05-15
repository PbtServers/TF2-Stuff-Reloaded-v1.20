package rafradek.tf2weapons.util;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import rafradek.tf2weapons.TF2weapons;

public final class TF2GuiOpener {
	private TF2GuiOpener() {
	}

	public static void openGui(Player player, TF2weapons mod, int id, Level level, int x, int y, int z) {
		if (player instanceof ServerPlayer serverPlayer) {
			TF2weapons.network.sendTo(new rafradek.tf2weapons.message.TF2Message.ShowGuiMessage(id), serverPlayer);
		}
	}
}
