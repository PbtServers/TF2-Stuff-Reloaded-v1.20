package rafradek.tf2weapons.block;



import net.minecraft.core.NonNullList;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import rafradek.tf2weapons.TF2weapons;
import rafradek.tf2weapons.registry.TF2BlockEntities;
import rafradek.tf2weapons.tileentity.TileEntityUpgrades;

import java.util.Random;

public class BlockUpgradeStation extends BaseEntityBlock {

	public static final BooleanProperty HOLDER = BooleanProperty.create("holder");
	public static final BooleanProperty PLACED = BooleanProperty.create("placed");
	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

	public BlockUpgradeStation() {
		super(BlockBehaviour.Properties.of().strength(3.0F, 6.0F).sound(SoundType.METAL));
		registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH)
				.setValue(HOLDER, true).setValue(PLACED, false));	}

	@Override
	public RenderShape getRenderType(BlockState state) {
		return RenderShape.MODEL;
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return state.getValue(PLACED) ? new TileEntityUpgrades(pos, state) : null;
	}

	@Override
	public boolean onBlockActivated(Level world, BlockPos pos, BlockState state, Player player, InteractionHand hand, Direction side, float hitX, float hitY, float hitZ) {
		if (!world.isClientSide)
			if (state.getValue(HOLDER)) { /* TODO 1.20.1: abrir menu con NetworkHooks.openScreen cuando los menus esten porteados. */ }
			else for (int x = -1; x < 2; x++)
				for (int y = -1; y < 2; y++)
					for (int z = -1; z < 2; z++)
						if (world.getBlockState(pos.add(x, y, z)).getBlock() instanceof BlockUpgradeStation
								&& world.getBlockState(pos.add(x, y, z)).getValue(HOLDER)) {
							{ /* TODO 1.20.1: abrir menu con NetworkHooks.openScreen cuando los menus esten porteados. */ }
							return true;
						}
		return true;
	}

	@Override
	@Deprecated
	public float getBlockHardness(BlockState blockState, Level world, BlockPos pos) {
		return blockState.getValue(PLACED) ? blockHardness : -1;
	}

	@Override
	public void breakBlock(Level world, BlockPos pos, BlockState state) {
		BlockEntity BlockEntity = world.getBlockEntity(pos);
		if (state.getValue(PLACED) && BlockEntity instanceof TileEntityUpgrades) {
			ItemStack itemstack = new ItemStack(Item.getItemFromBlock(this));
			CompoundTag nbt = new CompoundTag();
			CompoundTag tetag = new CompoundTag();
			((TileEntityUpgrades) BlockEntity).writeToNBT(tetag);
			nbt.removeTag("id");
			nbt.setTag("BlockEntityTag", tetag);
			itemstack.setTagCompound(nbt);
			spawnAsEntity(world, pos, itemstack);
		}
		super.breakBlock(world, pos, state);
	}

	@Override
	public BlockState getStateForPlacement(Level world, BlockPos pos, Direction facing, float hitX, float hitY, float hitZ, int meta, LivingEntity placer) {
		return defaultBlockState().setValue(FACING, placer.getHorizontalFacing().getOpposite());
	}

	@Override
	public void onBlockPlacedBy(Level world, BlockPos pos, BlockState state, LivingEntity placer,
			ItemStack stack) {
		state = state.setValue(FACING, placer.getHorizontalFacing().getOpposite()).setValue(PLACED, true);
		world.setBlock(pos, state, 2);
		Direction dirop = placer.getHorizontalFacing().rotateY();
		BlockState statehelp = state.setValue(HOLDER, false);
		if (world.isEmptyBlock(pos.relative(dirop)))
			world.setBlock(pos.relative(dirop), statehelp);
		if (world.isEmptyBlock(pos.relative(dirop, -1)))
			world.setBlock(pos.relative(dirop, -1), statehelp);
		if (world.isEmptyBlock(pos.relative(dirop, -1).up()))
			world.setBlock(pos.relative(dirop, -1).up(), statehelp);
		if (world.isEmptyBlock(pos.up()))
			world.setBlock(pos.up(), statehelp);
		if (world.isEmptyBlock(pos.relative(dirop).up()))
			world.setBlock(pos.relative(dirop).up(), statehelp);
	}

	@Override
	public void onBlockDestroyedByPlayer(Level world, BlockPos pos, BlockState state) {
		super.onBlockDestroyedByPlayer(world, pos, state);
		if (state.getValue(HOLDER))
			breakBlockAround(world, pos, state);
		for (int x = -1; x < 2; x++)
			for (int y = -1; y < 2; y++)
				for (int z = -1; z < 2; z++)
					if (world.getBlockState(pos.add(x, y, z)).getBlock() instanceof BlockUpgradeStation
							&& world.getBlockState(pos.add(x, y, z)).getValue(HOLDER)) {
						breakBlockAround(world, pos.add(x, y, z), state);
						world.destroyBlock(pos.add(x, y, z), true);
						return;
					}
	}

	public void breakBlockAround(Level world, BlockPos pos, BlockState state) {
		for (int x = -1; x <= 1; x++) {
			for (int y = -1; y <= 1; y++) {
				for (int z = -1; z <= 1; z++) {
					if (world.getBlockState(pos.add(x, y, z)).getBlock() instanceof BlockUpgradeStation
							&& !world.getBlockState(pos.add(x, y, z)).getValue(HOLDER)) {
						world.removeBlock(pos.add(x, y, z, false));
					}
				}
			}
		}

	}

	/**
	 * Returns the blockstate with the given mirror of the passed blockstate. If
	 * inapplicable, returns the passed blockstate.
	 */
	@Override
	public BlockState withRotation(BlockState state, Rotation rot) {
		return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
	}

	@Override
	public BlockState withMirror(BlockState state, Mirror mirrorIn) {
		return state.withRotation(mirrorIn.toRotation(state.getValue(FACING)));
	}

	@Override
	public BlockState getStateFromMeta(int meta) {
		return this.defaultBlockState().setValue(FACING, Direction.from3DDataValue((meta & 3) + 2))
				.setValue(HOLDER, (meta & 8) == 8).setValue(PLACED, (meta & 4) == 4);
	}

	@Override
	public int getMetaFromState(BlockState state) {
		return state.getValue(FACING).get3DDataValue() - 2 + (state.getValue(HOLDER) ? 8 : 0)
				+ (state.getValue(PLACED) ? 4 : 0);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) { builder.add(FACING, HOLDER, PLACED); }

	@Override
	@OnlyIn(Dist.CLIENT)
	public void getSubBlocks(CreativeModeTab tab, NonNullList<ItemStack> list) {
		list.add(new ItemStack(this, 1, 8));
	}

	@Override
	public boolean isOpaqueCube(BlockState state) {
		return false;
	}

	@Override
	public boolean isFullCube(BlockState state) {
		return false;
	}

	@Override
	public Item getItemDropped(BlockState state, Random rand, int fortune) {
		return null;
	}
}
