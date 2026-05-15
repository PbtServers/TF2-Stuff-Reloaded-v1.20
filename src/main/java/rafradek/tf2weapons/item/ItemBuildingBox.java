package rafradek.tf2weapons.item;




import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import rafradek.tf2weapons.TF2weapons;
import rafradek.tf2weapons.util.TF2Class;

import java.util.List;

@SuppressWarnings("deprecation")
public class ItemBuildingBox extends ItemMonsterPlacerPlus implements IItemNoSwitch {
	public ItemBuildingBox() {
		this.setCreativeTab(TF2weapons.tabspawnertf2);
		this.setUnlocalizedName(TF2weapons.MOD_ID + ".buildingbox");
		this.setHasSubtypes(true);
		this.setMaxStackSize(1);
	}

	@Override
	public int getItemStackLimit(ItemStack stack) {
		return stack.hasTagCompound() && stack.getTagCompound().hasKey("MaxStack")
				? stack.getTagCompound().getInteger("MaxStack")
				: 1;
	}

	@Override
	public void getSubItems(CreativeModeTab par2CreativeTabs, NonNullList<ItemStack> par3List) {
		if (!this.isInCreativeTab(par2CreativeTabs))
			return;
		for (int i = 18; i < 24; i++)
			par3List.add(new ItemStack(this, 1, i));
	}

	@Override
	public InteractionResult onItemUse(Player player, Level world, BlockPos pos, InteractionHand hand,
			Direction facing, float hitX, float hitY, float hitZ) {
		if (ItemToken.allowUse(player, TF2Class.ENGINEER)) {
			return super.onItemUse(player, world, pos, hand, facing, hitX, hitY, hitZ);
		} else {
			return InteractionResult.FAIL;
		}
	}

	@Override
	public InteractionResultHolder<ItemStack> onItemRightClick(Level world, Player player, InteractionHand hand) {
		if (ItemToken.allowUse(player, TF2Class.ENGINEER)) {
			return super.onItemRightClick(world, player, hand);
		} else {
			return new InteractionResultHolder<>(InteractionResult.FAIL, player.getHeldItem(hand));
		}
	}

	@Override
	public String getItemStackDisplayName(ItemStack stack) {
		// (I18n.translateToLocal(this.getUnlocalizedName()+".name")).trim();
		int i = stack.getItemDamage() / 2;
		String s1 = "sentry";
		if (stack.hasTagCompound() && stack.getTagCompound().getBoolean("Mini"))
			s1 += "mini";
		switch (i) {
		case 10:
			s1 = "dispenser";
			break;
		case 11:
			s1 = "teleporter";
			break;
		}
		return I18n.translateToLocal(this.getUnlocalizedName() + "." + s1 + ".name");
	}

	@OnlyIn(Dist.CLIENT)
	public int colorMultiplier(ItemStack p_82790_1_, int p_82790_2_) {
		return 16777215;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void addInformation(ItemStack stack, Level world, List<String> tooltip, TooltipFlag advanced) {
		if (Minecraft.getMinecraft().player != null && Minecraft.getMinecraft().player.isCreative()) {
			tooltip.add("Hold " + KeyBinding.getDisplayString("key.sneak").get() + " to spawn natural building");
		}
	}

	@Override
	public void onUpdate(ItemStack stack, Level world, Entity entityIn, int itemSlot, boolean isSelected) {
		this.forceItemSlot(stack, world, entityIn, itemSlot, isSelected);
	}

	@Override
	public boolean stopSlotSwitch(ItemStack stack, LivingEntity living) {
		return stack.hasTagCompound() && stack.getTagCompound().getCompoundTag("SavedEntity") != null;
	}
}
