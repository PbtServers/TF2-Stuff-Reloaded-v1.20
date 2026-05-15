package rafradek.tf2weapons.client;

import net.minecraft.world.entity.LivingEntity;
import rafradek.tf2weapons.message.TF2Message;

public class ClientHandler {
	public static LivingEntity getClientEntity(int entityId) {
		return null;
	}

	public static void createConfigGui(TF2Message.GuiConfigMessage message) {}

	public static void createGui(TF2Message.ShowGuiMessage message) {}

	public static void playerDropWeapon(String name) {}

	public static void processParticleSpawnMessage(TF2Message.ParticleSpawnMessage message) {}
}
