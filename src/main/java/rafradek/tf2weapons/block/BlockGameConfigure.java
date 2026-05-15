package rafradek.tf2weapons.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import rafradek.tf2weapons.registry.TF2BlockEntities;
import rafradek.tf2weapons.tileentity.TileEntityGameConfigure;
import rafradek.tf2weapons.tileentity.TileEntityResupplyCabinet;

public class BlockGameConfigure extends BaseEntityBlock {

	public BlockGameConfigure() {
		super(BlockBehaviour.Properties.of().strength(3.0F, 6.0F).sound(SoundType.METAL));
	}

	@Override
	public RenderShape getRenderType(BlockState state) {
		return RenderShape.MODEL;
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new TileEntityGameConfigure(pos, state);
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
			BlockEntityType<T> type) {
		return createTickerHelper(type, TF2BlockEntities.GAME_CONFIGURE.get(),
				(tickLevel, tickPos, tickState, blockEntity) -> blockEntity.tick());
	}

	@Override
	public boolean onBlockActivated(Level world, BlockPos pos, BlockState state, Player player, InteractionHand hand, Direction side, float hitX, float hitY, float hitZ) {
		if (!world.isClientSide) {}
		return false;
	}

	@Override
	public void onBlockAdded(Level world, BlockPos pos, BlockState state) {
		this.updateState(world, pos, state);
	}

	private void updateState(Level world, BlockPos pos, BlockState state) {}

	@Override
	public void onBlockDestroyedByPlayer(Level world, BlockPos pos, BlockState state) {}

	@Override
	public void neighborChanged(BlockState state, Level world, BlockPos pos, Block blockIn, BlockPos fromPos) {
		this.updateState(world, fromPos, state);
	}

	@Override
	public void onBlockPlacedBy(Level world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {}

	@Override
	public void breakBlock(Level world, BlockPos pos, BlockState state) {
		BlockEntity ent = world.getBlockEntity(pos);
		if (ent instanceof TileEntityGameConfigure) ((TileEntityGameConfigure) ent).removeGameArena();
	}

	@Override
	public boolean canPlaceBlockAt(Level world, BlockPos pos) {
		return super.canPlaceBlockAt(world, pos)
				&& world.getBlockState(pos.up()).getBlock().isReplaceable(world, pos.up());
	}

	@Override
	public boolean hasComparatorInputOverride(BlockState state) {
		return true;
	}

	@Override
	public int getComparatorInputOverride(BlockState blockState, Level world, BlockPos pos) {
		return world.getBlockEntity(pos) instanceof TileEntityResupplyCabinet &&
				((TileEntityResupplyCabinet) world.getBlockEntity(pos)).cooldownUse.size() > 0 ? 15 : 0;
	}
}
