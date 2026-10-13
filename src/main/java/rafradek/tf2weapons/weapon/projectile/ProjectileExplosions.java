package rafradek.tf2weapons.weapon.projectile;

import java.util.*;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.*;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.registries.ForgeRegistries;
import rafradek.tf2weapons.weapon.*;

public final class ProjectileExplosions {
    public static void explode(WeaponProjectile projectile, ItemStack stack, WeaponDefinition data, Entity direct, double multiplier) {
        if (!(projectile.level() instanceof ServerLevel level)) return;
        Map<String, Double> attributes = WeaponAttributes.of(stack, data);
        double baseRadius = projectile.flight() == WeaponProjectile.Flight.GRENADE || projectile.flight() == WeaponProjectile.Flight.STICKY ? 3.05 : projectile.flight() == WeaponProjectile.Flight.FLARE ? 2.1 : 2.8;
        double radius = Math.min(16, WeaponStats.multiply(baseRadius, attributes, "ExplosionRadiusBonus", "ExplosionRadiusPenalty"));
        LivingEntity owner = projectile.getOwner() instanceof LivingEntity living ? living : null;
        var source = CombatDamage.source(CombatDamage.Kind.EXPLOSION, projectile, owner);
        Explosion eventExplosion = new Explosion(level, projectile, source, null, projectile.getX(), projectile.getY(), projectile.getZ(), (float) radius, false, Explosion.BlockInteraction.KEEP);
        if (ForgeEventFactory.onExplosionStart(level, eventExplosion)) return;
        List<Entity> targets = new ArrayList<>(level.getEntities(projectile, projectile.getBoundingBox().inflate(radius), entity -> entity instanceof LivingEntity));
        ForgeEventFactory.onExplosionDetonate(level, eventExplosion, targets, radius);
        // Never call Explosion.explode: that would add vanilla damage on top of TF2 damage.
        double damage = (stack.hasTag() && stack.getTag().contains("ShotDamage") ? stack.getTag().getDouble("ShotDamage") : WeaponStats.of(data, attributes).damage()) * multiplier;
        for (Entity entity : new LinkedHashSet<>(targets)) {
            if (!(entity instanceof LivingEntity target) || !CombatDamage.canAttack(owner, target)) continue;
            double distance = target.getBoundingBox().getCenter().distanceTo(projectile.position());
            if (entity != direct && distance >= radius) continue;
            Vec3 eye = target.getEyePosition();
            boolean visible = level.clip(new ClipContext(projectile.position(), eye, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, projectile)).getType() == HitResult.Type.MISS;
            if (!visible && entity != direct) continue;
            double blast = CombatMath.blastFalloff(distance, radius, entity == direct);
            CombatDamage.hit(target, owner, projectile, stack, data, damage * blast, CombatMath.Critical.values()[projectile.criticalLevel()],
                    CombatDamage.Kind.EXPLOSION, owner == null ? 9.75 : owner.distanceTo(target), projectile.position(), false, false);
        }
        ResourceLocation soundId = ResourceLocation.tryParse(data.text("Explosion sound", ""));
        var sound = soundId == null ? null : ForgeRegistries.SOUND_EVENTS.getValue(soundId);
        level.playSound(null, projectile.blockPosition(), sound == null ? SoundEvents.GENERIC_EXPLODE : sound, SoundSource.PLAYERS, 1.5F, 1);
        level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, projectile.getX(), projectile.getY(), projectile.getZ(), 1, 0, 0, 0, 0);
    }
}
