package rafradek.tf2weapons.weapon;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** Extension point for real modern projectiles; absence means pending, never a fake hitscan. */
public final class ProjectileActions {
    @FunctionalInterface
    public interface Emitter {
        boolean spawn(Level level, Player player, ItemStack stack, WeaponDefinition data, WeaponStats stats);
    }
    private static final Map<String, Emitter> EMITTERS = new HashMap<>();
    public static void register(String projectile, Emitter emitter) {
        if (EMITTERS.putIfAbsent(projectile, java.util.Objects.requireNonNull(emitter)) != null)
            throw new IllegalArgumentException("Duplicate projectile behavior " + projectile);
    }
    public static Emitter find(WeaponDefinition data) { return EMITTERS.get(data.text("Projectile", data.weaponClass())); }
}
