package rafradek.tf2weapons.item;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import rafradek.tf2weapons.TF2ConfigVars;
import rafradek.tf2weapons.TF2weapons;
import rafradek.tf2weapons.common.TF2Attribute;
import rafradek.tf2weapons.common.WeaponsCapability;
import rafradek.tf2weapons.common.WeaponsCapability.RageType;
import rafradek.tf2weapons.entity.mercenary.EntityTF2Character;
import rafradek.tf2weapons.util.PropertyType;
import rafradek.tf2weapons.util.TF2Util;

import javax.annotation.Nullable;

public class ItemSoldierBackpack extends ItemBackpack {

	public static ItemStack horn = ItemStack.EMPTY;

	public ItemSoldierBackpack() {
		super();
		this.addPropertyOverride(new ResourceLocation("active"), (ItemStack stack, @Nullable Level world, @Nullable LivingEntity entityIn) -> {
			if (entityIn != null && WeaponsCapability.get(entityIn) != null
					&& WeaponsCapability.get(entityIn).isRageActive(RageType.BANNER))
				return 1;
			return 0;
		});

	}

	public Potion getBuff(ItemStack stack) {
		return Potion.getPotionFromResourceLocation(getData(stack).getString(PropertyType.EFFECT_TYPE));
	}

	@Override
	public RageType getRageType(ItemStack stack, LivingEntity living) {
		return RageType.BANNER;
	}

	@Override
	public float getMaxRage(ItemStack stack, LivingEntity living) {
		return 1f;
	}

	@Override
	public ItemStack getBackpackItemToUse(ItemStack stack, LivingEntity player) {
		if (horn.isEmpty())
			horn = new ItemStack(TF2weapons.itemHorn);
		return horn;
	}

	public void addRage(ItemStack stack, float damage, LivingEntity target, LivingEntity attacker) {
		if (target instanceof EntityTF2Character && !(attacker instanceof Player))
			damage *= 0.5f;
		else if (!(target instanceof Player))
			damage *= 0.35f;
		this.addRage(stack, attacker, damage / getData(stack).getFloat(PropertyType.DAMAGE));
	}

	@Override
	public void onArmorTickAny(Level world, final LivingEntity player, ItemStack itemStack) {
		super.onArmorTickAny(world, player, itemStack);
		if (!world.isRemote) {
			if (player.ticksExisted % 5 == 0 && WeaponsCapability.get(player).isRageActive(RageType.BANNER)) {
				/*
				 * ); itemStack.getTagCompound().setFloat("Rage", Math.max(0f,
				 * itemStack.getTagCompound().getFloat("Rage") - 1f / duration)); if
				 * (itemStack.getTagCompound().getFloat("Rage") <= 0)
				 * itemStack.getTagCompound().setBoolean("Active", false);
				 */
				for (LivingEntity living : world.getEntitiesWithinAABB(LivingEntity.class,
						player.getEntityBoundingBox().grow(10, 10, 10), input -> TF2Util.isOnSameTeam(player, input)))
					TF2Util.addAndSendEffect(living, new MobEffectInstance(this.getBuff(itemStack), 25));
			}
		}
	}

	public void setActive(LivingEntity player, ItemStack stack) {
		float duration = TF2Attribute.getModifier("Effect Duration", stack,
				getData(stack).getInt(PropertyType.DURATION), player) * (TF2ConfigVars.longDurationBanner ? 2 : 5);
		WeaponsCapability.get(player).setRageActive(RageType.BANNER, true, (1f / duration) * 20f);
	}
}
