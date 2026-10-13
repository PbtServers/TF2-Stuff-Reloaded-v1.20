package rafradek.tf2weapons.registry;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.*;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.*;
import rafradek.tf2weapons.TF2StuffReloaded;
import rafradek.tf2weapons.weapon.CombatDamage;

public final class TF2Effects {
    private static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, TF2StuffReloaded.MOD_ID);
    public static final RegistryObject<MobEffect> CRIT_BOOST = EFFECTS.register("crit_boost", () -> new BasicEffect(MobEffectCategory.BENEFICIAL, 0xFF3333));
    public static final RegistryObject<MobEffect> MINICRIT_BOOST = EFFECTS.register("minicrit_boost", () -> new BasicEffect(MobEffectCategory.BENEFICIAL, 0xFFFF33));
    public static final RegistryObject<MobEffect> MARKED = EFFECTS.register("marked_for_death", () -> new BasicEffect(MobEffectCategory.HARMFUL, 0xAA3333));
    public static final RegistryObject<MobEffect> AFTERBURN = EFFECTS.register("afterburn", () -> new DamageOverTime(true));
    public static final RegistryObject<MobEffect> BLEEDING = EFFECTS.register("bleeding", () -> new DamageOverTime(false));
    public static void register(IEventBus bus) { EFFECTS.register(bus); }
    private static class BasicEffect extends MobEffect { BasicEffect(MobEffectCategory category, int color) { super(category, color); } }
    private static final class DamageOverTime extends BasicEffect {
        private final boolean fire;
        DamageOverTime(boolean fire) { super(MobEffectCategory.HARMFUL, fire ? 0xFF6600 : 0xAA0000); this.fire = fire; }
        @Override public boolean isDurationEffectTick(int duration, int amplifier) { return true; }
        @Override public void applyEffectTick(LivingEntity entity, int amplifier) {
            if (!(entity.level() instanceof ServerLevel level)) return;
            if (fire && (entity.isInWaterRainOrBubble() || entity.fireImmune())) { entity.removeEffect(this); entity.clearFire(); return; }
            // One tick displays flames without scheduling vanilla on-fire damage a second time.
            if (fire && entity.getRemainingFireTicks() <= 1) entity.setRemainingFireTicks(1);
            MobEffectInstance instance = entity.getEffect(this);
            if (instance == null || instance.getDuration() % 10 != 0) return;
            String key = fire ? "TF2AfterburnOwner" : "TF2BleedOwner";
            Entity owner = entity.getPersistentData().hasUUID(key) ? level.getEntity(entity.getPersistentData().getUUID(key)) : null;
            CombatDamage.Kind kind = fire ? CombatDamage.Kind.AFTERBURN : CombatDamage.Kind.MELEE;
            if (!(owner instanceof LivingEntity living) || CombatDamage.canAttack(living, entity)) entity.hurt(CombatDamage.source(kind, entity, owner), (float) (0.4 * CombatDamage.resistance(entity, kind)));
        }
    }
}
