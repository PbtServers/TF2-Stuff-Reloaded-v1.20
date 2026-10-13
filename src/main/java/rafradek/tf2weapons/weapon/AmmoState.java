package rafradek.tf2weapons.weapon;

/** Pure state transitions shared by server item code and conservation tests. */
public record AmmoState(int ammo, int reserve, int remaining, boolean reloading) {
    public AmmoState bounded(WeaponStats stats) {
        return new AmmoState(Math.max(0, Math.min(stats.clip(), ammo)), Math.max(0, Math.min(stats.reserve(), reserve)),
                Math.max(0, Math.min(Math.max(stats.firstReload(), stats.shellReload()), remaining)), reloading);
    }
    public AmmoState start(WeaponStats stats) {
        if (!stats.usesClip() || ammo >= stats.clip() || reserve <= 0 || reloading) return this;
        return new AmmoState(ammo, reserve, stats.firstReload(), true);
    }
    public AmmoState tick(WeaponStats stats) {
        if (!reloading) return this;
        if (remaining > 1) return new AmmoState(ammo, reserve, remaining - 1, true);
        int moved = Math.min(reserve, stats.fullReload() ? stats.clip() - ammo : Math.min(1, stats.clip() - ammo));
        int nextAmmo = ammo + moved;
        int nextReserve = reserve - moved;
        boolean continueReload = !stats.fullReload() && nextAmmo < stats.clip() && nextReserve > 0;
        return new AmmoState(nextAmmo, nextReserve, continueReload ? stats.shellReload() : 0, continueReload);
    }
    public AmmoState fire(boolean creative, int cost) {
        if (!creative && ammo < cost) return this;
        return new AmmoState(creative ? ammo : ammo - cost, reserve, 0, false);
    }
}
