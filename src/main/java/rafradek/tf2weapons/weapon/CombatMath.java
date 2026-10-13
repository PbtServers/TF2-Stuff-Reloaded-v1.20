package rafradek.tf2weapons.weapon;

/** Server combat rules, kept free of game classes for regression tests. */
public final class CombatMath {
    public enum Critical { NORMAL(1), MINI(1.35), FULL(3); public final double multiplier; Critical(double multiplier) { this.multiplier = multiplier; } }
    public static double falloff(WeaponDefinition data, double distance, Critical critical) {
        if (critical == Critical.FULL) return 1;
        double center = Math.max(0.01, data.number("Damage falloff", 9.75));
        double factor = distance <= center ? lerp(data.number("Max damage", 1), 1, distance / center)
                : lerp(1, data.number("Min damage", 1), Math.min(1, (distance - center) / center));
        return critical == Critical.MINI ? Math.max(1, factor) : Math.max(0, factor);
    }
    public static double sniperCharge(int ticks, double chargeRate) {
        double full = 66 / Math.max(0.01, chargeRate);
        double start = 26 / Math.max(0.01, 0.5 * chargeRate + 0.5);
        return Math.max(0, Math.min(1, (ticks - start) / Math.max(1, full - start)));
    }
    public static double blastFalloff(double distance, double radius, boolean direct) {
        if (direct) return 1;
        return Math.max(0, Math.min(1, 1 - distance / Math.max(0.01, radius)));
    }
    public static double criticalChance(double recentDamage, boolean melee) {
        return melee ? Math.min(0.6, 0.15 + Math.max(0, recentDamage) / 177) : Math.min(0.125, 0.025 + Math.max(0, recentDamage) / 800);
    }
    private static double lerp(double a, double b, double t) { return a + (b - a) * t; }
}
