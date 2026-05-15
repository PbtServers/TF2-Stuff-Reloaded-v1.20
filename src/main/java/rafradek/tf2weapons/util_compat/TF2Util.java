package rafradek.tf2weapons.util;

import com.google.common.base.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.items.IItemHandler;
import rafradek.tf2weapons.client.particle.EnumTF2Particles;
import rafradek.tf2weapons.entity.building.EntityBuilding;
import rafradek.tf2weapons.entity.mercenary.EntityTF2Character;
import rafradek.tf2weapons.item.ItemFromData;
import rafradek.tf2weapons.item.ItemMoney;

import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class TF2Util {
	public static final UUID FOLLOW_MODIFIER = UUID.fromString("FA233E1C-4180-4865-B01B-BCCE978AD348");
	public static final Block[] NATURAL_BLOCKS = new Block[] { Blocks.STONE };
	public static final Predicate<Entity> TARGETABLE = target -> true;
	public static int[] colorCode = new int[32];
	public static final float[] ASIN_VALUES = new float[512];

	public static List<HitResult> pierce(Level world, Entity living, double startX, double startY, double startZ,
			double endX, double endY, double endZ, boolean headshot, float size, boolean pierce,
			Predicate<Entity> selector) {
		return Collections.emptyList();
	}

	public static List<HitResult> pierce(Level world, Entity living, double startX, double startY, double startZ,
			double endX, double endY, double endZ, boolean headshot, float size, boolean pierce) {
		return Collections.emptyList();
	}

	public static List<HitResult> pierce(Level world, Entity living, double length, boolean headshot, float size,
			boolean pierce) {
		return Collections.emptyList();
	}

	public static HitResult getTraceResult(Entity target, HitResult hitVec, float size, boolean headshot,
			Vec3 start, Vec3 end) {
		return hitVec;
	}

	public static AABB getHead(LivingEntity target) {
		return target.getBoundingBox();
	}

	public static Vec3 radiusRandom2D(float radius, Random random) {
		return new Vec3(random.nextDouble() * radius, 0, random.nextDouble() * radius);
	}

	public static Vec3 radiusRandom2D(float radius, Random random, float yaw, float pitch, double dist) {
		return radiusRandom2D(radius, random);
	}

	public static boolean isUsingShield(Entity shielded, DamageSource source) {
		return false;
	}

	public static int calculateCritPost(Entity target, LivingEntity shooter, int initial, ItemStack stack,
			DamageSource source) {
		return initial;
	}

	public static float calculateDamage(Entity target, Level world, LivingEntity living, ItemStack stack,
			int critical, float distance) {
		return 0;
	}

	public static float lerp(float v0, float v1, float t) {
		return v0 + (v1 - v0) * t;
	}

	public static float position(float v0, float v1, float lerp) {
		return v0 == v1 ? 0 : (lerp - v0) / (v1 - v0);
	}

	public static boolean isOnSameTeam(Entity entity1, Entity entity2) {
		return entity1 != null && entity2 != null && entity1.isAlliedTo(entity2);
	}

	public static net.minecraft.world.scores.Team getTeam(Entity living) {
		return living == null ? null : living.getTeam();
	}

	public static int getTeamColor(Entity living) {
		return getTeamColorNumber(living);
	}

	public static int getTeamForDisplay(Entity living) {
		return getTeamColorNumber(living);
	}

	public static int getTeamColorNumber(Entity living) {
		return 0;
	}

	public static boolean canHit(LivingEntity shooter, Entity ent) {
		return shooter != ent;
	}

	public static <T extends Entity> T getClosestEntityInCone(Vec3 start, Vec3 end, List<T> list, double aperture) {
		return list == null || list.isEmpty() ? null : list.get(0);
	}

	public static boolean lookingAt(LivingEntity entity, double max, double targetX, double targetY, double targetZ) {
		return true;
	}

	public static boolean lookingAt(LivingEntity entity, double max, Entity target) {
		return true;
	}

	public static boolean lookingAtFast(LivingEntity entity, double max, double targetX, double targetY,
			double targetZ) {
		return true;
	}

	public static boolean dealDamage(Entity entity, Level world, LivingEntity living, ItemStack stack, int critical,
			float damage, DamageSource source) {
		return false;
	}

	public static boolean dealDamageActual(Entity entity, Level world, LivingEntity living, ItemStack stack,
			int critical, float damage, DamageSource source) {
		return false;
	}

	public static float damageBlock(BlockPos pos, LivingEntity living, Level world, ItemStack stack,
			int critical, float damage, Vec3 forwardVec, net.minecraft.world.level.Explosion explosion) {
		return 0;
	}

	public static float getHardness(BlockState state, Level world, BlockPos pos) {
		return 0;
	}

	public static int getExperiencePoints(Player player) {
		return 0;
	}

	public static void setExperiencePoints(Player player, int amount) {
	}

	public static double getDistanceSqBox(Entity target, double x, double y, double z, double widthO, double heightO) {
		return target == null ? 0 : target.distanceToSqr(x, y, z);
	}

	public static double getDistanceBox(Entity target, double x, double y, double z, double widthO, double heightO) {
		return Math.sqrt(getDistanceSqBox(target, x, y, z, widthO, heightO));
	}

	public static boolean canInteract(LivingEntity entity) {
		return true;
	}

	public static void explosion(Level world, LivingEntity shooter, ItemStack weapon, Entity exploder,
			Entity direct, double x, double y, double z, float size, float damageMult, int critical, float distance) {
	}

	public static int calculateCritPre(ItemStack stack, LivingEntity living) {
		return 0;
	}

	public static DamageSourceProjectile causeBulletDamage(ItemStack weapon, Entity shooter, Entity projectile) {
		return new DamageSourceProjectile("tf2", projectile, shooter, weapon);
	}

	public static DamageSourceDirect causeDirectDamage(ItemStack weapon, Entity shooter) {
		return new DamageSourceDirect("tf2", shooter, shooter, weapon);
	}

	public static Vec3 radiusRandom3D(float radius, Random random) {
		return new Vec3(random.nextDouble() * radius, random.nextDouble() * radius, random.nextDouble() * radius);
	}

	public static Vec3 rangeRandom3D(float radius, Random random) {
		return radiusRandom3D(radius, random);
	}

	public static float asin(float value) {
		return (float) Math.asin(value);
	}

	public static void stun(LivingEntity living, int duration, boolean noMovement) {
	}

	public static void playSound(Entity entity, SoundEvent event, float volume, float pitch) {
		if (entity != null && event != null) {
			entity.level().playSound(null, entity.blockPosition(), event, SoundSource.PLAYERS, volume, pitch);
		}
	}

	public static boolean isEnemy(LivingEntity living, LivingEntity living2) {
		return living != living2;
	}

	public static void igniteAndAchievement(Entity target, LivingEntity living, int sec, float upgrade) {
		if (target != null) target.setSecondsOnFire(sec);
	}

	public static void sendParticle(EnumTF2Particles type, Entity tracking, double x, double y, double z,
			double offsetX, double offsetY, double offsetZ, int count, int... params) {
	}

	public static void sendParticle(EnumTF2Particles type, Level world, double x, double y, double z, double offsetX,
			double offsetY, double offsetZ, int count, int... params) {
	}

	public static void sendTracking(rafradek.tf2weapons.message.TF2Packet message, Entity entity) {
	}

	public static void sendTrackingExcluding(rafradek.tf2weapons.message.TF2Packet message, Entity entity) {
	}

	public static float getDamageBeforeAbsorb(float damage, float totalArmor, float toughnessAttribute) {
		return damage;
	}

	public static boolean isOre(String ore, ItemStack stack) {
		return false;
	}

	public static ItemStack getFirstItem(Container inventory, Predicate<ItemStack> pred) {
		return ItemStack.EMPTY;
	}

	public static ItemStack getFirstItem(IItemHandler inventory, Predicate<ItemStack> pred) {
		return ItemStack.EMPTY;
	}

	public static int getFirstItemSlot(Container inventory, Predicate<ItemStack> pred) {
		return -1;
	}

	public static int getFirstItemSlot(IItemHandler inventory, Predicate<ItemStack> pred) {
		return -1;
	}

	public static ItemStack getBestItem(Container inventory, Comparator<ItemStack> comp, Predicate<ItemStack> pred) {
		return ItemStack.EMPTY;
	}

	public static ItemStack getBestItem(IItemHandler inventory, Comparator<ItemStack> comp, Predicate<ItemStack> pred) {
		return ItemStack.EMPTY;
	}

	public static ItemStack mergeStackByDamage(IItemHandler inventory, ItemStack stack) {
		return stack;
	}

	public static boolean hasEnoughItem(Container inventory, Predicate<ItemStack> pred, int amount) {
		return true;
	}

	public static boolean hasEnoughItem(IItemHandler inventory, Predicate<ItemStack> pred, int amount) {
		return true;
	}

	public static int removeItemsMatching(IItemHandler inventory, int amount, Predicate<ItemStack> match) {
		return 0;
	}

	public static void attractMobs(LivingEntity living, Level world) {
	}

	public static void addModifierSafe(LivingEntity living, IAttribute attribute,
			net.minecraft.world.entity.ai.attributes.AttributeModifier modifier, boolean saved) {
	}

	public static boolean isHostile(LivingEntity living) {
		return false;
	}

	public static Vec3 getRotationVector(float pitch, float yaw) {
		return Vec3.directionFromRotation(pitch, yaw);
	}

	public static double getYaw(double x, double z) {
		return Math.atan2(z, x);
	}

	public static boolean isBaseSame(CompoundTag base, CompoundTag second) {
		return base == second || (base != null && base.equals(second));
	}

	public static Vec3 getMovementVector(LivingEntity living) {
		return living == null ? Vec3.ZERO : living.getDeltaMovement();
	}

	public static boolean teleportSafe(Mob toTeleport, Entity dest) {
		if (toTeleport != null && dest != null) toTeleport.teleportTo(dest.getX(), dest.getY(), dest.getZ());
		return true;
	}

	public static boolean isTeleportFriendlyBlock(LivingEntity owner, int x, int y, int z, Level world) {
		return true;
	}

	public static float getGravity(LivingEntity living) {
		return 0.08f;
	}

	public static void addAndSendEffect(LivingEntity living, MobEffectInstance effect) {
		if (living != null && effect != null) living.addEffect(effect);
	}

	public static Entity findAmmoSource(LivingEntity living, double range, boolean immediate) {
		return null;
	}

	public static boolean isNaturalBlock(Level world, BlockPos pos, BlockState state) {
		return true;
	}

	public static Vec2 getAngleFromFacing(Direction facing) {
		return new Vec2(0, 0);
	}

	public static void stomp(LivingEntity living) {
	}

	public static void extractData(String input, File output, File source) {
	}

	public static boolean isWeaponOfClass(ItemStack stack, int slot, TF2Class clazz) {
		return true;
	}

	public static TF2Class getWeaponUsedByClass(ItemStack stack) {
		return TF2Class.NONE;
	}

	public static LivingEntity getOwnerIfOwnable(LivingEntity living) {
		return living;
	}

	public static Vec3 getHeightVec(Level world, BlockPos pos) {
		return Vec3.ZERO;
	}

	public static void setVelocity(Entity entity, double motionX, double motionY, double motionZ) {
		if (entity != null) entity.setDeltaMovement(motionX, motionY, motionZ);
	}

	public static ItemStack pickAmmo(ItemStack stack, Player player, boolean addNormalInventory) {
		return stack;
	}

	public static int getValueOnAxis(Vec3i vec, Direction.Axis axis) {
		return axis == Direction.Axis.X ? vec.getX() : axis == Direction.Axis.Y ? vec.getY() : vec.getZ();
	}

	public static BlockPos setValueOnAxis(Vec3i vec, Direction.Axis axis, int value) {
		return axis == Direction.Axis.X ? new BlockPos(value, vec.getY(), vec.getZ())
				: axis == Direction.Axis.Y ? new BlockPos(vec.getX(), value, vec.getZ())
				: new BlockPos(vec.getX(), vec.getY(), value);
	}

	public static double getHeightAboveGround(Entity entity, Level world, boolean checkWater) {
		return 0;
	}

	public static double getHeightAboveGround(Vec3 pos, Level world, boolean checkWater) {
		return 0;
	}

	public static float getReducedHealing(LivingEntity attacker, LivingEntity target, float damage) {
		return damage;
	}

	public static float getDamageReduction(DamageSource source, LivingEntity living, float damage) {
		return damage;
	}

	public static int getTotalCurrency(IItemHandler handler) {
		return 0;
	}

	public static void setTotalCurrency(IItemHandler handler, int fromSlot, int amount) {
	}

	public static IItemHandler getLoadoutItemHandler(LivingEntity living) {
		return living instanceof EntityTF2Character character ? character.loadout : null;
	}

	public static void playSoundToPlayer(Player player, SoundEvent event, SoundSource category, double x,
			double y, double z, float volume, float pitch) {
	}

	public static boolean restoreAmmoToWeapons(Player player, float ammo) {
		return false;
	}

	public static <T> List<T> NBTTagListToList(Tag nbttag, Class<T> clazz) {
		return Collections.emptyList();
	}

	public static boolean matches(ItemStack stack1, ItemStack stack2) {
		return ItemStack.isSameItemSameTags(stack1, stack2);
	}

	public static boolean isLyingInCone(Vec3 x, Vec3 start, Vec3 end, float aperture) {
		return true;
	}
}
