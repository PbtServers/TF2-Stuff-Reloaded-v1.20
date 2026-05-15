package rafradek.tf2weapons.tileentity;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import rafradek.tf2weapons.TF2weapons;
import rafradek.tf2weapons.block.BlockOverheadDoor;
import rafradek.tf2weapons.common.TF2Attribute;
import rafradek.tf2weapons.registry.TF2BlockEntities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class TileEntityUpgrades extends BlockEntity {

	public static final int UPGRADES_COUNT = 10;
	public HashMap<TF2Attribute, Integer> attributes = new HashMap<>();
	public List<TF2Attribute> attributeList = new ArrayList<>();
	private int maxSize;
	public boolean placed;

	public TileEntityUpgrades() {
		this(BlockPos.ZERO, TF2weapons.blockUpgradeStation.defaultBlockState());
	}

	public TileEntityUpgrades(Level world) {
		this();
		this.level = world;
	}

	public TileEntityUpgrades(BlockPos pos, BlockState state) {
		super(TF2BlockEntities.UPGRADE_STATION.get(), pos, state);
	}

	/**
	 * Removes up to a specified number of items from an inventory slot and returns
	 * them in a new stack.
	 */
	public void generateUpgrades(Random rand) {
		// System.out.println("Max Size: "+MapList.nameToAttribute.size());
		List<TF2Attribute> passAttributes = TF2Attribute.getAllPassibleAttributesForUpgradeStation();
		this.maxSize = passAttributes.size();
		int size = (int) (passAttributes.size() * 0.67f);
		for (int i = 0; i < size; i++)
			while (true) {
				TF2Attribute attr = passAttributes.get(rand.nextInt(passAttributes.size()));
				if (!this.attributes.containsKey(attr) && attr.weight > rand.nextInt(8)) {
					this.attributeList.add(attr);
					boolean high = i % 2 == 0;
					this.attributes.put(attr,
							Math.max(1, Math.round(attr.numLevels * (high ? 1f : 0.5f))/*- (i < 4 ? 0 : 1)*/));
					break;
				}
			}
		// this.level.markAndNotifyBlock(worldPosition,
		// this.level.getChunkFromBlockCoords(getBlockPos()),
		// this.level.getBlockState(getBlockPos()),
		// this.level.getBlockState(worldPosition), 2);
		this.setChanged();
	}

	@Override
	public void readFromNBT(CompoundTag compound) {
		super.readFromNBT(compound);
		this.attributeList.clear();
		this.attributes.clear();
		this.maxSize = compound.getShort("MaxS");
		this.placed = compound.getBoolean("Placed");
		if (compound.hasKey("Attributes")
				&& maxSize == TF2Attribute.getAllPassibleAttributesForUpgradeStation().size()) {
			CompoundTag attrs = compound.getCompoundTag("Attributes");
			ListTag attrList = (ListTag) compound.getTag("AttributesList");
			for (String key : attrs.getKeySet())
				this.attributes.put(TF2Attribute.attributes[Integer.parseInt(key)], attrs.getInteger(key));
			for (int i = 0; i < attrList.tagCount(); i++)
				this.attributeList.add(TF2Attribute.attributes[attrList.getIntAt(i)]);
		}

	}

	@Override
	public CompoundTag writeToNBT(CompoundTag compound) {
		super.writeToNBT(compound);
		CompoundTag attrs = new CompoundTag();
		ListTag attrList = new ListTag();
		compound.setShort("MaxS", (short) this.maxSize);
		compound.setTag("Attributes", attrs);
		compound.setTag("AttributesList", attrList);
		compound.setBoolean("Placed", placed);
		for (TF2Attribute attr : this.attributeList)
			if (attr != null) {
				attrList.appendTag(new NBTTagInt(attr.id));
				attrs.setInteger(String.valueOf(attr.id), this.attributes.get(attr));
			}

		return compound;
	}

	@Override
	public boolean shouldRefresh(Level world, BlockPos pos, BlockState oldState, BlockState newSate) {
		return oldState.getBlock() != newSate.getBlock() || !newSate.getValue(BlockOverheadDoor.HOLDER);
	}

	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		// System.out.println("Sending packet");
		return new ClientboundBlockEntityDataPacket(this.worldPosition, 29, this.getUpdateTag());
	}

	@Override
	public CompoundTag getUpdateTag() {
		return this.writeToNBT(new CompoundTag());
	}

	@Override
	public void onDataPacket(net.minecraft.network.NetworkManager net,
			net.minecraft.network.play.server.ClientboundBlockEntityDataPacket pkt) {
		// System.out.println("Received: "+pkt.getNbtCompound());
		this.readFromNBT(pkt.getNbtCompound());
	}
}
