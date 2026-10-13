package rafradek.tf2weapons.weapon.projectile;

import java.util.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import rafradek.tf2weapons.registry.TF2EntityTypes;
import rafradek.tf2weapons.weapon.*;

public final class ModernProjectiles {
    public static final Map<String, WeaponProjectile.Flight> SUPPORTED = Map.of(
            "rocket", WeaponProjectile.Flight.ROCKET, "grenade", WeaponProjectile.Flight.GRENADE,
            "stickybomb", WeaponProjectile.Flight.STICKY, "flare", WeaponProjectile.Flight.FLARE,
            "arrow", WeaponProjectile.Flight.ARROW, "syringe", WeaponProjectile.Flight.SYRINGE,
            "energy", WeaponProjectile.Flight.ENERGY, "pomson", WeaponProjectile.Flight.ENERGY);
    public static void register() { SUPPORTED.keySet().forEach(key -> ProjectileActions.register(key, ModernProjectiles::spawn)); }
    public static boolean spawn(Level level, Player player, ItemStack stack, WeaponDefinition data, WeaponStats stats) {
        if (level.isClientSide) return false;
        WeaponProjectile.Flight flight = SUPPORTED.get(data.text("Projectile", data.weaponClass()));
        if (flight == null) return false;
        Map<String, Double> attributes = WeaponAttributes.of(stack, data);
        double speed = WeaponStats.multiply(data.number("Projectile speed", 1), attributes, "ProjectileSpeedBonus", "ProjectileSpeedPenalty");
        double charge = Math.max(0, Math.min(1, stack.getOrCreateTag().getDouble("ProjectileCharge")));
        if (flight == WeaponProjectile.Flight.STICKY) speed *= 1 + charge * 1.6;
        if (flight == WeaponProjectile.Flight.ARROW) speed *= 0.4 + 0.6 * charge;
        List<WeaponProjectile> prepared = new ArrayList<>();
        for (int i = 0; i < stats.pellets(); i++) {
            WeaponProjectile projectile = TF2EntityTypes.projectile(data.text("Projectile", data.weaponClass())).create(level);
            if (projectile == null) return false;
            projectile.configure(player, stack, flight);
            projectile.setPos(player.getEyePosition().add(player.getLookAngle().scale(0.3)));
            projectile.shootFromRotation(player, player.getXRot() + (flight == WeaponProjectile.Flight.GRENADE ? -3 : 0), player.getYRot(), 0, (float) Math.min(8, speed), (float) (stats.spread() * 66));
            prepared.add(projectile);
        }
        List<WeaponProjectile> added = new ArrayList<>();
        for (WeaponProjectile projectile : prepared) {
            if (!level.addFreshEntity(projectile)) { added.forEach(WeaponProjectile::discard); return false; }
            added.add(projectile);
        }
        if (flight == WeaponProjectile.Flight.STICKY && level instanceof ServerLevel server) {
            for (WeaponProjectile projectile : added) {
                List<WeaponProjectile> active = owned(server, player, WeaponProjectile.Flight.STICKY);
                int maximum = Math.max(1, Math.min(64, 8 + (int) Math.round(attributes.getOrDefault("StickybombBonus", 0D) + attributes.getOrDefault("StickybombPenalty", 0D))));
                if (active.size() > maximum) active.stream().max(Comparator.comparingInt(WeaponProjectile::age).thenComparingInt(old -> -old.getId())).ifPresent(old -> old.detonate(null, 1));
            }
        }
        return true;
    }
    public static List<WeaponProjectile> owned(ServerLevel level, Player player, WeaponProjectile.Flight flight) {
        List<WeaponProjectile> result = new ArrayList<>();
        level.getAllEntities().forEach(entity -> {
            if (entity instanceof WeaponProjectile projectile && projectile.flight() == flight && projectile.ownedBy(player.getUUID()) && !projectile.isRemoved()) result.add(projectile);
        });
        return result;
    }
    public static int detonate(Player player, ItemStack stack) {
        if (!(player.level() instanceof ServerLevel level) || !(stack.getItem() instanceof rafradek.tf2weapons.item.base.CatalogWeaponItem item) || item.definition(stack) == null) return 0;
        WeaponDefinition data = item.definition(stack);
        Map<String, Double> attributes = WeaponAttributes.of(stack, data);
        boolean sticky = data.weaponClass().equals("stickybomb");
        if (!sticky && attributes.getOrDefault("DetonateFlare", 0D) <= 0) return 0;
        List<WeaponProjectile> owned = owned(level, player, sticky ? WeaponProjectile.Flight.STICKY : WeaponProjectile.Flight.FLARE);
        int count = 0;
        for (WeaponProjectile projectile : owned) {
            if (sticky && !projectile.armed()) continue;
            if (attributes.getOrDefault("StickyControl", 0D) > 0 && projectile.position().subtract(player.getEyePosition()).normalize().dot(player.getLookAngle()) < Math.cos(Math.toRadians(30))) continue;
            if (projectile.detonate(null, 1)) count++;
        }
        return count;
    }
}
