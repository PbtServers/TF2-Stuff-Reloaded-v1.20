package rafradek.tf2weapons.registry;

import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import rafradek.tf2weapons.item.base.TF2WeaponItem;
import rafradek.tf2weapons.item.base.TF2WeaponStats;
import rafradek.tf2weapons.TF2StuffReloaded;

public final class TF2Items {
    private static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, TF2StuffReloaded.MOD_ID);

    public static final RegistryObject<Item> SCRAP_METAL =
            ITEMS.register("scrap_metal", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> RECLAIMED_METAL =
            ITEMS.register("reclaimed_metal", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> REFINED_METAL =
            ITEMS.register("refined_metal", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> SCATTERGUN =
            ITEMS.register("scattergun",
                    () -> new TF2WeaponItem(new Item.Properties().stacksTo(1),
                            new TF2WeaponStats(2, 12, 6, 30, 12.0D, 10, 0.08D)));

    private TF2Items() {
    }

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
