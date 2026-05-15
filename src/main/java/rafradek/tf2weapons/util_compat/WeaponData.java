package rafradek.tf2weapons.util;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonParseException;

import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import rafradek.tf2weapons.common.MapList;
import rafradek.tf2weapons.item.ItemFromData;

public class WeaponData implements ICapabilityProvider {
	public static PropertyType<?>[] propertyTypes = new PropertyType[256];
	public static Map<String, JsonDeserializer<ICapabilityProvider>> propertyDeserializers = new HashMap<>();

	public HashMap<PropertyType<?>, Object> properties = new HashMap<>();
	public int maxCrateValue;
	private String name = "";

	public WeaponData() {}

	public WeaponData(String name) {
		this.name = name;
	}

	public void addCapabilities(Map<ResourceLocation, ICapabilityProvider> map) {}

	public int getInt(PropertyType<Integer> propType) {
		return get(propType, propType.getDefaultValue());
	}

	public String getString(PropertyType<String> propType) {
		return get(propType, propType.getDefaultValue());
	}

	public boolean getBoolean(PropertyType<Boolean> propType) {
		return get(propType, propType.getDefaultValue());
	}

	public float getFloat(PropertyType<Float> propType) {
		return get(propType, propType.getDefaultValue());
	}

	public <A> A get(PropertyType<A> propType) {
		return get(propType, propType.getDefaultValue());
	}

	@SuppressWarnings("unchecked")
	public <A> A get(PropertyType<A> propType, A def) {
		Object value = properties.get(propType);
		return value == null ? def : (A) value;
	}

	public boolean hasProperty(PropertyType<?> property) {
		return properties.containsKey(property);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void addProperty(String name, JsonElement element, JsonDeserializationContext context) {
		PropertyType type = MapList.propertyTypes == null ? null : MapList.propertyTypes.get(name);
		if (type != null) {
			Object value = type.deserialize(element, null, context);
			if (value != null) {
				this.properties.put(type, value);
				if (value instanceof rafradek.tf2weapons.item.ItemCrate.CrateContent content) {
					this.maxCrateValue = content.maxCrateValue;
				}
			}
		}
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public static ArrayList<WeaponData> parseFile(String fileData, String filename) {
		ArrayList<WeaponData> weapons = new ArrayList<>();
		JsonElement root = JsonParser.parseString(fileData);
		if (!root.isJsonObject()) {
			return weapons;
		}
		for (Entry<String, JsonElement> entry : root.getAsJsonObject().entrySet()) {
			if (!entry.getValue().isJsonObject()) {
				continue;
			}
			WeaponData data = new WeaponData(entry.getKey());
			JsonObject object = entry.getValue().getAsJsonObject();
			for (Entry<String, JsonElement> property : object.entrySet()) {
				data.addProperty(property.getKey(), property.getValue(), null);
			}
			weapons.add(data);
		}
		return weapons;
	}

	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> capability, Direction facing) {
		return LazyOptional.empty();
	}

	public static class Serializer implements JsonDeserializer<WeaponData> {
		@Override
		public WeaponData deserialize(JsonElement json, java.lang.reflect.Type typeOfT,
				JsonDeserializationContext context) throws JsonParseException {
			return new WeaponData();
		}
	}

	public static abstract class SpecialProperty implements ICapabilityProvider {
		public abstract void serialize(DataOutput buf, WeaponData data) throws IOException;

		public abstract void deserialize(DataInput buf, WeaponData data) throws IOException;

		@Override
		public <T> LazyOptional<T> getCapability(Capability<T> capability, Direction facing) {
			return LazyOptional.empty();
		}
	}

	public static class WeaponDataCapability implements ICapabilityProvider {
		public WeaponData inst = ItemFromData.BLANK_DATA;
		public HashMap<String, Float> cachedAttrMult = new HashMap<>();
		public HashMap<String, Float> cachedAttrAdd = new HashMap<>();
		public boolean cached;
		public int active;
		public int usedClass = -1;
		public int fire1Cool;
		public int fire2Cool;
		public int clip;

		public float getAttributeValue(ItemStack stack, String nameattr, float initial) {
			return initial;
		}

		@Override
		@SuppressWarnings("unchecked")
		public <T> LazyOptional<T> getCapability(Capability<T> capability, Direction facing) {
			return LazyOptional.of(() -> (T) this);
		}
	}

	public static WeaponDataCapability getCapability(ItemStack stack) {
		return new WeaponDataCapability();
	}
}
