package rafradek.tf2weapons.item;

import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.block.state.pattern.BlockMaterialPredicate;
import net.minecraft.world.level.block.state.pattern.BlockPattern;
import net.minecraft.world.level.block.state.pattern.BlockStatePredicate;
import net.minecraft.world.level.block.state.pattern.BlockPatternBuilder;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionResult;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.NonNullList;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import rafradek.tf2weapons.TF2ConfigVars;
import rafradek.tf2weapons.TF2PlayerCapability;
import rafradek.tf2weapons.TF2weapons;
import rafradek.tf2weapons.entity.boss.EntityHHH;
import rafradek.tf2weapons.entity.boss.EntityMerasmus;
import rafradek.tf2weapons.entity.boss.EntityMonoculus;
import rafradek.tf2weapons.entity.boss.EntityTF2Boss;

import java.util.List;

public class ItemBossSpawner extends Item {

	public static final String[] NAMES = { "hhh", "monoculus", "merasmus" };

	public static BlockPattern patternHHH = BlockPatternBuilder.start().aisle("NAN", "BCB", "NBN")
			.where('A', BlockInWorld.hasState(BlockStatePredicate.forBlock(Blocks.LIT_PUMPKIN)))
			.where('C', BlockInWorld.hasState(BlockStatePredicate.forBlock(Blocks.OBSIDIAN)))
			.where('B', BlockInWorld.hasState(BlockStatePredicate.forBlock(Blocks.HAY_BLOCK)))
			.where('N', BlockInWorld.hasState(BlockMaterialPredicate.forMaterial(Material.AIR))).build();

	public ItemBossSpawner() {
		super();
		this.setHasSubtypes(true);
	}

	@Override
	public String getUnlocalizedName(ItemStack stack) {
		return "item." + TF2weapons.MOD_ID + "." + NAMES[stack.getMetadata() % NAMES.length];
	}

	@Override
	public void getSubItems(CreativeModeTab par2CreativeTabs, NonNullList<ItemStack> par3List) {
		// System.out.println(this.getCreativeTab());
		if (!this.isInCreativeTab(par2CreativeTabs))
			return;
		for (int i = 0; i < NAMES.length; i++)
			par3List.add(new ItemStack(this, 1, i));
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void addInformation(ItemStack stack, Level world, List<String> tooltip, TooltipFlag advanced) {
		tooltip.add(I18n.format("item." + TF2weapons.MOD_ID + NAMES[stack.getMetadata() % NAMES.length] + ".desc"));
	}

	@Override
	public InteractionResult onItemUse(Player player, Level world, BlockPos pos, InteractionHand hand, Direction facing,
			float hitX, float hitY, float hitZ) {
		if (world.isRemote || TF2ConfigVars.disableBossSpawnItems)
			return InteractionResult.SUCCESS;
		EntityTF2Boss boss = null;
		long time = world.getWorldTime();
		ItemStack stack = player.getHeldItem(hand);
		TF2PlayerCapability cap = TF2PlayerCapability.get(player);
		if (time % 24000 < 13500 || time % 24000 > 21500) {
			player.sendMessage(new Component("gui.boss.night"));
			return InteractionResult.SUCCESS;
		}

		if (stack.getItemDamage() == 0 && cap.hhhSummonedDay < time / 24000) {
			BlockPattern.PatternHelper pattern = patternHHH.match(world, pos);
			if (world.getBlockState(pos).getBlock() == Blocks.PORTAL) {
				boss = new EntityHHH(world);
				cap.hhhSummonedDay = (int) (time / 24000);
			} else {
				player.sendMessage(new Component("gui.boss.portal"));
				return InteractionResult.SUCCESS;
			}
		} else if (stack.getItemDamage() == 1 && cap.monoculusSummonedDay < time / 24000) {
			if (world.getBlockState(pos).getBlock() == Blocks.PORTAL) {
				boss = new EntityMonoculus(world);
				cap.monoculusSummonedDay = (int) (time / 24000);
			} else {
				player.sendMessage(new Component("gui.boss.portal"));
				return InteractionResult.SUCCESS;
			}
		} else if (stack.getItemDamage() == 2 && cap.merasmusSummonedDay < time / 24000) {
			if (world.getBlockState(pos).getBlock() == Blocks.PORTAL) {
				boss = new EntityMerasmus(world);
				cap.merasmusSummonedDay = (int) (time / 24000);
			} else {
				player.sendMessage(new Component("gui.boss.portal"));
				return InteractionResult.SUCCESS;
			}
		} else {
			player.sendMessage(new Component("gui.boss.nextnight"));
		}
		if (boss != null) {
			boss.setLocationAndAngles(pos.getX() + 0.5D, pos.getY() + 0.05D, pos.getZ() + 0.5D, 0.0F, 0.0F);
			boss.onInitialSpawn(world.getDifficultyForLocation(pos), null);
			boss.summoned = true;
			world.spawnEntity(boss);
			stack.shrink(1);
		}
		return InteractionResult.SUCCESS;
	}
}
