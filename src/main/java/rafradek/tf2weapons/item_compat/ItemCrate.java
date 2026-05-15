package rafradek.tf2weapons.item;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import rafradek.tf2weapons.util.PropertyType;
import rafradek.tf2weapons.util.WeaponData;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;

public class ItemCrate extends ItemFromData {
	public static class PropertyContent extends PropertyType<CrateContent> {
		public PropertyContent(int id, String name, Class<CrateContent> type) {
			super(id, name, type);
		}

		@Override
		public CrateContent deserialize(JsonElement json, java.lang.reflect.Type typeOfT,
				JsonDeserializationContext context) throws JsonParseException {
			CrateContent content = new CrateContent();
			if (json != null && json.isJsonObject()) {
				for (Entry<String, JsonElement> entry : json.getAsJsonObject().entrySet()) {
					int value = entry.getValue().getAsInt();
					content.content.put(entry.getKey(), value);
					content.maxCrateValue += value;
				}
			}
			return content;
		}

		@Override
		public void serialize(DataOutput buf, WeaponData data, CrateContent value) throws IOException {
			buf.writeByte(value == null ? 0 : value.content.size());
			if (value != null) {
				for (Entry<String, Integer> entry : value.content.entrySet()) {
					buf.writeUTF(entry.getKey());
					buf.writeInt(entry.getValue());
				}
			}
		}

		@Override
		public CrateContent deserialize(DataInput buf, WeaponData data) throws IOException {
			CrateContent content = new CrateContent();
			int count = buf.readUnsignedByte();
			for (int i = 0; i < count; i++) {
				String name = buf.readUTF();
				int value = buf.readInt();
				content.content.put(name, value);
				content.maxCrateValue += value;
			}
			return content;
		}
	}

	public static class CrateContent {
		public HashMap<String, Integer> content = new HashMap<>();
		public int maxCrateValue;
	}
}
