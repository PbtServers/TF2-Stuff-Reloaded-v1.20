package rafradek.tf2weapons.item;

import net.minecraft.world.level.material.Material;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ISpecialArmor;

import javax.annotation.Nullable;

public class ItemParachute extends ItemBackpack implements ISpecialArmor {

	public ItemParachute() {
		this.setMaxDamage(1000);
		this.addPropertyOverride(new ResourceLocation("active"), (ItemStack stack, @Nullable Level world, @Nullable LivingEntity entityIn) -> {
			if (entityIn != null && stack.getTagCompound().getBoolean("Deployed"))
				return 1;
			return 0;
		});
	}

	@Override
	public ArmorProperties getProperties(LivingEntity player, ItemStack armor, DamageSource source, double damage,
			int slot) {
		return new ArmorProperties(0, 0, Integer.MAX_VALUE);
	}

	@Override
	public int getArmorDisplay(Player player, ItemStack armor, int slot) {
		return 0;
	}

	@Override
	public void damageArmor(LivingEntity entity, ItemStack stack, DamageSource source, int damage, int slot) {
		stack.damageItem(damage * (stack.getTagCompound().getBoolean("Deployed") ? 8 : 1), entity);
	}

	@Override
	public void onArmorTickAny(Level world, final LivingEntity player, ItemStack itemStack) {
		if (itemStack.getTagCompound().getBoolean("Deployed")) {
			player.motionY = Math.max(-0.1f, player.motionY);
			player.fallDistance = 0f;
			/*
			 * if (player.ticksExisted % 30 == 0) { itemStack.damageItem(1, player); }
			 */
			if (player.onGround || player.isInsideOfMaterial(Material.WATER))
				itemStack.getTagCompound().setBoolean("Deployed", false);
		}
	}
}
