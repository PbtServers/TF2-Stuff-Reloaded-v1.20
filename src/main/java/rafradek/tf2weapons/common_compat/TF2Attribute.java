package rafradek.tf2weapons.common;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TF2Attribute {
	public static TF2Attribute[] attributes = new TF2Attribute[256];
	public static List<TF2Attribute> listUpgrades = new ArrayList<>();

	public int id;
	public String name;
	public Type typeOfValue;
	public String effect;
	public float defaultValue;
	public State state;
	public int numLevels;
	public float perLevel;
	public float perKill;
	public int cost;
	public int weight;
	public float austrUpgrade;

	private Predicate<ItemStack> canApply = Predicates.alwaysFalse();

	public static final Predicate<ItemStack> ITEM_WEAPON = stack -> true;
	public static final Predicate<ItemStack> NOT_FLAMETHROWER = stack -> true;
	public static final Predicate<ItemStack> FLAMETHROWER = stack -> true;
	public static final Predicate<ItemStack> IGNITE = stack -> true;
	public static final Predicate<ItemStack> WITH_CLIP = stack -> true;
	public static final Predicate<ItemStack> WITH_SPREAD = stack -> true;
	public static final Predicate<ItemStack> WITH_AMMO = stack -> true;
	public static final Predicate<ItemStack> CHARGE_RATE = stack -> true;
	public static final Predicate<ItemStack> DURATION = stack -> true;
	public static final Predicate<ItemStack> ITEM_BULLET = stack -> true;
	public static final Predicate<ItemStack> ITEM_PROJECTILE = stack -> true;
	public static final Predicate<ItemStack> ITEM_MINIGUN = stack -> true;
	public static final Predicate<ItemStack> ITEM_SNIPER_RIFLE = stack -> true;
	public static final Predicate<ItemStack> EXPLOSIVE = stack -> true;
	public static final Predicate<ItemStack> MEDIGUN = stack -> true;
	public static final Predicate<ItemStack> BANNER = stack -> true;
	public static final Predicate<ItemStack> BACKPACK = stack -> true;
	public static final Predicate<ItemStack> SHIELD = stack -> true;
	public static final Predicate<ItemStack> WATCH = stack -> true;
	public static final Predicate<ItemStack> WRENCH = stack -> true;
	public static final Predicate<ItemStack> JETPACK = stack -> true;
	public static final Predicate<ItemStack> PDA = stack -> true;
	public static final Predicate<ItemStack> JUMPER = stack -> true;
	public static final Predicate<ItemStack> ROCKET = stack -> true;
	public static final Predicate<ItemStack> GRENADE = stack -> true;
	public static final Predicate<ItemStack> KNIFE = stack -> true;

	public enum Type {
		PERCENTAGE, INVERTED_PERCENTAGE, ADDITIVE
	}

	public enum State {
		POSITIVE, NEGATIVE, NEUTRAL, HIDDEN
	}

	public TF2Attribute(int id, String name, String effect, Type typeOfValue, float defaultValue, State state) {
		this.id = id;
		this.name = name;
		this.effect = effect;
		this.typeOfValue = typeOfValue;
		this.defaultValue = defaultValue;
		this.state = state;
		if (id >= 0 && id < attributes.length) {
			attributes[id] = this;
		}
		if (MapList.nameToAttribute != null) {
			MapList.nameToAttribute.put(name, this);
		}
	}

	public TF2Attribute setUpgrade(Predicate<ItemStack> canApply, float perLevel, int numLevels, int cost, int weight) {
		this.canApply = canApply;
		this.perLevel = perLevel;
		this.numLevels = numLevels;
		this.cost = cost;
		this.weight = weight;
		if (!listUpgrades.contains(this)) {
			listUpgrades.add(this);
		}
		return this;
	}

	public TF2Attribute setKillstreak(float perKill) {
		this.perKill = perKill;
		return this;
	}

	public TF2Attribute setAustralium(float austrUpgrade) {
		this.austrUpgrade = austrUpgrade;
		return this;
	}

	public TF2Attribute setNoCostReduce() {
		return this;
	}

	public static void initAttributes() {
		for (int i = 0; i < attributes.length; i++) {
			if (attributes[i] == null) {
				new TF2Attribute(i, "Attribute" + i, "Attribute " + i, Type.ADDITIVE, 0, State.NEUTRAL);
			}
		}
	}

	public static float addValue(float value, CompoundTag tag, String key) {
		return value;
	}

	public static float getModifier(String attribute, ItemStack stack, float initial, LivingEntity living) {
		return initial;
	}

	public static float getModifierGlobal(String attribute, float initial, LivingEntity living) {
		return initial;
	}

	public static List<TF2Attribute> getAllPassibleAttributesForUpgradeStation() {
		return listUpgrades;
	}

	public static void setAttribute(ItemStack stack, TF2Attribute attribute, float value) {
	}

	public static void upgradeItemStack(ItemStack stack, int money, Random random) {
	}

	public static int getMaxExperience(ItemStack stack, Player player) {
		return 0;
	}

	public String getTranslatedString(float value, boolean invert) {
		return this.effect + ": " + value;
	}

	public float getPerLevel(ItemStack stack) {
		return this.perLevel;
	}

	public int calculateCurrLevel(ItemStack stack) {
		return 0;
	}

	public TF2Attribute getAttributeReplacement(ItemStack stack) {
		return this;
	}

	public int getUpgradeCost(ItemStack stack) {
		return this.cost;
	}

	public boolean canApply(ItemStack stack) {
		return this.canApply.apply(stack);
	}

	@Override
	public String toString() {
		return this.name;
	}
}
