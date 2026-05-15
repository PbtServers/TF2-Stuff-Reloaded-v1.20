package rafradek.tf2weapons.client;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import rafradek.tf2weapons.client.audio.ReloadSound;
import rafradek.tf2weapons.client.audio.WeaponLoopSound;
import rafradek.tf2weapons.common.CommonProxy;
import rafradek.tf2weapons.util.WeaponData;

public class ClientProxy extends CommonProxy {
	public static final ResourceLocation VIGNETTE = new ResourceLocation("textures/misc/vignette.png");
	public static final ResourceLocation WIDGETS_TEXTURE_MC = new ResourceLocation("textures/gui/widgets.png");
	public static final ResourceLocation blackTexture = new ResourceLocation("tf2weapons", "textures/gui/black.png");
	public static final ResourceLocation blueprintTexture = new ResourceLocation("tf2weapons", "textures/gui/blueprint.png");
	public static final ResourceLocation buildingTexture = new ResourceLocation("tf2weapons", "textures/gui/buildings.png");
	public static final ResourceLocation chargeTexture = new ResourceLocation("tf2weapons", "textures/gui/charge.png");
	public static final ResourceLocation circleTexture = new ResourceLocation("tf2weapons", "textures/gui/circle.png");
	public static final ResourceLocation healingTexture = new ResourceLocation("tf2weapons", "textures/gui/healing.png");
	public static final ResourceLocation scopeTexture = new ResourceLocation("tf2weapons", "textures/gui/scope.png");

	public static final Map<LivingEntity, WeaponLoopSound> fireSounds = new HashMap<>();
	public static final Map<LivingEntity, ReloadSound> reloadSounds = new HashMap<>();
	public static final Map<String, ModelResourceLocation> nameToModel = new HashMap<>();
	public static final ConcurrentMap<LivingEntity, ItemStack> soundsToStart = new ConcurrentHashMap<>();
	public static final Set<Class<? extends Block>> interactingBlocks = new HashSet<>();

	public static Object disguiseRender;
	public static Object disguiseRenderPlayer;
	public static Object disguiseRenderPlayerSmall;
	public static Object forceTextureRenderPlayer;
	public static int renderCritGlow;
	public static boolean buildingsUseEnergy;

	public static Player getLocalPlayer() {
		return null;
	}

	public static void RegisterWeaponData(WeaponData weapon) {}

	public static void doChargeTick(LivingEntity living) {}

	public static void playWeaponSound(LivingEntity living, SoundEvent sound, boolean repeat, int type, ItemStack stack) {}

	public static void removeReloadSound(LivingEntity living) {}

	public static void removeSprint() {}

	public static void setColor(int color, float alpha, float addRed, float addGreen, float addBlue) {}

	public static void showGuiDisguise() {}

	public static void spawnBubbleParticle(Level world, Vec3 pos) {}

	public static void spawnBulletHoleParticle(Level world, HitResult trace) {}

	public static void spawnBulletParticle(Level world, LivingEntity living, float spread, int critical, boolean burn, int pellet) {}

	public static void spawnFlameParticle(Level world, LivingEntity living, float offset, boolean rocket) {}

	public static void spawnFlashParticle(Level world, LivingEntity living, InteractionHand hand) {}

	public static void spawnParticle(Level world, Object particle) {}
}
