package rafradek.tf2weapons.weaponclient;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.*;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.lwjgl.glfw.GLFW;
import rafradek.tf2weapons.TF2StuffReloaded;
import rafradek.tf2weapons.registry.TF2EntityTypes;

@Mod.EventBusSubscriber(modid = TF2StuffReloaded.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class CombatClient {
    public static final KeyMapping RELOAD = new KeyMapping("key.rafradek_tf2_weapons.reload", GLFW.GLFW_KEY_R, "key.categories.rafradek_tf2_weapons");
    public static final KeyMapping DETONATE = new KeyMapping("key.rafradek_tf2_weapons.detonate", GLFW.GLFW_KEY_G, "key.categories.rafradek_tf2_weapons");
    @SubscribeEvent public static void keys(RegisterKeyMappingsEvent event) { event.register(RELOAD); event.register(DETONATE); }
    @SubscribeEvent public static void properties(FMLClientSetupEvent event) {
        event.enqueueWork(() -> rafradek.tf2weapons.registry.TF2Items.allWeaponItems().forEach(entry ->
                net.minecraft.client.renderer.item.ItemProperties.register(entry.get(), new net.minecraft.resources.ResourceLocation("backstab"),
                        (stack, level, entity, seed) -> backstabReady(stack, entity) ? 1 : 0)));
    }
    static boolean backstabReady(net.minecraft.world.item.ItemStack stack, net.minecraft.world.entity.LivingEntity entity) {
        var client = net.minecraft.client.Minecraft.getInstance();
        if (entity == null || entity != client.player || !(stack.getItem() instanceof rafradek.tf2weapons.item.base.CatalogWeaponItem item)
                || item.definition(stack) == null || !item.definition(stack).weaponClass().equals("knife")) return false;
        return client.hitResult instanceof net.minecraft.world.phys.EntityHitResult hit && hit.getEntity() instanceof net.minecraft.world.entity.LivingEntity target
                && entity.distanceToSqr(target) <= item.stats(stack).range() * item.stats(stack).range()
                && rafradek.tf2weapons.weapon.WeaponAttributes.of(stack, item.definition(stack)).getOrDefault("NoBackstab", 0D) == 0
                && rafradek.tf2weapons.weapon.CombatDamage.backstab(entity, target);
    }
    @SubscribeEvent public static void layers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        for (var flight : rafradek.tf2weapons.weapon.projectile.WeaponProjectile.Flight.values()) event.registerLayerDefinition(ProjectileRenderer.layer(flight), () -> ProjectileGeometry.create(flight));
    }
    @SubscribeEvent public static void renderers(EntityRenderersEvent.RegisterRenderers event) {
        TF2EntityTypes.projectiles().forEach(type -> event.registerEntityRenderer(type.get(), ProjectileRenderer::new));
    }
}
