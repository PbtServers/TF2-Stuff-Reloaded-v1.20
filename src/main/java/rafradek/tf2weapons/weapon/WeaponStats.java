package rafradek.tf2weapons.weapon;

import java.util.Map;

/** Original milliseconds become server ticks (round up so weapons cannot fire faster). */
public record WeaponStats(double damage, int cooldown, int clip, int reserve, int firstReload,
        int shellReload, boolean fullReload, boolean usesClip, double range, int pellets, double spread) {
    public static WeaponStats of(WeaponDefinition data, Map<String, Double> attributes) {
        boolean usesClip = data.flag("Reloads clip");
        int reserve = (int) data.number("Max ammo", 0);
        return new WeaponStats(
                multiply(data.number("Damage", 0), attributes, "DamageBonus", "DamagePenalty"),
                ticks(multiply(data.number("Firing speed", 800), attributes, "FireRateBonus", "FireRatePenalty")),
                usesClip ? Math.max(1, (int) Math.min(65536, Math.round(multiply(data.number("Clip size", 1), attributes, "ClipSizeBonus", "ClipSizePenalty")))) : reserve,
                reserve,
                ticks(multiply(data.number("Reload time first", 1000), attributes, "ReloadRateBonus", "ReloadRatePenalty")),
                ticks(multiply(data.number("Reload time", 500), attributes, "ReloadRateBonus", "ReloadRatePenalty")),
                data.flag("Reloads full clip"), usesClip,
                // Efficient range is an AI preference, not the original bullet trace limit.
                Math.min(256, multiply(data.number("Range", data.behavior() == WeaponDefinition.Behavior.MELEE ? 2.4 : 256), attributes, "RangeIncrease")),
                Math.max(1, (int) Math.min(128, Math.round(multiply(data.number("Pellets", 1), attributes, "PelletBonus", "PelletPenalty")))),
                Math.max(0, multiply(data.number("Spread", 0), attributes, "SpreadBonus", "SpreadPenalty") + attributes.getOrDefault("SpreadAdd", 0D)));
    }
    public static int ticks(double milliseconds) { return Math.max(1, (int) Math.min(72000, Math.ceil(milliseconds / 50D))); }
    public static double multiply(double value, Map<String, Double> attributes, String... keys) {
        for (String key : keys) value *= attributes.getOrDefault(key, 1D);
        return Math.max(0, value);
    }
}
