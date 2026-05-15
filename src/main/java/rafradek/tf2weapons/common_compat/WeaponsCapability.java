package rafradek.tf2weapons.common;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import rafradek.tf2weapons.entity.building.EntitySentry;
import rafradek.tf2weapons.entity.projectile.EntityGrapplingHook;
import rafradek.tf2weapons.entity.projectile.EntityStickybomb;
import rafradek.tf2weapons.message.TF2Message;

public class WeaponsCapability implements ICapabilityProvider, INBTSerializable<CompoundTag> {
	public static final int MAX_METAL = 200;
	public static final int MAX_METAL_ENGINEER = 500;
	public static final int PLAYER_MINCOOL = -200;
	public static final ExecutorService THREAD_POOL = Executors.newCachedThreadPool();

	public LivingEntity owner;
	public int state;
	public int minigunTicks;
	public InteractionHand reloadingHand;
	public int reloadCool;
	public int lastFire;
	public boolean mainHand;
	public HashMap<String, Integer> effectsCool = new HashMap<>();
	public int chargeTicks;
	public int critTimeCool;
	@SuppressWarnings("unchecked")
	public Deque<TF2Message.PredictionMessage>[] predictionList = new Deque[8];
	public float recoil;
	public int invisTicks;
	public int disguiseTicks;
	public boolean pressedStart;
	public int airJumps;
	public EntitySentry controlledSentry;
	public ResourceLocation skinDisguise;
	public boolean skinRetrieved;
	public String lastDisguiseValue = "";
	public boolean lastDisgused;
	public String skinType = "";
	public int ticksBash;
	public boolean bashCritical;
	public int collectedHeadsTime;
	public boolean wornEye;
	public int killsSpinning;
	public int tickAirblasted;
	public int itProtection;
	public int killsAirborne;
	public int focusShotTicks;
	public int focusShotRemaining;
	public int fanCool;
	public boolean appliedMouseSlow;
	public int hitNoMiss;
	public int sentryTargets = 5;
	public boolean dispenserPlayer;
	public boolean teleporterPlayer;
	public boolean teleporterEntity;
	public boolean forcedClass;
	public float lastHitCharge;
	public SynchedEntityData dataManager;
	public LivingEntity entityDisguise;
	public ArrayList<EntityStickybomb> activeBomb = new ArrayList<>();
	public float oldFactor;
	public int expJumpGround;
	public double lastPosX;
	public double lastPosY;
	public double lastPosZ;
	public ItemStack lastWeapon = ItemStack.EMPTY;
	public long ticksTotal;
	public boolean fireCoolReduced;
	public boolean autoFire;
	public LivingEntity lastAttacked;
	public boolean stabbedDisguise;
	public double gravity = -0.08;
	public float maxmetal = 1;
	public float damageArmorMin;
	public EnumMap<InteractionHand, ItemStack> stackActive = new EnumMap<>(InteractionHand.class);
	public int disguiseCounter;

	private boolean canExpJump = true;
	private boolean expJump;
	private boolean invisible;
	private boolean disguised;
	private boolean feign;
	private boolean charging;
	private boolean grappled;
	private boolean grappling;
	private int critTime;
	private int healTarget = -1;
	private int heads;
	private int metal = MAX_METAL;
	private int usedToken = -1;
	private int primaryCooldown;
	private int secondaryCooldown;
	private float uberView;
	private EntityGrapplingHook grapplingHook;
	private final EnumMap<RageType, Float> rage = new EnumMap<>(RageType.class);
	private final EnumMap<RageType, Boolean> rageActive = new EnumMap<>(RageType.class);

	public WeaponsCapability(LivingEntity entity) {
		this.owner = entity;
		for (int i = 0; i < this.predictionList.length; i++)
			this.predictionList[i] = new ArrayDeque<>();
	}

	public int getCritTime() { return critTime; }
	public void setCritTime(int time) { this.critTime = time; }
	public int getHealTarget() { return healTarget; }
	public void setHealTarget(int target) { this.healTarget = target; }
	public float getUberView() { return uberView; }
	public void setUberView(float uber) { this.uberView = uber; }
	public int getHeads() { return heads; }
	public int getMetal() { return metal; }
	public boolean hasMetal(int metal) { return this.metal >= metal; }
	public int consumeMetal(int metal, boolean allowPartial) {
		int consumed = allowPartial ? Math.min(this.metal, metal) : (this.metal >= metal ? metal : 0);
		this.metal -= consumed;
		return consumed;
	}
	public void setMetal(int metal) { this.metal = metal; }
	public void giveMetal(int metal) { this.metal += metal; }
	public int getMaxMetal() { return MAX_METAL; }
	public float getRage(RageType type) { return rage.getOrDefault(type, 0f); }
	public void setRage(RageType type, float rage) { this.rage.put(type, rage); }
	public boolean isRageActive(RageType type) { return rageActive.getOrDefault(type, false); }
	public void setRageActive(RageType type, boolean active, float drain) { this.rageActive.put(type, active); }
	public boolean isInvisible() { return invisible; }
	public void setInvisible(boolean invis) { this.invisible = invis; }
	public void setDisguised(boolean val) { this.disguised = val; }
	public boolean isDisguised() { return disguised; }
	public void setExpJump(boolean val) { this.expJump = val; }
	public boolean isExpJump() { return expJump; }
	public void setDisguiseType(String val) { this.lastDisguiseValue = val; }
	public String getDisguiseType() { return lastDisguiseValue == null ? "" : lastDisguiseValue; }
	public void setFeign(boolean val) { this.feign = val; }
	public boolean isFeign() { return feign; }
	public void setCharging(boolean val) { this.charging = val; }
	public boolean isCharging() { return charging; }
	public void setUsedToken(int val) { this.usedToken = val; }
	public int getUsedToken() { return usedToken; }
	public boolean canFire(InteractionHand hand, boolean primary) { return true; }
	public void setCanFire(boolean fire, InteractionHand hand, boolean primary) {}
	public void addEffectCooldown(String name, int time) { this.effectsCool.put(name, time); }
	public void addHead(ItemStack weapon) { this.heads++; }
	public boolean focusedShot(ItemStack stack) { return false; }
	public int focusShotTime(ItemStack stack) { return 0; }
	public void onChangeValue(Object param, Object newValue) {}
	public boolean isUsingParachute() { return false; }
	public void tick() {}
	public boolean startedPress() { boolean val = pressedStart; pressedStart = false; return val; }
	public boolean shouldShoot(LivingEntity player, int state, InteractionHand hand, int actualState) { return false; }
	public void stateDo(LivingEntity player, ItemStack stack, InteractionHand hand, int state) {}
	public void updateExpJump() {}
	public void preparePlayerPrediction(LivingEntity player, TF2Message.PredictionMessage message) {}
	public int getMaxAirJumps() { return 0; }
	public int getMinimalCooldown() { return PLAYER_MINCOOL; }
	public int getPrimaryCooldown() { return primaryCooldown; }
	public void setPrimaryCooldown(int fire1Cool) { this.primaryCooldown = fire1Cool; }
	public int getPrimaryCooldown(InteractionHand hand) { return primaryCooldown; }
	public void setPrimaryCooldown(InteractionHand hand, int fire1Cool) { this.primaryCooldown = fire1Cool; }
	public int getSecondaryCooldown() { return secondaryCooldown; }
	public void setSecondaryCooldown(int fire2Cool) { this.secondaryCooldown = fire2Cool; }
	public int getSecondaryCooldown(InteractionHand hand) { return secondaryCooldown; }
	public void setSecondaryCooldown(InteractionHand hand, int fire2Cool) { this.secondaryCooldown = fire2Cool; }
	public void stopReload() { this.reloadCool = 0; this.reloadingHand = null; }
	public boolean canExpJump() { return canExpJump; }
	public void setCanExpJump(boolean canExpJump) { this.canExpJump = canExpJump; }
	public void setActiveHand(InteractionHand hand, ItemStack stack) { this.stackActive.put(hand, stack); }
	public void setInactiveHand(InteractionHand hand, ItemStack stack) { this.stackActive.remove(hand); }
	public void disguise() {}
	public boolean isGrappling() { return grappling; }
	public boolean isGrappled() { return grappled; }
	public void setGrappled(boolean grappled) { this.grappled = grappled; }
	public EntityGrapplingHook getGrapplingHook() { return grapplingHook; }
	public void setGrapplingHook(EntityGrapplingHook grapplingHook) { this.grapplingHook = grapplingHook; }

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

	public static SynchedEntityData getDataManager(Entity ent) {
		return ent == null ? null : ent.getEntityData();
	}

	public static WeaponsCapability get(Entity ent) {
		return new WeaponsCapability(ent instanceof LivingEntity living ? living : null);
	}

	public enum RageType {
		NONE,
		PHLOG,
		MINICRIT,
		KNOCKBACK,
		BANNER
	}
}
