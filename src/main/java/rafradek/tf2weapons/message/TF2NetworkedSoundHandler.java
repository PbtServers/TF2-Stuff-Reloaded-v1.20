package rafradek.tf2weapons.message;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.world.entity.Entity;
import rafradek.tf2weapons.message.TF2Packet;
import rafradek.tf2weapons.message.TF2MessageHandler;
import rafradek.tf2weapons.message.MessageContext;
import rafradek.tf2weapons.client.audio.NetworkedSound;
import rafradek.tf2weapons.message.TF2Message.NetworkedSoundMessage;

import java.util.HashMap;

public class TF2NetworkedSoundHandler implements TF2MessageHandler<TF2Message.NetworkedSoundMessage, TF2Packet> {

	HashMap<Integer, SoundInstance> sounds = new HashMap<>();

	@Override
	public TF2Packet onMessage(final NetworkedSoundMessage message, MessageContext ctx) {
		Minecraft.getMinecraft().addScheduledTask(() -> {
			if (sounds.containsKey(message.id))
				sounds.remove(message.id);
			NetworkedSound sound;
			if (message.pos != null)
				sound = new NetworkedSound(message.pos, message.event, message.category, message.volume, message.pitch,
						message.id, message.repeat);
			else {
				Entity entity = Minecraft.getMinecraft().world.getEntityByID(message.target);
				sound = new NetworkedSound(entity, message.event, message.category, message.volume, message.pitch,
						message.id, message.repeat);
			}
			sounds.put(message.id, sound);
			Minecraft.getMinecraft().getSoundHandler().isSoundPlaying(sound);
		});
		return null;
	}

}
