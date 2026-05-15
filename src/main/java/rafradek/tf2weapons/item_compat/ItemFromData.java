package rafradek.tf2weapons.item;

import com.google.common.base.Predicate;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.Tesselator;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import org.jetbrains.annotations.Nullable;
import rafradek.tf2weapons.TF2weapons;
import rafradek.tf2weapons.client.compat.ItemMeshDefinition;
import rafradek.tf2weapons.common.MapList;
import rafradek.tf2weapons.common.TF2Attribute;
import rafradek.tf2weapons.common.WeaponsCapability.RageType;
import rafradek.tf2weapons.entity.mercenary.EntityTF2Character;
import rafradek.tf2weapons.util.PropertyType;
import rafradek.tf2weapons.util.TF2Class;
import rafradek.tf2weapons.util.WeaponData;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;

public class ItemFromData extends Item implements IItemOverlay {
	public static class PropertyAttribute extends PropertyType<AttributeProvider> {
		public PropertyAttribute(int id, String name, Class<AttributeProvider> type) {
			super(id, name, type);
		}

		@Override
		public AttributeProvider deserialize(JsonElement json, java.lang.reflect.Type typeOfT,
				JsonDeserializationContext context) throws JsonParseException {
			Map<TF2Attribute, Float> attributes = new HashMap<>();
			if (json != null && json.isJsonObject()) {
				for (Entry<String, JsonElement> entry : json.getAsJsonObject().entrySet()) {
					TF2Attribute attribute = MapList.nameToAttribute == null ? null : MapList.nameToAttribute.get(entry.getKey());
					if (attribute == null) {
						try {
							int id = Integer.parseInt(entry.getKey());
							if (id >= 0 && id < TF2Attribute.attributes.length) {
								attribute = TF2Attribute.attributes[id];
							}
						}
						catch (NumberFormatException ignored) {
						}
					}
					if (attribute != null) {
						attributes.put(attribute, entry.getValue().getAsFloat());
					}
				}
			}
			return new AttributeProvider(attributes);
		}

		@Override
		public void serialize(DataOutput buf, WeaponData data, AttributeProvider value) throws IOException {
			buf.writeByte(value == null ? 0 : value.attributes.size());
			if (value != null) {
				for (Entry<TF2Attribute, Float> attribute : value.attributes.entrySet()) {
					buf.writeByte(attribute.getKey().id);
					buf.writeFloat(attribute.getValue());
				}
			}
		}

		@Override
		public AttributeProvider deserialize(DataInput buf, WeaponData data) throws IOException {
			Map<TF2Attribute, Float> attributes = new HashMap<>();
			int count = buf.readUnsignedByte();
			for (int i = 0; i < count; i++) {
				int id = buf.readUnsignedByte();
				float value = buf.readFloat();
				if (id < TF2Attribute.attributes.length && TF2Attribute.attributes[id] != null) {
					attributes.put(TF2Attribute.attributes[id], value);
				}
			}
			return new AttributeProvider(attributes);
		}
	}

	public static class AttributeProvider {
		public Map<TF2Attribute, Float> attributes;

		public AttributeProvider(Map<TF2Attribute, Float> attributes) {
			this.attributes = attributes;
		}
	}

	public static final WeaponData BLANK_DATA = new WeaponData("toloadfiles");
	public static final Predicate<WeaponData> VISIBLE_WEAPON = data -> true;

	public ItemFromData() {
		super(new Item.Properties().stacksTo(1));
	}

	public void onUpdate(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
	}

	public static WeaponData getData(ItemStack stack) {
		return BLANK_DATA;
	}

	public static ItemStack getNewStack(String type) {
		return new ItemStack(TF2weapons.itemTF2 == null ? Items.AIR : TF2weapons.itemTF2);
	}

	public static ItemStack getNewStack(WeaponData type) {
		return getNewStack(type == null ? "" : type.getName());
	}

	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
		return new WeaponData.WeaponDataCapability();
	}

	public static List<ItemStack> getRandomWeapons(Random random, Predicate<WeaponData> predicate, int count) {
		List<ItemStack> stacks = new ArrayList<>();
		for (int i = 0; i < count; i++) {
			stacks.add(getNewStack(BLANK_DATA));
		}
		return stacks;
	}

	public static ItemStack getRandomWeapon(Random random, Predicate<WeaponData> predicate) {
		return getNewStack(BLANK_DATA);
	}

	public static ItemStack getRandomWeaponOfType(String type, float chanceOfParent, Random random) {
		return getNewStack(type);
	}

	public static ItemStack getRandomWeaponOfType(String name, Random random, boolean showHidden) {
		return getNewStack(name);
	}

	public static ItemStack getRandomWeaponOfSlotMob(TF2Class clazz, int slot, Random random, boolean showHidden,
			float stockWeight, boolean stockOnly) {
		return getNewStack(BLANK_DATA);
	}

	public static List<ItemStack> getRandomWeaponsOfSlotMob(TF2Class clazz, int slot, Random random,
			boolean showHidden, int count) {
		return getRandomWeapons(random, VISIBLE_WEAPON, count);
	}

	public static List<ItemStack> getRandomWeapons(ItemStack stack) {
		return new ArrayList<>();
	}

	public static ItemStack getRandomWeapon(ItemStack stack, Random random) {
		return getNewStack(BLANK_DATA);
	}

	public static ItemStack getDisplayWeapon(ItemStack stack, long ticks) {
		return stack;
	}

	public static int getWeaponCount(Predicate<WeaponData> predicate) {
		return 0;
	}

	public static boolean isSameType(ItemStack stack, String name) {
		return false;
	}

	public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
		return slotChanged;
	}

	public String getItemStackDisplayName(ItemStack stack) {
		return getTranslatedName(stack);
	}

	public static SoundEvent getSound(ItemStack stack, PropertyType<String> name) {
		return null;
	}

	public static int getSlotForClass(WeaponData data, TF2Class clazz) {
		return -1;
	}

	public static int getSlotForClass(WeaponData data, EntityTF2Character entity) {
		return entity == null ? -1 : getSlotForClass(data, entity.getTF2Class());
	}

	public static boolean isItemOfClassSlot(WeaponData data, int slot, TF2Class clazz) {
		return true;
	}

	public static boolean isItemOfClass(WeaponData data, TF2Class clazz) {
		return true;
	}

	public void addInformation(ItemStack stack, Level level, List<String> tooltip, TooltipFlag advanced) {
	}

	public boolean hasKillstreak(ItemStack stack, int minLevel) {
		return false;
	}

	public int getEntityLifespan(ItemStack itemStack, Level level) {
		return 6000;
	}

	@Override
	public boolean showInfoBox(ItemStack stack, Player player) {
		return false;
	}

	@Override
	public String[] getInfoBoxLines(ItemStack stack, Player player) {
		return new String[0];
	}

	@Override
	public void drawOverlay(ItemStack stack, Player player, Tesselator tesselator, BufferBuilder buffer,
			Window resolution) {
	}

	public boolean showDurabilityBar(ItemStack stack) {
		return false;
	}

	public double getDurabilityForDisplay(ItemStack stack) {
		return 0;
	}

	public RageType getRageType(ItemStack stack, LivingEntity living) {
		return RageType.NONE;
	}

	public float getMaxRage(ItemStack stack, LivingEntity living) {
		return 0;
	}

	public float getRage(ItemStack stack, LivingEntity living) {
		return 0;
	}

	public void setRage(ItemStack stack, LivingEntity living, float value) {
	}

	public void addRage(ItemStack stack, LivingEntity living, float value) {
	}

	public boolean isAmmoSufficient(ItemStack stack, LivingEntity living, boolean all) {
		return true;
	}

	public void consumeAmmoGlobal(LivingEntity living, ItemStack stack, int amount) {
	}

	public ItemStack searchForAmmo(LivingEntity owner, ItemStack stack) {
		return ItemStack.EMPTY;
	}

	public int getAmmoType(ItemStack stack) {
		return 0;
	}

	public static int getAmmoAmountType(Player owner, int type) {
		return 0;
	}

	public int getAmmoAmount(LivingEntity owner, ItemStack stack) {
		return 0;
	}

	public int getActualAmmoUse(ItemStack stack, LivingEntity living, int amount) {
		return amount;
	}

	public int getVisibilityFlags(ItemStack stack, LivingEntity living) {
		return 0;
	}

	public String getTranslatedName(ItemStack stack) {
		return getData(stack).getString(PropertyType.NAME);
	}

	public boolean canSwitchTo(ItemStack stack) {
		return true;
	}

	public ItemMeshDefinition getMeshDefinition() {
		return null;
	}

	public void registerModels(WeaponData weapon) {
	}
}
