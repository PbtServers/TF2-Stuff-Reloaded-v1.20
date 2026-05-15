package rafradek.tf2weapons;

import java.util.ArrayList;
import java.util.HashMap;

import com.google.common.collect.Multimap;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.ItemStackHandler;
import rafradek.tf2weapons.arena.GameArena;
import rafradek.tf2weapons.entity.boss.EntityTF2Boss;
import rafradek.tf2weapons.item.ItemAmmo;
import rafradek.tf2weapons.util.Contract;
import rafradek.tf2weapons.util.Contract.Objective;

public class TF2PlayerCapability implements ICapabilityProvider, INBTSerializable<CompoundTag> {
	public Player owner;
	public boolean pressedStart;
	public int zombieHuntTicks;
	public boolean wornEye;
	public int cratesOpened;
	public float dodgedDmg;
	public int tickAirblasted;
	public ItemStackHandler lostItems = new ItemStackHandler(0);
	public HashMap<Class<? extends Entity>, Short> highestBossLevel = new HashMap<>();
	public int nextBossTicks;
	public int stickybombKilled;
	public boolean engineerKilled;
	public boolean sentryKilled;
	public boolean dispenserKilled;
	public int[] cachedAmmoCount = new int[Math.max(16, ItemAmmo.AMMO_TYPES.length)];
	public int sapperTime;
	public int headshotsRow;
	public LivingEntity buildingOwnerKill;
	public Object lastMovementInput;
	public ArrayList<Contract> contracts = new ArrayList<>();
	public int mercenariesKilled;
	public int nextContractDay = -1;
	public boolean newContracts;
	public boolean newRewards;
	public int fastKillTimer;
	public float healed;
	public boolean sendContractsNextTick;
	public LivingEntity lastMedic;
	public short udpServerId;
	public int medicCall;
	public boolean medicCharge;
	public boolean breakBlocks = true;
	public boolean blockUse;
	public float robotsKilledInvasion;
	public CompoundTag carrying;
	public int carryingType;
	public int maxInvasionBeaten;
	public int hhhSummonedDay;
	public int monoculusSummonedDay;
	public int merasmusSummonedDay;
	public long bossSpawnTicks;
	public EntityTF2Boss bossToSpawn;
	public int lastDayInvasion;
	public SynchedEntityData dataManager;
	public SynchedEntityData dataManagerGlobal;
	@SuppressWarnings("unchecked")
	public Multimap<String, AttributeModifier>[] wearablesAttrib = new Multimap[5];

	public static final EntityDataAccessor<CompoundTag> SENTRY_VIEW =
			new EntityDataAccessor<>(0, EntityDataSerializers.COMPOUND_TAG);
	public static final EntityDataAccessor<CompoundTag> DISPENSER_VIEW =
			new EntityDataAccessor<>(1, EntityDataSerializers.COMPOUND_TAG);
	public static final EntityDataAccessor<CompoundTag> TELEPORTERA_VIEW =
			new EntityDataAccessor<>(2, EntityDataSerializers.COMPOUND_TAG);
	public static final EntityDataAccessor<CompoundTag> TELEPORTERB_VIEW =
			new EntityDataAccessor<>(3, EntityDataSerializers.COMPOUND_TAG);
	public static final EntityDataAccessor<ItemStack> BACKPACK_ITEM_HOLD =
			new EntityDataAccessor<>(4, EntityDataSerializers.ITEM_STACK);
	public static final EntityDataAccessor<Float> INVASION_ATTACK_DIR =
			new EntityDataAccessor<>(5, EntityDataSerializers.FLOAT);
	public static final EntityDataAccessor<Boolean> USE_CLASS_TEXTURE =
			new EntityDataAccessor<>(0, EntityDataSerializers.BOOLEAN);
	public static final EntityDataAccessor<Integer> RESPAWN_TIME =
			new EntityDataAccessor<>(1, EntityDataSerializers.INT);

	private final CompoundTag sentryView = new CompoundTag();
	private final CompoundTag dispenserView = new CompoundTag();
	private final CompoundTag teleporterAView = new CompoundTag();
	private final CompoundTag teleporterBView = new CompoundTag();
	private ItemStack backpackItemHold = ItemStack.EMPTY;
	private float invasionDir;
	private boolean forceClassTexture;
	private int respawnTime;
	private float totalLastDamage;
	private boolean backpackItemEquipped;
	private GameArena gameArena;

	public TF2PlayerCapability(Player entity) {
		this.owner = entity;
	}

	public void clone(TF2PlayerCapability cap) {}
	public void tick() {}
	public void completeObjective(Objective objective, ItemStack stack) {}
	public ItemStack getBackpackItemHold() { return backpackItemHold; }
	public void setBackpackItemHold(ItemStack stack) { this.backpackItemHold = stack == null ? ItemStack.EMPTY : stack; }
	public float getInvasionDir() { return invasionDir; }
	public void setInvasionDir(float dir) { this.invasionDir = dir; }
	public boolean isForceClassTexture() { return forceClassTexture; }
	public void setForceClassTexture(boolean force) { this.forceClassTexture = force; }
	public int getRespawnTime() { return respawnTime; }
	public void setRespawnTime(int time) { this.respawnTime = time; }
	public CompoundTag getSentryView() { return sentryView; }
	public CompoundTag getDispenserView() { return dispenserView; }
	public CompoundTag getTeleporterAView() { return teleporterAView; }
	public CompoundTag getTeleporterBView() { return teleporterBView; }
	public void updateBuildings() {}
	public void addLastDamage(float damage, boolean isPlayerTarget) { this.totalLastDamage += damage; }
	public float getTotalLastDamage() { return totalLastDamage; }
	public int calculateMaxSentries() { return 0; }
	public void setEquipBackpackItem(boolean equip) { this.backpackItemEquipped = equip; }
	public boolean isBackpackItemEquipped() { return backpackItemEquipped; }
	public void onChangeValue(EntityDataAccessor<?> key, Object value) {}
	public GameArena getGameArena() { return gameArena; }
	public void setGameArena(GameArena gameArena) { this.gameArena = gameArena; }

	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> capability, Direction facing) {
		return LazyOptional.empty();
	}

	@Override
	public CompoundTag serializeNBT() {
		return new CompoundTag();
	}

	@Override
	public void deserializeNBT(CompoundTag nbt) {}

	public static TF2PlayerCapability get(Player player) {
		return new TF2PlayerCapability(player);
	}
}
