package rafradek.tf2weapons.registry;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import rafradek.tf2weapons.TF2weapons;
import rafradek.tf2weapons.tileentity.TileEntityAmmoFurnace;
import rafradek.tf2weapons.tileentity.TileEntityCapturePoint;
import rafradek.tf2weapons.tileentity.TileEntityGameConfigure;
import rafradek.tf2weapons.tileentity.TileEntityOverheadDoor;
import rafradek.tf2weapons.tileentity.TileEntityResupplyCabinet;
import rafradek.tf2weapons.tileentity.TileEntityRobotDeploy;
import rafradek.tf2weapons.tileentity.TileEntityUpgrades;

public final class TF2BlockEntities {
	public static final DeferredRegister<BlockEntityType<?>> REGISTRY =
			DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, TF2weapons.MOD_ID);

	public static final RegistryObject<BlockEntityType<TileEntityAmmoFurnace>> AMMO_FURNACE =
			REGISTRY.register("ammo_furnace", () -> build(TileEntityAmmoFurnace::new, TF2weapons.blockAmmoFurnace));
	public static final RegistryObject<BlockEntityType<TileEntityOverheadDoor>> OVERHEAD_DOOR =
			REGISTRY.register("overhead_door", () -> build(TileEntityOverheadDoor::new, TF2weapons.blockOverheadDoor));
	public static final RegistryObject<BlockEntityType<TileEntityRobotDeploy>> ROBOT_DEPLOY =
			REGISTRY.register("robot_deploy", () -> build(TileEntityRobotDeploy::new, TF2weapons.blockRobotDeploy));
	public static final RegistryObject<BlockEntityType<TileEntityResupplyCabinet>> RESUPPLY_CABINET =
			REGISTRY.register("resupply_cabinet", () -> build(TileEntityResupplyCabinet::new, TF2weapons.blockResupplyCabinet));
	public static final RegistryObject<BlockEntityType<TileEntityCapturePoint>> CAPTURE_POINT =
			REGISTRY.register("capture_point", () -> build(TileEntityCapturePoint::new, TF2weapons.blockCapturePoint));
	public static final RegistryObject<BlockEntityType<TileEntityGameConfigure>> GAME_CONFIGURE =
			REGISTRY.register("game_configure", () -> build(TileEntityGameConfigure::new, TF2weapons.blockConfigure));
	public static final RegistryObject<BlockEntityType<TileEntityUpgrades>> UPGRADE_STATION =
			REGISTRY.register("upgrade_station", () -> build(TileEntityUpgrades::new, TF2weapons.blockUpgradeStation));

	private TF2BlockEntities() {
	}

	private static <T extends BlockEntity> BlockEntityType<T> build(BlockEntityType.BlockEntitySupplier<T> factory,
			Block... blocks) {
		return BlockEntityType.Builder.of(factory, blocks).build(null);
	}
}
