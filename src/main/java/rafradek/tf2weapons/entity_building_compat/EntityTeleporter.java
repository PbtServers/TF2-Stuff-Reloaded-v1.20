package rafradek.tf2weapons.entity.building;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

public class EntityTeleporter extends EntityBuilding {
	public static final int TP_PER_PLAYER = 2;
	public static final Map<UUID, TeleporterData[]> teleporters = new HashMap<>();
	private boolean exit;
	private int id;

	public EntityTeleporter(Level world) { super(world); }
	public void setExit(boolean exit) { this.exit = exit; }
	public boolean isExit() { return exit; }
	public void setID(int id) { this.id = id; }
	public int getID() { return id; }

	public static class TeleporterData {}

	public static class TeleporterDim extends TeleporterData {
		public TeleporterDim(ServerLevel world, BlockPos pos) {}
	}
}
