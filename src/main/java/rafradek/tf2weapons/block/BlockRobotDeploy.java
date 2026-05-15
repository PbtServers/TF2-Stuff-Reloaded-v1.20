package rafradek.tf2weapons.block;



import net.minecraft.core.NonNullList;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
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
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import rafradek.tf2weapons.TF2weapons;
import rafradek.tf2weapons.registry.TF2BlockEntities;
import rafradek.tf2weapons.tileentity.TileEntityRobotDeploy;

public class BlockRobotDeploy extends BaseEntityBlock {

	public static final BooleanProperty HOLDER = BooleanProperty.create("holder");
	public static final BooleanProperty ACTIVE = BooleanProperty.create("active");
	public static final BooleanProperty JOINED = BooleanProperty.create("joined");
	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

	public BlockRobotDeploy() {
		super(BlockBehaviour.Properties.of().strength(3.0F, 6.0F).sound(SoundType.METAL));
		this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH)
				.setValue(HOLDER, true).setValue(JOINED, false).setValue(ACTIVE, false));
	}

	@Override
	public RenderShape getRenderType(BlockState state) {
		return RenderShape.MODEL;
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return state.getValue(HOLDER) ? new TileEntityRobotDeploy(pos, state) : null;
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
			BlockEntityType<T> type) {
		return createTickerHelper(type, TF2BlockEntities.ROBOT_DEPLOY.get(),
				(tickLevel, tickPos, tickState, blockEntity) -> blockEntity.tick());
	}

	@Override
	public boolean onBlockActivated(Level world, BlockPos pos, BlockState state, Player player,
			InteractionHand hand, Direction side, float hitX, float hitY, float hitZ) {
		if (!world.isClientSide) {
			if (state.getValue(HOLDER))
				{ /* TODO 1.20.1: abrir menu con NetworkHooks.openScreen cuando los menus esten porteados. */ }
			for (int x = -1; x < 2; x++)
				for (int y = -1; y < 1; y++)
					for (int z = -1; z < 2; z++)
						if (world.getBlockState(pos.add(x, y, z)).getBlock() instanceof BlockRobotDeploy
								&& world.getBlockState(pos.add(x, y, z)).getValue(HOLDER)) {
							{ /* TODO 1.20.1: abrir menu con NetworkHooks.openScreen cuando los menus esten porteados. */ }
							return true;
						}

		}
		return true;
	}

	@Override
	public BlockState getStateForPlacement(Level world, BlockPos pos, Direction facing, float hitX, float hitY,
			float hitZ, int meta, LivingEntity placer) {
		return this.defaultBlockState().setValue(FACING, placer.getHorizontalFacing().getOpposite());
	}

	@Override
	public void onBlockDestroyedByPlayer(Level world, BlockPos pos, BlockState state) {

	}

	@Override
	public void onBlockPlacedBy(Level world, BlockPos pos, BlockState state, LivingEntity placer,
			ItemStack stack) {
		world.setBlock(pos, state = state.setValue(FACING, placer.getHorizontalFacing().getOpposite()), 2);

		if (placer instanceof Player) {
			BlockEntity ent = world.getBlockEntity(pos);
			for (Direction facing : Direction.HORIZONTALS) {
				BlockPos offpos = pos.relative(facing);
				BlockEntity entoff = world.getBlockEntity(offpos);
				if (entoff instanceof TileEntityRobotDeploy && !world.getBlockState(offpos).getValue(JOINED)
						&& world.getBlockState(offpos).getValue(HOLDER)) {
					Direction placefacing = facing.rotateY();
					if (placefacing == placer.getHorizontalFacing()) {
						CompoundTag tag = new CompoundTag();
						entoff.writeToNBT(tag);
						tag.setInteger("x", pos.getX());
						tag.setInteger("y", pos.getY());
						tag.setInteger("z", pos.getZ());
						((TileEntityRobotDeploy) ent).readFromNBT(tag);
						world.setBlock(offpos, world.getBlockState(offpos).setValue(HOLDER, false));
						world.setBlockEntity(offpos, null);
						placefacing = placefacing.getOpposite();
					} else {
						state = state.setValue(HOLDER, false);
					}
					state = state.setValue(FACING, placefacing).setValue(JOINED, true);
					world.setBlock(pos, state);
					world.setBlock(offpos,
							world.getBlockState(offpos).setValue(FACING, placefacing).setValue(JOINED, true));
					if (world.getBlockState(offpos.above()).getBlock() == this)
						world.setBlock(offpos.above(), world.getBlockState(offpos.above())
								.setValue(FACING, placefacing).setValue(JOINED, true));
					break;
				}
			}

			/*
			 * BlockEntity entbelow = world.getBlockEntity(pos.below()); BlockEntity entup =
			 * world.getBlockEntity(pos.above()); BlockState statebelow =
			 * world.getBlockState(pos.below()); BlockState stateup =
			 * world.getBlockState(pos.above()); if (entbelow instanceof
			 * TileEntityRobotDeploy && !world.getBlockState(pos.below()).getValue(JOINED))
			 * { world.setBlock(pos, state = state.setValue(JOINED, true));
			 * world.setBlock(pos, state = state.setValue(HOLDER, false));
			 * world.setBlockEntity(pos, null); world.setBlock(pos.below(),
			 * statebelow = statebelow.setValue(JOINED, true)); } if (entup instanceof
			 * TileEntityRobotDeploy && !world.getBlockState(pos.above()).getValue(JOINED)) {
			 * world.setBlock(pos, state.setValue(JOINED, true)); CompoundTag
			 * tag=new CompoundTag();
			 * ((TileEntityRobotDeploy)ent).readFromNBT(entup.writeToNBT(tag));
			 * world.setBlock(pos.above(), stateup = stateup.setValue(HOLDER,
			 * false)); world.setBlockEntity(pos.above(), null);
			 * world.setBlock(pos.above(), stateup = stateup.setValue(JOINED,
			 * true)); }
			 */
			if (ent instanceof TileEntityRobotDeploy)
				((TileEntityRobotDeploy) ent).setOwner(placer.getName(), placer.getUniqueID());
		}
		if (world.isEmptyBlock(pos.above()))
			world.setBlock(pos.above(), state.setValue(HOLDER, false), 2);
	}

	@Override
	public void breakBlock(Level world, BlockPos pos, BlockState state) {

		if (state.getValue(HOLDER)) {
			if (world.getBlockState(pos.above()).getBlock() == this) {
				world.removeBlock(pos.above(), false);
			}
		} else {
			if (world.getBlockState(pos.below()).getBlock() == this) {
				world.removeBlock(pos.below(), false);
			}
			if (world.getBlockState(pos.above()).getBlock() == this
					&& !world.getBlockState(pos.above()).getValue(HOLDER)) {
				world.removeBlock(pos.above(), false);
			}
		}

		if (state.getValue(JOINED)) {
			if (state.getValue(HOLDER) || (world.getBlockState(pos.below()).getBlock() == this
					&& world.getBlockState(pos.below()).getValue(HOLDER)))
				world.destroyBlock(pos.relative(state.getValue(FACING).rotateY()), true);
			else
				world.destroyBlock(pos.relative(state.getValue(FACING).rotateYCCW()), true);
		}

		BlockEntity ent = world.getBlockEntity(pos);
		if (ent instanceof TileEntityRobotDeploy) {
			((TileEntityRobotDeploy) ent).dropInventory();
		}
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
		return this.defaultBlockState().setValue(FACING, Direction.from3DDataValue((meta & 3) + 2))
				.setValue(HOLDER, (meta & 4) == 4).setValue(JOINED, (meta & 8) == 8);
	}

	@Override
	public int getMetaFromState(BlockState state) {
		return state.getValue(FACING).get3DDataValue() - 2 + (state.getValue(HOLDER) ? 4 : 0)
				+ (state.getValue(JOINED) ? 8 : 0);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) { builder.add(FACING, HOLDER, JOINED, ACTIVE); }

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
}
