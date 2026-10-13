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
        Map<String, Double> attributes = WeaponAttributes.of(stack, data);
        WeaponStats base = WeaponStats.of(data, attributes);
        if (data.weaponClass().equals("minigun") && stack.hasTag() && stack.getTag().getInt("SpinTicks") > 0) {
            double windup = WeaponStats.multiply(18, attributes, "MinigunSpinBonus", "MinigunSpinPenalty");
            double ramp = Math.max(0, Math.min(1, (stack.getTag().getInt("SpinTicks") - windup) / 20D));
            return new WeaponStats(base.damage() * (0.5 + 0.5 * ramp), base.cooldown(), base.clip(), base.reserve(), base.firstReload(), base.shellReload(),
                    base.fullReload(), base.usesClip(), base.range(), base.pellets(), base.spread() * (1.5 - 0.5 * ramp));
        }
        return base;
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
    public boolean chargedWeapon(ItemStack stack) {
        WeaponDefinition data = definition(stack);
        return data != null && java.util.Set.of("sniperrifle", "huntsman", "stickybomb").contains(data.weaponClass());
    }
    public double chargeFraction(ItemStack stack) {
        WeaponDefinition data = definition(stack);
        if (data == null || !stack.hasTag()) return 0;
        int ticks = stack.getTag().getInt("ChargeTicks");
        if (data.weaponClass().equals("huntsman") && ticks > 100) return 0; // Original overcharge spoils the arrow.
        if (data.weaponClass().equals("sniperrifle")) return CombatMath.sniperCharge(ticks,
                WeaponStats.multiply(1, WeaponAttributes.of(stack, data), "ChargeBonus", "ChargePenalty"));
        return Math.max(0, Math.min(1, ticks / (data.weaponClass().equals("stickybomb") ? 80D : 20D)));
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
            if (player.isShiftKeyDown()) {
                if (!definition(stack).weaponClass().equals("stickybomb") || rafradek.tf2weapons.weapon.projectile.ModernProjectiles.detonate(player, stack) == 0) startReload(stack, player);
            } else if (chargedWeapon(stack) || definition(stack).weaponClass().equals("minigun")) {
                stack.getOrCreateTag().putInt("ChargeTicks", 0);
                stack.getOrCreateTag().putInt("SpinTicks", 0);
                stack.getOrCreateTag().putBoolean("Scoped", definition(stack).weaponClass().equals("sniperrifle"));
                stack.getOrCreateTag().putBoolean("ScopedFired", false);
                if (definition(stack).weaponClass().equals("minigun")) playSound(level, player, definition(stack), "Wind up sound", null);
            } else fire(stack, level, player, hand);
        }
        if (!player.isShiftKeyDown() && (definition(stack).automatic() || chargedWeapon(stack) || definition(stack).weaponClass().equals("minigun"))) player.startUsingItem(hand);
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
    @Override
    public int getUseDuration(ItemStack stack) { return 72000; }
    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int remaining) {
        if (!level.isClientSide && entity instanceof Player player && canExecute(stack)) {
            initialize(stack);
            WeaponDefinition data = definition(stack);
            if (chargedWeapon(stack)) {
                stack.getOrCreateTag().putInt("ChargeTicks", Math.min(400, stack.getOrCreateTag().getInt("ChargeTicks") + 1));
                if (data.weaponClass().equals("stickybomb") && stack.getOrCreateTag().getInt("ChargeTicks") >= 80) {
                    fire(stack, level, player, player.getUsedItemHand()); player.stopUsingItem(); clearUseState(stack);
                }
            }
            else if (data.weaponClass().equals("minigun")) {
                int spin = Math.min(400, stack.getOrCreateTag().getInt("SpinTicks") + 1);
                stack.getOrCreateTag().putInt("SpinTicks", spin);
                int required = Math.max(1, (int) Math.round(WeaponStats.multiply(18, WeaponAttributes.of(stack, data), "MinigunSpinBonus", "MinigunSpinPenalty")));
                if (spin >= required) fire(stack, level, player, player.getUsedItemHand());
            } else if (data.automatic()) fire(stack, level, player, player.getUsedItemHand());
        }
    }
    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int remaining) {
        if (!level.isClientSide && entity instanceof Player player && definition(stack) != null) {
            if (chargedWeapon(stack) && canExecute(stack) && !stack.getOrCreateTag().getBoolean("ScopedFired")) fire(stack, level, player, player.getUsedItemHand());
            if (definition(stack).weaponClass().equals("minigun")) playSound(level, player, definition(stack), "Wind down sound", null);
            clearUseState(stack);
        }
    }
    private void clearUseState(ItemStack stack) {
        stack.getOrCreateTag().putInt("ChargeTicks", 0);
        stack.getOrCreateTag().putInt("SpinTicks", 0);
        stack.getOrCreateTag().putBoolean("Scoped", false);
        stack.getOrCreateTag().putBoolean("ScopedFired", false);
    }
    public void primaryScoped(Player player, ItemStack stack) {
        if (player.level().isClientSide || !player.isUsingItem() || player.getUseItem() != stack
                || definition(stack) == null || !definition(stack).weaponClass().equals("sniperrifle")) return;
        long previous = stack.getOrCreateTag().getLong("NextShotTick");
        fire(stack, player.level(), player, player.getUsedItemHand());
        if (stack.getOrCreateTag().getLong("NextShotTick") != previous) {
            stack.getOrCreateTag().putInt("ChargeTicks", 0);
            stack.getOrCreateTag().putBoolean("ScopedFired", true);
        }
    }
    public void reload(Player player, ItemStack stack) { if (!player.level().isClientSide && canExecute(stack)) {
        initialize(stack);
        if (player.isUsingItem() && player.getUseItem() == stack) { player.stopUsingItem(); clearUseState(stack); }
        startReload(stack, player);
    } }
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
        if (attributes.getOrDefault("NeedScope", 0D) > 0 && !tag.getBoolean("Scoped")) return;
        double cost = WeaponStats.multiply(1, attributes, "AmmoEfficiencyBonus", "AmmoEfficiencyPenalty");
        double priorDebt = tag.getDouble("AmmoDebt");
        double debt = (Double.isFinite(priorDebt) ? Math.max(0, Math.min(1, priorDebt)) : 0) + cost;
        int consume = (int) Math.floor(debt);
        boolean vanillaArrows = data.number("Ammo type", 0) == 1000;
        ItemStack arrow = ItemStack.EMPTY;
        if (vanillaArrows && !player.getAbilities().instabuild) {
            for (ItemStack candidate : player.getInventory().items) {
                if (candidate.is(net.minecraft.tags.ItemTags.ARROWS) && candidate.getCount() >= Math.max(1, consume)) { arrow = candidate; break; }
            }
            if (arrow.isEmpty()) {
                tag.putInt("Ammo", 0);
                playSound(level, player, data, "No fire sound", TF2Sounds.WEAPON_NO_AMMO.get());
                player.getCooldowns().addCooldown(this, 5); return;
            }
            ammo = new AmmoState(Math.min(stats.clip(), arrowCount(player)), 0, 0, false);
            ItemStack snapshot = arrow.copy(); snapshot.setCount(1); tag.put("ArrowItem", snapshot.save(new CompoundTag()));
        }
        if (data.usesAmmo() && !player.getAbilities().instabuild && ammo.ammo() < Math.max(1, consume)) {
            if (!startReload(stack, player)) {
                playSound(level, player, data, "No fire sound", TF2Sounds.WEAPON_NO_AMMO.get());
                player.getCooldowns().addCooldown(this, 5);
            }
            return;
        }
        CombatMath.Critical rolled = CombatDamage.roll(player, data, attributes);
        tag.putInt("ShotCritical", rolled.ordinal());
        tag.putDouble("ShotDamage", stats.damage());
        tag.putDouble("ProjectileCharge", chargeFraction(stack));
        if (data.behavior() == WeaponDefinition.Behavior.PROJECTILE) {
            if (!ProjectileActions.find(data).spawn(level, player, stack, data, stats)) return;
        } else performHitscan(level, player, stack, data, stats, attributes, rolled);
        if (data.usesAmmo()) {
            writeAmmo(stack, ammo.fire(player.getAbilities().instabuild, consume));
            if (vanillaArrows && !player.getAbilities().instabuild) { arrow.shrink(consume); tag.putInt("Ammo", Math.min(stats.clip(), arrowCount(player))); }
            tag.putDouble("AmmoDebt", debt - consume);
        }
        String sound = data.weaponClass().equals("minigun") ? "Fire loop sound" : "Fire sound";
        playSound(level, player, data, sound, TF2Sounds.WEAPON_SHOOT.get());
        if (level instanceof net.minecraft.server.level.ServerLevel server && data.behavior() != WeaponDefinition.Behavior.MELEE) {
            Vec3 muzzle = player.getEyePosition().add(player.getLookAngle().scale(0.8));
            server.sendParticles(net.minecraft.core.particles.ParticleTypes.FLASH, muzzle.x, muzzle.y, muzzle.z, 1, 0, 0, 0, 0);
        }
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
        if (definition(stack).number("Ammo type", 0) == 1000) stack.getOrCreateTag().putInt("Ammo", Math.min(stats(stack).clip(), arrowCount(player)));
        WeaponStats stats = stats(stack);
        AmmoState old = readAmmo(stack, stats);
        if (!selected && player.getOffhandItem() != stack) {
            if (stack.hasTag() && (stack.getTag().getInt("ChargeTicks") != 0 || stack.getTag().getInt("SpinTicks") != 0 || stack.getTag().getBoolean("Scoped"))) clearUseState(stack);
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
            clearUseState(stack);
        }
        tag.putString("Type", data.id());
        if (!tag.contains("Ammo", Tag.TAG_ANY_NUMERIC)) {
            tag.putInt("Ammo", tag.contains("Clip", Tag.TAG_ANY_NUMERIC) ? tag.getInt("Clip") : data.number("Ammo type", 0) == 1000 ? 0 : stats.clip());
        }
        if (!tag.contains("ReserveAmmo", Tag.TAG_ANY_NUMERIC)) tag.putInt("ReserveAmmo", stats.usesClip() ? stats.reserve() : 0);
        tag.putString("AmmoType", data.id());
        writeAmmo(stack, readAmmo(stack, stats));
    }
    private int arrowCount(Player player) { return player.getInventory().items.stream().filter(stack -> stack.is(net.minecraft.tags.ItemTags.ARROWS)).mapToInt(ItemStack::getCount).sum(); }
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
    private record ShotHit(Entity target, Vec3 location) {}
    private void performHitscan(Level level, Player player, ItemStack stack, WeaponDefinition data, WeaponStats stats, Map<String, Double> attributes, CombatMath.Critical rolled) {
        Vec3 look = player.getLookAngle().normalize();
        Map<Entity, Integer> hits = new LinkedHashMap<>();
        Map<Entity, Vec3> locations = new HashMap<>();
        for (int i = 0; i < stats.pellets(); i++) {
            Vec3 direction = look;
            if (stats.spread() > 0) direction = look.add(player.getRandom().nextGaussian() * stats.spread(),
                    player.getRandom().nextGaussian() * stats.spread(), player.getRandom().nextGaussian() * stats.spread()).normalize();
            ShotHit hit = findTarget(level, player, direction, stats.range());
            if (hit != null) { hits.merge(hit.target(), 1, Integer::sum); locations.put(hit.target(), hit.location()); }
        }
        hits.forEach((entity, count) -> {
            if (!(entity instanceof LivingEntity target)) {
                entity.hurt(CombatDamage.source(CombatDamage.Kind.BULLET, player, player), (float) (stats.damage() * count));
                return;
            }
            double damage = stats.damage() * count;
            boolean sniper = data.weaponClass().equals("sniperrifle");
            double charge = chargeFraction(stack);
            if (sniper) {
                damage *= 1 + 2 * charge;
                if (charge >= 1) damage = WeaponStats.multiply(damage, attributes, "DamageBonusCharged");
            }
            boolean headshot = (sniper && stack.getOrCreateTag().getBoolean("Scoped") && stack.getOrCreateTag().getInt("ChargeTicks") > 4
                    || attributes.getOrDefault("Headshot", 0D) > 0) && locations.get(target).y >= target.getBoundingBox().maxY - target.getBbHeight() * 0.25;
            boolean stabbed = data.weaponClass().equals("knife") && attributes.getOrDefault("NoBackstab", 0D) == 0 && CombatDamage.backstab(player, target);
            if (CombatDamage.hit(target, player, player, stack, data, damage, rolled,
                    data.behavior() == WeaponDefinition.Behavior.MELEE ? CombatDamage.Kind.MELEE : CombatDamage.Kind.BULLET,
                    player.distanceTo(target), player.getEyePosition(), headshot, stabbed)) {
                if (data.behavior() == WeaponDefinition.Behavior.MELEE) playSound(level, player, data, "Hit sound", TF2Sounds.WEAPON_SHOOT.get());
            }
        });
    }
    @Nullable
    private ShotHit findTarget(Level level, Player player, Vec3 direction, double range) {
        Vec3 start = player.getEyePosition(), end = start.add(direction.scale(range));
        BlockHitResult blockHit = level.clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        Vec3 clipped = blockHit.getType() == HitResult.Type.MISS ? end : blockHit.getLocation();
        ShotHit closest = null;
        double distance = start.distanceToSqr(clipped);
        for (Entity candidate : level.getEntities(player, new AABB(start, clipped).inflate(1),
                e -> (e instanceof LivingEntity || e instanceof rafradek.tf2weapons.weapon.projectile.WeaponProjectile) && e.isAlive() && !e.isSpectator() && e.isPickable())) {
            AABB box = candidate.getBoundingBox().inflate(candidate.getPickRadius());
            Optional<Vec3> hit = box.contains(start) ? Optional.of(start) : box.clip(start, clipped);
            if (hit.isPresent() && start.distanceToSqr(hit.get()) < distance) {
                distance = start.distanceToSqr(hit.get());
                closest = new ShotHit(candidate, hit.get());
            }
        }
        return closest;
    }
    private void playSound(Level level, Player player, WeaponDefinition data, String key, SoundEvent fallback) {
        ResourceLocation id = ResourceLocation.tryParse(data.text(key, ""));
        if (key.equals("Fire sound") || key.equals("Fire loop sound")) {
            ResourceLocation critical = id == null ? null : new ResourceLocation(id.getNamespace(), id.getPath() + ".crit");
            if (player.getItemInHand(player.getUsedItemHand()).hasTag() && player.getItemInHand(player.getUsedItemHand()).getTag().getInt("ShotCritical") == 2
                    && critical != null && ForgeRegistries.SOUND_EVENTS.containsKey(critical)) id = critical;
        }
        SoundEvent sound = id == null ? null : ForgeRegistries.SOUND_EVENTS.getValue(id);
        if (sound == null && fallback == null) return;
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
