package rafradek.tf2weapons.client.audio;

import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;

import net.minecraft.world.entity.Entity;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvent;

public class ReloadSound extends AbstractTickableSoundInstance {

	public boolean done;

	public ReloadSound(SoundEvent soundResource, Entity entity) {
		super(soundResource, SoundSource.NEUTRAL);
		xPosF = (float) entity.posX;
		yPosF = (float) entity.posY;
		zPosF = (float) entity.posZ;
		volume = 0.6f;
	}

	@Override
	public void update() {}

	@Override
	public boolean isStopped() {
		return done;
	}

}
