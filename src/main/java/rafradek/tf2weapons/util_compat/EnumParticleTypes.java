package rafradek.tf2weapons.util;

public enum EnumParticleTypes {
	EXPLOSION_NORMAL,
	REDSTONE,
	SPELL_MOB,
	FLAME,
	SMOKE_NORMAL,
	CRIT,
	CRIT_MAGIC;

	public int getRange() {
		return 64;
	}
}
