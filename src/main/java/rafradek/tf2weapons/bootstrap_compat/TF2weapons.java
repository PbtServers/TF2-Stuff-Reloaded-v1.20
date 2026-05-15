package rafradek.tf2weapons;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.function.Supplier;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.stats.Stat;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import rafradek.tf2weapons.arena.GameArena;
import rafradek.tf2weapons.common.CommonProxy;
import rafradek.tf2weapons.common.MapList;
import rafradek.tf2weapons.common.TF2Attribute;
import rafradek.tf2weapons.common.WeaponsCapability;
import rafradek.tf2weapons.config.Configuration;
import rafradek.tf2weapons.entity.EntityDummy;
import rafradek.tf2weapons.inventory.InventoryAmmoBelt;
import rafradek.tf2weapons.inventory.InventoryWearables;
import rafradek.tf2weapons.item.*;
import rafradek.tf2weapons.entity.projectile.*;
import rafradek.tf2weapons.message.TF2NetworkWrapper;
import rafradek.tf2weapons.message.udp.TF2UdpServer;
import rafradek.tf2weapons.util.PropertyType;
import rafradek.tf2weapons.util.WeaponData;

@Mod(TF2weapons.MOD_ID)
public class TF2weapons {
	public static final Logger LOGGER = LogManager.getLogger("TF2 Stuff Mod");
	public static final String MOD_ID = "rafradek_tf2_weapons";

	public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MOD_ID);
	public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MOD_ID);
	public static final DeferredRegister<MobEffect> MOB_EFFECTS = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, MOD_ID);
	public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
			DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MOD_ID);

	public static final RegistryObject<Block> PLACEHOLDER_BLOCK = BLOCKS.register("placeholder_block",
			() -> new Block(BlockBehaviour.Properties.of().strength(1.5F)));
	public static final RegistryObject<Item> PLACEHOLDER_BLOCK_ITEM = ITEMS.register("placeholder_block",
			() -> new BlockItem(PLACEHOLDER_BLOCK.get(), new Item.Properties()));
	public static final RegistryObject<Item> PLACEHOLDER_ITEM = ITEMS.register("placeholder_item",
			() -> new Item(new Item.Properties()));
	public static final RegistryObject<Item> ITEM_TF2 = ITEMS.register("tf2_material", ItemTF2::new);
	public static final RegistryObject<Item> ITEM_MONEY = ITEMS.register("money", ItemMoney::new);
	public static final RegistryObject<Item> ITEM_AMMO = ITEMS.register("ammo", ItemAmmo::new);
	public static final RegistryObject<Item> ITEM_AMMO_FIRE = ITEMS.register("ammo_fire", ItemFireAmmo::new);
	public static final RegistryObject<Item> ITEM_DISGUISE_KIT = ITEMS.register("disguise_kit", ItemDisguiseKit::new);
	public static final RegistryObject<Item> ITEM_PICKUP = ITEMS.register("pickup", ItemPickup::new);
	public static final RegistryObject<Item> WEAPON_SNIPER_RIFLE = registerItem("weapon_class_sniperrifle", ItemSniperRifle::new);
	public static final RegistryObject<Item> WEAPON_BULLET = registerItem("weapon_class_bullet", ItemBulletWeapon::new);
	public static final RegistryObject<Item> WEAPON_MINIGUN = registerItem("weapon_class_minigun", ItemMinigun::new);
	public static final RegistryObject<Item> WEAPON_PROJECTILE = registerItem("weapon_class_projectile", ItemProjectileWeapon::new);
	public static final RegistryObject<Item> WEAPON_STICKY_BOMB = registerItem("weapon_class_stickybomb", ItemStickyLauncher::new);
	public static final RegistryObject<Item> WEAPON_FLAMETHROWER = registerItem("weapon_class_flamethrower", ItemFlameThrower::new);
	public static final RegistryObject<Item> WEAPON_KNIFE = registerItem("weapon_class_knife", ItemKnife::new);
	public static final RegistryObject<Item> WEAPON_MEDIGUN = registerItem("weapon_class_medigun", ItemMedigun::new);
	public static final RegistryObject<Item> WEAPON_CLOAK = registerItem("weapon_class_cloak", ItemCloak::new);
	public static final RegistryObject<Item> WEAPON_WRENCH = registerItem("weapon_class_wrench", ItemWrench::new);
	public static final RegistryObject<Item> WEAPON_BONK = registerItem("weapon_class_bonk", ItemBonk::new);
	public static final RegistryObject<Item> WEAPON_COSMETIC = registerItem("weapon_class_cosmetic", ItemWearable::new);
	public static final RegistryObject<Item> WEAPON_MELEE = registerItem("weapon_class_melee", ItemMeleeWeapon::new);
	public static final RegistryObject<Item> WEAPON_SAPPER = registerItem("weapon_class_sapper", ItemSapper::new);
	public static final RegistryObject<Item> WEAPON_BACKPACK = registerItem("weapon_class_backpack", ItemSoldierBackpack::new);
	public static final RegistryObject<Item> WEAPON_CRATE = registerItem("weapon_class_crate", ItemCrate::new);
	public static final RegistryObject<Item> WEAPON_JAR = registerItem("weapon_class_jar", ItemJar::new);
	public static final RegistryObject<Item> WEAPON_WRANGLER = registerItem("weapon_class_wrangler", ItemWrangler::new);
	public static final RegistryObject<Item> WEAPON_SHIELD = registerItem("weapon_class_shield", ItemChargingTarge::new);
	public static final RegistryObject<Item> WEAPON_CLEAVER = registerItem("weapon_class_cleaver", ItemCleaver::new);
	public static final RegistryObject<Item> WEAPON_PARACHUTE = registerItem("weapon_class_parachute", ItemParachute::new);
	public static final RegistryObject<Item> WEAPON_HUNTSMAN = registerItem("weapon_class_huntsman", ItemHuntsman::new);
	public static final RegistryObject<Item> WEAPON_JETPACK = registerItem("weapon_class_jetpack", ItemJetpack::new);
	public static final RegistryObject<Item> WEAPON_JETPACK_TRIGGER = registerItem("weapon_class_jetpacktrigger", ItemJetpackTrigger::new);
	public static final RegistryObject<Item> WEAPON_PDA = registerItem("weapon_class_pda", ItemPDA::new);
	public static final RegistryObject<Item> WEAPON_GAS = registerItem("weapon_class_gas", ItemGas::new);
	public static final RegistryObject<Item> WEAPON_SHORT_CIRCUIT = registerItem("weapon_class_shortcircuit", ItemBulletWeapon::new);
	public static final RegistryObject<Item> WEAPON_AIRBLAST = registerItem("weapon_class_airblast", ItemAirblast::new);
	public static final RegistryObject<Item> WEAPON_BACKPACK_GENERIC = registerItem("weapon_class_backpackgeneric", ItemBackpack::new);
	public static final RegistryObject<Item> WEAPON_GRAPPLING_HOOK = registerItem("weapon_class_grapplinghook", ItemGrapplingHook::new);

	public static final RegistryObject<Potion> BONK = registerEffect("bonk", 0x6dd6ff);
	public static final RegistryObject<Potion> STUN = registerEffect("stun", 0xffd34d);
	public static final RegistryObject<Potion> CRIT = registerEffect("crit", 0xff3f3f);
	public static final RegistryObject<Potion> BUFF_BANNER = registerEffect("buff_banner", 0xf0c43c);
	public static final RegistryObject<Potion> BACKUP = registerEffect("backup", 0x4d8cff);
	public static final RegistryObject<Potion> CONCH = registerEffect("conch", 0x7ae36d);
	public static final RegistryObject<Potion> MARK_DEATH = registerEffect("mark_death", 0x6b1414);
	public static final RegistryObject<Potion> JARATE = registerEffect("jarate", 0xf2dd57);
	public static final RegistryObject<Potion> MAD_MILK = registerEffect("mad_milk", 0xf7f7df);
	public static final RegistryObject<Potion> CRIT_BOOST = registerEffect("crit_boost", 0xff5555);
	public static final RegistryObject<Potion> CHARGING = registerEffect("charging", 0xb5b5b5);
	public static final RegistryObject<Potion> UBER = registerEffect("uber", 0xffffff);
	public static final RegistryObject<Potion> IT = registerEffect("it", 0x9b59b6);
	public static final RegistryObject<Potion> BOMB_MRS = registerEffect("bomb_mrs", 0x333333);
	public static final RegistryObject<Potion> BLEEDING = registerEffect("bleeding", 0x8a0303);
	public static final RegistryObject<Potion> NO_KNOCKBACK = registerEffect("no_knockback", 0x9aa4aa);
	public static final RegistryObject<Potion> SAPPED = registerEffect("sapped", 0x6e6e6e);
	public static final RegistryObject<Potion> REGEN = registerEffect("regen", 0xff6fae);
	public static final RegistryObject<Potion> VIRAL_FIRE = registerEffect("viral_fire", 0xff7518);
	public static final RegistryObject<Potion> SHIELD_EXPLOSIVE = registerEffect("shield_explosive", 0xbd7f32);
	public static final RegistryObject<Potion> SHIELD_BULLET = registerEffect("shield_bullet", 0x7c8794);
	public static final RegistryObject<Potion> SHIELD_FIRE = registerEffect("shield_fire", 0xff551f);
	public static final RegistryObject<Potion> GAS = registerEffect("gas", 0xb3d454);
	public static final RegistryObject<Potion> QUICK_FIX = registerEffect("quick_fix", 0xff9eb8);

	public static final RegistryObject<CreativeModeTab> WEAPONS_TAB = CREATIVE_TABS.register("weapons",
			() -> CreativeModeTab.builder()
					.title(Component.translatable("itemGroup." + MOD_ID + ".weapons"))
					.icon(() -> new ItemStack(ITEM_TF2.get()))
					.displayItems((parameters, output) -> {
						output.accept(ITEM_TF2.get());
						output.accept(ITEM_AMMO.get());
						output.accept(ITEM_AMMO_FIRE.get());
						output.accept(ITEM_MONEY.get());
						output.accept(ITEM_DISGUISE_KIT.get());
						output.accept(ITEM_PICKUP.get());
						output.accept(WEAPON_BULLET.get());
						output.accept(WEAPON_PROJECTILE.get());
						output.accept(WEAPON_MEDIGUN.get());
						output.accept(WEAPON_MELEE.get());
					})
					.build());

	public static final Capability<WeaponsCapability> WEAPONS_CAP =
			CapabilityManager.get(new CapabilityToken<WeaponsCapability>() {});
	public static final Capability<InventoryWearables> INVENTORY_CAP =
			CapabilityManager.get(new CapabilityToken<InventoryWearables>() {});
	public static final Capability<InventoryAmmoBelt> INVENTORY_BELT_CAP =
			CapabilityManager.get(new CapabilityToken<InventoryAmmoBelt>() {});
	public static final Capability<TF2EventsCommon.TF2WorldStorage> WORLD_CAP =
			CapabilityManager.get(new CapabilityToken<TF2EventsCommon.TF2WorldStorage>() {});
	public static final Capability<WeaponData.WeaponDataCapability> WEAPONS_DATA_CAP =
			CapabilityManager.get(new CapabilityToken<WeaponData.WeaponDataCapability>() {});
	public static final Capability<TF2PlayerCapability> PLAYER_CAP =
			CapabilityManager.get(new CapabilityToken<TF2PlayerCapability>() {});

	public static TF2UdpServer udpServer;
	public static Configuration conf;
	public static TF2NetworkWrapper network;
	public static TF2weapons instance;
	public static CommonProxy proxy = new CommonProxy();
	public static MinecraftServer server;
	public static EntityDummy dummyEnt;
	public static byte[] itemDataCompressed;
	public static boolean squakeLoaded;
	public static boolean corrupted;
	public static boolean generateCopper;
	public static boolean generateLead;
	public static boolean generateAustralium;
	public static int weaponVersion;
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

	public TF2weapons() {
		instance = this;
		network = new TF2NetworkWrapper();

		IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
		ITEMS.register(modBus);
		BLOCKS.register(modBus);
		MOB_EFFECTS.register(modBus);
		CREATIVE_TABS.register(modBus);
		modBus.addListener(this::commonSetup);
		MinecraftForge.EVENT_BUS.register(this);
	}

	private static RegistryObject<Potion> registerEffect(String name, int color) {
		return MOB_EFFECTS.register(name, () -> new Potion(false, color));
	}

	private static RegistryObject<Item> registerItem(String name, Supplier<? extends Item> supplier) {
		return ITEMS.register(name, supplier::get);
	}

	private void commonSetup(FMLCommonSetupEvent event) {
		event.enqueueWork(() -> {
			assignRegistryBackups();
			initLegacyMaps();
			loadWeapons();
			LOGGER.info("TF2 Stuff Reloaded bootstrap initialized for Forge 1.20.1");
		});
	}

	private static void assignRegistryBackups() {
		tabutilitytf2 = WEAPONS_TAB.get();
		tabweapontf2 = WEAPONS_TAB.get();
		tabsurvivaltf2 = WEAPONS_TAB.get();
		tabspawnertf2 = WEAPONS_TAB.get();
		tabrareweapontf2 = WEAPONS_TAB.get();
		tabarenatf2 = WEAPONS_TAB.get();

		blockCabinet = PLACEHOLDER_BLOCK.get();
		blockCopperOre = PLACEHOLDER_BLOCK.get();
		blockProp = PLACEHOLDER_BLOCK.get();
		blockLeadOre = PLACEHOLDER_BLOCK.get();
		blockAustraliumOre = PLACEHOLDER_BLOCK.get();
		blockAustralium = PLACEHOLDER_BLOCK.get();
		blockUpgradeStation = PLACEHOLDER_BLOCK.get();
		blockAmmoFurnace = PLACEHOLDER_BLOCK.get();
		blockOverheadDoor = PLACEHOLDER_BLOCK.get();
		blockRobotDeploy = PLACEHOLDER_BLOCK.get();
		blockResupplyCabinet = PLACEHOLDER_BLOCK.get();
		blockCapturePoint = PLACEHOLDER_BLOCK.get();
		blockConfigure = PLACEHOLDER_BLOCK.get();

		bonk = BONK.get();
		stun = STUN.get();
		crit = CRIT.get();
		buffbanner = BUFF_BANNER.get();
		backup = BACKUP.get();
		conch = CONCH.get();
		markDeath = MARK_DEATH.get();
		jarate = JARATE.get();
		madmilk = MAD_MILK.get();
		critBoost = CRIT_BOOST.get();
		charging = CHARGING.get();
		uber = UBER.get();
		it = IT.get();
		bombmrs = BOMB_MRS.get();
		bleeding = BLEEDING.get();
		noKnockback = NO_KNOCKBACK.get();
		sapped = SAPPED.get();
		regen = REGEN.get();
		viralfire = VIRAL_FIRE.get();
		shieldExplosive = SHIELD_EXPLOSIVE.get();
		shieldBullet = SHIELD_BULLET.get();
		shieldFire = SHIELD_FIRE.get();
		gas = GAS.get();
		quickFix = QUICK_FIX.get();

		Item fallback = PLACEHOLDER_ITEM.get();
		itemPlacer = fallback;
		mobHeldItem = fallback;
		itemBuildingBox = fallback;
		itemSandvich = fallback;
		itemChocolate = fallback;
		itemAmmoPistol = ITEM_AMMO.get();
		itemAmmoMinigun = ITEM_AMMO.get();
		itemAmmoSMG = ITEM_AMMO.get();
		itemAmmoSyringe = ITEM_AMMO.get();
		itemAmmoPackage = ITEM_AMMO.get();
		itemAmmoMedigun = ITEM_AMMO.get();
		itemAmmoBelt = ITEM_AMMO.get();
		itemScoutBoots = fallback;
		itemMantreads = fallback;
		itemHorn = fallback;
		itemStatue = fallback;
		itemToken = fallback;
		itemTarget = fallback;
		itemPDA = fallback;
		itemKillstreak = fallback;
		itemGunboats = fallback;
		itemStrangifier = fallback;
		itemKillstreakFabricator = fallback;
		itemEventMaker = fallback;
		itemRobotPart = fallback;
		itemBossSpawn = fallback;
		itemDoorController = fallback;
		itemConfigurator = fallback;

		itemDisguiseKit = ITEM_DISGUISE_KIT.get();
		itemAmmo = ITEM_AMMO.get();
		itemAmmoFire = ITEM_AMMO_FIRE.get();
		itemTF2 = ITEM_TF2.get();
		itemMoney = ITEM_MONEY.get();
		itemPickup = ITEM_PICKUP.get();
	}

	private static void initLegacyMaps() {
		MapList.weaponClasses = new HashMap<>();
		MapList.projectileClasses = new HashMap<>();
		MapList.nameToData = new HashMap<>();
		MapList.propertyTypes = new HashMap<>();
		MapList.nameToAttribute = new HashMap<>();
		MapList.buildInAttributes = new HashMap<>();
		MapList.specialWeapons = new HashMap<>();
		populateWeaponClassMap();
		populateProjectileClassMap();
		PropertyType.init();
		TF2Attribute.initAttributes();
	}

	private static void populateWeaponClassMap() {
		MapList.weaponClasses.put("sniperrifle", WEAPON_SNIPER_RIFLE.get());
		MapList.weaponClasses.put("bullet", WEAPON_BULLET.get());
		MapList.weaponClasses.put("minigun", WEAPON_MINIGUN.get());
		MapList.weaponClasses.put("projectile", WEAPON_PROJECTILE.get());
		MapList.weaponClasses.put("stickybomb", WEAPON_STICKY_BOMB.get());
		MapList.weaponClasses.put("flamethrower", WEAPON_FLAMETHROWER.get());
		MapList.weaponClasses.put("knife", WEAPON_KNIFE.get());
		MapList.weaponClasses.put("medigun", WEAPON_MEDIGUN.get());
		MapList.weaponClasses.put("cloak", WEAPON_CLOAK.get());
		MapList.weaponClasses.put("wrench", WEAPON_WRENCH.get());
		MapList.weaponClasses.put("bonk", WEAPON_BONK.get());
		MapList.weaponClasses.put("cosmetic", WEAPON_COSMETIC.get());
		MapList.weaponClasses.put("melee", WEAPON_MELEE.get());
		MapList.weaponClasses.put("sapper", WEAPON_SAPPER.get());
		MapList.weaponClasses.put("backpack", WEAPON_BACKPACK.get());
		MapList.weaponClasses.put("crate", WEAPON_CRATE.get());
		MapList.weaponClasses.put("jar", WEAPON_JAR.get());
		MapList.weaponClasses.put("wrangler", WEAPON_WRANGLER.get());
		MapList.weaponClasses.put("shield", WEAPON_SHIELD.get());
		MapList.weaponClasses.put("cleaver", WEAPON_CLEAVER.get());
		MapList.weaponClasses.put("parachute", WEAPON_PARACHUTE.get());
		MapList.weaponClasses.put("huntsman", WEAPON_HUNTSMAN.get());
		MapList.weaponClasses.put("jetpack", WEAPON_JETPACK.get());
		MapList.weaponClasses.put("jetpacktrigger", WEAPON_JETPACK_TRIGGER.get());
		MapList.weaponClasses.put("pda", WEAPON_PDA.get());
		MapList.weaponClasses.put("gas", WEAPON_GAS.get());
		MapList.weaponClasses.put("shortcircuit", WEAPON_SHORT_CIRCUIT.get());
		MapList.weaponClasses.put("airblast", WEAPON_AIRBLAST.get());
		MapList.weaponClasses.put("backpackgeneric", WEAPON_BACKPACK_GENERIC.get());
		MapList.weaponClasses.put("grapplinghook", WEAPON_GRAPPLING_HOOK.get());
	}

	private static void populateProjectileClassMap() {
		MapList.projectileClasses.put("rocket", EntityRocket.class);
		MapList.projectileClasses.put("cowmangler", EntityRocket.class);
		MapList.projectileClasses.put("fire", EntityFlame.class);
		MapList.projectileClasses.put("gas", EntityJar.class);
		MapList.projectileClasses.put("fireball", EntityFuryFireball.class);
		MapList.projectileClasses.put("flare", EntityFlare.class);
		MapList.projectileClasses.put("grenade", EntityGrenade.class);
		MapList.projectileClasses.put("syringe", EntityStickProjectile.class);
		MapList.projectileClasses.put("jar", EntityJar.class);
		MapList.projectileClasses.put("ball", EntityBall.class);
		MapList.projectileClasses.put("repairclaw", EntityStickProjectile.class);
		MapList.projectileClasses.put("arrow", EntityStickProjectile.class);
		MapList.projectileClasses.put("cleaver", EntityCleaver.class);
		MapList.projectileClasses.put("hhhaxe", EntityProjectileSimple.class);
		MapList.projectileClasses.put("energy", EntityProjectileEnergy.class);
		MapList.projectileClasses.put("onyx", EntityOnyx.class);
		MapList.projectileClasses.put("pomson", EntityProjectileSimple.class);
		MapList.projectileClasses.put("grapplinghook", EntityGrapplingHook.class);
	}

	public static int getCurrentWeaponVersion() {
		return 50;
	}

	public static void loadWeapons() {
		if (MapList.nameToData == null) {
			MapList.nameToData = new HashMap<>();
		}
		MapList.nameToData.clear();
		loadWeaponFile("weapons.json");
		loadWeaponFile("cosmetics.json");
		loadWeaponFile("crates.json");
		LOGGER.info("Loaded {} TF2 weapon data entries", MapList.nameToData.size());
	}

	private static void loadWeaponFile(String filename) {
		String path = "assets/" + MOD_ID + "/weapons/" + filename;
		try (InputStream stream = TF2weapons.class.getClassLoader().getResourceAsStream(path)) {
			if (stream == null) {
				LOGGER.warn("Missing TF2 weapon data file {}", path);
				return;
			}
			String json = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
			for (WeaponData weapon : WeaponData.parseFile(json, filename)) {
				loadWeapon(weapon.getName(), weapon);
			}
		}
		catch (IOException | RuntimeException e) {
			LOGGER.error("Failed to load TF2 weapon data file {}", path, e);
		}
	}

	public static void loadWeapon(String name, WeaponData weapon) {
		if (name != null && weapon != null && MapList.nameToData != null) {
			MapList.nameToData.put(name, weapon);
		}
	}

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
	public static void updateOreGenStatus() {}
	public static void updateMobSpawning() {}

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
