package rafradek.tf2weapons.entity.building;

import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.Level;
import rafradek.tf2weapons.client.audio.TF2Sounds;
import rafradek.tf2weapons.item.ItemWrench;
import rafradek.tf2weapons.util.TF2DamageSource;
import rafradek.tf2weapons.util.TF2Util;

public class EntitySapper extends EntityBuilding {

	public EntityBuilding sappedBuilding;
	public ItemStack sapperItem;

	public EntitySapper(Level world) {
		super(world);
		this.setSize(1f, 1.1f);
	}

	@Override
	public void onLivingUpdate() {
		super.onLivingUpdate();
		if (!this.world.isRemote) {
			if (this.sappedBuilding == null || !this.sappedBuilding.isEntityAlive()) {
				this.setDead();
				return;
			}
			TF2Util.dealDamage(this.sappedBuilding, this.world, this.getOwner(), sapperItem, 0, 0.25f,
					TF2Util.causeBulletDamage(sapperItem, this.getOwner(), this));

			if (!this.isEntityAlive() && this.sappedBuilding != null)
				this.sappedBuilding.sapper = null;
		}

	}

	@Override
	public SoundEvent getSoundNameForState(int state) {
		switch (state) {
		case 0:
			return TF2Sounds.MOB_SAPPER_IDLE;
		default:
			return null;
		}
	}

	@Override
	public boolean attackEntityFrom(DamageSource source, float amount) {
		if ((source instanceof TF2DamageSource) && !((TF2DamageSource) source).getWeapon().isEmpty()
				&& ((TF2DamageSource) source).getWeapon().getItem() instanceof ItemWrench)
			super.attackEntityFrom(source, amount);
		return false;
	}

	@Override
	protected void entityInit() {
		super.entityInit();
	}

	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(12D);
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return null;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return TF2Sounds.MOB_SAPPER_DEATH;
	}

	@Override
	public void writeEntityToNBT(CompoundTag par1NBTTagCompound) {
		super.writeEntityToNBT(par1NBTTagCompound);
	}

	@Override
	public void readEntityFromNBT(CompoundTag par1NBTTagCompound) {
		super.readEntityFromNBT(par1NBTTagCompound);
	}
}
