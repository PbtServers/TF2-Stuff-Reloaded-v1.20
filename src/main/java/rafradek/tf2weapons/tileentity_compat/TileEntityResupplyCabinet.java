package rafradek.tf2weapons.tileentity;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntityResupplyCabinet extends BlockEntity implements IEntityConfigurable {
	public final Map<Player, Integer> cooldownUse = new HashMap<>();
	public boolean redstoneActivate;
	private final EntityOutputManager outputManager = new EntityOutputManager(this);

	public TileEntityResupplyCabinet(BlockPos pos, BlockState state) {
		super(null, pos, state);
	}

	public void setEnabled(boolean enabled) {}
	public CompoundTag writeConfig(CompoundTag tag) { return tag; }
	public void readConfig(CompoundTag tag) {}
	public EntityOutputManager getOutputManager() { return outputManager; }
	public String[] getOutputs() { return new String[0]; }
}
