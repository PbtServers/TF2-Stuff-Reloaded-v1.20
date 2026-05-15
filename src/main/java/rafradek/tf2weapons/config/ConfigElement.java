package rafradek.tf2weapons.config;

import java.util.ArrayList;
import java.util.List;

public class ConfigElement {
	private final ConfigCategory category;

	public ConfigElement(ConfigCategory category) {
		this.category = category;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public List getChildElements() {
		return new ArrayList(this.category.values());
	}
}
