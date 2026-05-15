package rafradek.tf2weapons.item;



import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionResult;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import rafradek.tf2weapons.TF2weapons;
import rafradek.tf2weapons.entity.EntityStatue;

import java.util.List;

@SuppressWarnings("deprecation")
public class ItemStatue extends Item {

	public ItemStatue() {
		this.setUnlocalizedName(TF2weapons.MOD_ID + ".statue");
	}

	@Override
	public InteractionResult onItemUse(Player player, Level world, BlockPos pos, InteractionHand hand,
			Direction facing, float hitX, float hitY, float hitZ) {
		ItemStack stack = player.getHeldItem(hand);
		if (!world.isRemote && stack.hasTagCompound() && stack.getTagCompound().hasKey("Statue")) {
			EntityStatue statue = new EntityStatue(world);
			statue.readEntityFromNBT(stack.getTagCompound().getCompoundTag("Statue"));
			BlockPos off = pos.offset(facing);
			statue.setPosition(off.getX() + 0.5, off.getY(), off.getZ() + 0.5);
			statue.rotationYaw = player.rotationYawHead;
			statue.renderYawOffset = player.rotationYawHead;

			statue.ticksLeft = -1;
			world.spawnEntity(statue);
			if (!player.capabilities.isCreativeMode)
				stack.shrink(1);
			return InteractionResult.SUCCESS;
		}
		return InteractionResult.SUCCESS;
	}

	public static ItemStack getStatue(EntityStatue statue) {
		ItemStack stack = new ItemStack(TF2weapons.itemStatue);
		stack.setTagCompound(new CompoundTag());
		CompoundTag tag = new CompoundTag();
		statue.writeEntityToNBT(tag);
		stack.getTagCompound().setTag("Statue", tag);
		return stack;
	}

	@Override
	public String getItemStackDisplayName(ItemStack stack) {
		if (!stack.hasTagCompound())
			return super.getItemStackDisplayName(stack);
		if (!stack.getTagCompound().getCompoundTag("Statue").getBoolean("Player"))
			return I18n.translateToLocal("entity."
					+ EntityList.getTranslationName(new ResourceLocation(
							stack.getTagCompound().getCompoundTag("Statue").getCompoundTag("Entity").getString("id")))
					+ ".name") + " " + I18n.translateToLocal(this.getUnlocalizedName() + ".name");
		else
			return I18n.translateToLocal(this.getUnlocalizedName() + ".player.name");
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void addInformation(ItemStack stack, Level world, List<String> tooltip, TooltipFlag advanced) {
		if (stack.hasTagCompound()) {
			if (stack.getTagCompound().getCompoundTag("Statue").getBoolean("Player")) {
				tooltip.add(
						stack.getTagCompound().getCompoundTag("Statue").getCompoundTag("Profile").getString("Name"));
			}
		}
	}

}
