package rafradek.tf2weapons;

import java.util.ArrayList;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Callable;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.stats.Stat;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.Fluid;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import rafradek.tf2weapons.arena.GameArena;
import rafradek.tf2weapons.common.CommonProxy;
import rafradek.tf2weapons.common.WeaponsCapability;
import rafradek.tf2weapons.config.Configuration;
import rafradek.tf2weapons.entity.EntityDummy;
import rafradek.tf2weapons.inventory.InventoryAmmoBelt;
import rafradek.tf2weapons.inventory.InventoryWearables;
import rafradek.tf2weapons.item.Potion;
import rafradek.tf2weapons.message.TF2NetworkWrapper;
import rafradek.tf2weapons.message.udp.TF2UdpServer;
import rafradek.tf2weapons.util.WeaponData;

public class TF2weapons {
	public static final Logger LOGGER = LogManager.getLogger("TF2 Stuff Mod");
	public static final String MOD_ID = "rafradek_tf2_weapons";

	public static final Capability<WeaponsCapability> WEAPONS_CAP = null;
	public static final Capability<InventoryWearables> INVENTORY_CAP = null;
	public static final Capability<InventoryAmmoBelt> INVENTORY_BELT_CAP = null;
	public static final Capability<TF2EventsCommon.TF2WorldStorage> WORLD_CAP = null;
	public static final Capability<WeaponData.WeaponDataCapability> WEAPONS_DATA_CAP = null;
	public static final Capability<TF2PlayerCapability> PLAYER_CAP = null;

	public static TF2UdpServer udpServer;
	public static Configuration conf;
	public static TF2NetworkWrapper network;
	public static TF2weapons instance = new TF2weapons();
	public static CommonProxy proxy = new CommonProxy();
	public static MinecraftServer server;
	public static EntityDummy dummyEnt;
	public static byte[] itemDataCompressed;
	public static boolean squakeLoaded;
	public static boolean corrupted;
	public static boolean generateCopper;
	public static boolean generateLead;
	public static boolean generateAustralium;
	public static Fluid refinedFuel;
	public static Map<String, GameArena> gameArenas;
	public static ArrayList<ResourceLocation> animalsDisguise = new ArrayList<>();

	public static CreativeModeTab tabutilitytf2;
	public static CreativeModeTab tabweapontf2;
	public static CreativeModeTab tabsurvivaltf2;
	public static CreativeModeTab tabspawnertf2;
	public static CreativeModeTab tabrareweapontf2;
	public static CreativeModeTab tabarenatf2;

	public static Block blockCabinet;
	public static Block blockCopperOre;
	public static Block blockProp;
	public static Block blockLeadOre;
	public static Block blockAustraliumOre;
	public static Block blockAustralium;
	public static Block blockUpgradeStation;
	public static Block blockAmmoFurnace;
	public static Block blockOverheadDoor;
	public static Block blockRobotDeploy;
	public static Block blockResupplyCabinet;
	public static Block blockCapturePoint;
	public static Block blockConfigure;

	public static Potion bonk;
	public static Potion stun;
	public static Potion crit;
	public static Potion buffbanner;
	public static Potion backup;
	public static Potion conch;
	public static Potion markDeath;
	public static Potion jarate;
	public static Potion madmilk;
	public static Potion critBoost;
	public static Potion charging;
	public static Potion uber;
	public static Potion it;
	public static Potion bombmrs;
	public static Potion bleeding;
	public static Potion noKnockback;
	public static Potion sapped;
	public static Potion regen;
	public static Potion viralfire;
	public static Potion shieldExplosive;
	public static Potion shieldBullet;
	public static Potion shieldFire;
	public static Potion gas;
	public static Potion quickFix;

	public static Item itemPlacer;
	public static Item mobHeldItem;
	public static Item itemDisguiseKit;
	public static Item itemBuildingBox;
	public static Item itemSandvich;
	public static Item itemChocolate;
	public static Item itemAmmo;
	public static Item itemAmmoFire;
	public static Item itemAmmoPistol;
	public static Item itemAmmoMinigun;
	public static Item itemAmmoSMG;
	public static Item itemAmmoSyringe;
	public static Item itemAmmoPackage;
	public static Item itemAmmoMedigun;
	public static Item itemAmmoBelt;
	public static Item itemScoutBoots;
	public static Item itemMantreads;
	public static Item itemTF2;
	public static Item itemHorn;
	public static Item itemStatue;
	public static Item itemToken;
	public static Item itemTarget;
	public static Item itemPDA;
	public static Item itemKillstreak;
	public static Item itemGunboats;
	public static Item itemStrangifier;
	public static Item itemKillstreakFabricator;
	public static Item itemEventMaker;
	public static Item itemRobotPart;
	public static Item itemBossSpawn;
	public static Item itemDoorController;
	public static Item itemMoney;
	public static Item itemConfigurator;
	public static Item itemPickup;

	public static ResourceLocation lootTF2Character;
	public static ResourceLocation lootScout;
	public static ResourceLocation lootSpy;
	public static ResourceLocation lootHeavy;
	public static ResourceLocation lootEngineer;
	public static ResourceLocation lootMedic;
	public static ResourceLocation lootPyro;
	public static ResourceLocation lootSoldier;
	public static ResourceLocation lootDemoman;
	public static ResourceLocation lootSniper;
	public static ResourceLocation lootHale;
	public static ResourceLocation lootTF2Base;

	public static Stat<?> cratesOpened;
	public static Stat<?> robotsKilled;
	public static BannerPattern redPattern;
	public static BannerPattern bluPattern;
	public static BannerPattern neutralPattern;
	public static BannerPattern fastSpawn;

	public static int getCurrentWeaponVersion() {
		return 50;
	}

	public static void loadWeapons() {}

	public static void loadWeapon(String name, WeaponData weapon) {}

	public static boolean isEnemy(LivingEntity attacker, LivingEntity target) {
		return attacker != target;
	}

	public static boolean canHit(Entity attacker, Entity target) {
		return attacker != target;
	}

	public static float calculateDamage(Entity attacker, Entity target, DamageSource source, float damage) {
		return damage;
	}

	public static int getMetal(LivingEntity living) {
		return 0;
	}

	public static void medigunLock(LivingEntity living, boolean lock) {}

	public static double[] radiusRandom2D(Random random, double radius) {
		return new double[] { 0, 0 };
	}

	public static double[] radiusRandom3D(Random random, double radius) {
		return new double[] { 0, 0, 0 };
	}

	public static void sendTracking(Object message, Entity entity) {}

	public static void syncConfig(boolean server) {}

	public static class NullCallable<T> implements Callable<T> {
		private final T value;

		public NullCallable(T value) {
			this.value = value;
		}

		@Override
		public T call() {
			return value;
		}
	}
}
