package rafradek.tf2weapons.registry;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import rafradek.tf2weapons.TF2StuffReloaded;

public final class TF2Sounds {
    private static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, TF2StuffReloaded.MOD_ID);

    public static final RegistryObject<SoundEvent> WEAPON_SHOOT = register("weapon_shoot");
    public static final RegistryObject<SoundEvent> WEAPON_RELOAD = register("weapon_reload");
    public static final RegistryObject<SoundEvent> WEAPON_NO_AMMO = register("weapon_no_ammo");

    static {
        java.util.Set<String> paths = new java.util.TreeSet<>();
        try (var stream = java.util.Objects.requireNonNull(TF2Sounds.class.getResourceAsStream("/assets/rafradek_tf2_weapons/sounds.json"));
                var reader = new java.io.InputStreamReader(stream, java.nio.charset.StandardCharsets.UTF_8)) {
            paths.addAll(com.google.gson.JsonParser.parseReader(reader).getAsJsonObject().keySet());
        } catch (java.io.IOException error) { throw new IllegalStateException("Cannot load original sound events", error); }
        for (var weapon : rafradek.tf2weapons.weapon.WeaponCatalog.get().all()) {
            weapon.rawData().entrySet().stream().filter(e -> e.getKey().endsWith("sound"))
                    .map(e -> ResourceLocation.tryParse(e.getValue().getAsString()))
                    .filter(id -> id != null && id.getNamespace().equals(TF2StuffReloaded.MOD_ID))
                    .forEach(id -> paths.add(id.getPath()));
        }
        paths.removeAll(java.util.Set.of("weapon_shoot", "weapon_reload", "weapon_no_ammo"));
        paths.forEach(TF2Sounds::register);
    }

    private TF2Sounds() {
    }

    public static void register(IEventBus eventBus) {
        SOUND_EVENTS.register(eventBus);
    }

    private static RegistryObject<SoundEvent> register(String name) {
        return SOUND_EVENTS.register(name,
                () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(TF2StuffReloaded.MOD_ID, name)));
    }
}
