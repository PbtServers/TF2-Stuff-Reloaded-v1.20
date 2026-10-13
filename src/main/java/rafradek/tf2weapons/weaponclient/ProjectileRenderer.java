package rafradek.tf2weapons.weaponclient;

import java.util.EnumMap;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import rafradek.tf2weapons.TF2StuffReloaded;
import rafradek.tf2weapons.weapon.projectile.WeaponProjectile;

public final class ProjectileRenderer extends EntityRenderer<WeaponProjectile> {
    private final EnumMap<WeaponProjectile.Flight, ModelPart> models = new EnumMap<>(WeaponProjectile.Flight.class);
    public static ModelLayerLocation layer(WeaponProjectile.Flight flight) { return new ModelLayerLocation(new ResourceLocation(TF2StuffReloaded.MOD_ID, "projectile/" + flight.name().toLowerCase(java.util.Locale.ROOT)), "main"); }
    public ProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
        for (var flight : WeaponProjectile.Flight.values()) models.put(flight, context.bakeLayer(layer(flight)));
    }
    @Override public ResourceLocation getTextureLocation(WeaponProjectile entity) {
        return switch (entity.flight()) {
            case ROCKET -> texture("rocket"); case GRENADE -> texture(entity.blueTeam() ? "grenadeblu" : "grenadered"); case STICKY -> texture(entity.blueTeam() ? "stickyblu" : "stickyred");
            case FLARE -> texture(entity.blueTeam() ? "flareblu" : "flarered"); case SYRINGE -> texture(entity.blueTeam() ? "syringeblu" : "syringered");
            case ARROW -> new ResourceLocation("minecraft", "textures/entity/projectiles/arrow.png");
            case ENERGY -> new ResourceLocation("minecraft", "textures/block/white_concrete.png");
        };
    }
    private static ResourceLocation texture(String name) { return new ResourceLocation(TF2StuffReloaded.MOD_ID, "textures/entity/projectile/" + name + ".png"); }
    @Override public void render(WeaponProjectile entity, float yaw, float partial, PoseStack pose, MultiBufferSource buffer, int light) {
        pose.pushPose();
        boolean axisZ = entity.flight() == WeaponProjectile.Flight.GRENADE || entity.flight() == WeaponProjectile.Flight.SYRINGE;
        pose.mulPose(Axis.YP.rotationDegrees(Mth.lerp(partial, entity.yRotO, entity.getYRot()) - (axisZ ? 0 : 90)));
        if (axisZ) pose.mulPose(Axis.XP.rotationDegrees(-Mth.lerp(partial, entity.xRotO, entity.getXRot())));
        else pose.mulPose(Axis.ZP.rotationDegrees(Mth.lerp(partial, entity.xRotO, entity.getXRot())));
        boolean beam = entity.flight() == WeaponProjectile.Flight.ENERGY;
        models.get(entity.flight()).render(pose, buffer.getBuffer(RenderType.entityCutoutNoCull(getTextureLocation(entity))),
                beam || entity.criticalLevel() > 0 ? 0xF000F0 : light, OverlayTexture.NO_OVERLAY, beam ? 0.25F : 1, beam ? 0.8F : 1, 1, 1);
        pose.popPose();
        super.render(entity, yaw, partial, pose, buffer, light);
    }
}
