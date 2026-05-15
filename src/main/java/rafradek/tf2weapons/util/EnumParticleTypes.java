package rafradek.tf2weapons.util;

import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.EntityEffectParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.block.Blocks;
import org.joml.Vector3f;

public final class EnumParticleTypes {
	public static final ParticleOptions BLOCK_CRACK = new BlockParticleOption(ParticleTypes.BLOCK,
			Blocks.STONE.defaultBlockState());
	public static final ParticleOptions CLOUD = ParticleTypes.CLOUD;
	public static final ParticleOptions CRIT = ParticleTypes.CRIT;
	public static final ParticleOptions CRIT_MAGIC = ParticleTypes.ENCHANTED_HIT;
	public static final ParticleOptions DRAGON_BREATH = ParticleTypes.DRAGON_BREATH;
	public static final ParticleOptions END_ROD = ParticleTypes.END_ROD;
	public static final ParticleOptions EXPLOSION_LARGE = ParticleTypes.EXPLOSION_EMITTER;
	public static final ParticleOptions EXPLOSION_NORMAL = ParticleTypes.EXPLOSION;
	public static final ParticleOptions FIREWORKS_SPARK = ParticleTypes.FIREWORK;
	public static final ParticleOptions FLAME = ParticleTypes.FLAME;
	public static final ParticleOptions HEART = ParticleTypes.HEART;
	public static final ParticleOptions NOTE = ParticleTypes.NOTE;
	public static final ParticleOptions PORTAL = ParticleTypes.PORTAL;
	public static final ParticleOptions REDSTONE = new DustParticleOptions(new Vector3f(1.0F, 0.0F, 0.0F), 1.0F);
	public static final ParticleOptions SLIME = ParticleTypes.ITEM_SLIME;
	public static final ParticleOptions SMOKE_LARGE = ParticleTypes.LARGE_SMOKE;
	public static final ParticleOptions SMOKE_NORMAL = ParticleTypes.SMOKE;
	public static final ParticleOptions SPELL_MOB = EntityEffectParticleOptions.create(ParticleTypes.ENTITY_EFFECT,
			0x7F7FFF);
	public static final ParticleOptions SUSPENDED_DEPTH = ParticleTypes.UNDERWATER;
	public static final ParticleOptions VILLAGER_ANGRY = ParticleTypes.ANGRY_VILLAGER;
	public static final ParticleOptions VILLAGER_HAPPY = ParticleTypes.HAPPY_VILLAGER;
	public static final ParticleOptions WATER_BUBBLE = ParticleTypes.BUBBLE;

	private EnumParticleTypes() {
	}
}
