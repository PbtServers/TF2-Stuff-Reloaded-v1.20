package rafradek.tf2weapons;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import rafradek.tf2weapons.registry.TF2Blocks;
import rafradek.tf2weapons.registry.TF2CreativeTabs;
import rafradek.tf2weapons.registry.TF2EntityTypes;
import rafradek.tf2weapons.registry.TF2Items;
import rafradek.tf2weapons.registry.TF2Sounds;

@Mod(TF2StuffReloaded.MOD_ID)
public final class TF2StuffReloaded {
    public static final String MOD_ID = "rafradek_tf2_weapons";

    public TF2StuffReloaded() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        TF2Items.register(modEventBus);
        TF2Blocks.register(modEventBus);
        TF2Sounds.register(modEventBus);
        TF2EntityTypes.register(modEventBus);
        TF2CreativeTabs.register(modEventBus);
    }
}
