package rafradek.tf2weapons.arena;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.phys.AABB;

public class GameArena {
	public int networkId;
	public String name = "";
	public AABB bounds;
	public Map<UUID, SynchedEntityData> playerInfoUUID = new HashMap<>();

	public void update() {}
}
