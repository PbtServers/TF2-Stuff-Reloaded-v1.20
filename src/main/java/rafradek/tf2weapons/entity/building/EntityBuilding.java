package rafradek.tf2weapons.entity.building;



import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import com.google.common.base.Optional;
import net.minecraft.client.gui.Gui;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.protocol.game.ClientboundMoveEntityPacket;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.scores.Team;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.items.ItemStackHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import rafradek.tf2weapons.TF2ConfigVars;
import rafradek.tf2weapons.TF2PlayerCapability;
import rafradek.tf2weapons.TF2weapons;
import rafradek.tf2weapons.client.ClientProxy;
import rafradek.tf2weapons.client.audio.BuildingSound;
import rafradek.tf2weapons.client.audio.TF2Sounds;
import rafradek.tf2weapons.common.TF2Attribute;
import rafradek.tf2weapons.common.WeaponsCapability;
import rafradek.tf2weapons.entity.IEntityTF2;
import rafradek.tf2weapons.entity.mercenary.EntityEngineer;
import rafradek.tf2weapons.item.ItemPDA;
import rafradek.tf2weapons.item.ItemSapper;
import rafradek.tf2weapons.util.PlayerPersistStorage;
import rafradek.tf2weapons.util.TF2Util;

import javax.annotation.Nullable;
import java.util.UUID;

public class EntityBuilding extends Mob implements OwnableEntity, IEntityTF2 {

	private static final EntityDataAccessor<Byte> VIS_TEAM = SynchedEntityData.createKey(EntityBuilding.class,
			EntityDataSerializers.BYTE);
	private static final EntityDataAccessor<Byte> LEVEL = SynchedEntityData.createKey(EntityBuilding.class,
			EntityDataSerializers.BYTE);
	private static final EntityDataAccessor<Byte> SOUND_STATE = SynchedEntityData.createKey(EntityBuilding.class,
			EntityDataSerializers.BYTE);
	private static final EntityDataAccessor<Integer> PROGRESS = SynchedEntityData.createKey(EntityBuilding.class,
			EntityDataSerializers.VARINT);
	private static final EntityDataAccessor<Integer> CONSTRUCTING = SynchedEntityData.createKey(EntityBuilding.class,
			EntityDataSerializers.VARINT);
	private static final EntityDataAccessor<Integer> ENERGY = SynchedEntityData.createKey(EntityBuilding.class,
			EntityDataSerializers.VARINT);
	private static final EntityDataAccessor<Byte> SAPPED = SynchedEntityData.createKey(EntityBuilding.class,
			EntityDataSerializers.BYTE);
	protected static final EntityDataAccessor<Optional<UUID>> OWNER_UUID = SynchedEntityData.createKey(EntityBuilding.class,
			EntityDataSerializers.OPTIONAL_UNIQUE_ID);

	private static final Logger LOGGER = LogManager.getLogger();

	public static final UUID UPGRADE_HEALTH_UUID = UUID.fromString("1184831d-b1dc-40c8-86e6-34fa8f30bada");

	public static final DamageSource DETONATE = new DamageSource("detonate").setDamageBypassesArmor()
			.setDamageIsAbsolute();
	public static final int SENTRY_COST = 130;
	public static final int DISPENSER_COST = 100;
	public static final int TELEPORTER_COST = 50;
	public static final int SENTRY_MINI_COST = 100;
	public static final int SENTRY_DISPOSABLE_COST = 60;

	public LivingEntity owner;
	public BuildingSound buildingSound;
	public int wrenchBonusTime;
	public float wrenchBonusMult;
	public ItemStack sapper = ItemStack.EMPTY;
	public LivingEntity sapperOwner;
	public boolean playerOwner;
	public boolean redeploy;
	public String ownerName;
	public EnergyStorage energy;
	public int ticksNoOwner;
	private boolean engMade;
	public ItemStackHandler charge;
	public boolean fromPDA;
	private int disposableID = -1;
	public UUID ownerEntityID;

	public EntityBuilding(Level world) {
		super(world);
		this.applyTasks();
		this.setHealth(0.1f);
		this.energy = new EnergyStorage(40000);
		this.charge = new ItemStackHandler(1);
		// this.notifyDataManagerChange(LEVEL);
		this.adjustSize();
		enablePersistence();
	}

	public void applyTasks() {

	}

	@Override
	public boolean canBePushed() {
		return false;
	}

	@Override
	public void applyEntityCollision(Entity entityIn) {
		if (entityIn.getEntityBoundingBox().intersects(this.getCollisionBoundingBox()))
			super.applyEntityCollision(entityIn);
	}

	public int getMaxLevel() {
		return 3;
	}

	@Override
	public void notifyDataManagerChange(EntityDataAccessor<?> key) {
		this.adjustSize();

		// System.out.println("Watcher update: "+data);
		if (!this.world.isRemote && CONSTRUCTING.equals(key)) {
			this.setSoundState(this.dataManager.get(CONSTRUCTING) >= this.getConstructionTime() ? 0 : 25);
		}
		if (this.world.isRemote && SOUND_STATE.equals(key)) {
			SoundEvent sound = this.getSoundNameForState(this.getSoundState());
			if (sound != null) {
				// System.out.println("Playing Sound: "+sound);
				if (this.buildingSound != null)
					this.buildingSound.stopPlaying();
				this.buildingSound = new BuildingSound(this, sound, this.getSoundState());
				ClientProxy.playBuildingSound(buildingSound);
			} else {
				if (this.buildingSound != null)
					this.buildingSound.stopPlaying();
			}
		}
	}

	@Override
	public boolean processInteract(Player player, InteractionHand hand) {
		if (!this.world.isRemote && player == this.getOwner() && hand == InteractionHand.MAIN_HAND) {
			this.grab();
			return true;
		}
		return false;
	}

	@Override
	public ItemStack getPickedResult(HitResult target) {
		ItemStack stack = new ItemStack(TF2weapons.itemBuildingBox, 1,
				(this instanceof EntitySentry ? 18 : (this instanceof EntityDispenser ? 20 : 22)) + this.getEntTeam());

		return stack;
	}

	public void grab() {
		if (!this.isDisabled() && this.disposableID == -1) {
			boolean grabbed = true;
			if (this.owner instanceof EntityEngineer) {
				CompoundTag tag = new CompoundTag();
				this.writeEntityToNBT(tag);
				((EntityEngineer) this.owner).grabbed = tag;
				((EntityEngineer) this.owner).grabbedid = this.getBuildingID();
				((EntityEngineer) this.owner).loadout.getStackInSlot(3).getTagCompound().setByte("Building",
						(byte) (this.getBuildingID() + 1));
				((EntityEngineer) this.owner).switchSlot(3);
			} else if (this.fromPDA) {
				int slotpda = TF2Util.getFirstItemSlot(((Player) this.getOwner()).inventory,
						stack -> stack.getItem() instanceof ItemPDA);
				if (slotpda != -1) {
					CompoundTag tag = new CompoundTag();
					this.writeEntityToNBT(tag);
					TF2PlayerCapability.get((Player) this.getOwner()).carrying = tag;
					TF2PlayerCapability.get((Player) this.getOwner()).carryingType = this.getBuildingID();
					this.clearReferences();
				} else
					grabbed = false;
			} else {
				ItemStack stack = this.getPickedResult(null);
				stack.setTagCompound(new CompoundTag());
				stack.getTagCompound().setTag("SavedEntity", new CompoundTag());
				this.writeEntityToNBT(stack.getTagCompound().getCompoundTag("SavedEntity"));
				this.entityDropItem(stack, 0);
			}

			// System.out.println("Saved:
			// "+stack.getTagCompound().getCompoundTag("SavedEntity"));
			if (grabbed)
				this.setDead();
		}
	}

	public void adjustSize() {

	}

	@Override
	public boolean isPotionApplicable(MobEffectInstance potioneffectIn) {
		return potioneffectIn.getPotion() == TF2weapons.stun;
	}

	public SoundEvent getSoundNameForState(int state) {
		return state == 50 ? TF2Sounds.MOB_SAPPER_IDLE : null;
	}

	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(16D * TF2ConfigVars.damageMultiplier);
		this.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(1D);
		this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0D);
	}

	@Override
	public boolean canBreatheUnderwater() {
		return true;
	}

	@Override
	public boolean attackEntityFrom(DamageSource source, float amount) {
		if (source == DamageSource.ON_FIRE)
			return false;
		return super.attackEntityFrom(source, amount);
	}

	@Override
	public void setFire(int time) {
		super.setFire(0);
	}

	@Override
	protected void entityInit() {
		super.entityInit();
		this.dataManager.register(VIS_TEAM, (byte) this.rand.nextInt(2));
		this.dataManager.register(OWNER_UUID, Optional.<UUID>absent());
		this.dataManager.register(LEVEL, (byte) 1);
		this.dataManager.register(SOUND_STATE, (byte) 25);
		this.dataManager.register(PROGRESS, 0);
		this.dataManager.register(SAPPED, (byte) 0);
		this.dataManager.register(CONSTRUCTING, 0);
		this.dataManager.register(ENERGY, 0);

	}

	public int getSoundState() {
		return this.dataManager.get(SOUND_STATE);
	}

	public void setSoundState(int state) {
		this.dataManager.set(SOUND_STATE, (byte) state);
	}

	@Override
	public UUID getOwnerId() {
		return this.dataManager.get(OWNER_UUID).orNull();
	}

	@Override
	public LivingEntity getOwner() {
		if (this.owner != null && !(this.owner instanceof Player && this.owner.isDead))
			return this.owner;
		else if (this.getOwnerId() != null)
			return this.owner = this.world.getPlayerEntityByUUID(this.getOwnerId());
		return null;
	}

	public void setOwner(LivingEntity owner) {
		this.owner = owner;
		if (owner instanceof Player) {
			this.ownerName = owner.getName();
			this.dataManager.set(OWNER_UUID, Optional.of(owner.getUniqueID()));
		} else if (this.getOwnerId() != null)
			this.dataManager.set(OWNER_UUID, Optional.absent());
		else if (owner != null)
			this.engMade = true;
	}

	@Override
	public void onDeath(DamageSource source) {
		super.onDeath(source);
		if (this.getOwner() instanceof EntityEngineer) {
			WeaponsCapability.get(this.getOwner()).giveMetal(
					getCost(this.getBuildingID(), ((EntityEngineer) this.getOwner()).loadout.getStackInSlot(2)) / 2);
		}
		this.clearReferences();
	}

	public void clearReferences() {
		if (this.getOwnerId() != null && this.fromPDA) {
			if (this.disposableID == -1)
				PlayerPersistStorage.get(this.world, getOwnerId()).buildings[this.getBuildingID()] = null;
			else {
				try {
					PlayerPersistStorage.get(this.world, getOwnerId()).disposableBuildings.remove(this.getUniqueID());
				} catch (IndexOutOfBoundsException e) {
					LOGGER.error("Disposable ID out of bounds");
				}
			}
		}
	}

	@Override
	public void onUpdate() {
		this.motionX = 0;
		this.motionZ = 0;
		if (this.firstUpdate && !this.world.isRemote && this.fromPDA
				&& !PlayerPersistStorage.get(this.world, this.getOwnerId()).allowBuilding(this)) {
			this.setDead();
			return;
		}

		if (!this.world.isRemote && this.engMade && this.getOwnerId() == null
				&& (this.owner == null || this.owner.isDead) && this.ticksNoOwner++ >= 120)
			this.setHealth(0);
		else
			this.ticksNoOwner = 0;
		if (this.motionY > 0)
			this.motionY = 0;
		if (!this.world.isRemote) {

			if (this.ticksExisted % 80 == 0) {
				int j1 = Mth.floor(this.rotationYaw * 256.0F / 360.0F);
				int l1 = Mth.floor(this.rotationPitch * 256.0F / 360.0F);
				((ServerLevel) this.world).getEntityTracker().sendToTracking(this,
						new ClientboundMoveEntityPacket.S16PacketEntityLook(this.getEntityId(), (byte) j1, (byte) l1, true));
			}

			if (this.fromPDA && this.ticksExisted % 5 == 0 && this.isEntityAlive()) {
				PlayerPersistStorage storage = PlayerPersistStorage.get(this.world, this.getOwnerId());
				if (this.disposableID == -1) {
					if (storage.buildings[this.getBuildingID()] == null
							|| !storage.buildings[this.getBuildingID()].getFirst().equals(this.getUniqueID())) {
						this.setHealth(0);
						this.onDeath(DETONATE);
						return;
					}
					CompoundTag tag = storage.buildings[this.getBuildingID()].getSecond();
					this.writeEntityToNBT(tag);
				} else {
					if (!storage.disposableBuildings.contains(this.getUniqueID())) {
						this.setHealth(0);
						this.onDeath(DETONATE);
						return;
					}
				}
				// storage.buildings[this.getBuildingID()] = new
				// Tuple<>(this.getUniqueID(),tag);
			}

			if (this.isSapped())
				TF2Util.dealDamage(this, this.world, this.sapperOwner, this.sapper, 0,
						this.sapper.isEmpty() ? 0.14f
								: ((ItemSapper) this.sapper.getItem()).getWeaponDamage(sapper, this.sapperOwner, this),
								TF2Util.causeDirectDamage(this.sapper, this.sapperOwner));

			if (this.charge.getStackInSlot(0).hasCapability(ForgeCapabilities.ENERGY, null)) {
				this.energy.receiveEnergy(this.charge.getStackInSlot(0).getCapability(ForgeCapabilities.ENERGY, null)
						.extractEnergy(this.energy.receiveEnergy(this.energy.getMaxEnergyStored(), true), false),
						false);
			}
			this.setInfoEnergy(this.energy.getEnergyStored());
			if (this.getOwnerId() != null && shouldUseBlocks())
				for (Direction facing : Direction.VALUES) {
					BlockPos pos = this.getPosition().offset(facing);
					BlockEntity ent = this.world.getTileEntity(pos);

					if (ent != null) {
						this.drawFromBlock(pos, ent, facing);
					}
				}
		}

		super.onUpdate();

		if (this.isConstructing())
			this.updateConstruction();
		this.wrenchBonusTime--;

	}

	public void detonate() {
		this.setHealth(0);
		this.onDeath(DETONATE);
	}

	public void drawFromBlock(BlockPos pos, BlockEntity ent, Direction facing) {
		if (ent.hasCapability(ForgeCapabilities.ENERGY, facing.getOpposite())) {
			this.energy.receiveEnergy(ent.getCapability(ForgeCapabilities.ENERGY, facing.getOpposite())
					.extractEnergy(this.energy.receiveEnergy(this.energy.getMaxEnergyStored(), true), false), false);
		}
	}

	public boolean shouldUseBlocks() {
		return this.energy.getEnergyStored() != this.energy.getMaxEnergyStored();
	}

	public boolean consumeEnergy(int amount) {
		return this.energy.getEnergyStored() >= amount && this.energy.extractEnergy(amount, true) == amount
				&& this.energy.extractEnergy(amount, false) == amount;
	}

	public int getMinEnergy() {
		return 0;
	}

	public void setSapped(LivingEntity owner, ItemStack sapper) {
		this.sapperOwner = owner;
		this.sapper = sapper;
		this.dataManager.set(SAPPED, (byte) 2);
		this.setSoundState(50);
	}

	@Override
	public boolean isAIDisabled() {
		return super.isAIDisabled() || this.isDisabled();
	}

	public boolean isSapped() {
		return this.dataManager.get(SAPPED) > 0;
	}

	public boolean isDisabled() {
		return !this.isEntityAlive() || this.isConstructing() || this.isSapped()
				|| this.energy.getEnergyStored() < this.getMinEnergy()
				|| this.getActivePotionEffect(TF2weapons.stun) != null;
	}

	public void removeSapper() {
		dataManager.set(SAPPED, (byte) (dataManager.get(SAPPED) - 1));
		if (!isSapped()) {
			this.setSoundState(0);
			this.playSound(TF2Sounds.MOB_SAPPER_DEATH, 1.5f, 1f);
			this.dropItem(Items.IRON_INGOT, 1);
		}
	}

	@Override
	public AABB getCollisionBox(Entity entityIn) {
		return ((entityIn != null && !TF2Util.isOnSameTeam(entityIn, this)) || entityIn == this.getOwner())
				&& this.isEntityAlive() ? entityIn.getEntityBoundingBox() : null;
	}

	@Override
	public AABB getCollisionBoundingBox() {
		if (!this.isEntityAlive())
			return null;
		/*
		 * else if(this.height>this.getCollHeight()){ AABB
		 * colBox=this.getEntityBoundingBox();
		 * colBox=colBox.grow((this.getCollWidth()-this.width)/2,
		 * (this.getCollHeight()-this.height)/2, (this.getCollWidth()-this.width)/2);
		 * colBox=colBox.offset(0, this.getEntityBoundingBox().minY-colBox.minY, 0);
		 * return colBox; }
		 */
		return this.getEntityBoundingBox();
	}

	@Override
	public Team getTeam() {
		if (this.getOwner() != null) {
			return this.getOwner().getTeam();
		} else if (this.getOwnerId() != null) {
			return this.world.getScoreboard().getPlayersTeam(this.ownerName);
		} else {
			switch (this.getEntTeam()) {
			case 0:
				return this.world.getScoreboard().getTeam("RED");
			case 1:
				return this.world.getScoreboard().getTeam("BLU");
			case 2:
				return this.world.getScoreboard().getTeam("Robots");
			default:
				return this.world.getScoreboard().getTeam("RED");
			}
		}
	}

	public int getProgress() {
		if (this.isConstructing())
			return (int) (((float) this.dataManager.get(CONSTRUCTING) / this.getConstructionTime()) * 200);
		else
			return this.dataManager.get(PROGRESS);
	}

	public void setProgress(int progress) {
		this.dataManager.set(PROGRESS, progress);
	}

	public int getInfoEnergy() {
		return this.dataManager.get(ENERGY);
	}

	public void setInfoEnergy(int energy) {
		this.dataManager.set(ENERGY, energy);
	}

	public int getLevel() {
		return this.dataManager.get(LEVEL);
	}

	public void setLevel(int level) {
		this.dataManager.set(LEVEL, (byte) level);
	}

	public void upgrade() {
		this.setLevel(this.getLevel() + 1);
		this.setProgress(0);
		this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH)
		.setBaseValue(this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).getBaseValue() * 1.2);
		this.setHealth(this.getMaxHealth());
		this.adjustSize();
	}

	public int getEntTeam() {
		return this.dataManager.get(VIS_TEAM);
	}

	public void setEntTeam(int team) {
		this.dataManager.set(VIS_TEAM, (byte) team);
	}

	public boolean isConstructing() {
		return this.dataManager.get(CONSTRUCTING) < this.getConstructionTime();
	}

	public void setConstructing(boolean constr) {

		this.dataManager.set(CONSTRUCTING, constr ? 0 : this.getConstructionTime());
	}

	public void updateConstruction() {
		if (!this.redeploy)
			this.heal((this.getConstructionRate() * this.getMaxHealth()) / this.getConstructionTime());
		this.dataManager.set(CONSTRUCTING, this.dataManager.get(CONSTRUCTING) + this.getConstructionRate());
		if (this.redeploy && this.dataManager.get(CONSTRUCTING) >= this.getConstructionTime())
			this.redeploy = false;
	}
	/*
	 * @Override public boolean writeToNBTOptional(CompoundTag tagCompund) {
	 * return this.getOwnerId() != null ? super.writeToNBTOptional(tagCompund) :
	 * false; }
	 */

	@Override
	public void writeEntityToNBT(CompoundTag par1NBTTagCompound) {
		super.writeEntityToNBT(par1NBTTagCompound);

		par1NBTTagCompound.setByte("Team", (byte) this.getEntTeam());
		par1NBTTagCompound.setByte("Level", (byte) this.getLevel());
		par1NBTTagCompound.setShort("Progress", (byte) this.getProgress());
		par1NBTTagCompound.setShort("Sapper", this.dataManager.get(SAPPED));
		par1NBTTagCompound.setShort("Construction", this.dataManager.get(CONSTRUCTING).shortValue());
		par1NBTTagCompound.setByte("WrenchBonus", (byte) this.wrenchBonusTime);
		par1NBTTagCompound.setBoolean("Redeploy", this.redeploy);
		par1NBTTagCompound.setBoolean("EngMade", this.engMade);
		par1NBTTagCompound.setBoolean("FromPDA", this.fromPDA);
		par1NBTTagCompound.setByte("TicksOwnerless", (byte) this.ticksNoOwner);
		par1NBTTagCompound.setTag("Charge", this.charge.serializeNBT());
		par1NBTTagCompound.setInteger("Energy", this.energy.getEnergyStored());
		par1NBTTagCompound.setByte("DisposableID", (byte) this.disposableID);
		if (this.getOwner() != null && !(this.getOwner() instanceof Player))
			par1NBTTagCompound.setUniqueId("OwnerE", this.getOwner().getUniqueID());
		if (this.getOwnerId() != null) {
			par1NBTTagCompound.setUniqueId("Owner", this.getOwnerId());
			par1NBTTagCompound.setString("OwnerName", this.ownerName);
		}
		if (this.isDisabled()) {
			par1NBTTagCompound.setBoolean("NoAI", false);
		}
	}

	@Override
	public void readEntityFromNBT(CompoundTag tag) {
		super.readEntityFromNBT(tag);

		this.setEntTeam(tag.getByte("Team"));
		this.setLevel(tag.getByte("Level"));
		this.setProgress(tag.getByte("Progress"));
		this.dataManager.set(CONSTRUCTING, (int) tag.getShort("Construction"));
		this.wrenchBonusTime = tag.getByte("WrenchBonus");
		this.redeploy = tag.getBoolean("Redeploy");
		this.ticksNoOwner = tag.getByte("Ownerless");
		this.engMade = tag.getBoolean("EngMade");
		this.fromPDA = tag.getBoolean("FromPDA");
		this.charge.deserializeNBT(tag.getCompoundTag("Charge"));
		this.energy.receiveEnergy(tag.getInteger("Energy"), false);
		this.disposableID = tag.getByte("DisposableID");
		if (tag.getByte("Sapper") != 0)
			this.setSapped(this, ItemStack.EMPTY);
		if (tag.hasUniqueId("OwnerE"))
			this.ownerEntityID = tag.getUniqueId("OwnerE");
		if (tag.hasUniqueId("Owner")) {
			UUID ownerID = tag.getUniqueId("Owner");
			this.dataManager.set(OWNER_UUID, Optional.of(ownerID));
			this.ownerName = tag.getString("OwnerName");
			this.getOwner();
			this.enablePersistence();
		}
		if (owner != null) {
			this.setEntTeam(TF2Util.getTeamForDisplay(owner));
		}
	}

	public int getBuildingID() {
		return 0;
	}

	public float getCollHeight() {
		return 1f;
	}

	public float getCollWidth() {
		return 0.95f;
	}

	public boolean canUseWrench() {
		return this.canUseWrenchImportant() || this.getLevel() < this.getMaxLevel();
	}

	public boolean canUseWrenchImportant() {
		return this.getMaxHealth() > this.getHealth();
	}

	@Override
	public boolean canBeHitWithPotion() {
		return false;
	}

	@Override
	protected float updateDistance(float p_110146_1_, float p_110146_2_) {
		this.renderYawOffset = this.rotationYaw;
		return p_110146_2_;
	}

	@Override
	protected void dropFewItems(boolean p_70628_1_, int p_70628_2_) {
		LivingEntity attacker = this.getAttackingEntity();
		if (this.fromPDA || (TF2Util.isOnSameTeam(attacker, this) && this.getOwnerId() == null))
			return;
		if (!(this.getOwner() instanceof EntityEngineer && ((EntityEngineer) this.getOwner()).buildCount >= 3)) {
			int count = this.getOwner() instanceof Player ? this.getIronDrop()
					: Mth.ceil(this.getIronDrop() / 2);
			for (int i = 0; i < count; i++)
				this.dropItem(Items.IRON_INGOT, 1);
		}
	}

	@Override
	protected void dropEquipment(boolean wasRecentlyHit, int lootingModifier) {
		super.dropEquipment(wasRecentlyHit, lootingModifier);
		this.entityDropItem(this.charge.getStackInSlot(0), 0);
	}

	public int getIronDrop() {
		return 1 + this.getLevel();
	}

	@Override
	protected boolean canDespawn() {
		return this.getOwnerId() == null && (this.getOwner() == null || !this.getOwner().isEntityAlive());
	}

	@OnlyIn(Dist.CLIENT)
	public void renderGUI(BufferBuilder renderer, Tesselator tessellator, Player player, int width, int height,
			Gui gui) {

	}

	public int getGuiHeight() {
		return 48;
	}

	public int getConstructionTime() {
		return 21000;
	}

	public int getConstructionRate() {
		int i = 50;
		if (this.wrenchBonusTime > 0)
			i += 75 * this.wrenchBonusMult;
		if (this.redeploy)
			i += 100;
		if (TF2ConfigVars.fastBuildEngineer && this.getOwner() != null && this.getOwner() instanceof EntityEngineer)
			i += 125;
		// System.out.println("Constr: "+i);
		return i;
	}

	@SuppressWarnings("unchecked")
	@Override
	@Nullable
	public <T> T getCapability(Capability<T> capability, @Nullable Direction facing) {
		if (capability == ForgeCapabilities.ENERGY) {
			return (T) this.energy;
		}
		return super.getCapability(capability, facing);
	}

	@Override
	public boolean hasCapability(Capability<?> capability, @Nullable Direction facing) {
		return capability == ForgeCapabilities.ENERGY || super.hasCapability(capability, facing);
	}

	public static int getCost(int building, ItemStack wrench) {
		if (building == 0)
			return TF2Attribute.getModifier("Weapon Mode", wrench, 0f, null) == 2 ? EntityBuilding.SENTRY_MINI_COST
					: EntityBuilding.SENTRY_COST;
		if (building == 1)
			return EntityBuilding.DISPENSER_COST;
		else if (building == 4)
			return EntityBuilding.SENTRY_DISPOSABLE_COST;
		else
			return (int) (EntityBuilding.TELEPORTER_COST
					/ TF2Attribute.getModifier("Teleporter Cost", wrench, 1f, null));
	}

	@Override
	public boolean hasHead() {
		return false;
	}

	@Override
	public AABB getHeadBox() {
		return null;
	}

	@Override
	public boolean hasDamageFalloff() {
		return false;
	}

	@Override
	public boolean isBuilding() {
		return true;
	}

	@Override
	public boolean isBackStabbable(LivingEntity attacker, ItemStack knife) {
		return false;
	}

	public int getDisposableID() {
		return disposableID;
	}

	public void setDisposableID(int disposableID) {
		this.disposableID = disposableID;
	}

}
