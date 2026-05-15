package rafradek.tf2weapons.item;

import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.NonNullList;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.items.ItemHandlerHelper;
import rafradek.tf2weapons.TF2weapons;
import rafradek.tf2weapons.tileentity.TileEntityRobotDeploy;
import rafradek.tf2weapons.util.TF2Class;

import java.util.List;

public class ItemTF2 extends Item {

	public static final String[] NAMES = new String[] { "ingotCopper", "ingotLead", "ingotAustralium", "scrapMetal",
			"reclaimedMetal", "refinedMetal", "nuggetAustralium", "key", "crate", "randomWeapon", "randomHat",
			"logicBoard", "robotPartsMedium", "robotPartsGiant" };

	public ItemTF2() {
		this.setHasSubtypes(true);
		this.setCreativeTab(TF2weapons.tabsurvivaltf2);
		this.setUnlocalizedName(TF2weapons.MOD_ID + ".tf2item");
	}

	@Override
	public String getUnlocalizedName(ItemStack stack) {
		StringBuilder name =  new StringBuilder("item.");
		name.append(TF2weapons.MOD_ID);
		name.append(".");
		name.append(NAMES[stack.getMetadata() % NAMES.length]);
		if (stack.hasTagCompound() && stack.getTagCompound().getBoolean("Australium")) name.append("Australium");
		return name.toString();
	}

	@Override
	public int getItemStackLimit(ItemStack stack) {
		return (stack.getMetadata() == 9 || stack.getMetadata() == 10) ? 1 : 64;
	}

	@Override
	public void getSubItems(CreativeModeTab par2CreativeTabs, NonNullList<ItemStack> par3List) {
		// System.out.println(this.getCreativeTab());
		if (!this.isInCreativeTab(par2CreativeTabs))
			return;
		for (int i = 0; i < 8; i++)
			par3List.add(new ItemStack(this, 1, i));
		par3List.add(new ItemStack(this, 1, 11));
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void addInformation(ItemStack stack, Level world, List<String> tooltip, TooltipFlag advanced) {
		if (stack.hasTagCompound() && stack.getTagCompound().hasKey("Token")) {
			tooltip.add(TF2Class.getClass(stack.getTagCompound().getByte("Token")).getLocalizedName().getFormattedText());
		}
		if (stack.getMetadata() == 12 || stack.getMetadata() == 13)
			tooltip.add("Right click to gain robot parts");
	}

	@Override
	public InteractionResultHolder<ItemStack> onItemRightClick(Level world, Player living, InteractionHand hand) {
		ItemStack stack = living.getHeldItem(hand);
		if (stack.getMetadata() == 12 || stack.getMetadata() == 13) {
			if (!world.isRemote) {
				int notDropPiece = living.getRNG().nextInt(ItemRobotPart.LEVEL.length);
				for (int i = 0; i < ItemRobotPart.LEVEL.length; i++) {
					if (i != notDropPiece)
						ItemHandlerHelper.giveItemToPlayer(living,
								new ItemStack(TF2weapons.itemRobotPart,
										stack.getMetadata() == 13
												? TileEntityRobotDeploy.GIANT_REQUIRE[ItemRobotPart.LEVEL[i]]
												: TileEntityRobotDeploy.NORMAL_REQUIRE[ItemRobotPart.LEVEL[i]],
										i),
								0);
				}
				stack.shrink(1);
			}
			return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
		} else if (stack.getMetadata() == 9 || stack.getMetadata() == 10) {
			if (!world.isRemote) {
				ItemStack weapon = ItemFromData.getRandomWeapon(stack, living.getRNG());
				living.setHeldItem(hand, weapon);
			}
			return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
		} else
			return new InteractionResultHolder<>(InteractionResult.PASS, stack);
	}

}
