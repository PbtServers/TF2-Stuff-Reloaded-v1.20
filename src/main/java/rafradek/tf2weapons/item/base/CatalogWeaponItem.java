package rafradek.tf2weapons.item.base;

import java.util.*;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;
import net.minecraftforge.registries.ForgeRegistries;
import rafradek.tf2weapons.registry.TF2Sounds;
import rafradek.tf2weapons.weapon.*;

/** Extends the modern Scattergun with catalog data; the historical base remains available. */
public class CatalogWeaponItem extends TF2WeaponItem {
    private final String defaultType;
    public CatalogWeaponItem(Properties properties, String defaultType) {
        super(properties);
        this.defaultType = defaultType;
    }
    @Nullable
    public WeaponDefinition definition(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains("Type")) {
            return tag.contains("Type", Tag.TAG_STRING) ? WeaponCatalog.get().find(tag.getString("Type")) : null;
        }
        return defaultType == null ? null : WeaponCatalog.get().find(defaultType);
    }
    public WeaponStats stats(ItemStack stack) {
        WeaponDefinition data = Objects.requireNonNull(definition(stack), "Invalid weapon Type");
        return WeaponStats.of(data, WeaponAttributes.of(stack, data));
    }
    @Override
    public ItemStack getDefaultInstance() {
        ItemStack stack = super.getDefaultInstance();
        if (defaultType != null) stack.getOrCreateTag().putString("Type", defaultType);
        return stack;
    }
    @Override
    public Component getName(ItemStack stack) {
        WeaponDefinition data = definition(stack);
        return data == null ? Component.translatable("item.rafradek_tf2_weapons.weapon.invalid_type")
                : Component.translatableWithFallback("item.rafradek_tf2_weapons." + data.id(), data.name());
    }
    public boolean canExecute(ItemStack stack) {
        WeaponDefinition data = definition(stack);
        return data != null && (data.canAttack() || (data.behavior() == WeaponDefinition.Behavior.PROJECTILE && ProjectileActions.find(data) != null))
                && !WeaponAttributes.of(stack, data).containsKey("MetalAsAmmo");
    }
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!canExecute(stack)) {
            if (!level.isClientSide) player.displayClientMessage(Component.translatable("item.rafradek_tf2_weapons.weapon.pending"), true);
            return InteractionResultHolder.fail(stack);
        }
        if (!level.isClientSide) {
            initialize(stack);
            if (player.isShiftKeyDown()) startReload(stack, player);
            else fire(stack, level, player, hand);
        }
        if (!player.isShiftKeyDown() && definition(stack).automatic()) player.startUsingItem(hand);
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
    @Override
    public int getUseDuration(ItemStack stack) { return 72000; }
    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int remaining) {
        if (!level.isClientSide && entity instanceof Player player && canExecute(stack) && definition(stack).automatic()) {
            initialize(stack);
            fire(stack, level, player, player.getUsedItemHand());
        }
    }
    @Override
    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
        if (definition(stack) != null && definition(stack).behavior() == WeaponDefinition.Behavior.MELEE
                && !player.level().isClientSide && canExecute(stack)) {
            initialize(stack);
            fire(stack, player.level(), player, InteractionHand.MAIN_HAND);
        }
        return true;
    }
    private void fire(ItemStack stack, Level level, Player player, InteractionHand hand) {
        WeaponDefinition data = definition(stack);
        WeaponStats stats = stats(stack);
        CompoundTag tag = stack.getOrCreateTag();
        if (player.getCooldowns().isOnCooldown(this) || level.getGameTime() < tag.getLong("NextShotTick")) return;
        AmmoState ammo = readAmmo(stack, stats);
        if (ammo.reloading() && (stats.fullReload() || ammo.ammo() == 0)) return;
        Map<String, Double> attributes = WeaponAttributes.of(stack, data);
        double cost = WeaponStats.multiply(1, attributes, "AmmoEfficiencyBonus", "AmmoEfficiencyPenalty");
        double priorDebt = tag.getDouble("AmmoDebt");
        double debt = (Double.isFinite(priorDebt) ? Math.max(0, Math.min(1, priorDebt)) : 0) + cost;
        int consume = (int) Math.floor(debt);
        if (data.usesAmmo() && !player.getAbilities().instabuild && ammo.ammo() < Math.max(1, consume)) {
            if (!startReload(stack, player)) {
                playSound(level, player, data, "No fire sound", TF2Sounds.WEAPON_NO_AMMO.get());
                player.getCooldowns().addCooldown(this, 5);
            }
            return;
        }
        if (data.behavior() == WeaponDefinition.Behavior.PROJECTILE) {
            if (!ProjectileActions.find(data).spawn(level, player, stack, data, stats)) return;
        } else performHitscan(level, player, data, stats, attributes);
        if (data.usesAmmo()) {
            writeAmmo(stack, ammo.fire(player.getAbilities().instabuild, consume));
            tag.putDouble("AmmoDebt", debt - consume);
        }
        String sound = data.weaponClass().equals("minigun") ? "Fire loop sound" : "Fire sound";
        playSound(level, player, data, sound, TF2Sounds.WEAPON_SHOOT.get());
        tag.putLong("NextShotTick", level.getGameTime() + stats.cooldown());
        player.getCooldowns().addCooldown(this, stats.cooldown());
        player.swing(hand, true);
    }
    private boolean startReload(ItemStack stack, Player player) {
        WeaponStats stats = stats(stack);
        AmmoState old = readAmmo(stack, stats);
        AmmoState next = old.start(stats);
        if (next.equals(old)) return false;
        writeAmmo(stack, next);
        playSound(player.level(), player, definition(stack), "Reload sound", TF2Sounds.WEAPON_RELOAD.get());
        return true;
    }
    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (level.isClientSide || !(entity instanceof Player player) || definition(stack) == null) return;
        initialize(stack);
        WeaponStats stats = stats(stack);
        AmmoState old = readAmmo(stack, stats);
        if (!selected && player.getOffhandItem() != stack) {
            if (old.reloading()) writeAmmo(stack, new AmmoState(old.ammo(), old.reserve(), 0, false));
            return;
        }
        if (old.reloading()) {
            AmmoState next = old.tick(stats);
            writeAmmo(stack, next);
            if (next.ammo() > old.ammo() && next.reloading()) playSound(level, player, definition(stack), "Reload sound", TF2Sounds.WEAPON_RELOAD.get());
        } else if (definition(stack).usesAmmo() && old.ammo() == 0 && canExecute(stack)) startReload(stack, player);
    }
    public void initialize(ItemStack stack) {
        WeaponDefinition data = definition(stack);
        if (data == null) return;
        WeaponStats stats = stats(stack);
        CompoundTag tag = stack.getOrCreateTag();
        if (tag.contains("AmmoType", Tag.TAG_STRING) && !tag.getString("AmmoType").equals(data.id())) {
            writeAmmo(stack, new AmmoState(0, 0, 0, false));
            tag.putDouble("AmmoDebt", 0);
        }
        tag.putString("Type", data.id());
        if (!tag.contains("Ammo", Tag.TAG_ANY_NUMERIC)) {
            tag.putInt("Ammo", tag.contains("Clip", Tag.TAG_ANY_NUMERIC) ? tag.getInt("Clip") : stats.clip());
        }
        if (!tag.contains("ReserveAmmo", Tag.TAG_ANY_NUMERIC)) tag.putInt("ReserveAmmo", stats.usesClip() ? stats.reserve() : 0);
        tag.putString("AmmoType", data.id());
        writeAmmo(stack, readAmmo(stack, stats));
    }
    public AmmoState readAmmo(ItemStack stack, WeaponStats stats) {
        CompoundTag tag = stack.getTag();
        return tag == null ? new AmmoState(stats.clip(), stats.usesClip() ? stats.reserve() : 0, 0, false)
                : new AmmoState(tag.contains("Ammo", Tag.TAG_ANY_NUMERIC) ? tag.getInt("Ammo") : stats.clip(),
                        tag.contains("ReserveAmmo", Tag.TAG_ANY_NUMERIC) ? tag.getInt("ReserveAmmo") : stats.usesClip() ? stats.reserve() : 0,
                        tag.getInt("ReloadTicks"), tag.getBoolean("Reloading")).bounded(stats);
    }
    private void writeAmmo(ItemStack stack, AmmoState ammo) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putInt("Ammo", ammo.ammo());
        tag.putInt("ReserveAmmo", ammo.reserve());
        tag.putInt("ReloadTicks", ammo.remaining());
        tag.putBoolean("Reloading", ammo.reloading());
    }
    public int addAmmo(ItemStack stack, int units) {
        initialize(stack);
        WeaponDefinition data = definition(stack);
        if (data == null || !data.usesAmmo() || units <= 0) return 0;
        WeaponStats stats = stats(stack);
        AmmoState state = readAmmo(stack, stats);
        int accepted = Math.min(units, stats.usesClip() ? stats.reserve() - state.reserve() : stats.clip() - state.ammo());
        writeAmmo(stack, new AmmoState(state.ammo() + (stats.usesClip() ? 0 : accepted), state.reserve() + (stats.usesClip() ? accepted : 0), state.remaining(), state.reloading()));
        return accepted;
    }
    private void performHitscan(Level level, Player player, WeaponDefinition data, WeaponStats stats, Map<String, Double> attributes) {
        Vec3 look = player.getLookAngle().normalize();
        Map<LivingEntity, Integer> hits = new LinkedHashMap<>();
        for (int i = 0; i < stats.pellets(); i++) {
            Vec3 direction = look;
            if (stats.spread() > 0) direction = look.add(player.getRandom().nextGaussian() * stats.spread(),
                    player.getRandom().nextGaussian() * stats.spread(), player.getRandom().nextGaussian() * stats.spread()).normalize();
            LivingEntity target = findTarget(level, player, direction, stats.range());
            if (target != null) hits.merge(target, 1, Integer::sum);
        }
        hits.forEach((target, count) -> {
            double damage = WeaponStats.multiply(stats.damage() * count, attributes,
                    target.isOnFire() ? "DamageBurnBonus" : "DamageNonBurnPenalty", "DamagePlayerPenalty");
            if (data.behavior() == WeaponDefinition.Behavior.HITSCAN) {
                double distance = player.distanceTo(target), falloff = Math.max(0.01, data.number("Damage falloff", 9.75));
                damage *= distance <= falloff ? lerp(data.number("Max damage", 1), 1, distance / falloff)
                        : lerp(1, data.number("Min damage", 1), Math.min(1, (distance - falloff) / falloff));
            }
            if (target.hurt(player.damageSources().playerAttack(player), (float) damage)) {
                double burn = attributes.getOrDefault("BurnOnHit", 0D);
                if (burn > 0) target.setSecondsOnFire(Math.max(1, (int) Math.ceil(burn)));
                player.heal(attributes.getOrDefault("HealthOnHit", 0D).floatValue());
                if (!target.isAlive()) player.heal(attributes.getOrDefault("HealthOnKill", 0D).floatValue());
                if (data.behavior() == WeaponDefinition.Behavior.MELEE) playSound(level, player, data, "Hit sound", TF2Sounds.WEAPON_SHOOT.get());
            }
        });
    }
    private static double lerp(double a, double b, double t) { return a + (b - a) * t; }
    @Nullable
    private LivingEntity findTarget(Level level, Player player, Vec3 direction, double range) {
        Vec3 start = player.getEyePosition(), end = start.add(direction.scale(range));
        BlockHitResult blockHit = level.clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        Vec3 clipped = blockHit.getType() == HitResult.Type.MISS ? end : blockHit.getLocation();
        LivingEntity closest = null;
        double distance = start.distanceToSqr(clipped);
        for (LivingEntity candidate : level.getEntitiesOfClass(LivingEntity.class, new AABB(start, clipped).inflate(1),
                e -> e != player && e.isAlive() && !e.isSpectator() && e.isPickable())) {
            AABB box = candidate.getBoundingBox().inflate(candidate.getPickRadius());
            Optional<Vec3> hit = box.contains(start) ? Optional.of(start) : box.clip(start, clipped);
            if (hit.isPresent() && start.distanceToSqr(hit.get()) < distance) {
                distance = start.distanceToSqr(hit.get());
                closest = candidate;
            }
        }
        return closest;
    }
    private void playSound(Level level, Player player, WeaponDefinition data, String key, SoundEvent fallback) {
        ResourceLocation id = ResourceLocation.tryParse(data.text(key, ""));
        SoundEvent sound = id == null ? null : ForgeRegistries.SOUND_EVENTS.getValue(id);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), sound == null ? fallback : sound, SoundSource.PLAYERS, 1, 1);
    }
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        WeaponDefinition data = definition(stack);
        if (data == null) {
            tooltip.add(Component.translatable("item.rafradek_tf2_weapons.weapon.invalid_type").withStyle(ChatFormatting.RED));
            return;
        }
        WeaponStats stats = stats(stack);
        tooltip.add(Component.literal("Type: " + data.id()));
        tooltip.add(Component.translatable("item.rafradek_tf2_weapons.weapon.damage", stats.damage()));
        tooltip.add(Component.translatable("item.rafradek_tf2_weapons.weapon.cooldown", stats.cooldown()));
        tooltip.add(Component.translatable("item.rafradek_tf2_weapons.weapon.range", stats.range()));
        tooltip.add(Component.translatable("item.rafradek_tf2_weapons.weapon.pellets", stats.pellets()));
        tooltip.add(Component.translatable("item.rafradek_tf2_weapons.weapon.spread", stats.spread()));
        if (data.usesAmmo()) {
            AmmoState ammo = readAmmo(stack, stats);
            tooltip.add(Component.translatable("item.rafradek_tf2_weapons.weapon.ammo", ammo.ammo(), stats.clip()));
            tooltip.add(Component.translatable("item.rafradek_tf2_weapons.weapon.reserve", stats.usesClip() ? ammo.reserve() : ammo.ammo(), stats.reserve()));
            if (ammo.reloading()) tooltip.add(Component.translatable("item.rafradek_tf2_weapons.weapon.reloading"));
        }
        tooltip.add(Component.translatable(canExecute(stack) ? "item.rafradek_tf2_weapons.weapon.partial" : "item.rafradek_tf2_weapons.weapon.pending").withStyle(ChatFormatting.YELLOW));
        if (data.render().endsWith(":weapon_unavailable")) tooltip.add(Component.translatable("item.rafradek_tf2_weapons.weapon.provisional_model").withStyle(ChatFormatting.YELLOW));
        WeaponAttributes.of(stack, data).forEach((name, value) -> tooltip.add(Component.literal(name + ": " + value
                + (WeaponDefinition.SUPPORTED_ATTRIBUTES.contains(name) && canExecute(stack) ? "" : " [pending]"))));
    }
    @Override
    public boolean isBarVisible(ItemStack stack) { return definition(stack) != null && definition(stack).usesAmmo(); }
    @Override
    public int getBarWidth(ItemStack stack) {
        if (!isBarVisible(stack)) return 0;
        WeaponStats stats = stats(stack);
        AmmoState ammo = readAmmo(stack, stats);
        return (int) Math.round(13 * (ammo.reloading() ? 1 - (double) ammo.remaining() / Math.max(stats.firstReload(), stats.shellReload()) : (double) ammo.ammo() / Math.max(1, stats.clip())));
    }
    @Override
    public int getBarColor(ItemStack stack) {
        if (!isBarVisible(stack)) return 0x55FF55;
        AmmoState ammo = readAmmo(stack, stats(stack));
        return ammo.reloading() ? 0x55FFFF : ammo.ammo() == 0 ? 0xFF5555 : 0x55FF55;
    }
}
