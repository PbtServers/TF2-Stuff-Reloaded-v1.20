package rafradek.tf2weapons.item;

import net.minecraft.client.Minecraft;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.UseAnim;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionHand;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import rafradek.tf2weapons.TF2ConfigVars;
import rafradek.tf2weapons.TF2weapons;
import rafradek.tf2weapons.common.WeaponsCapability;
import rafradek.tf2weapons.message.TF2Message.PredictionMessage;
import rafradek.tf2weapons.util.PropertyType;

import javax.annotation.Nullable;
import java.util.List;

public class ItemJar extends ItemProjectileWeapon {

	public ItemJar() {
		super();
		this.setMaxStackSize(64);
		this.addPropertyOverride(new ResourceLocation("empty"), new IItemPropertyGetter() {
			@Override
			@OnlyIn(Dist.CLIENT)
			public float apply(ItemStack stack, @Nullable Level world, @Nullable LivingEntity entityIn) {
				if (stack.getTagCompound().getBoolean("IsEmpty"))
					return 1;
				return 0;
			}
		});
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public CreativeModeTab getCreativeTab() {
		return TF2weapons.tabutilitytf2;
	}

	@Override
	public boolean canFire(Level world, LivingEntity living, ItemStack stack) {
		return !stack.getTagCompound().getBoolean("IsEmpty") && super.canFire(world, living, stack)
				&& !(living instanceof Player && ((Player) living).getCooldownTracker().hasCooldown(this));
	}

	@Override
	public String getItemStackDisplayName(ItemStack stack) {
		String string = super.getItemStackDisplayName(stack);
		if (stack.hasTagCompound() && stack.getTagCompound().getBoolean("IsEmpty"))
			string = "Empty Jar - ".concat(string);
		return string;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void addInformation(ItemStack stack, Level world, List<String> tooltip, TooltipFlag advanced) {
		if (stack.hasTagCompound() && stack.getTagCompound().getBoolean("IsEmpty"))
			tooltip.add("Right click to fill the container");
		super.addInformation(stack, world, tooltip, advanced);
	}

	@Override
	public boolean use(ItemStack stack, LivingEntity living, Level world, InteractionHand hand,
			PredictionMessage message) {
		if (super.use(stack, living, world, hand, message) && !world.isRemote) {
			if (living instanceof Player && !((Player) living).capabilities.isCreativeMode
					&& !TF2ConfigVars.freeUseItems)
				stack.shrink(1);
			if (living instanceof Player)
				((Player) living).getCooldownTracker().setCooldown(this, (int) (this.getFiringSpeed(stack, living)
						/ 50
						* (TF2ConfigVars.fastItemCooldown ? 1f : getData(stack).getFloat(PropertyType.COOLDOWN_LONG))));
		}
		return true;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public boolean showDurabilityBar(ItemStack stack) {
		Integer value = Minecraft.getMinecraft().player.getCapability(TF2weapons.WEAPONS_CAP, null).effectsCool
				.get(getData(stack).getName());
		return stack.getTagCompound().getBoolean("IsEmpty") && value != null && value > 0;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public double getDurabilityForDisplay(ItemStack stack) {
		Integer value = Minecraft.getMinecraft().player.getCapability(TF2weapons.WEAPONS_CAP, null).effectsCool
				.get(getData(stack).getName());
		return (double) (value != null ? value : 0) / (double) 1600;
	}

	@Override
	public int getMaxItemUseDuration(ItemStack stack) {
		return 40;
	}

	@Override
	public EnumAction getItemUseAction(ItemStack stack) {
		return EnumAction.DRINK;
	}

	@Override
	public void onUpdate(ItemStack stack, Level par2World, Entity par3Entity, int par4, boolean par5) {
		super.onUpdate(stack, par2World, par3Entity, par4, par5);
		if (!par2World.isRemote && par3Entity instanceof Player && stack.getTagCompound().getBoolean("IsEmpty")) {
			Integer value = WeaponsCapability.get(par3Entity).effectsCool.get(getData(stack).getName());
			if (value == null || value <= 0) {
				ItemStack newStack = stack.copy();
				newStack.setCount(1);
				newStack.getTagCompound().removeTag("IsEmpty");
				String name = getData(stack).getName();
				if (((Player) par3Entity).inventory.addItemStackToInventory(newStack) || stack.getCount() == 1) {
					if (stack.getCount() == 1)
						((Player) par3Entity).inventory.setInventorySlotContents(par4, newStack);
					stack.shrink(1);
					WeaponsCapability.get(par3Entity).addEffectCooldown(name, 1600);
				}
			}
		}
	}

	@Override
	public boolean doMuzzleFlash(ItemStack stack, LivingEntity attacker, InteractionHand hand) {
		return false;
	}
}
