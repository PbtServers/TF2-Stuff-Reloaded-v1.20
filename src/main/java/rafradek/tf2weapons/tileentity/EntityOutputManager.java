package rafradek.tf2weapons.tileentity;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.level.block.entity.BlockEntityComparator;
import net.minecraft.core.Direction;
import net.minecraft.util.Tuple;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import rafradek.tf2weapons.TF2weapons;

public class EntityOutputManager {
	public Level world;
	public Multimap<String, Tuple<BlockPos, Integer>> outputs = HashMultimap.create();
	public String name = "";
	public IEntityConfigurable entity;

	public EntityOutputManager(IEntityConfigurable entity) {
		this.entity = entity;
	}

	public void setWorld(Level world) {
		this.world = world;
	}

	public void loadOutputs(CompoundTag tag) {
		outputs.clear();
		for (String key : tag.getKeySet()) {
			ListTag list = tag.getTagList(key, 11);
			for (int i = 0; i < list.tagCount(); i++) {
				int[] arr = ((NBTTagIntArray) list.get(i)).getIntArray();
				outputs.put(key, new Tuple<>(new BlockPos(arr[0], arr[1], arr[2]), arr[3]));
			}
		}
	}

	public CompoundTag saveOutputs(CompoundTag tag) {
		for (String key : outputs.keySet()) {
			ListTag list = new ListTag();
			for (Tuple<BlockPos, Integer> tuple : outputs.get(key)) {
				list.appendTag(new NBTTagIntArray(new int[] { tuple.getFirst().getX(), tuple.getFirst().getY(),
						tuple.getFirst().getZ(), tuple.getSecond() }));
			}
			tag.setTag(key, list);
		}
		return tag;
	}

	public void readConfig(CompoundTag tag) {
		this.loadOutputs(tag.getCompoundTag("Outputs"));
		this.name = tag.getString("Link Name");
		this.entity.readConfig(tag);
	}

	public CompoundTag writeConfig(CompoundTag tag) {
		tag.setTag("Outputs", this.saveOutputs(new CompoundTag()));
		tag.setString("Link Name", name);
		this.entity.writeConfig(tag);
		return tag;
	}

	public void activateOutput(String output, float power, int minTime) {
		TF2weapons.LOGGER.info("activated " + output);
		for (Tuple<BlockPos, Integer> tup : outputs.get(output)) {
			BlockPos pos = tup.getFirst();
			BlockState state = world.getBlockState(pos);
			if (state.getBlock() instanceof BlockButton) {
				world.setBlock(pos, state.setValue(BlockButton.POWERED, Boolean.valueOf(true)), 3);
				world.updateNeighborsAt(pos, state.getBlock(), false);
				world.updateNeighborsAt(pos.relative(state.getValue(BlockDirectional.FACING).getOpposite()),
						state.getBlock(), false);
				world.scheduleTick(pos, state.getBlock(),
						(int) (20 * power * 15f / tup.getSecond()));
			} else if (state.getBlock() instanceof BlockLever) {
				state = state.setValue(BlockLever.POWERED, tup.getSecond() != 0);
				Direction Direction = state.getValue(BlockLever.FACING).getFacing();
				world.setBlock(pos, state, 3);
				world.updateNeighborsAt(pos.relative(Direction.getOpposite()), state.getBlock(), false);
				world.updateNeighborsAt(pos, state.getBlock(), false);
			} else if (state.getBlock() instanceof BlockRedstoneComparator) {
				world.setBlock(pos, state.setValue(BlockButton.POWERED, Boolean.valueOf(true)), 3);
				if (world.getBlockEntity(pos) != null)
					((TileEntityComparator) world.getBlockEntity(pos)).setOutputSignal((int) (tup.getSecond() * power));

				world.scheduleTick(pos, state.getBlock(), minTime);
				world.updateNeighborsAt(pos, state.getBlock(), false);
			} else if (state.getBlock() instanceof BlockRedstoneWire) {
				state = state.setValue(BlockRedstoneWire.POWER, 15);
				world.setBlock(pos, state, 3);
				world.updateNeighborsAt(pos, state.getBlock(), false);
			}
		}
	}
}
