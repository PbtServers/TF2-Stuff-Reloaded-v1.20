package rafradek.tf2weapons.item;




import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import rafradek.tf2weapons.TF2PlayerCapability;
import rafradek.tf2weapons.TF2weapons;
import rafradek.tf2weapons.client.ClientProxy;
import rafradek.tf2weapons.common.TF2Attribute;
import rafradek.tf2weapons.common.WeaponsCapability;
import rafradek.tf2weapons.entity.building.EntityBuilding;
import rafradek.tf2weapons.entity.building.EntityDispenser;
import rafradek.tf2weapons.entity.building.EntitySentry;
import rafradek.tf2weapons.entity.building.EntityTeleporter;
import rafradek.tf2weapons.util.PlayerPersistStorage;
import rafradek.tf2weapons.util.TF2Class;
import rafradek.tf2weapons.util.TF2Util;

import javax.annotation.Nullable;

public class ItemPDA extends ItemFromData implements IItemSlotNumber, IItemOverlay, IItemNoSwitch {

	@SuppressWarnings("unchecked")
	private static final EntityDataAccessor<CompoundTag>[] VIEWS = new EntityDataAccessor[] { TF2PlayerCapability.SENTRY_VIEW,
			TF2PlayerCapability.DISPENSER_VIEW, TF2PlayerCapability.TELEPORTERA_VIEW,
			TF2PlayerCapability.TELEPORTERB_VIEW };
	private static final String[] GUI_BUILD_NAMES = new String[] { "gui.build.sentry", "gui.build.dispenser",
			"gui.build.entrance", "gui.build.exit", "gui.build.disposable" };

	public ItemPDA() {
		this.setMaxStackSize(1);
		this.addPropertyOverride(new ResourceLocation("building"), new IItemPropertyGetter() {
			@Override
			@OnlyIn(Dist.CLIENT)
			public float apply(ItemStack stack, @Nullable Level world, @Nullable LivingEntity entityIn) {
				if (!stack.hasTagCompound() || (stack.getTagCompound().getByte("Building") == 0)) {
					return 0f;
				} else {
					int building = stack.getTagCompound().getByte("Building");
					if (building == 1 || building == 5) {
						return 0.33f;
					} else if (building == 2) {
						return 0.66f;
					} else {
						return 1f;
					}
				}
			}
		});
	}

	@Override
	public boolean catchSlotHotkey(ItemStack stack, Player player) {
		return ItemToken.allowUse(player, TF2Class.ENGINEER) && !stack.hasTagCompound()
				|| stack.getTagCompound().getByte("Building") == 0;
	}

	@Override
	public void onSlotSelection(ItemStack stack, Player player, int slot) {
		if (!player.world.isRemote && TF2PlayerCapability.get(player).carrying == null && slot < 5) {
			if (slot == 4 && TF2PlayerCapability.get(player).calculateMaxSentries() <= 0)
				return;
			if (!PlayerPersistStorage.get(player).hasBuilding(slot)) {
				int metal = EntityBuilding.getCost(slot, TF2Util.getFirstItem(player.inventory,
						stackL -> (TF2Attribute.getModifier("Teleporter Cost", stackL, 1, player) != 1)));

				if (WeaponsCapability.get(player).hasMetal(metal)) {
					if (!stack.hasTagCompound())
						stack.setTagCompound(new CompoundTag());
					stack.getTagCompound().setByte("Building", (byte) (slot + 1));
				}
			} else {
				PlayerPersistStorage.get(player).buildings[slot] = null;
			}
		}
	}

	@Override
	public void onUpdate(ItemStack stack, Level world, Entity entityIn, int itemSlot, boolean isSelected) {
		this.forceItemSlot(stack, world, entityIn, itemSlot, isSelected);
		if (!world.isRemote) {

			if (!stack.hasTagCompound())
				stack.setTagCompound(new CompoundTag());
			PlayerPersistStorage storage = PlayerPersistStorage.get(((Player) entityIn));
			stack.getTagCompound().setBoolean("IsC", TF2PlayerCapability.get((Player) entityIn).carrying != null);
			if (TF2PlayerCapability.get((Player) entityIn).carrying != null) {
				stack.getTagCompound().setByte("Building",
						(byte) ((byte) TF2PlayerCapability.get((Player) entityIn).carryingType + 1));
			} else if (stack.getTagCompound().getByte("Building") > 0) {
				int metal = EntityBuilding.getCost(stack.getTagCompound().getByte("Building") - 1, TF2Util.getFirstItem(
						((Player) entityIn).inventory, stackL -> stackL.getItem() instanceof ItemWrench));
				if (!WeaponsCapability.get(entityIn).hasMetal(metal)
						|| storage.hasBuilding(stack.getTagCompound().getByte("Building") - 1))
					stack.getTagCompound().setByte("Building", (byte) 0);
			}

		}
	}

	@Override
	public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
		return slotChanged || oldStack.getItem() != newStack.getItem();

	}

	@Override
	public InteractionResult onItemUse(Player player, Level world, BlockPos pos, InteractionHand hand,
			Direction facing, float hitX, float hitY, float hitZ) {

		ItemStack stack = player.getHeldItem(hand);
		if (world.isRemote) {
			if ((!stack.hasTagCompound() || stack.getTagCompound().getByte("Building") == 0))
				return InteractionResult.PASS;
			else
				return InteractionResult.SUCCESS;
		} else if (!player.canPlayerEdit(pos.offset(facing), facing, stack) || !stack.hasTagCompound()
				|| stack.getTagCompound().getByte("Building") == 0)
			return InteractionResult.PASS;
		else {
			BlockState BlockState = world.getBlockState(pos);

			pos = pos.offset(facing);
			double d0 = 0.0D;

			if (facing == Direction.UP && BlockState.getBlock() instanceof FenceBlock)
				d0 = 0.5D;

			boolean disposable = stack.getTagCompound().getByte("Building") == 5;

			int id = 16 + stack.getTagCompound().getByte("Building") * 2;
			if (stack.getTagCompound().getByte("Building") == 4)
				id -= 2;
			else if (disposable)
				id = 18;

			EntityBuilding entity = (EntityBuilding) ItemMonsterPlacerPlus.spawnCreature(player, world, id,
					pos.getX() + 0.5D, pos.getY() + d0, pos.getZ() + 0.5D, TF2PlayerCapability.get(player).carrying);

			if (entity != null) {

				entity.setEntTeam(TF2Util.getTeamForDisplay(player));
				entity.setOwner(player);
				if (entity instanceof EntitySentry) {
					((EntitySentry) entity).attackRateMult = TF2Attribute.getModifier("Sentry Fire Rate", stack, 1,
							player);
					TF2Util.addModifierSafe(entity, SharedMonsterAttributes.FOLLOW_RANGE, new AttributeModifier(
							"upgraderange", TF2Attribute.getModifier("Sentry Range", stack, 1f, entity) - 1f, 2), true);
					((EntitySentry) entity).setHeat((int) TF2Attribute.getModifier("Piercing", stack, 0, player));
					if (disposable || !TF2Util
							.getFirstItem(player.inventory,
									stackL -> stackL.getItem() instanceof ItemWrench
											&& TF2Attribute.getModifier("Weapon Mode", stackL, 0, player) == 2)
							.isEmpty()) {
						((EntitySentry) entity).setMini(true);
						if (entity.getLevel() > 1)
							entity.onDeath(DamageSource.GENERIC);
					}
				}
				if (TF2PlayerCapability.get(player).carrying != null) {
					entity.setConstructing(true);
					entity.redeploy = true;
				}
				TF2Util.addModifierSafe(entity, SharedMonsterAttributes.MAX_HEALTH,
						new AttributeModifier(EntityBuilding.UPGRADE_HEALTH_UUID, "upgradehealth",
								TF2Attribute.getModifier("Building Health", stack, 1f, entity) - 1f, 2),
						true);
				if (entity instanceof EntityDispenser) {
					((EntityDispenser) entity).setRange(TF2Attribute.getModifier("Dispenser Range", stack, 1, entity));
				}

				entity.rotationYaw = player.rotationYawHead;
				entity.renderYawOffset = player.rotationYawHead;
				entity.rotationYawHead = player.rotationYawHead;
				entity.fromPDA = true;
				if (stack.getTagCompound().getByte("Building") == 5 && entity.getDisposableID() == -1)
					entity.setDisposableID(PlayerPersistStorage.get(player).disposableBuildings.size());

				if (entity instanceof EntityTeleporter) {
					((EntityTeleporter) entity).setID(127);
					((EntityTeleporter) entity).setExit(stack.getTagCompound().getByte("Building") == 4);
				}
				PlayerPersistStorage.get(player).setBuilding(entity,
						TF2PlayerCapability.get(player).calculateMaxSentries());
				TF2PlayerCapability.get(player).carrying = null;
				if (!player.capabilities.isCreativeMode && TF2PlayerCapability.get(player).carrying == null)
					WeaponsCapability.get(player).consumeMetal(EntityBuilding.getCost(
							stack.getTagCompound().getByte("Building") - 1,
							TF2Util.getFirstItem(player.inventory, stackL -> stackL.getItem() instanceof ItemWrench)),
							false);
			}

			stack.getTagCompound().setByte("Building", (byte) 0);
			return InteractionResult.SUCCESS;
		}
	}

	@Override
	public boolean showInfoBox(ItemStack stack, Player player) {
		return true;
	}

	@Override
	public String[] getInfoBoxLines(ItemStack stack, Player player) {
		return new String[] { "METAL",
				Integer.toString(player.getCapability(TF2weapons.WEAPONS_CAP, null).getMetal()) };
	}

	@Override
	public InteractionResultHolder<ItemStack> onItemRightClick(Level world, Player living, InteractionHand hand) {
		if (living.isSneaking()) {
			living.getHeldItem(hand).getTagCompound().setBoolean("ShowHud",
					!living.getHeldItem(hand).getTagCompound().getBoolean("ShowHud"));
			System.out.println(living.getHeldItem(hand).getTagCompound().getBoolean("ShowHud"));
			return new InteractionResultHolder<>(InteractionResult.SUCCESS, living.getHeldItem(hand));
		}
		return new InteractionResultHolder<>(InteractionResult.FAIL, living.getHeldItem(hand));
	}

	@Override
	public boolean canSwitchTo(ItemStack stack) {
		return true;
	}

	@Override
	public void drawOverlay(ItemStack stack, Player player, Tesselator tessellator, BufferBuilder buffer,
			Window resolution) {
		if (!stack.hasTagCompound() || (stack.getTagCompound().getByte("Building") == 0)) {
			TF2PlayerCapability plcap = TF2PlayerCapability.get(player);
			Minecraft.getMinecraft().getTextureManager().bindTexture(ClientProxy.blueprintTexture);

			GlStateManager.color(1.0F, 1.0F, 1.0F, 0.7F);
			Gui gui = Minecraft.getMinecraft().ingameGUI;
			boolean hasTag = stack.hasTagCompound();
			int buildCount = TF2Attribute.getModifier("Extra Sentry", stack, 0, player) > 0 ? 5 : 4;
			for (int i = 0; i < buildCount; i++) {
				int cost = EntityBuilding.getCost(i,
						TF2Util.getFirstItem(player.inventory, stackL -> stackL.getItem() instanceof ItemWrench));
				if (hasTag && i < 4 && plcap.dataManager.get(VIEWS[i]).getSize() != 0) {
					gui.drawTexturedModalRect(resolution.getScaledWidth() / 2 - 140 + i * 72,
							resolution.getScaledHeight() / 2, 0, 64, 64, 64);
					gui.drawTexturedModalRect(resolution.getScaledWidth() / 2 - 132 + i * 72,
							resolution.getScaledHeight() / 2 + 12, 208, 64 + i * 48, 48, 48);
				} else if (WeaponsCapability.get(player).getMetal() >= cost) {
					// gui.drawString(gui.getFontRenderer(),
					// gui.getFontRenderer().getStringWidth(Integer.toString(cost));
					gui.drawTexturedModalRect(resolution.getScaledWidth() / 2 - 140 + i * 72,
							resolution.getScaledHeight() / 2, i == 4 ? 0 : i * 64, 0, 64, 64);
				}
				/*
				 * else gui.drawTexturedModalRect(resolution.getScaledWidth()/2-140 + i * 72,
				 * resolution.getScaledHeight()/2, 0, 0, 64, 64);
				 */

			}
			for (int i = 0; i < buildCount; i++) {
				int cost = EntityBuilding.getCost(i,
						TF2Util.getFirstItem(player.inventory, stackL -> stackL.getItem() instanceof ItemWrench));
				gui.drawString(gui.getFontRenderer(), Integer.toString(cost),
						resolution.getScaledWidth() / 2 - 72
								- gui.getFontRenderer().getStringWidth(Integer.toString(cost)) + i * 72,
						resolution.getScaledHeight() / 2 - 8, 0xFFFFFFFF);
				gui.drawCenteredString(gui.getFontRenderer(), "[" + (i + 1) + "]",
						resolution.getScaledWidth() / 2 - 108 + i * 72, resolution.getScaledHeight() / 2 + 72,
						0xFFFFFFFF);
				gui.drawString(gui.getFontRenderer(), I18n.format(GUI_BUILD_NAMES[i]),
						resolution.getScaledWidth() / 2 - 140 + i * 72, resolution.getScaledHeight() / 2 - 18,
						0xFFFFFFFF);
				if (WeaponsCapability.get(player).getMetal() < cost
						&& !(hasTag && i < 4 && plcap.dataManager.get(VIEWS[i]).getSize() != 0)) {
					gui.getFontRenderer().drawSplitString(I18n.format("gui.build.nometal"),
							resolution.getScaledWidth() / 2 - 140 + i * 72, resolution.getScaledHeight() / 2 + 20, 80,
							0xFFF00F0F);
				}

			}
			/*
			 * gui.drawTexturedModalRect(resolution.getScaledWidth()/2-68,
			 * resolution.getScaledHeight()/2-32, 64, 0, 64, 64);
			 * gui.drawTexturedModalRect(resolution.getScaledWidth()/2+4,
			 * resolution.getScaledHeight()/2-32, 128, 0, 64, 64);
			 * gui.drawTexturedModalRect(resolution.getScaledWidth()/2+72,
			 * resolution.getScaledHeight()/2-32, 192, 0, 64, 64);
			 * 
			 * gui.drawCenteredString(gui.getFontRenderer(), "[2]",
			 * resolution.getScaledWidth()/2-36, resolution.getScaledHeight()/2+40,
			 * 0xFFFFFFFF); gui.drawCenteredString(gui.getFontRenderer(), "[3]",
			 * resolution.getScaledWidth()/2+36, resolution.getScaledHeight()/2+40,
			 * 0xFFFFFFFF); gui.drawCenteredString(gui.getFontRenderer(), "[4]",
			 * resolution.getScaledWidth()/2+108, resolution.getScaledHeight()/2+40,
			 * 0xFFFFFFFF); gui.drawCenteredString(gui.getFontRenderer(),
			 * I18n.format("gui.build.sentry"), resolution.getScaledWidth()/2-108,
			 * resolution.getScaledHeight()/2-40, 0xFFFFFFFF);
			 * gui.drawCenteredString(gui.getFontRenderer(),
			 * I18n.format("gui.build.dispenser"), resolution.getScaledWidth()/2-36,
			 * resolution.getScaledHeight()/2-40, 0xFFFFFFFF);
			 * gui.drawCenteredString(gui.getFontRenderer(),
			 * I18n.format("gui.build.entrance"), resolution.getScaledWidth()/2+36,
			 * resolution.getScaledHeight()/2-40, 0xFFFFFFFF);
			 * gui.drawCenteredString(gui.getFontRenderer(), I18n.format("gui.build.exit"),
			 * resolution.getScaledWidth()/2+108, resolution.getScaledHeight()/2-40,
			 * 0xFFFFFFFF);
			 */
			gui.drawCenteredString(gui.getFontRenderer(), I18n.format("gui.build"), resolution.getScaledWidth() / 2,
					resolution.getScaledHeight() / 2 - 40, 0xFFFFFFFF);

			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		}

	}

	@Override
	public boolean stopSlotSwitch(ItemStack stack, LivingEntity living) {
		return stack.getTagCompound().getBoolean("IsC");
	}

}
