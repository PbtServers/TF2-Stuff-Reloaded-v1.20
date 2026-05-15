package rafradek.tf2weapons.client.audio;

import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvent;
import rafradek.tf2weapons.entity.building.EntityBuilding;

public class BuildingSound extends AbstractTickableSoundInstance {

	public EntityBuilding sentry;
	private int state;

	public BuildingSound(EntityBuilding sentry, SoundEvent location, int state) {
		super(location, SoundSource.NEUTRAL);
		this.sentry = sentry;
		volume = 0.65f;
		repeat = true;
		this.state = state;
	}

	@Override
	public void update() {
		xPosF = (float) sentry.posX;
		yPosF = (float) sentry.posY;
		zPosF = (float) sentry.posZ;
		if (sentry.getHealth() <= 0 || sentry.isDead) stopPlaying();
	}

	public void stopPlaying() {
		donePlaying = true;
	}

}
