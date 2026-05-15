package rafradek.tf2weapons.tileentity;

import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.scores.Team;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import rafradek.tf2weapons.TF2weapons;
import rafradek.tf2weapons.block.BlockOverheadDoor;
import rafradek.tf2weapons.registry.TF2BlockEntities;
import rafradek.tf2weapons.util.TF2Util;

public class TileEntityOverheadDoor extends BlockEntity  {

	public float amountScrolled = 1;
	public Team team;
	public Allow allow;
	boolean entitySome;
	// boolean master;
	public BlockPos minBounds;
	public BlockPos maxBounds;
	public float motion;
	public boolean powered;
	public boolean hasEntity;
	public boolean clientOpen;
	public boolean lastIsEntity;
	public long tickTime;

	public enum Allow {
		ENTITY, PLAYER, TEAM
	}

	public TileEntityOverheadDoor(BlockPos pos, BlockState state) {
		super(TF2BlockEntities.OVERHEAD_DOOR.get(), pos, state);
	}

	public float getUpSpeed() {
		return 0.25f;
	}

	public float getDownSpeed() {
		return 0.25f;
	}

	@Override
	public void setRemoved() {

		if (this.hasLevel()) {
			for (Direction facing : Direction.HORIZONTALS) {
				BlockPos sidepos = this.worldPosition.relative(facing);
				BlockEntity ent = this.level.getBlockEntity(sidepos);
				if (ent instanceof TileEntityOverheadDoor) {
					((TileEntityOverheadDoor) ent).updateMasterStatus();
				}
			}
			this.dropController();
		}
		super.setRemoved();
	}

	@Override
	public void tick() {

		if (minBounds == null)
			minBounds = this.worldPosition;
		if (minBounds != null) {
			BlockState state = this.level.getBlockState(minBounds.below());
			if (state.getBlock().isAir(state, world, minBounds.below()))
				this.minBounds = minBounds.below();
			else if (this.level.getBlockState(this.minBounds.below()).getBlock() instanceof BlockOverheadDoor) {
				this.minBounds = minBounds.below();
				this.amountScrolled += 1;
			} else if (!(this.level.getBlockState(this.minBounds).getBlock() instanceof BlockOverheadDoor
					|| world.isEmptyBlock(this.minBounds))) {
				this.minBounds = new BlockPos(this.minBounds.getX(), this.worldPosition.getY(), this.minBounds.getZ());
			}
		}

		if (!this.level.isClientSide && this.allow != null && !powered) {
			hasEntity = this.level.getEntitiesOfClass(LivingEntity.class,
					new AABB(this.maxBounds, this.minBounds).inflate(2, 1, 2),
					input -> ((allow == Allow.PLAYER && input instanceof Player)
							|| (allow == Allow.TEAM && input.getTeam() == team)
							|| (allow == Allow.ENTITY && input instanceof LivingEntity)))
					.size() > 0;

		} else
			hasEntity = false;
		this.motion = 0;
		boolean isEntity = powered || this.hasEntity || this.entitySome || this.clientOpen;

		if (isEntity && !this.entitySome && !this.level.isClientSide)
			this.updateAmountScrolled(15, false);

		if (isEntity && amountScrolled > 0.25) {
			this.motion = -this.getUpSpeed();

		}
		if (!isEntity && !entitySome && worldPosition.getY() - this.minBounds.getY() + 1 > amountScrolled) {
			this.motion = this.getDownSpeed();

		}
		if (this.motion != 0) {
			amountScrolled += this.motion;

		}
		if (lastIsEntity != isEntity) {
			this.level.blockEvent(this.worldPosition, this.getBlockType(), 1, isEntity ? 1 : 0);
			if (!entitySome) {
				this.updateAmountScrolled(15, true);
			}
		}

		boolean isClosed = this.level.getBlockState(this.minBounds).getBlock() == this.getBlockType();
		// this.level.setBlock(worldPosition,
		// this.level.getBlockState(worldPosition).setValue(BlockOverheadDoor.SLIDING,
		// !isClosed));
		if (isClosed && worldPosition.getY() - this.minBounds.getY() + 1 > amountScrolled) {
			if (!this.level.isClientSide) {
				for (int y = worldPosition.getY() - 1; y >= this.minBounds.getY(); y--) {
					// BlockState state = this.level.getBlockState(worldPosition);
					BlockPos doorpos = new BlockPos(worldPosition.getX(), y, worldPosition.getZ());
					if (this.level.getBlockState(doorpos).getBlock() == this.getBlockType())
						this.level.removeBlock(doorpos, false);
				}
				this.level.setBlock(worldPosition,
						this.level.getBlockState(worldPosition).setValue(BlockOverheadDoor.SLIDING, true));
			}
		} else if (!isClosed && worldPosition.getY() - this.minBounds.getY() + 1 <= amountScrolled) {

			BlockState state = this.level.getBlockState(worldPosition).setValue(BlockOverheadDoor.HOLDER, false)
					.setValue(BlockOverheadDoor.SLIDING, false);
			for (int y = worldPosition.getY() - 1; y >= this.minBounds.getY(); y--) {
				BlockPos doorpos = new BlockPos(worldPosition.getX(), y, worldPosition.getZ());
				if (this.level.isEmptyBlock(doorpos)) {
					if (!this.level.isClientSide)
						this.level.setBlock(doorpos, state);
				}

				else
					this.minBounds = new BlockPos(worldPosition.getX(), doorpos.getY() + 1, worldPosition.getZ());
			}
			if (!this.level.isClientSide)
				this.level.setBlock(worldPosition,
						this.level.getBlockState(worldPosition).setValue(BlockOverheadDoor.SLIDING, false));
		}
		// System.out.println("done"+this.minBounds.getY()+" "+this.worldPosition.getY()+" ");
		this.lastIsEntity = isEntity;
		this.tickTime = this.level.getGameTime();
		entitySome = false;
	}

	public boolean isPowered() {
		return this.hasEntity || this.powered || this.clientOpen || this.entitySome;
	}

	public void updateAmountScrolled(int reach, boolean updateEvents) {
		Direction facing = this.level.getBlockState(worldPosition).getValue(HorizontalDirectionalBlock.FACING).rotateAround(Axis.Y);
		if (facing.getAxisDirection() == AxisDirection.NEGATIVE)
			facing = facing.getOpposite();
		int axispos = TF2Util.getValueOnAxis(this.minBounds, facing.getAxis());
		int minReach = axispos - reach;
		int maxReach = axispos + reach;
		for (int i = axispos - 1; i >= minReach; i--) {
			BlockPos pos = TF2Util.setValueOnAxis(this.worldPosition, facing.getAxis(), i);
			BlockEntity ent = this.level.getBlockEntity(worldPosition);
			if (ent instanceof TileEntityOverheadDoor) {
				if (((TileEntityOverheadDoor) ent).amountScrolled >= this.amountScrolled) {
					if (!updateEvents)
						((TileEntityOverheadDoor) ent).entitySome = true;
					else
						this.level.blockEvent(worldPosition, this.getBlockType(), 1,
								((TileEntityOverheadDoor) ent).isPowered() ? 1 : 0);
				}
			} else
				break;
		}
		for (int i = axispos + 1; i <= maxReach; i++) {
			BlockPos pos = TF2Util.setValueOnAxis(this.worldPosition, facing.getAxis(), i);
			BlockEntity ent = this.level.getBlockEntity(worldPosition);
			if (ent instanceof TileEntityOverheadDoor) {
				if (((TileEntityOverheadDoor) ent).amountScrolled >= this.amountScrolled) {
					if (!updateEvents)
						((TileEntityOverheadDoor) ent).entitySome = true;
					else
						this.level.blockEvent(worldPosition, this.getBlockType(), 1,
								((TileEntityOverheadDoor) ent).isPowered() ? 1 : 0);
				}
			} else
				break;
		}
	}

	@Override
	public void onLoad() {
		updateMasterStatus();
	}

	public void updateMasterStatus() {
		Direction facing = this.level.getBlockState(worldPosition).getValue(HorizontalDirectionalBlock.FACING).rotateAround(Axis.Y);
		if (facing.getAxisDirection() == AxisDirection.NEGATIVE)
			facing = facing.getOpposite();
		BlockPos minBounds = this.worldPosition;
		BlockPos maxBounds = this.worldPosition;
		/*
		 * for(int i = 1; i <= 16; i++) { Vec3i offset = facing.getDirectionVec();
		 * offset = new BlockPos(offset.getX()*i, offset.getY()*i, offset.getZ()*i);
		 * BlockPos nearpos = this.worldPosition.add(offset); if
		 * (this.level.getBlockState(nearpos).getBlock() == this.getBlockType()) {
		 * BlockEntity ent = this.level.getBlockEntity(nearpos); if (ent instanceof
		 * TileEntityOverheadDoor && ((TileEntityOverheadDoor)ent).master) { BlockPos
		 * worldPosition = ((TileEntityOverheadDoor)ent).minBounds; worldPosition =
		 * TF2Util.setValueOnAxis(worldPosition, facing.getAxis(),
		 * Math.min(TF2Util.getValueOnAxis(worldPosition, facing.getAxis()),
		 * TF2Util.getValueOnAxis(this.worldPosition, facing.getAxis())));
		 * ((TileEntityOverheadDoor)ent).minBounds = worldPosition; this.amountScrolled =
		 * ((TileEntityOverheadDoor)ent).amountScrolled; this.minBounds = new
		 * BlockPos(this.worldPosition.getX(),((TileEntityOverheadDoor)ent).minBounds.getY(),this.
		 * worldPosition.getZ()); //this.master = false; return; } maxBounds = nearpos; } else
		 * break; } for(int i = -1; i >= -16; i--) { Vec3i offset =
		 * facing.getDirectionVec(); offset = new BlockPos(offset.getX()*i,
		 * offset.getY()*i, offset.getZ()*i); BlockPos nearpos = this.worldPosition.add(offset);
		 * 
		 * if (this.level.getBlockState(nearpos).getBlock() == this.getBlockType()) {
		 * BlockEntity ent = this.level.getBlockEntity(nearpos);
		 * 
		 * if (ent instanceof TileEntityOverheadDoor &&
		 * ((TileEntityOverheadDoor)ent).master) { BlockPos pos =
		 * ((TileEntityOverheadDoor)ent).maxBounds; worldPosition = TF2Util.setValueOnAxis(worldPosition,
		 * facing.getAxis(), Math.max(TF2Util.getValueOnAxis(worldPosition, facing.getAxis()),
		 * TF2Util.getValueOnAxis(this.worldPosition, facing.getAxis())));
		 * ((TileEntityOverheadDoor)ent).maxBounds = worldPosition; this.amountScrolled =
		 * ((TileEntityOverheadDoor)ent).amountScrolled; this.minBounds = new
		 * BlockPos(this.worldPosition.getX(),((TileEntityOverheadDoor)ent).minBounds.getY(),this.
		 * worldPosition.getZ()); //this.master = false; return; } minBounds = nearpos; } else
		 * break; }
		 */
		// this.master = true;
		this.minBounds = minBounds;
		this.maxBounds = maxBounds;
	}

	@Override
	public boolean receiveClientEvent(int id, int type) {
		if (id == 1) {
			if (this.level.isClientSide) {
				if (type == 0)
					this.clientOpen = false;
				else
					this.clientOpen = true;
			}
			return true;
		}
		return super.receiveClientEvent(id, type);
	}

	@Override
	public boolean shouldRefresh(Level world, BlockPos pos, BlockState oldState, BlockState newSate) {
		return oldState.getBlock() != newSate.getBlock() || !newSate.getValue(BlockOverheadDoor.HOLDER);
	}

	@Override
	public net.minecraft.world.phys.AABB getRenderBoundingBox() {
		if (this.minBounds == null)
			return new AABB(worldPosition);
		else
			return new AABB(worldPosition, this.minBounds).inflate(1);
	}

	@Override
	public CompoundTag writeToNBT(CompoundTag compound) {
		super.writeToNBT(compound);
		// compound.setBoolean("Master", this.master);
		if (team != null)
			compound.setString("Team", this.team.getName());
		if (allow != null)
			compound.setByte("Allow", (byte) this.allow.ordinal());
		return compound;
	}

	@Override
	public void readFromNBT(CompoundTag compound) {
		super.readFromNBT(compound);
		// this.master = compound.getBoolean("Master");
		if (this.hasLevel() && compound.hasKey("Team"))
			this.team = this.level.getScoreboard().getTeam(compound.getString("Team"));
		if (compound.hasKey("Allow"))
			this.allow = Allow.values()[compound.getByte("Allow")];
	}

	public boolean setController(String string) {
		this.dropController();
		if (string.equals("players"))
			allow = Allow.PLAYER;
		else if (string.equals("mobs"))
			allow = Allow.ENTITY;
		else {
			Team target = this.level.getScoreboard().getTeam(string);
			if (target == null)
				return false;
			allow = Allow.TEAM;
			team = target;
		}
		return true;
	}

	public void dropController() {
		if (allow == null)
			return;
		int meta = 0;
		if (allow == Allow.ENTITY)
			meta = 1;
		else if (allow == Allow.TEAM) {
			if (this.team != null && team.getName().equals("RED"))
				meta = 2;
			else if (this.team != null && team.getName().equals("BLU"))
				meta = 3;
		}
		this.getLevel().spawnEntity(new ItemEntity(this.level, this.worldPosition.getX(), this.worldPosition.getY(), this.worldPosition.getZ(),
				new ItemStack(TF2weapons.itemDoorController, 1, meta)));
	}

	@Override
	protected void setWorldCreate(Level world) {
		this.setWorld(world);
	}
}
