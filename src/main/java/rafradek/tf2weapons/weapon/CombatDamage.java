package rafradek.tf2weapons.weapon;

import java.util.Map;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import rafradek.tf2weapons.TF2StuffReloaded;
import rafradek.tf2weapons.registry.TF2Effects;
import net.minecraft.world.effect.MobEffectInstance;

public final class CombatDamage {
    public enum Kind { BULLET, PROJECTILE, EXPLOSION, MELEE, AFTERBURN, FLARE }
    public static DamageSource source(Kind kind, Entity direct, Entity owner) {
        return source(kind, direct, owner, false);
    }
    private static DamageSource source(Kind kind, Entity direct, Entity owner, boolean penetrating) {
        ResourceKey<DamageType> key = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation(TF2StuffReloaded.MOD_ID, kind.name().toLowerCase(java.util.Locale.ROOT) + (penetrating ? "_piercing" : "")));
        return new DamageSource(direct.level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(key), direct, owner);
    }
    public static boolean canAttack(LivingEntity attacker, LivingEntity target) {
        return target.isAlive() && !target.isSpectator() && (target == attacker || attacker == null || !attacker.isAlliedTo(target));
    }
    public static boolean backstab(LivingEntity attacker, LivingEntity target) {
        Vec3 toward = target.position().subtract(attacker.position()).multiply(1, 0, 1).normalize();
        Vec3 attackerLook = attacker.getLookAngle().multiply(1, 0, 1).normalize();
        Vec3 targetLook = target.getLookAngle().multiply(1, 0, 1).normalize();
        return toward.dot(targetLook) > 0 && attackerLook.dot(targetLook) > 0;
    }
    public static CombatMath.Critical roll(LivingEntity shooter, WeaponDefinition data, Map<String, Double> attributes) {
        if (shooter.hasEffect(TF2Effects.CRIT_BOOST.get())) return CombatMath.Critical.FULL;
        if (shooter.hasEffect(TF2Effects.MINICRIT_BOOST.get())) return CombatMath.Critical.MINI;
        if (data.flag("Random crits") && attributes.getOrDefault("NoRandomCrit", 0D) == 0
                && shooter.getRandom().nextFloat() < CombatMath.criticalChance(recentDamage(shooter), data.behavior() == WeaponDefinition.Behavior.MELEE)) return CombatMath.Critical.FULL;
        return CombatMath.Critical.NORMAL;
    }
    public static CombatMath.Critical critical(LivingEntity target, WeaponDefinition data, Map<String, Double> attributes,
            CombatMath.Critical initial, boolean headshot, boolean stabbed) {
        CombatMath.Critical result = initial;
        boolean burning = target.isOnFire() || target.hasEffect(TF2Effects.AFTERBURN.get());
        if (stabbed || (headshot && attributes.getOrDefault("NoHeadshot", 0D) == 0)
                || (burning && attributes.getOrDefault("CritBurning", 0D) > 0 && attributes.getOrDefault("MiniCritBurning", 0D) == 0)) result = CombatMath.Critical.FULL;
        else if (result == CombatMath.Critical.NORMAL && ((burning && attributes.getOrDefault("MiniCritBurning", 0D) > 0)
                || (!target.onGround() && attributes.getOrDefault("MinicritAirborne", 0D) > 0)
                || target.hasEffect(TF2Effects.MARKED.get()))) result = CombatMath.Critical.MINI;
        if (result == CombatMath.Critical.MINI && attributes.getOrDefault("CritMini", 0D) > 0) result = CombatMath.Critical.FULL;
        if (result == CombatMath.Critical.FULL && attributes.getOrDefault("CritsBecomeMiniCrits", 0D) > 0) result = CombatMath.Critical.MINI;
        return result;
    }
    public static boolean hit(LivingEntity target, LivingEntity attacker, Entity direct, ItemStack weapon, WeaponDefinition data,
            double base, CombatMath.Critical rolled, Kind kind, double distance, Vec3 origin, boolean headshot, boolean stabbed) {
        if (direct.level().isClientSide || !canAttack(attacker, target)) return false;
        Map<String, Double> attributes = WeaponAttributes.of(weapon, data);
        CombatMath.Critical critical = target == attacker ? CombatMath.Critical.NORMAL : critical(target, data, attributes, rolled, headshot, stabbed);
        double amount = WeaponStats.multiply(base, attributes, target.isOnFire() ? "DamageBurnBonus" : "DamageNonBurnPenalty", "DamagePlayerPenalty");
        if (kind != Kind.MELEE) amount *= CombatMath.falloff(data, distance, critical);
        amount *= critical.multiplier;
        if (stabbed) amount *= 4; // Original Knife bonus before the guaranteed critical.
        if (target == attacker) amount = WeaponStats.multiply(amount, attributes, "SelfDamageReduced", "SelfDamageIncreased");
        amount *= resistance(target, kind);
        double armorScale = Math.max(0, Math.min(1, data.number("Armor penetration scale", 1)));
        if (attributes.getOrDefault("Unblockable", 0D) > 0) armorScale = 0;
        boolean penetrating = armorScale < 1;
        if (penetrating) amount = CombatRules.getDamageAfterAbsorb((float) amount, (float) (target.getArmorValue() * armorScale), (float) (target.getAttributeValue(Attributes.ARMOR_TOUGHNESS) * armorScale));
        double before = target.getHealth() + target.getAbsorptionAmount();
        boolean hit = amount > 0 && target.hurt(source(kind, direct, attacker, penetrating), (float) Math.min(100000, amount));
        // Jumpers have zero self damage but retain their explosion impulse.
        if (hit || (kind == Kind.EXPLOSION && target == attacker)) {
            double force = WeaponStats.multiply(data.number("Knockback", 3) * Math.max(Math.max(1, base), data.number("Damage", 1)) * 0.016, attributes, "KnockbackBonus", "KnockbackPenalty");
            if (target == attacker) force = WeaponStats.multiply(force, attributes, "SelfPushForceBonus", "SelfPushForcePenalty");
            force *= 1 - Math.min(1, target.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE));
            Vec3 direction = target.getBoundingBox().getCenter().subtract(origin).normalize();
            target.push(direction.x * force, direction.y * force + (kind == Kind.EXPLOSION && target.onGround() ? force * 0.35 : 0), direction.z * force);
            target.hurtMarked = true;
        }
        if (hit && target != attacker && attacker != null) {
            recordDamage(attacker, Math.max(0, before - target.getHealth() - target.getAbsorptionAmount()), target instanceof Player);
            if (critical != CombatMath.Critical.NORMAL && direct.level() instanceof net.minecraft.server.level.ServerLevel level)
                level.sendParticles(net.minecraft.core.particles.ParticleTypes.CRIT, target.getX(), target.getEyeY(), target.getZ(), 6, 0.2, 0.2, 0.2, 0.1);
            double burn = attributes.getOrDefault("BurnOnHit", 0D);
            if (burn > 0 && !target.fireImmune()) {
                target.getPersistentData().putUUID("TF2AfterburnOwner", attacker.getUUID());
                target.addEffect(new MobEffectInstance(TF2Effects.AFTERBURN.get(), Math.min(1200, Math.max(20, (int) (burn * 20))), 0), attacker);
            }
            double bleed = attributes.getOrDefault("BleedingDuration", 0D);
            if (bleed > 0) {
                target.getPersistentData().putUUID("TF2BleedOwner", attacker.getUUID());
                target.addEffect(new MobEffectInstance(TF2Effects.BLEEDING.get(), Math.min(1200, (int) (bleed * 20)), 0), attacker);
            }
            double marked = attributes.getOrDefault("MarkForDeath", 0D);
            if (marked > 0) target.addEffect(new MobEffectInstance(TF2Effects.MARKED.get(), Math.min(1200, (int) (marked * 20)), 0), attacker);
            attacker.heal(attributes.getOrDefault("HealthOnHit", 0D).floatValue());
            if (!target.isAlive()) attacker.heal(attributes.getOrDefault("HealthOnKill", 0D).floatValue());
        }
        return hit;
    }
    public static double resistance(LivingEntity target, Kind kind) {
        double factor = 1;
        for (ItemStack stack : target.getAllSlots()) {
            if (!(stack.getItem() instanceof rafradek.tf2weapons.item.base.CatalogWeaponItem item) || item.definition(stack) == null) continue;
            Map<String, Double> attributes = WeaponAttributes.of(stack, item.definition(stack));
            factor = WeaponStats.multiply(factor, attributes, "DamageResistBonus", "DamageResistPenalty",
                    kind == Kind.EXPLOSION ? "ExplosionResistBonus" : kind == Kind.MELEE ? "MeleeResistBonus" : "RangedResistBonus");
            if (kind == Kind.AFTERBURN || kind == Kind.FLARE) factor = WeaponStats.multiply(factor, attributes, "FireResistBonus");
        }
        return Math.max(0, Math.min(10, factor));
    }
    public static double recentDamage(LivingEntity actor) {
        var history = actor.getPersistentData().getList("TF2DamageHistory", net.minecraft.nbt.Tag.TAG_COMPOUND);
        long second = actor.level().getGameTime() / 20;
        double total = 0;
        for (int index = history.size() - 1; index >= 0; index--) {
            var bucket = history.getCompound(index);
            if (bucket.getLong("Second") < second - 19 || bucket.getLong("Second") > second) history.remove(index);
            else total += bucket.getDouble("Damage");
        }
        actor.getPersistentData().put("TF2DamageHistory", history);
        return total;
    }
    private static void recordDamage(LivingEntity actor, double amount, boolean player) {
        double total = recentDamage(actor);
        if (!player) amount = Math.max(0, Math.min(amount * 0.5, 60 - total));
        if (amount <= 0) return;
        var history = actor.getPersistentData().getList("TF2DamageHistory", net.minecraft.nbt.Tag.TAG_COMPOUND);
        long second = actor.level().getGameTime() / 20;
        var bucket = history.isEmpty() ? null : history.getCompound(history.size() - 1);
        if (bucket == null || bucket.getLong("Second") != second) {
            bucket = new net.minecraft.nbt.CompoundTag(); bucket.putLong("Second", second); history.add(bucket);
        }
        bucket.putDouble("Damage", Math.min(1000000, bucket.getDouble("Damage") + amount));
        actor.getPersistentData().put("TF2DamageHistory", history);
    }
}
