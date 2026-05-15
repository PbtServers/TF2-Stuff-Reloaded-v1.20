package rafradek.tf2weapons.tileentity;


import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.scores.Team;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import rafradek.tf2weapons.TF2weapons;
import rafradek.tf2weapons.entity.mercenary.EntityTF2Character;
import rafradek.tf2weapons.item.ItemAmmoPackage;
import rafradek.tf2weapons.item.ItemFromData;
import rafradek.tf2weapons.item.ItemWeapon;
import rafradek.tf2weapons.registry.TF2BlockEntities;
import rafradek.tf2weapons.util.PropertyType;
import rafradek.tf2weapons.util.TF2Util;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class TileEntityResupplyCabinet extends BlockEntity implements IEntityConfigurable {

	private static final String[] OUTPUT_NAMES = { "OnResupply", "OnResupplyLeave" };
	public Team team;
	public Map<LivingEntity, Integer> cooldownUse = new HashMap<>();
	public boolean usedBy;
	public boolean enabled = true;
	public boolean redstoneActivate;
	private EntityOutputManager outputManager = new EntityOutputManager(this);

	public TileEntityResupplyCabinet(BlockPos pos, BlockState state) {
		super(TF2BlockEntities.RESUPPLY_CABINET.get(), pos, state);
	}

	public void setEnabled(boolean enable) {

		this.level.blockEvent(this.worldPosition, this.getBlockType(), 0, enabled ? 1 : 0);
		if (this.enabled != enable) {
			this.enabled = enable;
			this.level.notifyNeighborsOfStateChange(this.worldPosition, this.getBlockType(), false);
		}

	}

	@Override
	public void tick() {
		if (!this.level.isClientSide) {
			int playersold = cooldownUse.size();
			cooldownUse.entrySet().removeIf(entry -> {
				entry.setValue(entry.getValue() - 1);
				return entry.getValue() <= 0;
			});

			if (this.enabled)
				for (LivingEntity living : this.level.getEntitiesOfClass(LivingEntity.class,
						new AABB(this.worldPosition).inflate(2),
						entityf -> (entityf.isEntityAlive() && (team == null || entityf.getTeam() == team)
								&& !cooldownUse.containsKey(entityf)
								&& entityf.hasCapability(TF2weapons.WEAPONS_CAP, null)))) {
					living.setHealth(living.getMaxHealth());
					ArrayList<Potion> badEffects = new ArrayList<>();
					for (Entry<Potion, MobEffectInstance> entry : living.getActivePotionMap().entrySet()) {
						if (entry.getKey().isBadEffect())
							badEffects.add(entry.getKey());
					}
					for (Potion potion : badEffects) {
						living.removePotionEffect(potion);
					}

					if (living instanceof EntityTF2Character) {
						((EntityTF2Character) living).restoreAmmo(1);
					} else if (living instanceof Player) {
						Player player = ((Player) living);
						player.getFoodStats().addStats(20, 20);
						for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
							ItemStack stack = player.inventory.getStackInSlot(i);
							if (stack.getItem() instanceof ItemFromData) {
								int ammotype = ((ItemFromData) stack.getItem()).getAmmoType(stack);
								int ammocount = ItemFromData.getAmmoAmountType(player, ammotype);
								if (ammocount < ItemFromData.getData(stack).getInt(PropertyType.MAX_AMMO)) {
									TF2Util.pickAmmo(ItemAmmoPackage.getAmmoForType(ammotype,
											ItemFromData.getData(stack).getInt(PropertyType.MAX_AMMO) - ammocount),
											player, true);
								}
							}
							if (stack.getItem() instanceof ItemWeapon) {
								((ItemWeapon) stack.getItem()).setClip(stack,
										((ItemWeapon) stack.getItem()).getWeaponClipSize(stack, living));
							}
						}
					}
					this.activateOutput("OnResupply");
					cooldownUse.put(living, 50);
					this.level.notifyNeighborsOfStateChange(this.worldPosition, this.getBlockType(), false);
				}
			if (playersold > 0 && cooldownUse.size() == 0) {
				this.activateOutput("OnResupplyLeave");
				this.level.notifyNeighborsOfStateChange(this.worldPosition, this.getBlockType(), false);
			}
		}
	}

	@Override
	public CompoundTag writeToNBT(CompoundTag compound) {
		super.writeToNBT(compound);
		compound.setTag("Config", this.getOutputManager().writeConfig(new CompoundTag()));
		compound.setBoolean("Enabled", this.enabled);
		return compound;
	}

	@Override
	public void readFromNBT(CompoundTag compound) {
		super.readFromNBT(compound);
		this.getOutputManager().readConfig(compound.getCompoundTag("Config"));
		this.enabled = compound.getBoolean("Enabled");
	}

	@Override
	public boolean receiveClientEvent(int id, int type) {
		if (id == 1) {
			this.enabled = type != 0;
			return true;
		} else {
			return super.receiveClientEvent(id, type);
		}
	}

	/*
	 * @Nullable public ClientboundBlockEntityDataPacket getUpdatePacket() { CompoundTag
	 * tag = new CompoundTag(); if (this.maxprogress > 0) { tag.setByte("P",
	 * (byte) ((float)this.progress/(float)this.maxprogress*7f)); if (this.progress
	 * > 0) tag.setByte("C", (byte)
	 * ItemToken.getClassID(TF2Util.getWeaponUsedByClass(this.weapon.extractItem(
	 * hasWeapon,64,true)))); } return new ClientboundBlockEntityDataPacket(this.worldPosition, 9999,
	 * tag); }
	 * 
	 * public void onDataPacket(net.minecraft.network.NetworkManager net,
	 * net.minecraft.network.play.server.ClientboundBlockEntityDataPacket pkt) {
	 * this.progressClient = pkt.getNbtCompound().getByte("P"); this.classType =
	 * pkt.getNbtCompound().getByte("C"); }
	 */

	@Override
	public boolean hasCapability(net.minecraftforge.common.capabilities.Capability<?> capability,
			@Nullable net.minecraft.core.Direction facing) {
		if (facing != null && capability == net.minecraftforge.common.capabilities.ForgeCapabilities.ITEM_HANDLER_CAPABILITY) {
			return true;
		}
		return super.hasCapability(capability, facing);
	}

	@Override
	@Nullable
	public <T> T getCapability(net.minecraftforge.common.capabilities.Capability<T> capability,
			@Nullable net.minecraft.core.Direction facing) {
		/*
		 * if (facing != null && capability ==
		 * net.minecraftforge.common.capabilities.ForgeCapabilities.ITEM_HANDLER_CAPABILITY) { if
		 * (facing.getAxis() == Axis.Y) return
		 * ForgeCapabilities.ITEM_HANDLER.cast(this.weapon); else if
		 * (facing.getAxis() == Axis.X) return
		 * ForgeCapabilities.ITEM_HANDLER.cast(this.parts); else return
		 * ForgeCapabilities.ITEM_HANDLER.cast(this.money); } else
		 */
		return super.getCapability(capability, facing);
	}

	@Override
	public void onLoad() {}

	@Override
	public boolean shouldRefresh(Level world, BlockPos pos, BlockState oldState, BlockState newSate) {
		return super.shouldRefresh(world, worldPosition, oldState, newSate);
	}

	@Override
	protected void setWorldCreate(Level world) {
		this.setWorld(world);
	}

	@Override
	public void setWorld(Level world) {
		super.setWorld(world);
		this.getOutputManager().Level = world;
	}

	@Override
	public CompoundTag writeConfig(CompoundTag tag) {
		tag.setTag("Outputs", this.getOutputManager().saveOutputs(new CompoundTag()));
		if (team != null)
			tag.setString("T:Team", this.team.getName());
		else
			tag.setString("T:Team", "");
		tag.setBoolean("Redstone Activates", this.redstoneActivate);
		return tag;
	}

	@Override
	public void readConfig(CompoundTag tag) {
		this.getOutputManager().loadOutputs(tag.getCompoundTag("Outputs"));
		this.redstoneActivate = tag.getBoolean("Redstone Activates");
		if (this.hasLevel() && tag.hasKey("T:Team"))
			this.team = this.level.getScoreboard().getTeam(tag.getString("T:Team"));
	}

	@Override
	public EntityOutputManager getOutputManager() {
		return this.outputManager;
	}

	@Override
	public String[] getOutputs() {
		return OUTPUT_NAMES;
	}

}
