package net.minecraft.core.particles;

import net.minecraft.network.FriendlyByteBuf;

public class EntityEffectParticleOptions implements ParticleOptions {
	@Override
	public ParticleType<?> getType() {
		return ParticleTypes.ENTITY_EFFECT;
	}

	@Override
	public void writeToNetwork(FriendlyByteBuf buf) {}

	@Override
	public String writeToString() {
		return "entity_effect";
	}
}
