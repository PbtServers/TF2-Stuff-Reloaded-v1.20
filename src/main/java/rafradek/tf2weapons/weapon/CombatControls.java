package rafradek.tf2weapons.weapon;

import java.util.Optional;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.*;
import net.minecraftforge.network.simple.SimpleChannel;
import rafradek.tf2weapons.TF2StuffReloaded;
import rafradek.tf2weapons.item.base.CatalogWeaponItem;
import rafradek.tf2weapons.weapon.projectile.ModernProjectiles;

/** Client sends only an intent; all targets, charge, cooldowns and ownership are resolved on the server. */
public final class CombatControls {
    public enum Action { RELOAD, DETONATE, SCOPED_SHOT }
    public record Request(Action action) {
        static void encode(Request request, FriendlyByteBuf buffer) { buffer.writeEnum(request.action); }
        static Request decode(FriendlyByteBuf buffer) { return new Request(buffer.readEnum(Action.class)); }
        static void handle(Request request, Supplier<NetworkEvent.Context> supplier) {
            NetworkEvent.Context context = supplier.get();
            context.enqueueWork(() -> execute(context.getSender(), request.action));
            context.setPacketHandled(true);
        }
    }
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(new ResourceLocation(TF2StuffReloaded.MOD_ID, "combat"),
            () -> "1", "1"::equals, "1"::equals);
    public static void register() { CHANNEL.registerMessage(0, Request.class, Request::encode, Request::decode, Request::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER)); }
    public static void execute(ServerPlayer player, Action action) {
        if (player == null || player.isSpectator() || !player.isAlive()) return;
        ItemStack stack = action == Action.SCOPED_SHOT ? player.getUseItem() : player.getMainHandItem();
        if (!(stack.getItem() instanceof CatalogWeaponItem item) || item.definition(stack) == null) return;
        switch (action) {
            case RELOAD -> item.reload(player, stack);
            case SCOPED_SHOT -> item.primaryScoped(player, stack);
            case DETONATE -> {
                long now = player.level().getGameTime();
                if (now < player.getPersistentData().getLong("TF2NextDetonation")) return;
                player.getPersistentData().putLong("TF2NextDetonation", now + 2);
                ModernProjectiles.detonate(player, stack);
            }
        }
    }
}
