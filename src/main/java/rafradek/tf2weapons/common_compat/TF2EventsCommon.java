package rafradek.tf2weapons;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import rafradek.tf2weapons.arena.GameArena;
import rafradek.tf2weapons.entity.building.EntityBuilding;
import rafradek.tf2weapons.entity.mercenary.InvasionEvent;
import rafradek.tf2weapons.world.gen.structure.MannCoBuilding;
import rafradek.tf2weapons.world.gen.structure.ScatteredFeatureTF2Base;

public class TF2EventsCommon {
	public static EntityBuilding teleporterBView;
	public static EntityBuilding teleporterAView;
	public static EntityBuilding dispenserView;
	public static EntityBuilding sentryView;
	public static final String[] STRANGE_TITLES = new String[] { "Strange" };
	public static final int[] STRANGE_KILLS = new int[] { 0 };
	public static HashMap<LivingEntity, LivingEntity> fakeEntities = new HashMap<>();
	public static ArrayList<LivingEntity> pathsToDefine = new ArrayList<>();
	public static final EntityDataAccessor<Float> ENTITY_OVERHEAL =
			new EntityDataAccessor<>(170, EntityDataSerializers.FLOAT);
	public static final UUID REMOVE_ARMOR = UUID.fromString("5a0959c5-90e8-486b-ae51-26f69f19a248");
	public static long[] tickTimeLiving = new long[20];
	public static long[] tickTimeMercUpdate = new long[20];
	public static long[] tickTimeOther = new long[20];

	public static double avg(long[] values) {
		return 0;
	}

	public static boolean isSpawnEvent(Level world) {
		return false;
	}

	public static float getDamageReductionFromItem(ItemStack stack, DamageSource source, LivingEntity target,
			boolean armor) {
		return 0;
	}

	public static void onStrangeUpdate(ItemStack stack, LivingEntity player) {}

	public static class DestroyBlockEntry {
		public BlockPos pos;
		public float curDamage;
		public Level world;
		public Level Level;

		public DestroyBlockEntry(BlockPos pos, Level world) {
			this.world = world;
			this.Level = world;
			this.pos = pos;
		}
	}

	public static class InboundDamage {
		public DamageSource source;
		public float damage;
		public int critical;
		public LivingEntity living;
		public ItemStack stack;

		public InboundDamage(DamageSource source, float damage, int critical, LivingEntity living, ItemStack stack) {
			this.source = source;
			this.damage = damage;
			this.critical = critical;
			this.living = living;
			this.stack = stack;
		}
	}

	public static class TF2WorldStorage implements ICapabilityProvider, INBTSerializable<CompoundTag> {
		public int eventFlag;
		public Level world;
		public HashMap<Entity, InboundDamage> damage = new HashMap<>();
		public ArrayList<BlockPos> banners = new ArrayList<>();
		public HashMap<String, MerchantOffers> lostItems = new HashMap<>();
		public Map<UUID, InvasionEvent> invasions = new HashMap<>();
		public ArrayList<DestroyBlockEntry> destroyProgress = new ArrayList<>();
		public HashMap<String, GameArena> gameArenas = new HashMap<>();
		public MannCoBuilding.MapGen mannCoGenerator = new MannCoBuilding.MapGen();
		public ScatteredFeatureTF2Base.MapGen tf2BaseGenerator = new ScatteredFeatureTF2Base.MapGen(null);
		public boolean silent;

		@Override
		public <T> LazyOptional<T> getCapability(Capability<T> capability, Direction facing) {
			return LazyOptional.empty();
		}

		@Override
		public CompoundTag serializeNBT() {
			return new CompoundTag();
		}

		@Override
		public void deserializeNBT(CompoundTag nbt) {}
	}
}
