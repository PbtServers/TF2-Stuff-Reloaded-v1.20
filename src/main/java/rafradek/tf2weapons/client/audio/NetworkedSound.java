package rafradek.tf2weapons.client.audio;

import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.phys.Vec3;

public class NetworkedSound extends AbstractTickableSoundInstance {

	private Entity parent;
	private boolean isStatic;
	private int id;

	public NetworkedSound(Entity parent, SoundEvent soundIn, SoundSource categoryIn, float volume, float pitch,
			int id, boolean repeat) {
		super(soundIn, categoryIn);
		this.parent = parent;
		this.volume = volume;
		this.pitch = pitch;
		this.id = id;
		this.repeat = repeat;
	}

	public NetworkedSound(Vec3 pos, SoundEvent soundIn, SoundSource categoryIn, float volume, float pitch, int id,
			boolean repeat) {
		super(soundIn, categoryIn);
		isStatic = true;
		this.volume = volume;
		this.pitch = pitch;
		this.id = id;
		this.repeat = repeat;
		xPosF = (float) pos.x;
		yPosF = (float) pos.y;
		zPosF = (float) pos.z;
	}

	@Override
	public void update() {
		if (isStatic) return;
		if (parent != null && !parent.isDead) {
			xPosF = (float) parent.posX;
			yPosF = (float) parent.posY;
			zPosF = (float) parent.posZ;
		} else donePlaying = true;
	}

}
