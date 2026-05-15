package rafradek.tf2weapons.client.renderer.entity;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.RenderManager;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.opengl.GL11;
import rafradek.tf2weapons.TF2weapons;
import rafradek.tf2weapons.client.ClientProxy;
import rafradek.tf2weapons.client.model.ModelMonoculus;
import rafradek.tf2weapons.entity.boss.EntityMonoculus;
import rafradek.tf2weapons.util.TF2Util;

public class RenderMonoculus extends RenderLivingBase<EntityMonoculus> {
	private static final ResourceLocation TEXTURE = new ResourceLocation(TF2weapons.MOD_ID,
			"textures/entity/tf2/monoculus.png");
	private static final ResourceLocation TEXTURE_ANGRY = new ResourceLocation(TF2weapons.MOD_ID,
			"textures/entity/tf2/monoculusangry.png");

	public RenderMonoculus(RenderManager renderManagerIn) {
		super(renderManagerIn, new ModelMonoculus(), 0f);
	}

	@Override
	protected boolean canRenderName(EntityMonoculus entity) {
		return false;
	}

	@Override
	protected void preRenderCallback(EntityMonoculus entity, float partialTickTime) {
		float f = 2F;
		if (entity.begin > 0) {
			GlStateManager.translate(0, entity.begin * 0.133333f, 0);
		}
		/*
		 * GlStateManager.rotate(entity.prevRotationPitch + (entity.rotationPitch -
		 * entity.prevRotationPitch) * partialTickTime, 0.0F, 0.0F, 1.0F);
		 */
		GlStateManager.scale(f, f, f);
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityMonoculus par1EntityLiving) {
		return par1EntityLiving.isAngry() ? TEXTURE_ANGRY : TEXTURE;
	}

	@Override
	public void doRender(EntityMonoculus entity, double x, double y, double z, float entityYaw, float partialTicks) {
		super.doRender(entity, x, y, z, entityYaw, partialTicks);
		if (entity.isLaser()) {
			float scale = Math.min(0.12f, (EntityMonoculus.LASER_DURATION - entity.laserTime) * 0.006f);
			Tesselator tessellator = Tesselator.getInstance();
			BufferBuilder renderer = tessellator.getBuffer();
			GlStateManager.pushMatrix();
			GlStateManager.translate((float) x, (float) y + entity.getEyeHeight(), (float) z);
			GL11.glRotatef(
					(entity.prevRotationYawHead + (entity.rotationYawHead - entity.prevRotationYawHead) * partialTicks)
							* -1,
					0.0F, 1.0F, 0.0F);
			GL11.glRotatef(
					(entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks), 1.0F,
					0.0F, 0.0F);
			GlStateManager.disableTexture2D();
			GlStateManager.disableLighting();
			GL11.glEnable(GL11.GL_BLEND);
			OpenGlHelper.glBlendFunc(770, 771, 1, 0);
			ClientProxy.setColor(TF2Util.getTeamColor(entity), 0.28f, 0, 0f, 1f);
			renderer.begin(7, DefaultVertexFormat.POSITION);
			renderer.pos(-scale, -scale, 0).endVertex();
			renderer.pos(scale, scale, 0).endVertex();
			renderer.pos(scale, scale, 50).endVertex();
			renderer.pos(-scale, -scale, 50).endVertex();
			tessellator.draw();
			renderer.begin(7, DefaultVertexFormat.POSITION);
			renderer.pos(-scale, -scale, 50).endVertex();
			renderer.pos(scale, scale, 50).endVertex();
			renderer.pos(scale, scale, 0).endVertex();
			renderer.pos(-scale, -scale, 0).endVertex();
			tessellator.draw();
			renderer.begin(7, DefaultVertexFormat.POSITION);
			renderer.pos(scale, -scale, 0).endVertex();
			renderer.pos(-scale, scale, 0).endVertex();
			renderer.pos(-scale, scale, 50).endVertex();
			renderer.pos(scale, -scale, 50).endVertex();
			tessellator.draw();
			renderer.begin(7, DefaultVertexFormat.POSITION);
			renderer.pos(scale, -scale, 50).endVertex();
			renderer.pos(-scale, scale, 50).endVertex();
			renderer.pos(-scale, scale, 0).endVertex();
			renderer.pos(scale, -scale, 0).endVertex();
			tessellator.draw();
			/*
			 * renderer.startDrawingQuads(); renderer.addVertex(-0.03,
			 * p_76986_1_.getEyeHeight()-0.03,0); renderer.addVertex(0.03,
			 * p_76986_1_.getEyeHeight()+0.03,0);
			 * renderer.addVertex(lookVec.x*50+0.03,lookVec.y*64+
			 * p_76986_1_.getEyeHeight()+0.03, lookVec.z*64);
			 * renderer.addVertex(lookVec.x*50-0.03,lookVec.y*64+
			 * p_76986_1_.getEyeHeight()-0.03, lookVec.z*64); tessellator.draw();
			 */
			/*
			 * renderer.startDrawingQuads(); renderer.addVertex(-0.03,
			 * p_76986_1_.getEyeHeight()-0.03, -0.03); renderer.addVertex(0.03,
			 * p_76986_1_.getEyeHeight()+0.03, +0.03);
			 * renderer.addVertex(lookVec.x*50+0.03,lookVec.y*64+
			 * p_76986_1_.getEyeHeight()+0.03, lookVec.z*64+0.03);
			 * renderer.addVertex(lookVec.x*50-0.03,lookVec.y*64+
			 * p_76986_1_.getEyeHeight()-0.03, lookVec.z*64-0.03); tessellator.draw();
			 * renderer.startDrawingQuads(); renderer.addVertex(0.03,
			 * p_76986_1_.getEyeHeight()-0.03, 0.03); renderer.addVertex(-0.03,
			 * p_76986_1_.getEyeHeight()+0.03, -0.03);
			 * renderer.addVertex(lookVec.x*50-0.03,lookVec.y*64+
			 * p_76986_1_.getEyeHeight()+0.03, lookVec.z*64-0.03);
			 * renderer.addVertex(lookVec.x*50+0.03,lookVec.y*64+
			 * p_76986_1_.getEyeHeight()-0.03, lookVec.z*64+0.03); tessellator.draw();
			 */
			GlStateManager.color(1.0F, 1.0F, 1.0F, 1F);
			GL11.glDisable(GL11.GL_BLEND);
			GlStateManager.enableTexture2D();
			GlStateManager.enableLighting();
			GlStateManager.popMatrix();
		}
	}
}
