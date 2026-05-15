package rafradek.tf2weapons.item;




import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.stats.Stats;
import net.minecraft.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.HitResult;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import rafradek.tf2weapons.TF2weapons;
import rafradek.tf2weapons.entity.boss.EntityHHH;
import rafradek.tf2weapons.entity.boss.EntityMerasmus;
import rafradek.tf2weapons.entity.boss.EntityMonoculus;
import rafradek.tf2weapons.entity.building.EntityBuilding;
import rafradek.tf2weapons.entity.building.EntityDispenser;
import rafradek.tf2weapons.entity.building.EntitySentry;
import rafradek.tf2weapons.entity.building.EntityTeleporter;
import rafradek.tf2weapons.entity.mercenary.*;
import rafradek.tf2weapons.util.TF2Class;

import java.util.List;

@SuppressWarnings("deprecation")
public class ItemMonsterPlacerPlus extends Item {

	public ItemMonsterPlacerPlus() {
		this.setHasSubtypes(true);
		this.setCreativeTab(TF2weapons.tabspawnertf2);
	}

	/**
	 * Callback for item usage. If the item does something special on right
	 * clicking, he will have one of those. Return True if something happen and
	 * false if it don't. This is for ITEMS, not BLOCKS
	 */
	@Override
	public InteractionResult onItemUse(Player player, Level world, BlockPos pos, InteractionHand hand,
			Direction facing, float hitX, float hitY, float hitZ) {

		ItemStack stack = player.getHeldItem(hand);
		if (world.isRemote)
			return InteractionResult.SUCCESS;
		else if (!player.canPlayerEdit(pos.offset(facing), facing, stack))
			return InteractionResult.FAIL;
		else {
			BlockState BlockState = world.getBlockState(pos);

			pos = pos.offset(facing);
			double d0 = 0.0D;

			if (facing == Direction.UP && BlockState.getBlock() instanceof FenceBlock)
				d0 = 0.5D;

			boolean hastag = stack.getTagCompound() != null && stack.getTagCompound().hasKey("SavedEntity");

			LivingEntity entity = spawnCreature(player, world, stack.getItemDamage(), pos.getX() + 0.5D,
					pos.getY() + d0, pos.getZ() + 0.5D,
					hastag ? stack.getTagCompound().getCompoundTag("SavedEntity") : null);

			if (entity != null) {
				if (entity instanceof LivingEntity && stack.hasDisplayName())
					entity.setCustomNameTag(stack.getDisplayName());

				if (!player.capabilities.isCreativeMode)
					stack.shrink(1);
				if (entity instanceof EntityBuilding) {
					if (entity instanceof EntityTeleporter || !(player.isCreative() && player.isSneaking()))
						((EntityBuilding) entity).setOwner(player);
					if (hastag) {
						((EntityBuilding) entity).setConstructing(true);
						((EntityBuilding) entity).redeploy = true;
					}
					entity.rotationYaw = player.rotationYawHead;
					entity.renderYawOffset = player.rotationYawHead;
					entity.rotationYawHead = player.rotationYawHead;
					if (entity instanceof EntityTeleporter)
						((EntityTeleporter) entity).setExit(stack.getItemDamage() > 23);
				}
			}

			return InteractionResult.SUCCESS;
		}
	}

	/**
	 * Applies the data in the EntityTag tag of the given ItemStack to the given
	 * Entity.
	 */

	@Override
	public InteractionResultHolder<ItemStack> onItemRightClick(Level world, Player player, InteractionHand hand) {
		ItemStack itemStackIn = player.getHeldItem(hand);
		if (world.isRemote)
			return new InteractionResultHolder<>(InteractionResult.PASS, itemStackIn);
		else {
			HitResult raytraceresult = this.rayTrace(world, player, true);

			if (raytraceresult != null && raytraceresult.typeOfHit == HitResult.Type.BLOCK) {
				BlockPos blockpos = raytraceresult.getBlockPos();

				if (!(world.getBlockState(blockpos).getBlock() instanceof LiquidBlock))
					return new InteractionResultHolder<>(InteractionResult.PASS, itemStackIn);
				else if (world.isBlockModifiable(player, blockpos)
						&& player.canPlayerEdit(blockpos, raytraceresult.sideHit, itemStackIn)) {

					boolean hastag = itemStackIn.getTagCompound() != null
							&& itemStackIn.getTagCompound().hasKey("SavedEntity");

					LivingEntity entity = spawnCreature(player, world, itemStackIn.getItemDamage(),
							blockpos.getX() + 0.5D, blockpos.getY() + 0.5D, blockpos.getZ() + 0.5D,
							hastag ? itemStackIn.getTagCompound().getCompoundTag("SavedEntity") : null);

					if (entity == null)
						return new InteractionResultHolder<>(InteractionResult.PASS, itemStackIn);
					else {
						if (entity instanceof LivingEntity && itemStackIn.hasDisplayName())
							entity.setCustomNameTag(itemStackIn.getDisplayName());

						if (!player.capabilities.isCreativeMode)
							itemStackIn.shrink(1);
						if (entity instanceof EntityBuilding) {
							if (entity instanceof EntityTeleporter || !(player.isCreative() && player.isSneaking()))
								((EntityBuilding) entity).setOwner(player);
							if (hastag) {
								((EntityBuilding) entity).setConstructing(true);
								((EntityBuilding) entity).redeploy = true;
							}
							if (entity instanceof EntitySentry && itemStackIn.hasTagCompound()
									&& itemStackIn.getTagCompound().getBoolean("Mini"))
								((EntitySentry) entity).setMini(true);

							entity.rotationYaw = player.rotationYawHead;
							entity.renderYawOffset = player.rotationYawHead;
							entity.rotationYawHead = player.rotationYawHead;

							/*
							 * if(entity instanceof EntityTeleporter){ ((EntityTeleporter)
							 * entity).setExit(itemStackIn.getItemDamage()>23); }
							 */
						}
						player.addStat(Stats.getObjectUseStats(this));
						return new InteractionResultHolder<>(InteractionResult.SUCCESS, itemStackIn);
					}
				} else
					return new InteractionResultHolder<>(InteractionResult.FAIL, itemStackIn);
			} else
				return new InteractionResultHolder<>(InteractionResult.PASS, itemStackIn);
		}
	}

	public static Mob spawnCreature(Entity spawner, Level par0World, int par1, double par2, double par4,
			double par6, CompoundTag nbtdata) {
		Mob entity = null;

		for (int j = 0; j < 1; ++j) {
			int team = par1 % 2;
			if (par1 < 18 || (par1 >= 36 && par1 < 45)) {
				switch (par1 % 9) {
				case 0:
					entity = new EntityScout(par0World);
					break;
				case 1:
					entity = new EntitySoldier(par0World);
					break;
				case 2:
					entity = new EntityPyro(par0World);
					break;
				case 3:
					entity = new EntityDemoman(par0World);
					break;
				case 4:
					entity = new EntityHeavy(par0World);
					break;
				case 5:
					entity = new EntityEngineer(par0World);
					break;
				case 6:
					entity = new EntityMedic(par0World);
					break;
				case 7:
					entity = new EntitySniper(par0World);
					break;
				case 8:
					entity = new EntitySpy(par0World);
					break;
				}
				team = par1 / 9;
				if (par1 >= 36)
					team = 2;
			} else if (par1 / 2 == 9)
				entity = new EntitySentry(par0World);
			else if (par1 / 2 == 10)
				entity = new EntityDispenser(par0World);
			else if (par1 / 2 == 11)
				entity = new EntityTeleporter(par0World);
			else if (par1 / 2 == 13)
				entity = new EntitySaxtonHale(par0World);
			else if (par1 == 28)
				entity = new EntityMonoculus(par0World);
			else if (par1 == 29)
				entity = new EntityHHH(par0World);
			else if (par1 == 30)
				entity = new EntityMerasmus(par0World);
			if (entity != null) {
				Mob entityliving = entity;
				if (nbtdata != null)
					entityliving.readFromNBT(nbtdata);
				// System.out.println("read");
				entity.setLocationAndAngles(par2, par4, par6,
						Mth.wrapDegrees(par0World.rand.nextFloat() * 360.0F), 0.0F);
				entityliving.rotationYawHead = entityliving.rotationYaw;
				entityliving.renderYawOffset = entityliving.rotationYaw;
				entityliving.enablePersistence();
				TF2CharacterAdditionalData data = new TF2CharacterAdditionalData();
				data.team = team;
				data.noEquipment = team < 2 && spawner != null && spawner.isSneaking();
				data.isGiant = team == 2 && spawner != null && spawner.isSneaking();
				if (nbtdata == null)
					entityliving.onInitialSpawn(par0World.getDifficultyForLocation(new BlockPos(entityliving)), data);
				entityliving.playLivingSound();
				if (entity instanceof EntityBuilding)
					((EntityBuilding) entity).setEntTeam(team);
				if (entity instanceof EntitySaxtonHale && par1 % 2 == 1)
					((EntitySaxtonHale) entity).setHostile();
				if (!par0World.getCollisionBoxes(entity, entity.getEntityBoundingBox()).isEmpty())
					return null;
				par0World.spawnEntity(entity);

			}

		}

		return entity;
	}

	@Override
	@OnlyIn(Dist.CLIENT)

	/**
	 * returns a list of items with the same ID, but different meta (eg: dye returns
	 * 16 items)
	 */
	public void getSubItems(CreativeModeTab par2CreativeTabs, NonNullList<ItemStack> par3List) {
		if (!this.isInCreativeTab(par2CreativeTabs))
			return;
		for (int i = 0; i < 18; i++)
			par3List.add(new ItemStack(this, 1, i));
		for (int i = 36; i < 45; i++)
			par3List.add(new ItemStack(this, 1, i));
		par3List.add(new ItemStack(this, 1, 26));
		par3List.add(new ItemStack(this, 1, 27));
		par3List.add(new ItemStack(this, 1, 28));
		par3List.add(new ItemStack(this, 1, 29));
		par3List.add(new ItemStack(this, 1, 30));

	}

	@Override
	public String getItemStackDisplayName(ItemStack p_77653_1_) {
		String s = ("" + I18n.translateToLocal(this.getUnlocalizedName() + ".name")).trim();
		int i = p_77653_1_.getItemDamage();
		String s1 = "hale";
		if (i < 18 || (i >= 36 && i < 45))
			s1 = TF2Class.getClass(i % 9).getName();
		if (p_77653_1_.getItemDamage() == 28)
			s1 = "monoculus";
		if (p_77653_1_.getItemDamage() == 29)
			s1 = "hhh";
		if (p_77653_1_.getItemDamage() == 30)
			s1 = "merasmus";
		s1 = I18n.translateToLocal("entity." + s1 + ".name");
		if (p_77653_1_.getItemDamage() == 27)
			s1 = s1.concat(" " + I18n.translateToLocal("item." + TF2weapons.MOD_ID + ".placer.hostile"));
		return s.concat(" " + s1);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void addInformation(ItemStack stack, Level world, List<String> tooltip, TooltipFlag advanced) {
		if (stack.getMetadata() < 18)
			tooltip.add("Hold " + KeyBinding.getDisplayString("key.sneak").get() + " to spawn with default equipment");
		if (stack.getMetadata() >= 36 && stack.getMetadata() < 45)
			tooltip.add("Hold " + KeyBinding.getDisplayString("key.sneak").get() + " to spawn a giant");
	}
}
