package rafradek.tf2weapons.weaponclient;

import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.*;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import rafradek.tf2weapons.TF2StuffReloaded;
import rafradek.tf2weapons.item.base.CatalogWeaponItem;
import rafradek.tf2weapons.weapon.CombatControls;

@Mod.EventBusSubscriber(modid = TF2StuffReloaded.MOD_ID, value = Dist.CLIENT)
public final class CombatInput {
    private static boolean scoped() {
        var player = Minecraft.getInstance().player;
        if (player == null || !player.isUsingItem()) return false;
        ItemStack stack = player.getUseItem();
        return stack.getItem() instanceof CatalogWeaponItem item && item.definition(stack) != null && item.definition(stack).weaponClass().equals("sniperrifle");
    }
    @SubscribeEvent public static void tick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || Minecraft.getInstance().player == null || Minecraft.getInstance().screen != null) return;
        while (CombatClient.RELOAD.consumeClick()) CombatControls.CHANNEL.sendToServer(new CombatControls.Request(CombatControls.Action.RELOAD));
        while (CombatClient.DETONATE.consumeClick()) CombatControls.CHANNEL.sendToServer(new CombatControls.Request(CombatControls.Action.DETONATE));
    }
    @SubscribeEvent public static void attack(InputEvent.InteractionKeyMappingTriggered event) {
        if (event.isAttack() && scoped()) {
            event.setCanceled(true); event.setSwingHand(false);
            CombatControls.CHANNEL.sendToServer(new CombatControls.Request(CombatControls.Action.SCOPED_SHOT));
        }
    }
    @SubscribeEvent public static void fov(ComputeFovModifierEvent event) { if (scoped()) event.setNewFovModifier(event.getNewFovModifier() * 0.25F); }
    @SubscribeEvent public static void scope(RenderGuiOverlayEvent.Pre event) {
        if (!scoped() || !event.getOverlay().id().equals(net.minecraftforge.client.gui.overlay.VanillaGuiOverlay.CROSSHAIR.id())) return;
        event.setCanceled(true);
        var client = Minecraft.getInstance();
        var gui = event.getGuiGraphics();
        int width = event.getWindow().getGuiScaledWidth(), height = event.getWindow().getGuiScaledHeight();
        int size = Math.min(width, height), x = (width - size) / 2, y = (height - size) / 2;
        gui.fill(0, 0, x, height, 0xFF000000); gui.fill(x + size, 0, width, height, 0xFF000000);
        gui.blit(new net.minecraft.resources.ResourceLocation(TF2StuffReloaded.MOD_ID, "textures/misc/scope.png"), x, y, size, size, 0F, 0F, 512, 512, 512, 512);
        var stack = client.player.getUseItem();
        double charge = ((CatalogWeaponItem) stack.getItem()).chargeFraction(stack);
        gui.fill(width / 2 - 40, height - 30, width / 2 + 40, height - 25, 0xFF333333);
        gui.fill(width / 2 - 40, height - 30, width / 2 - 40 + (int) (charge * 80), height - 25, 0xFFFFCC00);
    }
}
