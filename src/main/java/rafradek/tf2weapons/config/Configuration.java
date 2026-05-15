package rafradek.tf2weapons.config;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class Configuration {
	private final Map<String, ConfigCategory> categories = new LinkedHashMap<>();
	private boolean changed;

	public Configuration(File file) {
	}

	public ConfigCategory getCategory(String name) {
		return this.categories.computeIfAbsent(name, ConfigCategory::new);
	}

	public Set<String> getCategoryNames() {
		return this.categories.keySet();
	}

	public boolean hasKey(String category, String key) {
		return getCategory(category).containsKey(key);
	}

	public void moveProperty(String fromCategory, String key, String toCategory) {
		Property prop = getCategory(fromCategory).remove(key);
		if (prop != null) {
			getCategory(toCategory).put(key, prop);
			this.changed = true;
		}
	}

	public Property get(String category, String key, boolean defaultValue) {
		return get(category, key, String.valueOf(defaultValue), "");
	}

	public Property get(String category, String key, int defaultValue) {
		return get(category, key, String.valueOf(defaultValue), "");
	}

	public Property get(String category, String key, String defaultValue) {
		return get(category, key, defaultValue, "");
	}

	public Property get(String category, String key, boolean defaultValue, String comment) {
		return get(category, key, String.valueOf(defaultValue), comment);
	}

	public Property get(String category, String key, int defaultValue, String comment) {
		return get(category, key, String.valueOf(defaultValue), comment);
	}

	public Property get(String category, String key, String defaultValue, String comment) {
		Property.Type type = "true".equalsIgnoreCase(defaultValue) || "false".equalsIgnoreCase(defaultValue)
				? Property.Type.BOOLEAN
				: Property.Type.STRING;
		return getOrCreate(category, key, new Property(key, defaultValue, type).setComment(comment));
	}

	public Property get(String category, String key, String defaultValue, String comment, String[] validValues) {
		return get(category, key, defaultValue, comment).setValidValues(validValues);
	}

	public boolean getBoolean(String key, String category, boolean defaultValue, String comment) {
		return getOrCreate(category, key,
				new Property(key, String.valueOf(defaultValue), Property.Type.BOOLEAN).setComment(comment)).getBoolean();
	}

	public int getInt(String key, String category, int defaultValue, int min, int max, String comment) {
		int value = getOrCreate(category, key,
				new Property(key, String.valueOf(defaultValue), Property.Type.INTEGER).setComment(comment)).getInt();
		return Math.max(min, Math.min(max, value));
	}

	public float getFloat(String key, String category, float defaultValue, float min, float max, String comment) {
		float value = (float) getOrCreate(category, key,
				new Property(key, String.valueOf(defaultValue), Property.Type.DOUBLE).setComment(comment)).getDouble();
		return Math.max(min, Math.min(max, value));
	}

	public String[] getStringList(String key, String category, String[] defaultValue, String comment) {
		return getOrCreate(category, key, new Property(key, defaultValue, Property.Type.STRING).setComment(comment))
				.getStringList();
	}

	public boolean hasChanged() {
		return this.changed;
	}

	public void save() {
		this.changed = false;
	}

	private Property getOrCreate(String category, String key, Property property) {
		ConfigCategory cat = getCategory(category);
		if (!cat.containsKey(key)) {
			cat.put(key, property);
			this.changed = true;
		}
		return cat.get(key);
	}
}
