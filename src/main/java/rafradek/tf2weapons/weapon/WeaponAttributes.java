package rafradek.tf2weapons.weapon;

import com.google.gson.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

public final class WeaponAttributes {
    private record AttributeInfo(String name, boolean additive) {}
    private static final Map<String, AttributeInfo> IDS = loadIds();
    private static Map<String, AttributeInfo> loadIds() {
        try (Reader reader = new InputStreamReader(Objects.requireNonNull(WeaponAttributes.class.getResourceAsStream(
                "/assets/rafradek_tf2_weapons/weapon_metadata/attribute_ids.json")), StandardCharsets.UTF_8)) {
            Map<String, AttributeInfo> names = new HashMap<>();
            JsonParser.parseReader(reader).getAsJsonObject().entrySet().forEach(e -> {
                JsonObject data = e.getValue().getAsJsonObject();
                AttributeInfo info = new AttributeInfo(data.get("name").getAsString(), data.get("type").getAsString().equals("ADDITIVE"));
                names.put(e.getKey(), info);
                names.put(info.name(), info);
            });
            return Map.copyOf(names);
        } catch (IOException error) { throw new IllegalStateException(error); }
    }
    public static Map<String, Double> of(ItemStack stack, WeaponDefinition definition) {
        Map<String, Double> result = new LinkedHashMap<>(definition.attributes());
        if (stack.hasTag() && stack.getTag().contains("Attributes", Tag.TAG_COMPOUND)) {
            CompoundTag overrides = stack.getTag().getCompound("Attributes");
            for (String key : overrides.getAllKeys()) {
                if (!overrides.contains(key, Tag.TAG_ANY_NUMERIC)) continue;
                AttributeInfo info = IDS.get(key);
                String name = info == null ? key : info.name();
                double value = overrides.getDouble(key);
                if (Double.isFinite(value) && Math.abs(value) <= 1000) {
                    if (info == null) result.put(name, value);
                    else result.merge(name, value, (base, upgrade) -> info.additive() ? base + upgrade : base * upgrade);
                }
            }
        }
        return Collections.unmodifiableMap(result);
    }
}
