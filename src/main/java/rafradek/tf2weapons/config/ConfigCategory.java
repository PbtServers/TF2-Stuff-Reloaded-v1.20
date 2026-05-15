package rafradek.tf2weapons.config;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class ConfigCategory extends LinkedHashMap<String, Property> {
	private static final long serialVersionUID = 1L;

	private final String name;
	private final List<ConfigCategory> children = new ArrayList<>();

	public ConfigCategory(String name) {
		this.name = name;
	}

	public String getName() {
		return this.name;
	}

	public List<ConfigCategory> getChildren() {
		return this.children;
	}
}
