package rafradek.tf2weapons.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public class EntityPickup extends Entity {
	public int age;
	public int lifespan = 1200;
	public boolean rematerialize;
	public float hoverStart;
	private Type type = Type.AMMO_SMALL;
	private boolean disabled;

	public enum Type {
		AMMO_SMALL(0, 0.2f, new ItemStack(Items.AIR), true),
		AMMO_NORMAL(0, 0.5f, new ItemStack(Items.AIR), true),
		AMMO_BIG(0, 1f, new ItemStack(Items.AIR), true),
		HEALTH_SMALL(0.205f, 0, new ItemStack(Items.AIR), true),
		HEALTH_MEDIUM(0.5f, 0, new ItemStack(Items.AIR), true),
		HEALTH_BIG(1f, 0, new ItemStack(Items.AIR), true),
		SANDVICH(0.5f, 0, new ItemStack(Items.AIR), false);

		public final float healthRegen;
		public final float ammoRegen;
		public final ItemStack model;
		public final boolean visible;

		Type(float healthRegen, float ammoRegen, ItemStack model, boolean visible) {
			this.healthRegen = healthRegen;
			this.ammoRegen = ammoRegen;
			this.model = model;
			this.visible = visible;
		}
	}

	public EntityPickup(Level level) {
		super(EntityType.MARKER, level);
	}

	public EntityPickup(Level level, Type type, boolean rematerialize) {
		this(level);
		this.type = type;
		this.rematerialize = rematerialize;
	}

	public void setCollected() {
		this.disabled = true;
	}

	public void onCollideWithPlayer(Player player) {
	}

	public void onUpdate() {
		tick();
	}

	public void setType(Type type) {
		this.type = type;
	}

	public Type getPickupType() {
		return this.type;
	}

	public boolean isDisabled() {
		return this.disabled;
	}

	@Override
	protected void defineSynchedData() {}

	@Override
	protected void readAdditionalSaveData(CompoundTag tag) {}

	@Override
	protected void addAdditionalSaveData(CompoundTag tag) {}
}
