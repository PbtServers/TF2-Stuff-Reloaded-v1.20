package rafradek.tf2weapons.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import rafradek.tf2weapons.TF2StuffReloaded;

public final class TF2CreativeTabs {
    private static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, TF2StuffReloaded.MOD_ID);

    public static final RegistryObject<CreativeModeTab> TF2_STUFF = CREATIVE_MODE_TABS.register("tf2_stuff",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.rafradek_tf2_weapons.tf2_stuff"))
                    .icon(() -> TF2Items.SCRAP_METAL.get().getDefaultInstance())
                    .displayItems((parameters, output) -> {
                        output.accept(TF2Items.SCRAP_METAL.get());
                        output.accept(TF2Items.RECLAIMED_METAL.get());
                        output.accept(TF2Items.REFINED_METAL.get());
                        output.accept(TF2Items.SCATTERGUN.get());
                    })
                    .build());

    private TF2CreativeTabs() {
    }

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}
