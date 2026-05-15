package rafradek.tf2weapons.tileentity;


import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import rafradek.tf2weapons.TF2weapons;
import rafradek.tf2weapons.arena.GameArena;
import rafradek.tf2weapons.registry.TF2BlockEntities;

import javax.annotation.Nullable;

public class TileEntityGameConfigure extends BlockEntity implements IEntityConfigurable {

	private static final String[] OUTPUT_NAMES = {};
	private EntityOutputManager outputManager = new EntityOutputManager(this);
	private String name = "";

	public TileEntityGameConfigure(BlockPos pos, BlockState state) {
		super(TF2BlockEntities.GAME_CONFIGURE.get(), pos, state);
	}

	@Override
	public void tick() {

	}

	@Override
	public CompoundTag writeToNBT(CompoundTag compound) {
		super.writeToNBT(compound);
		compound.setTag("Config", this.getOutputManager().writeConfig(new CompoundTag()));

		return compound;
	}

	@Override
	public void readFromNBT(CompoundTag compound) {
		super.readFromNBT(compound);
		this.getOutputManager().readConfig(compound.getCompoundTag("Config"));

	}

	@Override
	public boolean receiveClientEvent(int id, int type) {
		return super.receiveClientEvent(id, type);
	}

	@Override
	public boolean hasCapability(net.minecraftforge.common.capabilities.Capability<?> capability,
			@Nullable net.minecraft.core.Direction facing) {
		if (facing != null && capability == net.minecraftforge.common.capabilities.ForgeCapabilities.ITEM_HANDLER_CAPABILITY) {
			return true;
		}
		return super.hasCapability(capability, facing);
	}

	@Override
	@Nullable
	public <T> T getCapability(net.minecraftforge.common.capabilities.Capability<T> capability,
			@Nullable net.minecraft.core.Direction facing) {
		return super.getCapability(capability, facing);
	}

	@Override
	public void onLoad() {}

	@Override
	public boolean shouldRefresh(Level world, BlockPos pos, BlockState oldState, BlockState newSate) {
		return super.shouldRefresh(world, worldPosition, oldState, newSate);
	}

	@Override
	protected void setWorldCreate(Level world) {
		this.setWorld(world);
	}

	@Override
	public void setWorld(Level world) {
		super.setWorld(world);
		this.getOutputManager().Level = world;
	}

	@Override
	public CompoundTag writeConfig(CompoundTag tag) {
		tag.setString("Arena Name", this.getName());
		GameArena arena = this.level.getCapability(TF2weapons.WORLD_CAP, null).gameArenas.get(this.getName());
		if (arena != null) {
			arena.writeConfig(tag);
		} else {
			tag.setIntArray("Min Bounds", new int[3]);
			tag.setIntArray("Max Bounds", new int[3]);
		}
		return tag;
	}

	@Override
	public void readConfig(CompoundTag tag) {
		this.name = tag.getString("Arena Name").trim();
		if (!this.name.isEmpty()) {
			GameArena arena = this.level.getCapability(TF2weapons.WORLD_CAP, null).gameArenas.get(this.getName());
			if (arena != null) {
				arena.readConfig(tag);
			} else {
				arena = new GameArena(this.getLevel(), this.name, this.getBlockPos());
				arena.readConfig(tag);
				this.level.getCapability(TF2weapons.WORLD_CAP, null).gameArenas.put(this.name, arena);
			}
		}
	}

	@Override
	public EntityOutputManager getOutputManager() {
		return this.outputManager;
	}

	@Override
	public String[] getOutputs() {
		return OUTPUT_NAMES;
	}

	public String getName() {
		return name;
	}

	public void removeGameArena() {
		GameArena arena = this.level.getCapability(TF2weapons.WORLD_CAP, null).gameArenas.get(this.getName());
		if (arena != null && arena.getName().equals(this.name)) {
			arena.markDelete = true;
		}
	}

}
