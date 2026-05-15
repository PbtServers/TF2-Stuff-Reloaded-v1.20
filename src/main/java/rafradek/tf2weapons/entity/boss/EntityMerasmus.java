package rafradek.tf2weapons.entity.boss;



import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.*;
import net.minecraft.world.entity.ai.control.*;
import rafradek.tf2weapons.entity.ai.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.pathfinding.PathNavigateClimber;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.util.*;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import rafradek.tf2weapons.TF2weapons;
import rafradek.tf2weapons.block.BlockProp;
import rafradek.tf2weapons.client.audio.TF2Sounds;
import rafradek.tf2weapons.client.particle.EnumTF2Particles;
import rafradek.tf2weapons.common.TF2Attribute;
import rafradek.tf2weapons.common.WeaponsCapability;
import rafradek.tf2weapons.entity.building.EntityBuilding;
import rafradek.tf2weapons.entity.mercenary.EntityTF2Character;
import rafradek.tf2weapons.item.ItemFromData;
import rafradek.tf2weapons.item.ItemProjectileWeapon;
import rafradek.tf2weapons.item.ItemWeapon;
import rafradek.tf2weapons.message.TF2Message;
import rafradek.tf2weapons.util.TF2Util;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class EntityMerasmus extends EntityTF2Boss {

	private int begin = 30;
	private int teleportCooldown = 240;
	private int bombCooldown = 160;
	private int bombDuration;
	public boolean hidden;
	public int topBlock;
	public BlockPos hiddenBlock;
	private int hideCount;
	public ArrayList<BlockPos> usedPos = new ArrayList<>();
	private static final EntityDataAccessor<Boolean> SPELL_BOMB = SynchedEntityData.createKey(EntityMerasmus.class,
			EntityDataSerializers.BOOLEAN);

	public EntityMerasmus(Level world) {
		super(world);
		this.setSize(1.15f, 3.5f);
		this.stepHeight = 1.05f;
		this.setNoAI(true);
		this.setHeldItem(InteractionHand.MAIN_HAND, ItemFromData.getNewStack("mrsbomb"));
	}

	@Override
	public void entityInit() {
		super.entityInit();
		this.dataManager.register(SPELL_BOMB, false);
	}

	@Override
	protected void initEntityAI() {
		this.tasks.addTask(1, new FloatGoal(this));
		this.tasks.addTask(4, new AIAttack(this));
		this.tasks.addTask(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
		this.tasks.addTask(6, new RandomLookAroundGoal(this));

		this.targetTasks.addTask(1, new HurtByTargetGoal(this, false, new Class[0]));
		this.targetTasks.addTask(2, new EntityAINearestAttackableTarget<LivingEntity>(this, LivingEntity.class,
				3, false, false, input -> input instanceof EntityTF2Character || input instanceof Player) {
			@Override
			protected double getTargetDistance() {
				return super.getTargetDistance() * 0.35;
			}

			@Override
			public boolean shouldExecute() {

				return super.shouldExecute();
			}
		});
		this.targetTasks.addTask(2, new EntityAINearestAttackableTarget<Player>(this, Player.class, 5,
				false, false, input -> input instanceof Player) {
			@Override
			public boolean shouldExecute() {
				boolean ex = super.shouldExecute();
				return ex;
			}
		});
	}

	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		// this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).
		this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(256.0D);
		this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(200);
		this.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(1.0D);
		this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.098D);
		this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(0D);

	}

	protected PathNavigate getNewNavigator(Level world) {
		return new PathNavigateClimber(this, world);
	}

	@Override
	public float getEyeHeight() {
		return 2.1f;
	}

	@Override
	public boolean attackEntityFrom(DamageSource source, float amount) {
		float prevHP = this.getHealth();
		if (super.attackEntityFrom(source, amount)) {
			float newHP = this.getHealth();
			if (this.isEntityAlive() && !this.isBombSpell()) {
				if (this.hideCount == 0 && newHP / this.getMaxHealth() <= 0.55f) {
					this.hide(true);
					this.hideCount = 1;
				} else if (this.hideCount == 1 && newHP / this.getMaxHealth() <= 0.1f) {
					this.hide(true);
					this.hideCount = 2;
				}
			}
			return true;
		}
		return false;
	}

	@Override
	public void onLivingUpdate() {
		if (this.getHeldItemMainhand().isEmpty() || !(this.getHeldItemMainhand().getItem() instanceof ItemWeapon))
			this.setHeldItem(InteractionHand.MAIN_HAND, ItemFromData.getNewStack("mrsbomb"));

		super.onLivingUpdate();
		if (this.getActivePotionEffect(TF2weapons.stun) != null)
			this.rotationPitch = 90;
		if (this.begin-- > 20 && this.world.isRemote)
			for (int i = 0; i < 40; i++) {
				Vec3 pos = TF2Util.radiusRandom2D(2.2f, this.rand);
				this.world.spawnParticle(EnumParticleTypes.PORTAL, pos.x + this.posX, this.posY - 0.5,
						pos.y + this.posZ, 0, 0, 0, new int[0]);
			}

		if (!world.isRemote) {
			if (this.begin == 0) {
				this.setNoAI(false);
				this.getEntityAttribute(SharedMonsterAttributes.ARMOR).removeModifier(BOSS_ARMOR_SPAWN);
			}
			this.bombDuration--;
			for (LivingEntity living : this.world.getEntitiesWithinAABB(LivingEntity.class,
					this.getEntityBoundingBox().grow(1.5, 1, 1.5), input -> input.getActivePotionEffect(TF2weapons.bombmrs) != null)) {
				living.removePotionEffect(TF2weapons.bombmrs);
				living.removePotionEffect(TF2weapons.stun);
				living.removePotionEffect(MobEffects.NAUSEA);
				this.playSound(TF2Sounds.MOB_MERASMUS_STUN, 1F, 1f);
				this.attackEntityFrom(new EntityDamageSource("magicb", living).setMagicDamage(), 15);
				this.addPotionEffect(new MobEffectInstance(TF2weapons.stun, 120, 3));
				this.teleportCooldown = 120;
			}
			if (!this.hidden) {

				if (this.ticksExisted % 5 == 0) {
					if (this.getAttackTarget() != null && !this.getEntitySenses().canSee(this.getAttackTarget())) {
						TF2Attribute.setAttribute(this.getHeldItemMainhand(), TF2Attribute.attributes[39], 0.35f);
					} else
						TF2Attribute.setAttribute(this.getHeldItemMainhand(), TF2Attribute.attributes[39], 0f);
				}

				if (this.bombCooldown-- <= 0) {
					List<Player> list = this.world.getEntitiesWithinAABB(Player.class,
							this.getEntityBoundingBox().grow(30, 15, 30), input -> getDistanceSq(input) < 900
									&& !TF2Util.isOnSameTeam(EntityMerasmus.this, input) && EntityAITarget
											.isSuitableTarget(EntityMerasmus.this, input, false, false));
					if (!list.isEmpty()) {
						Player living = list.get(this.rand.nextInt(list.size()));
						living.addPotionEffect(new MobEffectInstance(TF2weapons.bombmrs, 300));
						TF2Util.stun(living, 300, false);
						((ServerPlayer) living).connection
								.sendPacket(new ClientboundSoundPacket(TF2Sounds.MOB_MERASMUS_HEADBOMB,
										this.getSoundCategory(), living.posX, living.posY, living.posZ, 4F, 1f));
						// this.teleportCooldown=90;
					}
					this.bombCooldown = Math.max(680 - this.playersAttacked * 60, 480);
				}

				if (this.teleportCooldown-- <= 0 && !this.isBombSpell()) {
					this.teleport();
				}
				if (this.isBombSpell()) {
					float prevPitch = this.rotationPitch;
					float prevYaw = this.rotationYawHead;
					if (this.ticksExisted % 8 == 0) {
						for (int i = 0; i < 16; i++) {
							this.rotationPitch = -70 + this.rand.nextFloat() * 120f;
							this.rotationYawHead = i * 22.5f + this.rand.nextFloat() * 22.5f;
							((ItemProjectileWeapon) this.getHeldItemMainhand().getItem())
									.shoot(this.getHeldItemMainhand(), this, this.world, 0, InteractionHand.MAIN_HAND);
						}
					}
					if (this.posY < topBlock) {
						this.motionY = 0.15f;
					} else {
						this.motionY = 0;
					}
					if (this.bombDuration == 200 || !this.getMoveHelper().isUpdating()) {

						Random random = this.getRNG();
						BlockPos pos = this.world.getTopSolidOrLiquidBlock(this.getPosition());
						double d0 = this.posX;
						double d2 = this.posZ;
						if (pos.getY() + 7 < this.posY) {
							d0 += (random.nextFloat() * 2.0F - 1.0F) * 16.0F;
							d2 += (random.nextFloat() * 2.0F - 1.0F) * 16.0F;
						}
						double d1 = pos.getY() + 7 + random.nextInt(3);
						this.getMoveHelper().setMoveTo(d0, this.topBlock, d2, 1.0D);

					}
					this.rotationPitch = prevPitch;
					this.rotationYawHead = prevYaw;
					if (this.bombDuration < 40) {
						this.setBombSpell(false);
					}
				}
			}

			if (this.hidden) {
				if (this.ticksExisted % 5 == 0)
					this.heal(this.getMaxHealth() * 0.001f);
				if (this.world.getBlockState(this.hiddenBlock).getBlock() != TF2weapons.blockProp) {
					this.hide(false);
				}
			}
		}

	}

	public void teleport() {
		this.teleportCooldown = 240;
		this.playSound(TF2Sounds.MOB_MERASMUS_DISAPPEAR, 1F, 1f);

		for (int i = 0; i < 10; i++) {
			double x;
			double z;
			if (this.getAttackTarget() != null) {
				x = this.getAttackTarget().posX + rand.nextDouble() * 40 - 20;
				z = this.getAttackTarget().posZ + rand.nextDouble() * 40 - 20;
			} else {
				x = this.posX + rand.nextDouble() * 40 - 20;
				z = this.posZ + rand.nextDouble() * 40 - 20;
			}
			double y = this.world.getTopSolidOrLiquidBlock(new BlockPos(x, 0, z)).getY() + 3;
			if (this.attemptTeleport(x, y, z)) {
				this.playSound(TF2Sounds.MOB_MERASMUS_APPEAR, 1F, 1f);
				for (int j = 0; j < 40; j++) {
					Vec3 pos = TF2Util.radiusRandom3D(2.7f, this.rand);
					this.world.spawnParticle(EnumParticleTypes.PORTAL, pos.x + this.posX, pos.y + this.posY,
							pos.z + this.posZ, 0, 0, 0, new int[0]);
				}
				this.teleportCooldown += 200 + rand.nextInt(80);
				break;
			}
		}
	}

	public boolean isBombSpell() {
		return this.getDataManager().get(SPELL_BOMB);
	}

	public void setBombSpell(boolean bomb) {
		this.getDataManager().set(SPELL_BOMB, bomb);
		this.getNavigator().clearPath();
		if (bomb) {
			this.moveHelper = new FloatingMoveHelper(this);
			this.setItemStackToSlot(EquipmentSlot.OFFHAND, ItemFromData.getNewStack("bombinomicon"));
			this.playSound(TF2Sounds.MOB_MERASMUS_BOMBINOMICON, 3.3F, 1F);
		} else {
			this.moveHelper = new MoveControl(this);
			this.setItemStackToSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
		}
		TF2Attribute.setAttribute(this.getHeldItemMainhand(), TF2Attribute.attributes[19], bomb ? 0.65f : 1);
		TF2Attribute.setAttribute(this.getHeldItemMainhand(), TF2Attribute.attributes[39], bomb ? 0f : 0.3f);
	}

	@Override
	public void dropFewItems(boolean hit, int looting) {
		ItemStack hat = ItemFromData.getNewStack("merasmushat");
		hat.getTagCompound().setShort("BossLevel", (short) this.level);
		this.entityDropItem(hat, 0);
	}

	/*
	 * public void addAchievement(Player player){
	 * super.addAchievement(player); player.addStat(TF2Achievements.MERASMUS); }
	 */
	@Override
	public SoundEvent getDeathSound() {
		return TF2Sounds.MOB_MERASMUS_DEFEAT;
	}

	@Override
	public SoundEvent getAppearSound() {
		return TF2Sounds.MOB_MERASMUS_START;
	}

	@Override
	public void setDead() {
		super.setDead();
		for (BlockPos pos : this.usedPos) {
			if (this.world.getBlockState(pos).getBlock() == TF2weapons.blockProp)
				this.world.setBlockState(pos, Blocks.AIR.getDefaultState());
		}
	}

	@Override
	public void writeEntityToNBT(CompoundTag nbt) {
		super.writeEntityToNBT(nbt);
		nbt.setBoolean("Hidden", this.hidden);
		if (hidden) {
			nbt.setIntArray("HiddenPos",
					new int[] { this.hiddenBlock.getX(), this.hiddenBlock.getY(), this.hiddenBlock.getZ() });
			ListTag list = new ListTag();
			nbt.setTag("Props", list);
			for (BlockPos pos : this.usedPos)
				list.appendTag(new NBTTagIntArray(new int[] { pos.getX(), pos.getY(), pos.getZ() }));
		}
		nbt.setShort("Begin", (short) this.begin);
		nbt.setShort("Teleport", (short) this.teleportCooldown);
		nbt.setShort("BombCooldown", (short) this.bombCooldown);
		nbt.setShort("BombDuration", (short) this.bombDuration);
		nbt.setShort("TopBlock", (short) this.topBlock);
		nbt.setByte("HideCount", (byte) this.hideCount);
		nbt.setBoolean("Bomb", this.isBombSpell());

	}

	@Override
	public void readEntityFromNBT(CompoundTag nbt) {
		super.readEntityFromNBT(nbt);
		this.begin = nbt.getShort("Begin");
		this.setBombSpell(nbt.getBoolean("Bomb"));
		this.bombCooldown = nbt.getShort("BombCooldown");
		this.bombDuration = nbt.getShort("BombDuration");
		this.topBlock = nbt.getShort("TopBlock");
		this.teleportCooldown = nbt.getShort("Teleport");
		this.hidden = nbt.getBoolean("Hidden");
		this.hideCount = nbt.getByte("HideCount");
		if (hidden) {
			this.setNoAI(true);
			int[] pos = nbt.getIntArray("HiddenPos");
			this.hiddenBlock = new BlockPos(pos[0], pos[1], pos[2]);
			ListTag list = nbt.getTagList("Props", 11);
			for (int i = 0; i < list.tagCount(); i++) {
				int[] arr = list.getIntArrayAt(i);
				this.usedPos.add(new BlockPos(arr[0], arr[1], arr[2]));
			}
		}
	}

	@SuppressWarnings("deprecation")
	public void hide(boolean hide) {
		this.hidden = hide;
		this.motionX = 0;
		this.motionY = 0;
		this.motionZ = 0;
		if (hide) {
			this.navigator.clearPath();
			// this.moveHelper=null;
			this.playSound(TF2Sounds.MOB_MERASMUS_HIDE, this.getSoundVolume(), 1F);
			this.setInvisible(true);
			int blockCount = (int) Math.min(100,
					10 * (0.7f + 0.3f * this.playersAttacked) * (0.9f + 0.1f * this.level));
			BlockPos initial = this.getPosition();
			for (int i = 0; i < blockCount; i++) {
				BlockPos pos = initial.add(
						this.rand.nextInt((int) (40 + blockCount * 0.3f)) - 20 - (int) (blockCount * 0.15f), 0,
						this.rand.nextInt((int) (40 + blockCount * 0.3f)) - 20 - (int) (blockCount * 0.15f));
				pos = this.world.getTopSolidOrLiquidBlock(pos);
				// pos=pos.add(0, 1, 0);
				if (this.world.getBlockState(pos).getBlock().isReplaceable(this.world, pos)) {
					this.world.setBlockState(pos, TF2weapons.blockProp
							.getStateFromMeta(this.rand.nextInt(BlockProp.EnumBlockType.values().length)));
				}
				if (i == 0)
					this.hiddenBlock = pos;
				this.setPositionAndUpdate(pos.getX(), -20, pos.getZ());
				this.usedPos.add(pos);
			}

		} else {
			this.setInvisible(false);
			for (BlockPos pos : this.usedPos) {
				if (this.world.getBlockState(pos).getBlock() == TF2weapons.blockProp)
					this.world.setBlockState(pos, Blocks.AIR.getDefaultState());
			}
			this.usedPos.clear();
			this.setPositionAndUpdate(this.hiddenBlock.getX() + 0.5, this.hiddenBlock.getY(),
					this.hiddenBlock.getZ() + 0.5);
			this.teleportCooldown = 20;
		}
		this.setNoAI(hide);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void travel(float strafe, float forward, float par3) {
		if (!this.isBombSpell() && !this.hidden) {
			super.travel(strafe, forward, par3);
			return;
		}
		if (this.isInWater()) {
			this.moveRelative(strafe, forward, par3, 0.02F);
			this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);
			this.motionX *= 0.800000011920929D;
			this.motionY *= 0.800000011920929D;
			this.motionZ *= 0.800000011920929D;
		} else if (this.isInLava()) {
			this.moveRelative(strafe, forward, par3, 0.02F);
			this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);
			this.motionX *= 0.5D;
			this.motionY *= 0.5D;
			this.motionZ *= 0.5D;
		} else {
			float f = 0.91F;

			if (this.onGround)
				f = this.world
						.getBlockState(new BlockPos(Mth.floor(this.posX),
								Mth.floor(this.getEntityBoundingBox().minY) - 1, Mth.floor(this.posZ)))
						.getBlock().slipperiness * 0.91F;

			float f1 = 0.16277136F / (f * f * f);
			this.moveRelative(strafe, forward, par3, this.onGround ? 0.1F * f1 : 0.02F);
			f = 0.91F;

			if (this.onGround)
				f = this.world
						.getBlockState(new BlockPos(Mth.floor(this.posX),
								Mth.floor(this.getEntityBoundingBox().minY) - 1, Mth.floor(this.posZ)))
						.getBlock().slipperiness * 0.91F;

			this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);
			this.motionX *= f;
			this.motionY *= f;
			this.motionZ *= f;
		}

		this.prevLimbSwingAmount = this.limbSwingAmount;
		double d1 = this.posX - this.prevPosX;
		double d0 = this.posZ - this.prevPosZ;
		float f2 = Mth.sqrt(d1 * d1 + d0 * d0) * 4.0F;

		if (f2 > 1.0F)
			f2 = 1.0F;

		this.limbSwingAmount += (f2 - this.limbSwingAmount) * 0.4F;
		this.limbSwing += this.limbSwingAmount;
	}

	public static class AIAttack extends EntityAIBase {

		public EntityMerasmus host;

		public int attacksMade;
		public int attackDuration;
		public boolean lastAttackMagic;
		public boolean liftup;

		public AIAttack(EntityMerasmus host) {
			this.host = host;
		}

		@Override
		public boolean shouldExecute() {
			return (this.host.getAttackTarget() != null && this.host.getActivePotionEffect(TF2weapons.stun) == null)
					|| host.envDamage > 0;
		}

		@Override
		public void updateTask() {
			LivingEntity target = this.host.getAttackTarget();
			if (target == null)
				target = this.host;
			Level world = this.host.Level;
			this.host.getLookHelper().setLookPositionWithEntity(target, 30F, 90F);
			if (attackDuration < 20) {
				this.host.getNavigator().tryMoveToEntityLiving(target, 1f);
			}
			if (--this.attackDuration <= 0) {
				if (host.envDamage > 0)
					host.envDamage -= 6;
				this.host.swingArm(InteractionHand.MAIN_HAND);
				boolean sup = this.host.level > 1 && (this.attacksMade % 7 == 0 || (this.attacksMade - 1) % 7 == 0);
				if (this.attacksMade > 0 && this.attacksMade % 13 == 0) {
					this.host.setBombSpell(true);
					this.host.bombDuration = 200;
					this.host.teleport();
					BlockPos pos = this.host.world.getTopSolidOrLiquidBlock(this.host.getPosition());
					this.host.topBlock = pos.getY() + 7 + this.host.rand.nextInt(3);
					this.attackDuration = 200;
				} else if (this.attacksMade % 2 == 0) {
					this.attackDuration = 20 - this.host.level / 4;
					if (target != host && this.host.getDistanceSq(target) < 6) {
						if (this.host.attackEntityAsMob(target)) {
							target.knockBack(this.host, 1.5f, Mth.sin(this.host.rotationYaw * 0.017453292F),
									(-Mth.cos(this.host.rotationYaw * 0.017453292F)));
						}
					} else {
						this.host.faceEntity(target, 180, 90);
						((ItemProjectileWeapon) this.host.getHeldItemMainhand().getItem())
								.shoot(this.host.getHeldItemMainhand(), this.host, world, 0, InteractionHand.MAIN_HAND);
						if (sup) {
							Vec3 right = this.host.getVectorForRotation(0, this.host.rotationYawHead + 90);
							this.host.rotationYawHead -= 24;
							for (int i = -2; i <= 2; i++) {
								this.host.posX += right.x * i;
								this.host.posZ += right.z * i;
								this.host.rotationYawHead += 12;
								((ItemProjectileWeapon) this.host.getHeldItemMainhand().getItem()).shoot(
										this.host.getHeldItemMainhand(), this.host, world, 0, InteractionHand.MAIN_HAND);
							}
							this.host.rotationYawHead -= 24;
						}
					}
				} else {
					this.attackDuration = (int) (55 / (0.91 + this.host.level * 0.09f));
					this.host.getNavigator().clearPath();
					this.host.playSound(TF2Sounds.MOB_MERASMUS_SPELL, 2F, 1F);
					boolean attacked = false;
					double range = 10d + this.host.level * 0.8;
					for (LivingEntity living : world.getEntitiesWithinAABB(LivingEntity.class,
							this.host.getEntityBoundingBox().grow(range, range * 0.4, range),
							input -> input.getDistanceSq(host) < range * range
									&& !TF2Util.isOnSameTeam(host, input)
									&& EntityAITarget.isSuitableTarget(host, input, false, false))) {
						TF2Util.sendParticle(EnumTF2Particles.BULLET_TRACER, this.host, this.host.posX,
								this.host.posY + this.host.height / 2, this.host.posZ, living.posX,
								living.posY + living.height / 2, living.posZ, 1, 0, 0x60FF60, 10000);
						living.attackEntityFrom(
								new EntityDamageSource("magicm", this.host).setMagicDamage().setDifficultyScaled(),
								4.4f + this.host.level * 0.7f);
						living.addVelocity(0, 0.7, 0);
						living.onGround = false;
						if (living instanceof ServerPlayer)
							TF2Util.sendTracking(new TF2Message.VelocityAddMessage(new Vec3(0, 0.7, 0), true), living);
						if (living.hasCapability(TF2weapons.WEAPONS_CAP, null))
							WeaponsCapability.get(living).setExpJump(true);
						else
							living.fallDistance = -10;
						attacked = true;
					}
					if (!attacked)
						this.host.teleportCooldown -= 20;
				}
				if (sup)
					this.attackDuration *= 0.35f;
				this.attacksMade++;
			}
		}
	}

	static class FloatingMoveHelper extends MoveControl {
		private final EntityMerasmus parentEntity;
		private int courseChangeCooldown;

		public FloatingMoveHelper(EntityMerasmus ghast) {
			super(ghast);
			this.parentEntity = ghast;
		}

		@Override
		public void onUpdateMoveHelper() {
			if (this.action == MoveControl.Action.MOVE_TO) {
				double d0 = this.posX - this.parentEntity.posX;
				double d1 = this.posY - this.parentEntity.posY;
				double d2 = this.posZ - this.parentEntity.posZ;
				double d3 = d0 * d0 + d1 * d1 + d2 * d2;

				if (this.courseChangeCooldown-- <= 0) {
					this.courseChangeCooldown += this.parentEntity.getRNG().nextInt(5) + 2;
					d3 = Mth.sqrt(d3);

					if (this.isNotColliding(this.posX, this.posY, this.posZ, d3)) {
						this.parentEntity.motionX += d0 / d3 * 0.05D;
						this.parentEntity.motionZ += d2 / d3 * 0.05D;
					} else
						this.action = MoveControl.Action.WAIT;
				}
			}
		}

		/**
		 * Checks if entity bounding box is not colliding with terrain
		 */
		private boolean isNotColliding(double x, double y, double z, double p_179926_7_) {
			double d0 = (x - this.parentEntity.posX) / p_179926_7_;
			double d1 = (y - this.parentEntity.posY) / p_179926_7_;
			double d2 = (z - this.parentEntity.posZ) / p_179926_7_;
			AABB AABB = this.parentEntity.getEntityBoundingBox();

			for (int i = 1; i < p_179926_7_; ++i) {
				AABB = AABB.offset(d0, d1, d2);

				if (!this.parentEntity.world.getCollisionBoxes(this.parentEntity, AABB).isEmpty())
					return false;
			}

			return true;
		}
	}

	@Override
	public void setAttackTarget(LivingEntity ent) {
		if (this.getAttackTarget() != null && ent instanceof EntityBuilding)
			return;
		super.setAttackTarget(ent);
	}

	@Override
	public void returnSpawnItems() {
		if (!this.usedPos.isEmpty())
			this.setPosition(hiddenBlock.getX(), hiddenBlock.getY(), hiddenBlock.getZ());
		this.entityDropItem(new ItemStack(TF2weapons.itemBossSpawn, 1, 1), 0);
	}
}
