package rafradek.tf2weapons.item.base;

public final class TF2WeaponStats {
    private final int damage;
    private final int cooldownTicks;
    private final int maxAmmo;
    private final int reloadTicks;
    private final double range;
    private final int pelletCount;
    private final double spread;

    public TF2WeaponStats(int damage, int cooldownTicks, int maxAmmo, int reloadTicks, double range, int pelletCount,
            double spread) {
        this.damage = damage;
        this.cooldownTicks = cooldownTicks;
        this.maxAmmo = maxAmmo;
        this.reloadTicks = reloadTicks;
        this.range = range;
        this.pelletCount = pelletCount;
        this.spread = spread;
    }

    public static TF2WeaponStats simple(int damage, int cooldownTicks, int maxAmmo, int reloadTicks, double range,
            int pelletCount, double spread) {
        return new TF2WeaponStats(damage, cooldownTicks, maxAmmo, reloadTicks, range, pelletCount, spread);
    }

    public int getDamage() {
        return damage;
    }

    public int getCooldownTicks() {
        return cooldownTicks;
    }

    public int getMaxAmmo() {
        return maxAmmo;
    }

    public int getReloadTicks() {
        return reloadTicks;
    }

    public double getRange() {
        return range;
    }

    public int getPelletCount() {
        return pelletCount;
    }

    public double getSpread() {
        return spread;
    }
}
