package rafradek.tf2weapons.weaponclient;

import java.util.*;
import javax.annotation.Nullable;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.model.BakedModelWrapper;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import rafradek.tf2weapons.TF2StuffReloaded;
import rafradek.tf2weapons.item.base.CatalogWeaponItem;
import rafradek.tf2weapons.registry.TF2Items;
import rafradek.tf2weapons.weapon.*;

/** Client-only model routing uses Type in all vanilla ItemRenderer perspectives. */
@Mod.EventBusSubscriber(modid = TF2StuffReloaded.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class WeaponModels {
    private static final ResourceLocation PROVISIONAL = model("rafradek_tf2_weapons:weapon_unavailable");
    private static ResourceLocation model(String render) {
        ResourceLocation id = new ResourceLocation(render);
        return new ResourceLocation(id.getNamespace(), "item/" + id.getPath());
    }
    @SubscribeEvent
    public static void register(ModelEvent.RegisterAdditional event) {
        event.register(PROVISIONAL);
        WeaponCatalog.get().all().forEach(data -> event.register(model(data.render())));
    }
    @SubscribeEvent
    public static void bake(ModelEvent.ModifyBakingResult event) {
        Map<ResourceLocation, BakedModel> baked = event.getModels();
        Map<String, BakedModel> weapons = new HashMap<>();
        BakedModel provisional = Objects.requireNonNull(baked.get(PROVISIONAL), "Missing provisional weapon model");
        for (WeaponDefinition data : WeaponCatalog.get().all()) weapons.put(data.id(), baked.getOrDefault(model(data.render()), provisional));
        Map<String, BakedModel> resolved = Map.copyOf(weapons);
        TF2Items.allWeaponItems().forEach(item -> {
            ResourceLocation id = item.getId();
            BakedModel base = resolved.getOrDefault(id.getPath(), provisional);
            baked.put(new ModelResourceLocation(id, "inventory"), new RoutedModel(base, resolved, provisional));
        });
        if (Boolean.getBoolean("tf2weapons.validateModels")) validate(baked, resolved, provisional);
    }

    private static void validate(Map<ResourceLocation, BakedModel> baked, Map<String, BakedModel> resolved, BakedModel provisional) {
        int originals = 0;
        var carrier = TF2Items.LEGACY_CARRIERS.get("bullet");
        BakedModel routedCarrier = baked.get(new ModelResourceLocation(carrier.getId(), "inventory"));
        for (WeaponDefinition data : WeaponCatalog.get().all()) {
            BakedModel selected = resolved.get(data.id());
            if (!data.render().endsWith(":weapon_unavailable")) originals++;
            // Verify both the individual registry item and a historical Type carrier.
            ItemStack stack = TF2Items.WEAPONS.get(data.id()).get().getDefaultInstance();
            BakedModel routed = baked.get(new ModelResourceLocation(TF2Items.WEAPONS.get(data.id()).getId(), "inventory"));
            if (routed.getOverrides().resolve(routed, stack, null, null, 0) != selected)
                throw new IllegalStateException("Wrong individual model: " + data.id());
            ItemStack legacy = new ItemStack(carrier.get());
            legacy.getOrCreateTag().putString("Type", data.id());
            if (routedCarrier.getOverrides().resolve(routedCarrier, legacy, null, null, 0) != selected)
                throw new IllegalStateException("Wrong Type model: " + data.id());
            List<net.minecraft.client.renderer.block.model.BakedQuad> quads = new ArrayList<>(selected.getQuads(null, null, RandomSource.create(0)));
            for (Direction direction : Direction.values()) quads.addAll(selected.getQuads(null, direction, RandomSource.create(0)));
            if (quads.isEmpty() || quads.stream().anyMatch(quad -> quad.getSprite().contents().name().equals(MissingTextureAtlasSprite.getLocation())))
                throw new IllegalStateException("Empty model or missing face texture: " + data.id() + ", render=" + data.render()
                        + ", class=" + selected.getClass().getName() + ", quads=" + quads.size()
                        + ", sprites=" + quads.stream().map(q -> q.getSprite().contents().name()).distinct().toList());
        }
        ItemStack invalid = new ItemStack(carrier.get());
        invalid.getOrCreateTag().putString("Type", "not_a_weapon");
        if (routedCarrier.getOverrides().resolve(routedCarrier, invalid, null, null, 0) != provisional)
            throw new IllegalStateException("Unknown Type must use explicit provisional model");
        com.mojang.logging.LogUtils.getLogger().info("Weapon model validation passed: {} original resource mappings, {} provisional, {} Type routes",
                originals, resolved.size() - originals, resolved.size());
    }
    private static final class RoutedModel extends BakedModelWrapper<BakedModel> {
        private final ItemOverrides overrides;
        RoutedModel(BakedModel base, Map<String, BakedModel> models, BakedModel provisional) {
            super(base);
            overrides = new ItemOverrides() {
                @Override
                public BakedModel resolve(BakedModel original, ItemStack stack, @Nullable ClientLevel level,
                        @Nullable LivingEntity entity, int seed) {
                    if (!(stack.getItem() instanceof CatalogWeaponItem item)) return original;
                    WeaponDefinition data = item.definition(stack);
                    BakedModel selected = data == null ? provisional : models.getOrDefault(data.id(), provisional);
                    BakedModel animated = selected.getOverrides().resolve(selected, stack, level, entity, seed);
                    return animated == null ? selected : animated;
                }
            };
        }
        @Override
        public ItemOverrides getOverrides() { return overrides; }
    }
}
