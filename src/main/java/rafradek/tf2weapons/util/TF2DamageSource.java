package rafradek.tf2weapons.util;


import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.damagesource.DamageSource;

public interface TF2DamageSource {

	public static int BACKSTAB = 1;
	public static int HEADSHOT = 2;
	public static int SENTRY_PDA = 4;
	public static int SENTRY = 8;

	ItemStack getWeapon();

	ItemStack getWeaponOrig();

	// void onShieldBlock(LivingEntity living);
	int getCritical();

	DamageSource setCritical(int crit);

	void setAttackSelf();

	int getAttackFlags();

	void addAttackFlag(int flag);

	default boolean hasAttackFlag(int flag) {
		return (this.getAttackFlags() & flag) == flag;
	}

	float getAttackPower();

	void setAttackPower(float power);

}