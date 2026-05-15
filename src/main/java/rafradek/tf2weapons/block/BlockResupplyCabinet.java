package rafradek.tf2weapons.block;



import net.minecraft.core.NonNullList;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import rafradek.tf2weapons.registry.TF2BlockEntities;
import rafradek.tf2weapons.tileentity.TileEntityResupplyCabinet;
import rafradek.tf2weapons.tileentity.TileEntityRobotDeploy;

public class BlockResupplyCabinet extends BaseEntityBlock {

	public static final BooleanProperty HOLDER = BooleanProperty.create("holder");
	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

	public BlockResupplyCabinet() {
		super(BlockBehaviour.Properties.of().strength(3.0F, 6.0F).sound(SoundType.METAL));
		registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(HOLDER, true));
	}

	@Override
	public RenderShape getRenderType(BlockState state) {
		return RenderShape.MODEL;
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return state.getValue(HOLDER) ? new TileEntityResupplyCabinet(pos, state) : null;
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
			BlockEntityType<T> type) {
		return createTickerHelper(type, TF2BlockEntities.RESUPPLY_CABINET.get(),
				(tickLevel, tickPos, tickState, blockEntity) -> blockEntity.tick());
	}

	@Override
	public boolean onBlockActivated(Level world, BlockPos pos, BlockState state, Player player, InteractionHand hand, Direction side, float hitX, float hitY, float hitZ) {
		if (!world.isClientSide) {}
		return false;
	}

	@Override
	public BlockState getStateForPlacement(Level world, BlockPos pos, Direction facing, float hitX, float hitY, float hitZ, int meta, LivingEntity placer) {
		return defaultBlockState().setValue(FACING, placer.getHorizontalFacing().getOpposite());
	}

	@Override
	public void onBlockAdded(Level world, BlockPos pos, BlockState state) {
		this.updateState(world, pos, state);
	}

	private void updateState(Level world, BlockPos pos, BlockState state) {
		BlockEntity ent = world.getBlockEntity(pos);
		if (ent instanceof TileEntityResupplyCabinet && ((TileEntityResupplyCabinet) ent).redstoneActivate)
			((TileEntityResupplyCabinet) ent).setEnabled(world.hasNeighborSignal(pos));
	}

	@Override
	public void onBlockDestroyedByPlayer(Level world, BlockPos pos, BlockState state) {

	}

	@Override
	public void neighborChanged(BlockState state, Level world, BlockPos pos, Block blockIn, BlockPos fromPos) {
		this.updateState(world, fromPos, state);
	}

	@Override
	public void onBlockPlacedBy(Level world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
		world.setBlock(pos, state = state.setValue(FACING, placer.getHorizontalFacing().getOpposite()), 2);
		if (placer instanceof Player) {}
		if (world.isEmptyBlock(pos.above()))
			world.setBlock(pos.above(), state.setValue(HOLDER, false), 2);
	}

	@Override
	public void breakBlock(Level world, BlockPos pos, BlockState state) {
		if (state.getValue(HOLDER)) {
			if (world.getBlockState(pos.above()).getBlock() == this) world.removeBlock(pos.above(), false);
		} else {
			if (world.getBlockState(pos.below()).getBlock() == this) world.removeBlock(pos.below(), false);
			if (world.getBlockState(pos.above()).getBlock() == this && !world.getBlockState(pos.above()).getValue(HOLDER)) world.removeBlock(pos.above(), false);
		}
		BlockEntity ent = world.getBlockEntity(pos);
		if (ent instanceof TileEntityRobotDeploy) ((TileEntityRobotDeploy) ent).dropInventory();
	}

	@Override
	public boolean canPlaceBlockAt(Level world, BlockPos pos) {
		return super.canPlaceBlockAt(world, pos)
				&& world.getBlockState(pos.above()).getBlock().isReplaceable(world, pos.above());
	}

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
		return defaultBlockState().setValue(FACING, Direction.from3DDataValue((meta & 3) + 2)).setValue(HOLDER, (meta & 4) == 4);
	}

	@Override
	public int getMetaFromState(BlockState state) {
		return state.getValue(FACING).get3DDataValue() - 2 + (state.getValue(HOLDER) ? 4 : 0);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) { builder.add(FACING, HOLDER); }

	@Override
	@OnlyIn(Dist.CLIENT)
	public void getSubBlocks(CreativeModeTab tab, NonNullList<ItemStack> list) {
		list.add(new ItemStack(this, 1, 4));
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
	public boolean hasComparatorInputOverride(BlockState state) {
		return true;
	}

	@Override
	public int getComparatorInputOverride(BlockState blockState, Level world, BlockPos pos) {
		return  world.getBlockEntity(pos) instanceof TileEntityResupplyCabinet
				&& ((TileEntityResupplyCabinet) world.getBlockEntity(pos)).cooldownUse.size() > 0 ? 15 : 0;
	}
}
