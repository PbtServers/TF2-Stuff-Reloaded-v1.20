package rafradek.tf2weapons.util;



import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.world.phys.Vec3;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Component;
import net.minecraft.client.resources.language.I18n;
import rafradek.tf2weapons.TF2weapons;

@SuppressWarnings("deprecation")
public class DamageSourceDirect extends EntityDamageSource implements TF2DamageSource {
	public ItemStack weapon;
	public int critical;
	private boolean selfdmg;
	private int attackFlags;

	public float power = 1;

	public DamageSourceDirect(ItemStack weapon, Entity shooter) {
		super("bullet", shooter);
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
		String s = "death.attack." + this.damageType;
		String s1 = s + ".item";
		return !weapon.isEmpty() && I18n.canTranslate(s1)
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
		return this.isProjectile() ? null : this.damageSourceEntity.getPositionVector();
	}

	@Override
	public ItemStack getWeaponOrig() {
		return this.getWeapon();
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
	 * @Override public void onShieldBlock(LivingEntity living) {
	 * if(!this.getWeapon().isEmpty() && this.getWeapon().getItem() instanceof
	 * ItemBulletWeapon && !(this.getWeapon().getItem() instanceof ItemMeleeWeapon))
	 *
	 * }
	 */
}
