package rafradek.tf2weapons.weapon;

import com.google.gson.JsonObject;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/** Resolved, immutable view of the original weapon format. Unknown fields are retained. */
public final class WeaponDefinition {
    public enum Behavior { HITSCAN, MELEE, PROJECTILE, SPECIAL }
    private final String id;
    private final JsonObject data;
    private final Map<String, Double> attributes;

    WeaponDefinition(String id, JsonObject data) {
        this.id = id;
        this.data = data.deepCopy();
        Map<String, Double> values = new LinkedHashMap<>();
        if (data.has("Attributes")) {
            data.getAsJsonObject("Attributes").entrySet().forEach(e -> {
                double value = e.getValue().getAsDouble();
                if (!Double.isFinite(value)) throw new IllegalArgumentException(id + ": invalid attribute " + e.getKey());
                values.put(e.getKey(), value);
            });
        }
        attributes = Collections.unmodifiableMap(values);
    }

    public String id() { return id; }
    public String name() { return text("Name", id); }
    public String weaponClass() { return text("Class", ""); }
    public String text(String key, String fallback) { return data.has(key) ? data.get(key).getAsString() : fallback; }
    public double number(String key, double fallback) { return data.has(key) ? data.get(key).getAsDouble() : fallback; }
    public boolean flag(String key) { return data.has(key) && data.get(key).getAsBoolean(); }
    public Map<String, Double> attributes() { return attributes; }
    public JsonObject rawData() { return data.deepCopy(); }
    public String render() { return text("Render", "rafradek_tf2_weapons:weapon_unavailable"); }

    public Behavior behavior() {
        if (!text("Projectile", "").isEmpty()) return Behavior.PROJECTILE;
        return switch (weaponClass()) {
            case "bullet", "sniperrifle", "minigun" -> Behavior.HITSCAN;
            case "melee", "knife", "wrench" -> Behavior.MELEE;
            case "projectile", "stickybomb", "huntsman", "jar", "cleaver", "flamethrower" -> Behavior.PROJECTILE;
            default -> Behavior.SPECIAL;
        };
    }

    public boolean canAttack() { return behavior() == Behavior.HITSCAN || behavior() == Behavior.MELEE; }
    public boolean automatic() { return behavior() == Behavior.HITSCAN && (flag("Rapidfire crits") || weaponClass().equals("minigun")); }
    public boolean usesAmmo() { return behavior() != Behavior.MELEE && number("Max ammo", 0) > 0; }

    /** These are mechanically supported; everything else stays visible as pending. */
    public static final Set<String> SUPPORTED_ATTRIBUTES = Set.of(
            "DamageBonus", "DamagePenalty", "ClipSizeBonus", "ClipSizePenalty", "FireRateBonus", "FireRatePenalty",
            "SpreadBonus", "SpreadPenalty", "SpreadAdd", "PelletBonus", "PelletPenalty", "ReloadRateBonus", "ReloadRatePenalty",
            "AmmoEfficiencyBonus", "AmmoEfficiencyPenalty", "RangeIncrease", "BurnOnHit",
            "HealthOnHit", "HealthOnKill", "DamageBurnBonus", "DamageNonBurnPenalty", "DamagePlayerPenalty");
}
