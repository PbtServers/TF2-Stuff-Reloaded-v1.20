package rafradek.tf2weapons.item.base;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import rafradek.tf2weapons.registry.TF2Sounds;

public class TF2WeaponItem extends Item {
    private static final String AMMO_KEY = "Ammo";
    private static final String RELOADING_KEY = "Reloading";
    private static final String RELOAD_TICKS_KEY = "ReloadTicks";

    private final TF2WeaponStats stats;

    public TF2WeaponItem(Properties properties) {
        this(properties, new TF2WeaponStats(0, 20, 0, 0, 0.0D, 1, 0.0D));
    }

    public TF2WeaponItem(Properties properties, TF2WeaponStats stats) {
        super(properties);
        this.stats = stats;
    }

    public TF2WeaponStats getStats() {
        return stats;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide) {
            initializeAmmoIfNeeded(stack);

            int ammo = getAmmo(stack);

            if (isReloading(stack)) {
            } else if (ammo > 0) {
                setAmmo(stack, ammo - 1);
                level.playSound(null, player.getX(), player.getY(), player.getZ(), TF2Sounds.WEAPON_SHOOT.get(),
                        SoundSource.PLAYERS, 1.0F, 1.0F);
                performHitscan(level, player);
                player.getCooldowns().addCooldown(this, stats.getCooldownTicks());
            } else {
                level.playSound(null, player.getX(), player.getY(), player.getZ(), TF2Sounds.WEAPON_NO_AMMO.get(),
                        SoundSource.PLAYERS, 1.0F, 1.0F);
                if (stats.getReloadTicks() > 0) {
                    setReloading(stack, true);
                    setReloadTicks(stack, stats.getReloadTicks());
                    level.playSound(null, player.getX(), player.getY(), player.getZ(), TF2Sounds.WEAPON_RELOAD.get(),
                            SoundSource.PLAYERS, 1.0F, 1.0F);
                }
                player.getCooldowns().addCooldown(this, 5);
            }
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.rafradek_tf2_weapons.weapon.tooltip"));
        tooltip.add(Component.translatable("item.rafradek_tf2_weapons.weapon.damage", stats.getDamage()));
        tooltip.add(Component.translatable("item.rafradek_tf2_weapons.weapon.cooldown", stats.getCooldownTicks()));
        tooltip.add(Component.translatable("item.rafradek_tf2_weapons.weapon.max_ammo", stats.getMaxAmmo()));
        tooltip.add(Component.translatable("item.rafradek_tf2_weapons.weapon.range", stats.getRange()));
        tooltip.add(Component.translatable("item.rafradek_tf2_weapons.weapon.pellets", stats.getPelletCount()));
        tooltip.add(Component.translatable("item.rafradek_tf2_weapons.weapon.spread", stats.getSpread()));
        if (hasAmmoInitialized(stack)) {
            tooltip.add(Component.translatable("item.rafradek_tf2_weapons.weapon.ammo", getAmmo(stack), stats.getMaxAmmo()));
        }
        if (isReloading(stack)) {
            tooltip.add(Component.translatable("item.rafradek_tf2_weapons.weapon.reloading"));
        }
        super.appendHoverText(stack, level, tooltip, flag);
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return stats.getMaxAmmo() > 0;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        if (stats.getMaxAmmo() <= 0) {
            return 0;
        }

        if (isReloading(stack)) {
            return clampBarWidth(getReloadProgress(stack));
        }

        return clampBarWidth((double) getVisualAmmo(stack) / (double) stats.getMaxAmmo());
    }

    @Override
    public int getBarColor(ItemStack stack) {
        if (isReloading(stack)) {
            return 0x55FFFF;
        }

        if (stats.getMaxAmmo() <= 0) {
            return 0x55FF55;
        }

        double ammoRatio = (double) getVisualAmmo(stack) / (double) stats.getMaxAmmo();
        if (ammoRatio > 0.66D) {
            return 0x55FF55;
        }
        if (ammoRatio > 0.33D) {
            return 0xFFFF55;
        }
        return 0xFF5555;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (!level.isClientSide && entity instanceof Player) {
            initializeAmmoIfNeeded(stack);

            if (isReloading(stack)) {
                int remainingTicks = Math.max(0, getReloadTicks(stack) - 1);
                setReloadTicks(stack, remainingTicks);
                if (remainingTicks == 0) {
                    setAmmo(stack, stats.getMaxAmmo());
                    setReloading(stack, false);
                }
            }
        }

        super.inventoryTick(stack, level, entity, slotId, isSelected);
    }

    private int getAmmo(ItemStack stack) {
        return stack.getOrCreateTag().getInt(AMMO_KEY);
    }

    private void setAmmo(ItemStack stack, int ammo) {
        stack.getOrCreateTag().putInt(AMMO_KEY, Math.max(ammo, 0));
    }

    private boolean hasAmmoInitialized(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.contains(AMMO_KEY);
    }

    private boolean isReloading(ItemStack stack) {
        return stack.getOrCreateTag().getBoolean(RELOADING_KEY);
    }

    private void setReloading(ItemStack stack, boolean reloading) {
        stack.getOrCreateTag().putBoolean(RELOADING_KEY, reloading);
        if (!reloading) {
            setReloadTicks(stack, 0);
        }
    }

    private int getReloadTicks(ItemStack stack) {
        return stack.getOrCreateTag().getInt(RELOAD_TICKS_KEY);
    }

    private void setReloadTicks(ItemStack stack, int ticks) {
        stack.getOrCreateTag().putInt(RELOAD_TICKS_KEY, Math.max(ticks, 0));
    }

    private int getVisualAmmo(ItemStack stack) {
        return hasAmmoInitialized(stack) ? getAmmo(stack) : stats.getMaxAmmo();
    }

    private void initializeAmmoIfNeeded(ItemStack stack) {
        if (!hasAmmoInitialized(stack)) {
            setAmmo(stack, stats.getMaxAmmo());
        }
    }

    private void performHitscan(Level level, Player player) {
        if (level.isClientSide || stats.getRange() <= 0.0D) {
            return;
        }

        Vec3 lookDirection = player.getLookAngle().normalize();
        int pelletCount = Math.max(1, stats.getPelletCount());

        for (int i = 0; i < pelletCount; i++) {
            Vec3 pelletDirection = applySpread(player, lookDirection);
            performHitscanPellet(level, player, pelletDirection);
        }
    }

    private void performHitscanPellet(Level level, Player player, Vec3 direction) {
        Vec3 start = player.getEyePosition();
        Vec3 end = start.add(direction.scale(stats.getRange()));
        BlockHitResult blockHitResult = level.clip(new ClipContext(start, end, ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE, player));
        Vec3 clippedEnd = blockHitResult.getType() == HitResult.Type.MISS ? end : blockHitResult.getLocation();
        AABB searchBox = new AABB(start, clippedEnd).inflate(1.0D);
        LivingEntity target = findHitscanTarget(level, player, start, clippedEnd, searchBox);

        if (target != null) {
            target.hurt(player.damageSources().playerAttack(player), (float) stats.getDamage());
        }
    }

    private Vec3 applySpread(Player player, Vec3 lookDirection) {
        Vec3 normalizedLookDirection = lookDirection.normalize();
        if (stats.getSpread() <= 0.0D) {
            return normalizedLookDirection;
        }

        Vec3 spreadDirection = normalizedLookDirection.add(
                player.getRandom().nextGaussian() * stats.getSpread(),
                player.getRandom().nextGaussian() * stats.getSpread(),
                player.getRandom().nextGaussian() * stats.getSpread());
        return spreadDirection.normalize();
    }

    @Nullable
    private LivingEntity findHitscanTarget(Level level, Player player, Vec3 start, Vec3 end, AABB searchBox) {
        LivingEntity closestTarget = null;
        double closestDistanceSqr = start.distanceToSqr(end);

        for (LivingEntity candidate : level.getEntitiesOfClass(LivingEntity.class, searchBox,
                entity -> entity != player && entity.isAlive() && entity.isPickable())) {
            AABB candidateBox = candidate.getBoundingBox().inflate(candidate.getPickRadius());
            java.util.Optional<Vec3> hitLocation = candidateBox.clip(start, end);
            if (hitLocation.isEmpty()) {
                continue;
            }

            double hitDistanceSqr = start.distanceToSqr(hitLocation.get());
            if (hitDistanceSqr < closestDistanceSqr) {
                closestDistanceSqr = hitDistanceSqr;
                closestTarget = candidate;
            }
        }

        return closestTarget;
    }

    private double getReloadProgress(ItemStack stack) {
        if (stats.getReloadTicks() <= 0) {
            return 1.0D;
        }

        int currentTicks = Math.max(0, getReloadTicks(stack));
        double progress = 1.0D - ((double) currentTicks / (double) stats.getReloadTicks());
        return Math.max(0.0D, Math.min(1.0D, progress));
    }

    private int clampBarWidth(double ratio) {
        double clampedRatio = Math.max(0.0D, Math.min(1.0D, ratio));
        return (int) Math.round(13.0D * clampedRatio);
    }
}
