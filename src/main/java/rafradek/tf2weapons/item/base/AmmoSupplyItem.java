package rafradek.tf2weapons.item.base;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;

/** One inventory ammo unit replenishes one clip/reserve unit; requires matching original Ammo type. */
public final class AmmoSupplyItem extends Item {
    private final int ammoType;
    public AmmoSupplyItem(int ammoType) { super(new Properties()); this.ammoType = ammoType; }
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack supply = player.getItemInHand(hand);
        ItemStack weapon = player.getItemInHand(hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND);
        if (!(weapon.getItem() instanceof CatalogWeaponItem item) || item.definition(weapon) == null
                || item.definition(weapon).number("Ammo type", 0) != ammoType) return InteractionResultHolder.fail(supply);
        if (!level.isClientSide) {
            int accepted = item.addAmmo(weapon, supply.getCount());
            if (accepted == 0) return InteractionResultHolder.fail(supply);
            if (!player.getAbilities().instabuild) supply.shrink(accepted);
        }
        return InteractionResultHolder.sidedSuccess(supply, level.isClientSide);
    }
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.rafradek_tf2_weapons.weapon.ammo_supply"));
    }
}
