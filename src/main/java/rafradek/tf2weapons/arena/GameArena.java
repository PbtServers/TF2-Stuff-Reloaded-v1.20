package rafradek.tf2weapons.arena;

import com.google.common.base.Optional;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.scores.Team;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.wrapper.InvWrapper;
import rafradek.tf2weapons.NBTLiterals;
import rafradek.tf2weapons.TF2PlayerCapability;
import rafradek.tf2weapons.TF2weapons;
import rafradek.tf2weapons.common.WeaponsCapability;
import rafradek.tf2weapons.item.ItemFromData;
import rafradek.tf2weapons.item.ItemToken;
import rafradek.tf2weapons.message.TF2Message;
import rafradek.tf2weapons.tileentity.TileEntityGameConfigure;
import rafradek.tf2weapons.util.TF2Class;
import rafradek.tf2weapons.util.TF2Util;

import java.util.*;
import java.util.Map.Entry;

public class GameArena implements INBTSerializable<CompoundTag> {

	private static int ID_INCREMENT;
	private boolean active;
	private AABB bounds = new AABB(0, 0, 0, 0, 0, 0);
	private Level world;
	private String name;
	private BlockPos confPos;
	private boolean first = true;
	public boolean markDelete;
	public Map<UUID, Player> playerUUID = new HashMap<>();
	public Map<UUID, SynchedEntityData> playerInfoUUID = new HashMap<>();
	private int defaultClass = -1;
	private Team defaultTeam = null;
	private boolean showClassSelection = false;
	private boolean showTeamSelection = false;
	private boolean disableHunger = true;
	private boolean forceClassTexture = false;
	public boolean clearInventory = false;
	private List<Team> allowedTeams;
	private int unbalanceLimit = 0;
	private int classLimit = 0;

	public static final EntityDataAccessor<String> PLAYER_NAME = new EntityDataAccessor<>(0, EntityDataSerializers.STRING);
	public static final EntityDataAccessor<Optional<UUID>> PLAYER_UUID = new EntityDataAccessor<>(1,
			EntityDataSerializers.OPTIONAL_UNIQUE_ID);
	public static final EntityDataAccessor<Integer> TEAM = new EntityDataAccessor<>(2, EntityDataSerializers.VARINT);
	public static final EntityDataAccessor<Integer> POINTS = new EntityDataAccessor<>(3, EntityDataSerializers.VARINT);

	private CompoundTag joinTeamDataCache;
	private CompoundTag joinClassDataCache;
	public int networkId;

	public GameArena(Level world, String name, BlockPos confPos) {
		this.world = world;
		this.name = name;
		this.confPos = confPos;
	}

	public GameArena(Level world, CompoundTag tag) {
		this.world = world;
		this.deserializeNBT(tag);

	}

	public GameArena(Level world, int id) {
		this.world = world;
		this.networkId = id;
	}

	public void addToWorld() {
		this.networkId = ID_INCREMENT++;
	}

	public void tick() {
		if (first) {
			this.allowedTeams = new ArrayList<>();
			this.allowedTeams.add(this.world.getScoreboard().getTeam("RED"));
			this.allowedTeams.add(this.world.getScoreboard().getTeam("BLU"));
			this.first = false;
			if (!(confPos != null && this.world.getTileEntity(confPos) instanceof TileEntityGameConfigure
					&& ((TileEntityGameConfigure) this.world.getTileEntity(confPos)).getName().equals(this.name))) {
				this.markDelete = true;
			}
		}
		if (active) {
			this.joinTeamDataCache = null;
			boolean checkAll = this.bounds == null || (this.bounds.maxX == this.bounds.minX
					&& this.bounds.maxY == this.bounds.minY && this.bounds.maxZ == this.bounds.minZ);
			List<Player> players = checkAll ? world.playerEntities
					: world.getEntitiesWithinAABB(Player.class, bounds);
			for (Player player : players) {
				if (!playerUUID.containsKey(player.getUniqueID())
						&& (TF2PlayerCapability.get(player).getGameArena() == null
								|| TF2PlayerCapability.get(player).getGameArena() == this)) {
					playerUUID.put(player.getUniqueID(), player);
					this.onPlayerJoinMatch(player);
				}
			}

			boolean naturalRegen = this.world.getGameRules().getBoolean("naturalRegeneration");

			Iterator<Entry<UUID, Player>> itePly = playerUUID.entrySet().iterator();
			while (itePly.hasNext()) {
				Entry<UUID, Player> entry = itePly.next();
				Player player = entry.getValue();

				if (this.world.getPlayerEntityByUUID(entry.getKey()) != player
						&& this.world.getPlayerEntityByUUID(entry.getKey()) != null) {
					entry.setValue(player = this.world.getPlayerEntityByUUID(entry.getKey()));

				}
				TF2PlayerCapability playercap = TF2PlayerCapability.get(player);
				WeaponsCapability cap = WeaponsCapability.get(player);
				if (this.world.getPlayerEntityByUUID(entry.getKey()) == null
						|| (!checkAll && !player.getEntityBoundingBox().intersects(this.bounds))) {
					itePly.remove();
					removePlayer(entry.getKey(), player, true);
					continue;
				}
				if (player.getTeam() == null && this.showTeamSelection && !player.isSpectator()) {
					player.setGameType(GameType.SPECTATOR);

					this.showJoinTeamDialog(player);
				}
				if (!player.isSpectator() && this.defaultTeam != null && player.getTeam() == null) {
					world.getScoreboard().addPlayerToTeam(player.getName(), this.defaultTeam.getName());
				}

				if (playercap.getRespawnTime() > 0 && this.world.getTotalWorldTime() % 20 == 0) {
					playercap.setRespawnTime(playercap.getRespawnTime() - 1);
				}

				if (player.isSpectator() && player.getTeam() != null && playercap.getRespawnTime() <= 0
						&& (cap.getUsedToken() != -1 || (this.defaultClass == -1 && !this.showClassSelection))) {
					player.setGameType(GameType.ADVENTURE);
					this.respawnPlayer(player);
				}
				/*
				 * if (this.allowedTeams.contains(player.getTeam())) {
				 * this.playerTeam.put(player, player.getTeam()); } else {
				 * this.playerTeam.remove(player); }
				 */

				if (this.defaultClass != -1 && !cap.forcedClass) {
					((ItemToken) TF2weapons.itemToken)
							.updateAttributes(new ItemStack(TF2weapons.itemToken, 1, this.defaultClass), player);
					WeaponsCapability.get(player).forcedClass = true;
				}

				if (this.disableHunger) {
					player.getFoodStats().setFoodLevel(naturalRegen ? 17 : 20);
				}
			}
		}
	}

	public void placePlayerAtSpawn(Player player) {
		BlockPos blockpos = player.getBedLocation(player.dimension);
		if (blockpos != null) {
			BlockPos blockpos1 = Player.getBedSpawnLocation(this.world, blockpos, true);

			if (blockpos1 != null) {
				player.setLocationAndAngles(blockpos1.getX() + 0.5F, blockpos1.getY() + 0.1F, blockpos1.getZ() + 0.5F,
						0.0F, 0.0F);
			}
		}

		this.world.getChunkProvider().provideChunk((int) player.posX >> 4, (int) player.posZ >> 4);

		while (!this.world.getCollisionBoxes(player, player.getEntityBoundingBox()).isEmpty() && player.posY < 256.0D) {
			player.setPosition(player.posX, player.posY + 1.0D, player.posZ);
		}
	}

	public void respawnPlayer(Player player) {
		// player.setDead();
		// Player newplayer =
		// this.world.getMinecraftServer().getPlayerList().recreatePlayerEntity((ServerPlayer)player,
		// player.dimension, false);
		this.placePlayerAtSpawn(player);
		this.resupplyPlayer(player);
	}

	public void resupplyPlayer(Player player) {
		if (this.clearInventory) {
			player.inventory.clear();
			player.getCapability(TF2weapons.INVENTORY_CAP, null).clear();
		} else {
			TF2Util.removeItemsMatching(new InvWrapper(player.getCapability(TF2weapons.INVENTORY_CAP, null)), 64,
					stackl -> stackl.hasTagCompound()
							&& stackl.getTagCompound().getBoolean(NBTLiterals.STACK_ARENA_ASSIGNED));
			TF2Util.removeItemsMatching(player.getCapability(ForgeCapabilities.ITEM_HANDLER, null), 64,
					stackl -> stackl.hasTagCompound()
							&& stackl.getTagCompound().getBoolean(NBTLiterals.STACK_ARENA_ASSIGNED));
		}
		if (WeaponsCapability.get(player).getUsedToken() != -1) {
			for (int i = 0; i < 5; i++) {
				ItemStack weapon = ItemFromData.getRandomWeaponOfSlotMob(
						TF2Class.getClass(WeaponsCapability.get(player).getUsedToken()), i, player.getRNG(), false,
						0xFFFFFFFF, false);
				if (!weapon.isEmpty()) {
					weapon.getTagCompound().setBoolean(NBTLiterals.STACK_ARENA_ASSIGNED, true);
					ItemHandlerHelper.giveItemToPlayer(player, weapon, i);
				}
			}
		}
		TF2Util.restoreAmmoToWeapons(player, 1f);
	}

	public void onPlayerJoinMatch(Player player) {
		TF2PlayerCapability.get(player).setGameArena(this);
		TF2PlayerCapability.get(player).setForceClassTexture(this.forceClassTexture);
		this.world.getScoreboard().removePlayerFromTeams(player.getName());
		player.setGameType(GameType.SPECTATOR);
		if (this.defaultTeam != null) {
			world.getScoreboard().addPlayerToTeam(player.getName(), this.defaultTeam.getName());
		}
		if (this.showTeamSelection) {
			this.showJoinTeamDialog(player);
		}
	}

	public void tryPlayerJoinTeam(Player player, int number) {
		Team team = this.allowedTeams.get(number % this.allowedTeams.size());
		this.world.getScoreboard().addPlayerToTeam(player.getName(), team.getName());
	}

	public void showJoinTeamDialog(Player player) {
		CompoundTag data = this.joinTeamDataCache;
		if (data == null) {
			data = new CompoundTag();
			ListTag listteamnbt = new ListTag();
			int[] count = new int[allowedTeams.size()];
			byte[] allowed = new byte[allowedTeams.size()];
			int lowestPlayerCount = Integer.MAX_VALUE;

			for (int i = 0; i < this.allowedTeams.size(); i++) {
				Team team = this.allowedTeams.get(i);
				listteamnbt.appendTag(new StringTag(team.getName()));
				for (Player playerl : this.playerUUID.values()) {
					if (playerl.getTeam() == team) {
						count[i] += 1;
					}
				}
				if (count[i] <= lowestPlayerCount) {
					lowestPlayerCount = count[i];
				}
			}

			for (int i = 0; i < this.allowedTeams.size(); i++) {
				if (count[i] <= lowestPlayerCount + this.unbalanceLimit)
					allowed[i] = 1;
			}
			data.setTag("Teams", listteamnbt);
			data.setByteArray("Allowed", allowed);
			data.setIntArray("Count", count);
			this.joinTeamDataCache = data;
		}
		TF2weapons.network.sendTo(new TF2Message.ShowGuiMessage(100, data), (ServerPlayer) player);
	}

	public void showJoinClassDialog(Player player) {
		CompoundTag data = this.joinClassDataCache;
		if (data == null) {
			data = new CompoundTag();
			ListTag listteamnbt = new ListTag();
			int[] count = new int[allowedTeams.size()];
			byte[] allowed = new byte[allowedTeams.size()];
			for (int i = 0; i < this.allowedTeams.size(); i++) {
				Team team = this.allowedTeams.get(i);
				listteamnbt.appendTag(new StringTag(team.getName()));
				for (Player playerl : this.playerUUID.values()) {
					if (playerl.getTeam() == team) {
						count[i] += 1;
					}
				}
				allowed[i] = 1;
			}
			data.setTag("Teams", listteamnbt);
			data.setByteArray("Allowed", allowed);
			data.setIntArray("Count", count);
			this.joinClassDataCache = data;
		}
		TF2weapons.network.sendTo(new TF2Message.ShowGuiMessage(100, data), (ServerPlayer) player);
	}

	public void removePlayer(UUID uuid, Player player, boolean alreadyRemoved) {
		if (!alreadyRemoved) {
			player = playerUUID.remove(uuid);
		}
		playerInfoUUID.remove(uuid);
		if (player != null) {
			this.world.getScoreboard().removePlayerFromTeams(player.getName());
			// playerTeam.remove(player);
			player.setGameType(this.world.getWorldInfo().getGameType());
			TF2PlayerCapability.get(player).setGameArena(null);
			TF2PlayerCapability.get(player).setForceClassTexture(false);
			WeaponsCapability cap = WeaponsCapability.get(player);
			if (cap.forcedClass) {
				((ItemToken) TF2weapons.itemToken).updateAttributes(ItemStack.EMPTY, player);
				WeaponsCapability.get(player).forcedClass = false;
			}
		}
	}

	public void setActive(boolean active) {
		this.active = active;
		if (!active) {
			this.playerUUID.entrySet().removeIf((entry) -> {
				removePlayer(entry.getKey(), entry.getValue(), true);
				return true;
			});
		}
	}

	public void onPlayerKill(Player player) {
		player.setGameType(GameType.SPECTATOR);
		TF2PlayerCapability.get(player).setRespawnTime(10);
	}

	public void changePlayerTeam(Player player, Team team) {
		if (this.playerUUID.containsKey(player.getUniqueID()) && this.allowedTeams.contains(team)
				&& player.getTeam() != team) {

		}
	}

	public void readConfig(CompoundTag tag) {
		if (tag.hasKey("Min Bounds")) {
			int[] boundsMin = tag.getIntArray("Min Bounds");
			int[] boundsMax = tag.getIntArray("Max Bounds");
			this.bounds = new AABB(boundsMin[0], boundsMin[1], boundsMin[2], boundsMax[0], boundsMax[1],
					boundsMax[2]);
		}
		this.defaultClass = TF2Class.getClass(tag.getString("C:Default Class")).getIndex();
		this.showClassSelection = tag.getBoolean("Show Class Selection");
		this.showTeamSelection = tag.getBoolean("Show Team Selection");
		if (tag.hasKey("T:Default Team"))
			this.defaultTeam = this.world.getScoreboard().getTeam(tag.getString("T:Default Team"));

		this.disableHunger = tag.getBoolean("Disable Hunger");
		this.forceClassTexture = tag.getBoolean("Force Class Texture");
	}

	public CompoundTag writeConfig(CompoundTag tag) {
		if (bounds != null) {
			tag.setIntArray("Min Bounds", new int[] { (int) bounds.minX, (int) bounds.minY, (int) bounds.minZ });
			tag.setIntArray("Max Bounds", new int[] { (int) bounds.maxX, (int) bounds.maxY, (int) bounds.maxZ });
		}
		tag.setString("C:Default Class", this.defaultClass == -1 ? "none" : TF2Class.getClass(this.defaultClass).getName());
		tag.setBoolean("Show Class Selection", this.showClassSelection);
		if (this.defaultTeam != null)
			tag.setString("T:Default Team", this.defaultTeam.getName());
		else
			tag.setString("T:Default Team", "");
		tag.setBoolean("Show Team Selection", this.showTeamSelection);
		tag.setBoolean("Disable Hunger", this.disableHunger);
		tag.setBoolean("Force Class Texture", this.forceClassTexture);
		return tag;
	}

	@Override
	public CompoundTag serializeNBT() {
		CompoundTag tag = new CompoundTag();
		tag.setString("Name", this.name);
		tag.setLong("ConfPos", this.confPos.toLong());
		tag.setBoolean("Active", this.active);
		this.writeConfig(tag);
		return tag;
	}

	@Override
	public void deserializeNBT(CompoundTag nbt) {
		this.name = nbt.getString("Name");
		this.active = nbt.getBoolean("Active");
		this.readConfig(nbt);
		this.confPos = BlockPos.fromLong(nbt.getLong("ConfPos"));
	}

	public String getName() {
		return name;
	}

	public static class PlayerMatchInfo {

	}
}
