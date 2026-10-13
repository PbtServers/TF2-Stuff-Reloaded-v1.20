package rafradek.tf2weapons.registry;

import net.minecraft.world.item.Item;
import java.util.*;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import rafradek.tf2weapons.item.base.CatalogWeaponItem;
import rafradek.tf2weapons.item.base.AmmoSupplyItem;
import rafradek.tf2weapons.weapon.WeaponCatalog;
import rafradek.tf2weapons.weapon.WeaponDefinition;
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
    public static final Map<String, RegistryObject<Item>> WEAPONS = registerWeapons();
    public static final Map<String, RegistryObject<Item>> LEGACY_CARRIERS = registerCarriers();
    public static final RegistryObject<Item> SCATTERGUN = WEAPONS.get("scattergun");
    public static final Map<String, RegistryObject<Item>> AMMO = registerAmmo();

    private static Map<String, RegistryObject<Item>> registerAmmo() {
        String[] names = {"shotgun", "minigun", "pistol", "revolver", "smg", "sniper", "rocket", "grenade", "syringe", "fire", "sticky", "medigun"};
        Map<String, RegistryObject<Item>> result = new LinkedHashMap<>();
        for (int i = 0; i < names.length; i++) {
            int type = i + 1;
            String id = "ammo_" + names[i];
            result.put(id, ITEMS.register(id, () -> new AmmoSupplyItem(type)));
        }
        return Collections.unmodifiableMap(result);
    }

    private static Map<String, RegistryObject<Item>> registerWeapons() {
        Map<String, RegistryObject<Item>> result = new LinkedHashMap<>();
        for (WeaponDefinition data : WeaponCatalog.get().all()) {
            result.put(data.id(), ITEMS.register(data.id(), () -> new CatalogWeaponItem(new Item.Properties().stacksTo(1), data.id())));
        }
        return Collections.unmodifiableMap(result);
    }

    private static Map<String, RegistryObject<Item>> registerCarriers() {
        Map<String, RegistryObject<Item>> result = new TreeMap<>();
        for (WeaponDefinition data : WeaponCatalog.get().all()) {
            String id = data.weaponClass();
            // Some original class IDs (minigun, wrench, etc.) are also weapon IDs.
            if (!WEAPONS.containsKey(id) && !result.containsKey(id)) {
                result.put(id, ITEMS.register(id, () -> new CatalogWeaponItem(new Item.Properties().stacksTo(1), null)));
            }
        }
        return Collections.unmodifiableMap(result);
    }

    public static Collection<RegistryObject<Item>> allWeaponItems() {
        List<RegistryObject<Item>> result = new ArrayList<>(WEAPONS.values());
        result.addAll(LEGACY_CARRIERS.values());
        return result;
    }

    private TF2Items() {
    }

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
