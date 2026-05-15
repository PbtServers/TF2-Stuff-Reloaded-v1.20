package rafradek.tf2weapons.util;



import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.util.EntityDamageSourceIndirect;
import net.minecraft.world.phys.Vec3;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Component;
import net.minecraft.client.resources.language.I18n;
import rafradek.tf2weapons.TF2weapons;
import rafradek.tf2weapons.entity.projectile.EntityProjectileBase;

@SuppressWarnings("deprecation")
public class DamageSourceProjectile extends EntityDamageSourceIndirect implements TF2DamageSource {
	public ItemStack weapon;
	public int critical;
	public boolean notProjectile;
	public boolean selfdmg;
	private int attackFlags;
	private float power = 1;

	public DamageSourceProjectile(ItemStack weapon, Entity projectile, Entity shooter) {
		super("bullet", projectile, shooter);
		this.weapon = weapon;
	}

	@Override
	public boolean isDifficultyScaled() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see rafradek.tf2weapons.TF2DamageSource#getWeapon()
	 */
	@Override
	public ItemStack getWeapon() {
		return weapon;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see rafradek.tf2weapons.TF2DamageSource#getCritical()
	 */
	@Override
	public int getCritical() {
		return critical;
	}

	/*
	 * public DamageSource bypassesArmor() { this.setDamageBypassesArmor(); return
	 * this; }
	 */
	/**
	 * Returns the message to be displayed on player death.
	 */
	@Override
	public Component getDeathMessage(LivingEntity p_151519_1_) {
		// ItemStack itemstack = this.damageSourceEntity instanceof
		// LivingEntity ?
		// ((LivingEntity)this.damageSourceEntity).getHeldItem(InteractionHand.MAIN_HAND)
		// : null;
		if (this.getTrueSource() == TF2weapons.dummyEnt)
			return Component.translatable("death.attack.explosion", p_151519_1_.getDisplayName());
		String s = "death.attack." + this.damageType;
		String s1 = s + ".item";
		return weapon != null && I18n.canTranslate(s1)
				? Component.translatable(s1, p_151519_1_.getDisplayName(), this.getTrueSource().getDisplayName(),
						weapon.getDisplayName())
				: Component.translatable(s, p_151519_1_.getDisplayName(), this.getTrueSource().getDisplayName());
	}

	/*
	 * public String getDeathMessage(Player par1EntityPlayer) { return
	 * StatCollector.translateToLocalFormatted("death." + this.damageType, new
	 * Object[] {par1EntityPlayer.getDisplayName(),
	 * this.shooter.getCommandSenderName(),
	 * StatCollector.translateToLocal(this.weapon)}); }
	 */
	@Override
	public Vec3 getDamageLocation() {
		return this.isExplosion() ? null : this.damageSourceEntity.getPositionVector();
	}

	public void removeProjecileStatus() {
		this.notProjectile = true;
	}

	@Override
	public boolean isProjectile() {
		return super.isProjectile() && !this.notProjectile;
	}

	@Override
	public Entity getTrueSource() {
		return selfdmg ? TF2weapons.dummyEnt : super.getTrueSource();
	}

	@Override
	public void setAttackSelf() {
		this.selfdmg = true;
	}

	@Override
	public ItemStack getWeaponOrig() {
		return this.getImmediateSource() instanceof EntityProjectileBase
				&& !((EntityProjectileBase) this.getImmediateSource()).usedWeaponOrig.isEmpty()
						? ((EntityProjectileBase) this.getImmediateSource()).usedWeaponOrig
						: this.getWeapon();
	}

	@Override
	public int getAttackFlags() {
		return this.attackFlags;
	}

	@Override
	public void addAttackFlag(int flag) {
		this.attackFlags += flag;
	}

	@Override
	public float getAttackPower() {
		return this.power;
	}

	@Override
	public void setAttackPower(float power) {
		this.power = power;
	}

	@Override
	public DamageSource setCritical(int crit) {
		this.critical = crit;
		return this;
	}

	/*
	 * @Override public void onShieldBlock(LivingEntity living) {}
	 */

}
