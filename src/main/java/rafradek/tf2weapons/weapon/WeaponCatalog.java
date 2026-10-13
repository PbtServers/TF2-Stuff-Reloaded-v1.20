package rafradek.tf2weapons.weapon;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** Bundled common-side catalog: loaded before DeferredRegister suppliers are created. */
public final class WeaponCatalog {
    private static final String PATH = "/assets/rafradek_tf2_weapons/weapons/weapons.json";
    private static final WeaponCatalog INSTANCE = loadBundled();
    private final Map<String, WeaponDefinition> weapons;

    private WeaponCatalog(Map<String, WeaponDefinition> weapons) {
        this.weapons = Collections.unmodifiableMap(weapons);
    }
    public static WeaponCatalog get() { return INSTANCE; }
    public Collection<WeaponDefinition> all() { return weapons.values(); }
    public WeaponDefinition find(String type) {
        if (type.startsWith("rafradek_tf2_weapons:")) type = type.substring("rafradek_tf2_weapons:".length());
        return weapons.get(type);
    }
    public WeaponDefinition require(String id) {
        WeaponDefinition result = find(id);
        if (result == null) throw new IllegalArgumentException("Unknown weapon: " + id);
        return result;
    }
    private static WeaponCatalog loadBundled() {
        InputStream stream = WeaponCatalog.class.getResourceAsStream(PATH);
        if (stream == null) throw new IllegalStateException("Missing weapon catalog " + PATH);
        try (Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            return parse(reader);
        } catch (IOException error) { throw new IllegalStateException("Cannot load weapon catalog", error); }
    }

    public static WeaponCatalog parse(Reader input) throws IOException {
        JsonReader reader = new JsonReader(input);
        reader.setLenient(false);
        Map<String, JsonObject> raw = new TreeMap<>();
        reader.beginObject();
        while (reader.hasNext()) {
            String id = reader.nextName();
            if (!id.matches("[a-z0-9_./-]+")) throw new IllegalArgumentException("Invalid weapon id " + id);
            JsonObject value = JsonParser.parseReader(reader).getAsJsonObject();
            if (raw.putIfAbsent(id, value) != null) throw new IllegalArgumentException("Duplicate weapon id " + id);
        }
        reader.endObject();
        if (reader.peek() != com.google.gson.stream.JsonToken.END_DOCUMENT) throw new IllegalArgumentException("Trailing catalog data");
        Map<String, JsonObject> resolved = new TreeMap<>();
        Map<String, WeaponDefinition> definitions = new LinkedHashMap<>();
        for (String id : raw.keySet()) {
            JsonObject data = resolve(id, raw, resolved, new HashSet<>());
            if (!data.has("Class")) throw new IllegalArgumentException(id + ": missing Class");
            for (String key : List.of("Damage", "Firing speed", "Spread", "Clip size", "Max ammo", "Pellets", "Reload time", "Reload time first")) {
                if (data.has(key) && (!Double.isFinite(data.get(key).getAsDouble()) || data.get(key).getAsDouble() < 0))
                    throw new IllegalArgumentException(id + ": invalid " + key);
            }
            if (data.has("Render") && !data.get("Render").getAsString().matches("[a-z0-9_.-]+:[a-z0-9_./-]+"))
                throw new IllegalArgumentException(id + ": invalid Render");
            definitions.put(id, new WeaponDefinition(id, data));
        }
        return new WeaponCatalog(definitions);
    }

    private static JsonObject resolve(String id, Map<String, JsonObject> raw, Map<String, JsonObject> resolved, Set<String> visiting) {
        if (resolved.containsKey(id)) return resolved.get(id);
        if (!visiting.add(id)) throw new IllegalArgumentException("Weapon inheritance cycle at " + id);
        JsonObject source = raw.get(id);
        if (source == null) throw new IllegalArgumentException("Missing parent weapon " + id);
        JsonObject result = source.has("Based on")
                ? resolve(source.get("Based on").getAsString(), raw, resolved, visiting).deepCopy() : new JsonObject();
        source.entrySet().forEach(e -> {
            if (e.getKey().equals("Attributes") && result.has("Attributes")) {
                JsonObject attributes = result.getAsJsonObject("Attributes").deepCopy();
                e.getValue().getAsJsonObject().entrySet().forEach(a -> attributes.add(a.getKey(), a.getValue().deepCopy()));
                result.add("Attributes", attributes);
            } else result.add(e.getKey(), e.getValue().deepCopy());
        });
        visiting.remove(id);
        resolved.put(id, result);
        return result;
    }
}
