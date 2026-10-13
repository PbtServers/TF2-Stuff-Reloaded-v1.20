package rafradek.tf2weapons.weapon.projectile;

import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.projectile.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.*;
import net.minecraft.world.phys.*;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.network.NetworkHooks;
import rafradek.tf2weapons.weapon.*;

/** Real entity with authoritative continuous collision tests; legacy sources remain untouched. */
public final class WeaponProjectile extends Projectile implements IEntityAdditionalSpawnData {
    public enum Flight { ROCKET, GRENADE, STICKY, FLARE, ARROW, SYRINGE, ENERGY }
    private static final EntityDataAccessor<Integer> FLIGHT = SynchedEntityData.defineId(WeaponProjectile.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> CRITICAL = SynchedEntityData.defineId(WeaponProjectile.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> STUCK = SynchedEntityData.defineId(WeaponProjectile.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> BLUE = SynchedEntityData.defineId(WeaponProjectile.class, EntityDataSerializers.BOOLEAN);
    private ItemStack weapon = ItemStack.EMPTY;
    private UUID ownerId;
    private int age, fuse = 46;
    private boolean bounced, exploded;
    private BlockPos attachedBlock;
    private final Set<UUID> hitEntities = new HashSet<>();

    public WeaponProjectile(EntityType<? extends WeaponProjectile> type, Level level) { super(type, level); }
    @Override protected void defineSynchedData() { entityData.define(FLIGHT, 0); entityData.define(CRITICAL, 0); entityData.define(STUCK, false); entityData.define(BLUE, false); }
    public boolean blueTeam() { return entityData.get(BLUE); }
    public Flight flight() { return Flight.values()[Math.max(0, Math.min(Flight.values().length - 1, entityData.get(FLIGHT)))]; }
    public int criticalLevel() { return entityData.get(CRITICAL); }
    public boolean stuck() { return entityData.get(STUCK); }
    public int age() { return age; }
    public boolean ownedBy(UUID uuid) { return uuid.equals(ownerId); }
    public WeaponDefinition definition() { return WeaponCatalog.get().find(weapon.hasTag() ? weapon.getTag().getString("Type") : ""); }
    public void configure(LivingEntity owner, ItemStack stack, Flight flight) {
        setOwner(owner);
        ownerId = owner.getUUID();
        entityData.set(BLUE, owner.getTeam() != null && (owner.getTeam().getColor() == net.minecraft.ChatFormatting.BLUE || owner.getTeam().getName().equalsIgnoreCase("blu")));
        weapon = stack.copy(); weapon.setCount(1);
        entityData.set(FLIGHT, flight.ordinal());
        entityData.set(CRITICAL, Math.max(0, Math.min(2, stack.getOrCreateTag().getInt("ShotCritical"))));
        WeaponDefinition data = definition();
        if (data != null) fuse = Math.max(1, (int) Math.round(46 * WeaponAttributes.of(weapon, data).getOrDefault("FuseTimeBonus", 1D)));
    }
    public int armTime() {
        WeaponDefinition data = definition();
        return data == null ? 16 : Math.max(1, (int) Math.round((0.8 + WeaponAttributes.of(weapon, data).getOrDefault("ArmTimePenalty", 0D)) * 20));
    }
    public boolean armed() { return age >= armTime(); }
    @Override public boolean isPickable() { return flight() == Flight.STICKY && stuck() && !isRemoved(); }
    @Override public boolean hurt(DamageSource source, float amount) {
        if (!level().isClientSide && isPickable() && amount > 0 && source.getEntity() != null && !ownedBy(source.getEntity().getUUID())) {
            if (getOwner() != null && source.getEntity().isAlliedTo(getOwner())) return false;
            discard(); return true;
        }
        return false;
    }
    @Override protected boolean canHitEntity(Entity entity) {
        return super.canHitEntity(entity) && entity instanceof LivingEntity living && !hitEntities.contains(entity.getUUID())
                && !(entity == getOwner() && age < 5)
                && CombatDamage.canAttack(getOwner() instanceof LivingEntity owner ? owner : null, living);
    }
    @Override public void tick() {
        super.tick();
        age++;
        if (level().isClientSide) {
            if (!stuck()) {
                setPos(position().add(getDeltaMovement()));
                setDeltaMovement(getDeltaMovement().scale(drag()).add(0, -gravity(), 0));
            }
            if (flight() == Flight.ROCKET || flight() == Flight.FLARE) level().addParticle(flight() == Flight.FLARE ? ParticleTypes.FLAME : ParticleTypes.SMOKE,
                    getX(), getY(), getZ(), 0, 0, 0);
            if (criticalLevel() > 0) level().addParticle(ParticleTypes.CRIT, getX(), getY(), getZ(), 0, 0, 0);
            return;
        }
        if (definition() == null || age > (flight() == Flight.STICKY ? 72000 : 200)) { discard(); return; }
        if (flight() == Flight.GRENADE && --fuse <= 0) { detonate(null, 0.64); return; }
        if (stuck()) {
            if (attachedBlock != null && level().getBlockState(attachedBlock).isAir()) { entityData.set(STUCK, false); attachedBlock = null; }
            else return;
        }
        Vec3 movement = getDeltaMovement();
        Vec3 end = position().add(movement);
        HitResult hit = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
        if (hit.getType() != HitResult.Type.MISS && !ForgeEventFactory.onProjectileImpact(this, hit)) {
            setPos(hit.getLocation());
            if (hit instanceof EntityHitResult entityHit) impactEntity(entityHit);
            else if (hit instanceof BlockHitResult blockHit) impactBlock(blockHit, movement);
            if (isRemoved() || stuck() || hit instanceof BlockHitResult) return;
        }
        setPos(end);
        setDeltaMovement(movement.scale(drag()).add(0, -gravity(), 0));
        Vec3 direction = getDeltaMovement();
        setYRot((float) Math.toDegrees(Math.atan2(direction.x, direction.z)));
        setXRot((float) Math.toDegrees(Math.atan2(direction.y, direction.horizontalDistance())));
        checkInsideBlocks();
    }
    private double gravity() {
        if (isNoGravity() || stuck()) return 0;
        return switch (flight()) { case ROCKET, ENERGY -> 0; case FLARE -> 0.019; case GRENADE, STICKY -> 0.0381; default -> 0.05; };
    }
    private double drag() { return isInWater() ? 0.8 : flight() == Flight.ROCKET || flight() == Flight.ENERGY ? 1 : 0.99; }
    private void impactEntity(EntityHitResult hit) {
        if (flight() == Flight.STICKY) return;
        if (flight() == Flight.ROCKET || (flight() == Flight.GRENADE && !bounced)) { detonate(hit.getEntity(), 1); return; }
        if (flight() == Flight.GRENADE) return; // A bounced pipe is only an area explosion at its fuse.
        WeaponDefinition data = definition();
        Map<String, Double> attributes = WeaponAttributes.of(weapon, data);
        if (attributes.getOrDefault("DestroyOnImpact", 0D) == 2) { detonate(hit.getEntity(), 1); return; }
        if (!hitEntities.add(hit.getEntity().getUUID())) return;
        if (hit.getEntity() instanceof LivingEntity target && getOwner() instanceof LivingEntity owner) {
            boolean headshot = flight() == Flight.ARROW && hit.getLocation().y >= target.getBoundingBox().maxY - target.getBbHeight() * 0.25;
            double damage = weapon.getTag().contains("ShotDamage") ? weapon.getTag().getDouble("ShotDamage") : WeaponStats.of(data, attributes).damage();
            if (flight() == Flight.ARROW) damage *= 1 + 1.4 * charge();
            boolean damaged = CombatDamage.hit(target, owner, this, weapon, data, damage, CombatMath.Critical.values()[criticalLevel()], flight() == Flight.FLARE ? CombatDamage.Kind.FLARE : CombatDamage.Kind.PROJECTILE,
                    owner.distanceTo(target), hit.getLocation(), headshot, false);
            if (damaged && flight() == Flight.ARROW && weapon.hasTag()) {
                ItemStack arrow = ItemStack.of(weapon.getTag().getCompound("ArrowItem"));
                if (arrow.is(net.minecraft.world.item.Items.SPECTRAL_ARROW)) target.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.GLOWING, 200), owner);
                net.minecraft.world.item.alchemy.PotionUtils.getMobEffects(arrow).forEach(effect -> target.addEffect(new net.minecraft.world.effect.MobEffectInstance(effect.getEffect(), Math.max(1, effect.getDuration() / 8), effect.getAmplifier()), owner));
            }
        }
        boolean penetrating = data.flag("Penetrate") || attributes.getOrDefault("Penetration", 0D) > 0;
        if (!penetrating) discard();
    }
    private double charge() { return weapon.hasTag() ? Math.max(0, Math.min(1, weapon.getTag().getDouble("ProjectileCharge"))) : 0; }
    private void impactBlock(BlockHitResult hit, Vec3 velocity) {
        Vec3 normal = Vec3.atLowerCornerOf(hit.getDirection().getNormal());
        setPos(hit.getLocation().add(normal.scale(0.03)));
        switch (flight()) {
            case ROCKET -> detonate(null, 1);
            case STICKY -> { entityData.set(STUCK, true); attachedBlock = hit.getBlockPos(); setDeltaMovement(Vec3.ZERO); }
            case GRENADE -> {
                bounced = true;
                double mode = WeaponAttributes.of(weapon, definition()).getOrDefault("DestroyOnImpact", 0D);
                if (mode == 1) discard();
                else if (mode == 2) detonate(null, 0.64);
                else setDeltaMovement(velocity.subtract(normal.scale(2 * velocity.dot(normal))).scale(0.55).add(0, -gravity(), 0));
            }
            case ARROW -> { entityData.set(STUCK, true); attachedBlock = hit.getBlockPos(); setDeltaMovement(Vec3.ZERO); }
            default -> { if (WeaponAttributes.of(weapon, definition()).getOrDefault("DestroyOnImpact", 0D) == 2) detonate(null, 1); else discard(); }
        }
    }
    public boolean detonate(Entity direct, double damageMultiplier) {
        if (level().isClientSide || exploded || isRemoved()) return false;
        exploded = true;
        WeaponDefinition data = definition();
        if (data != null) ProjectileExplosions.explode(this, weapon, data, direct, damageMultiplier);
        discard(); return true;
    }
    @Override public Packet<ClientGamePacketListener> getAddEntityPacket() { return NetworkHooks.getEntitySpawningPacket(this); }
    @Override protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.put("Weapon", weapon.save(new CompoundTag()));
        if (ownerId != null) tag.putUUID("WeaponOwner", ownerId);
        tag.putInt("Flight", flight().ordinal()); tag.putInt("Critical", criticalLevel());
        tag.putInt("Age", age); tag.putInt("Fuse", fuse); tag.putBoolean("Bounced", bounced); tag.putBoolean("Stuck", stuck());
        tag.putBoolean("Blue", blueTeam());
        if (attachedBlock != null) tag.putLong("AttachedBlock", attachedBlock.asLong());
        net.minecraft.nbt.ListTag hits = new net.minecraft.nbt.ListTag();
        hitEntities.stream().limit(256).forEach(uuid -> hits.add(net.minecraft.nbt.NbtUtils.createUUID(uuid)));
        tag.put("HitEntities", hits);
    }
    @Override protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        weapon = ItemStack.of(tag.getCompound("Weapon"));
        ownerId = tag.hasUUID("WeaponOwner") ? tag.getUUID("WeaponOwner") : null;
        entityData.set(FLIGHT, Math.max(0, Math.min(Flight.values().length - 1, tag.getInt("Flight"))));
        entityData.set(CRITICAL, Math.max(0, Math.min(2, tag.getInt("Critical"))));
        entityData.set(STUCK, tag.getBoolean("Stuck"));
        entityData.set(BLUE, tag.getBoolean("Blue"));
        age = Math.max(0, tag.getInt("Age")); fuse = Math.max(1, Math.min(1200, tag.getInt("Fuse"))); bounced = tag.getBoolean("Bounced");
        attachedBlock = tag.contains("AttachedBlock") ? BlockPos.of(tag.getLong("AttachedBlock")) : null;
        hitEntities.clear();
        tag.getList("HitEntities", net.minecraft.nbt.Tag.TAG_INT_ARRAY).stream().limit(256).forEach(value -> hitEntities.add(net.minecraft.nbt.NbtUtils.loadUUID(value)));
    }
    @Override public void writeSpawnData(FriendlyByteBuf buffer) {
        buffer.writeVarInt(flight().ordinal()); buffer.writeVarInt(criticalLevel()); buffer.writeBoolean(stuck());
        buffer.writeBoolean(blueTeam());
        Vec3 velocity = getDeltaMovement(); buffer.writeDouble(velocity.x); buffer.writeDouble(velocity.y); buffer.writeDouble(velocity.z);
    }
    @Override public void readSpawnData(FriendlyByteBuf buffer) {
        entityData.set(FLIGHT, Math.max(0, Math.min(Flight.values().length - 1, buffer.readVarInt())));
        entityData.set(CRITICAL, Math.max(0, Math.min(2, buffer.readVarInt()))); entityData.set(STUCK, buffer.readBoolean());
        entityData.set(BLUE, buffer.readBoolean());
        setDeltaMovement(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
    }
}
