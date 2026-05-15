package rafradek.tf2weapons.client.audio;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionHand;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvent;
import rafradek.tf2weapons.TF2ConfigVars;
import rafradek.tf2weapons.client.ClientProxy;
import rafradek.tf2weapons.item.ItemFromData;
import rafradek.tf2weapons.util.WeaponData;

public class WeaponSound extends AbstractTickableSoundInstance {
	public LivingEntity entity;
	public int type;
	public WeaponData conf;
	public boolean endsnextTick;
	public WeaponSound playOnEnd;

	public WeaponSound(SoundEvent sound, LivingEntity entity, int type, WeaponData conf) {
		super(sound, SoundSource.NEUTRAL);
		this.type = type;
		this.entity = entity;
		this.conf = conf;
		volume = entity instanceof Player ? TF2ConfigVars.gunVolume : TF2ConfigVars.mercenaryVolume;
	}

	@Override
	public void update() {
		if (endsnextTick) setDone();
		xPosF = (float) entity.posX;
		yPosF = (float) entity.posY;
		zPosF = (float) entity.posZ;
		if (/*
			 * !(entity instanceof Player &&
			 * ((Player)entity).inventory.currentItem != slot)
			 */ItemFromData.getData(entity.getHeldItem(InteractionHand.MAIN_HAND)) != conf || entity.isDead) setDone();
	}

	public void setDone() {
		ClientProxy.fireSounds.remove(entity);
		if (playOnEnd != null) {
			Minecraft.getMinecraft().getSoundHandler().playSound(playOnEnd);
			ClientProxy.fireSounds.put(entity, playOnEnd);
		}
		donePlaying = true;
	}
}
