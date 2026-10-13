package rafradek.tf2weapons.registry;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.RegistryObject;
import rafradek.tf2weapons.weapon.projectile.WeaponProjectile;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import rafradek.tf2weapons.TF2StuffReloaded;

public final class TF2EntityTypes {
    private static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, TF2StuffReloaded.MOD_ID);
    private static final java.util.Map<String, RegistryObject<EntityType<WeaponProjectile>>> PROJECTILES = new java.util.LinkedHashMap<>();
    static {
        for (String name : rafradek.tf2weapons.weapon.projectile.ModernProjectiles.SUPPORTED.keySet()) {
            PROJECTILES.put(name, ENTITY_TYPES.register(name, () -> EntityType.Builder.<WeaponProjectile>of(WeaponProjectile::new, MobCategory.MISC)
                    .sized(0.3F, 0.3F).clientTrackingRange(8).updateInterval(1).setShouldReceiveVelocityUpdates(true)
                    .build(TF2StuffReloaded.MOD_ID + ":" + name)));
        }
    }
    public static EntityType<WeaponProjectile> projectile(String name) { return PROJECTILES.get(name).get(); }
    public static java.util.Collection<RegistryObject<EntityType<WeaponProjectile>>> projectiles() { return java.util.Collections.unmodifiableCollection(PROJECTILES.values()); }

    private TF2EntityTypes() {
    }

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }
}
