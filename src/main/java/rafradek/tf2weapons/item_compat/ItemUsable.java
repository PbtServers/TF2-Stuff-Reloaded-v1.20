package rafradek.tf2weapons.item;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import rafradek.tf2weapons.util.PropertyType;

public class ItemUsable extends ItemFromData implements IWeaponItem {
	public boolean use(ItemStack stack, LivingEntity living, Level level, int oldState, int newState) { return false; }
	public boolean isAmmoSufficient(ItemStack stack, LivingEntity living, boolean all) { return true; }
	public int getAmmoType(ItemStack stack) { return 0; }
	public int getAmmoAmount(LivingEntity owner, ItemStack stack) { return 0; }
	public int getActualAmmoUse(ItemStack stack, LivingEntity living, int amount) { return amount; }
	public void consumeAmmoGlobal(LivingEntity living, ItemStack stack, int amount) {}
	public boolean canFire(Level level, LivingEntity living, ItemStack stack) { return true; }
	public boolean canAltFire(Level level, LivingEntity living, ItemStack stack) { return true; }
	public boolean canSwitchTo(ItemStack stack) { return true; }
	public boolean startUse(ItemStack stack, LivingEntity living, Level level, int action, int newState) { return true; }
	public void endUse(ItemStack stack, LivingEntity living, Level level, int action, int newState) {}
	public float getWeaponSpreadBase(ItemStack stack, LivingEntity living) { return 0; }
	public float getWeaponMinDamage(ItemStack stack, LivingEntity living) { return 1; }
	public double getDoubleWieldBonus(ItemStack stack, LivingEntity living) { return 1; }
	public int getClip(ItemStack stack) { return 0; }
	public int getMaxClip(ItemStack stack) { return 0; }
	public boolean hasClip(ItemStack stack) { return false; }
	public PropertyType<?> getDataType() { return null; }
	public static boolean isAltFire(Player player) { return false; }
}
