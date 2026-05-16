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
