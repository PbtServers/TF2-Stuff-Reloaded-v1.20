package rafradek.tf2weapons.client.renderer.entity;

import net.minecraft.client.model.ModelBase;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.lwjgl.opengl.GL11;
import rafradek.tf2weapons.TF2weapons;
import rafradek.tf2weapons.client.ClientProxy;
import rafradek.tf2weapons.client.model.ModelBuild;
import rafradek.tf2weapons.client.model.ModelDispenser;
import rafradek.tf2weapons.client.model.ModelDispenser2;
import rafradek.tf2weapons.client.model.ModelDispenser3;
import rafradek.tf2weapons.entity.building.EntityDispenser;
import rafradek.tf2weapons.util.TF2Util;

public class RenderDispenser extends RenderLiving<EntityDispenser> {

	private static final ResourceLocation DISPENSER_RED = new ResourceLocation(TF2weapons.MOD_ID,
			"textures/entity/tf2/red/Dispenser.png");
	private static final ResourceLocation DISPENSER_BLU = new ResourceLocation(TF2weapons.MOD_ID,
			"textures/entity/tf2/blu/Dispenser.png");
	private static final ResourceLocation DISPENSER_ROBOT = new ResourceLocation(TF2weapons.MOD_ID,
			"textures/entity/tf2/robot/Dispenser.png");
	private static final ResourceLocation BOX_RED = new ResourceLocation(TF2weapons.MOD_ID,
			"textures/entity/tf2/red/box.png");
	private static final ResourceLocation BOX_BLU = new ResourceLocation(TF2weapons.MOD_ID,
			"textures/entity/tf2/blu/box.png");

	public ModelBase level1;
	public ModelBase level2;
	public ModelBase level3;
	public ModelBase box;

	public RenderDispenser(RenderManager renderManager) {
		super(renderManager, new ModelDispenser(), 0.8f);
		level1 = this.mainModel;
		level2 = new ModelDispenser2();
		level3 = new ModelDispenser3();
		box = new ModelBuild();
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityDispenser par1EntityLiving) {
		boolean constr = par1EntityLiving.isConstructing();
		switch (par1EntityLiving.getEntTeam()) {
		case 0:
			return constr ? BOX_RED : DISPENSER_RED;
		case 1:
			return constr ? BOX_BLU : DISPENSER_BLU;
		case 2:
			return constr ? BOX_BLU : DISPENSER_ROBOT;
		default:
			return constr ? BOX_BLU : DISPENSER_BLU;
		}
	}

	@Override
	public void doRender(EntityDispenser living, double p_76986_2_, double p_76986_4_, double p_76986_6_,
			float p_76986_8_, float p_76986_9_) {
		if (living.isConstructing())
			this.mainModel = this.box;
		else {
			if (living.getLevel() == 1)
				this.mainModel = this.level1;
			else if (living.getLevel() == 2)
				this.mainModel = this.level2;
			else
				this.mainModel = this.level3;
		}
		super.doRender(living, p_76986_2_, p_76986_4_, p_76986_6_, p_76986_8_, p_76986_9_);
		Tesselator tessellator = Tesselator.getInstance();
		BufferBuilder renderer = tessellator.getBuffer();
		if (living.dispenserTarget != null)
			for (LivingEntity target : living.dispenserTarget) {
				double xPos2 = target.prevPosX + (target.posX - target.prevPosX) * p_76986_9_;
				double yPos2 = target.prevPosY + (target.posY - target.prevPosY) * p_76986_9_;
				double zPos2 = target.prevPosZ + (target.posZ - target.prevPosZ) * p_76986_9_;
				double xDist = xPos2 - (living.prevPosX + (living.posX - living.prevPosX) * p_76986_9_);
				double yDist = (yPos2 + target.height / 2)
						- (living.prevPosY + (living.posY - living.prevPosY) * p_76986_9_ + living.height / 2);
				double zDist = zPos2 - (living.prevPosZ + (living.posZ - living.prevPosZ) * p_76986_9_);
				float f = Mth.sqrt(xDist * xDist + zDist * zDist);
				float fullDist = Mth.sqrt(xDist * xDist + yDist * yDist + zDist * zDist);
				GlStateManager.pushMatrix();
				GlStateManager.translate((float) p_76986_2_, (float) p_76986_4_ + living.height / 2,
						(float) p_76986_6_);
				GL11.glRotatef((float) (Math.atan2(xDist, zDist) * 180.0D / Math.PI), 0.0F, 1.0F, 0.0F);
				GL11.glRotatef((float) (Math.atan2(yDist, f) * 180.0D / Math.PI) * -1, 1.0F, 0.0F, 0.0F);
				GlStateManager.disableTexture2D();
				GlStateManager.disableLighting();
				GL11.glEnable(GL11.GL_BLEND);
				OpenGlHelper.glBlendFunc(770, 771, 1, 0);
				ClientProxy.setColor(TF2Util.getTeamColor(living), 0.23f, 0, 0f, 1f);
				renderer.begin(7, DefaultVertexFormat.POSITION);
				renderer.pos(-0.04, -0.04, 0).endVertex();
				renderer.pos(0.04, 0.04, 0).endVertex();
				renderer.pos(0.04, 0.04, fullDist).endVertex();
				renderer.pos(-0.04, -0.04, fullDist).endVertex();
				tessellator.draw();
				renderer.begin(7, DefaultVertexFormat.POSITION);
				renderer.pos(-0.04, -0.04, fullDist).endVertex();
				renderer.pos(0.04, 0.04, fullDist).endVertex();
				renderer.pos(0.04, 0.04, 0).endVertex();
				renderer.pos(-0.04, -0.04, 0).endVertex();
				tessellator.draw();
				renderer.begin(7, DefaultVertexFormat.POSITION);
				renderer.pos(0.04, -0.04, 0).endVertex();
				renderer.pos(-0.04, 0.04, 0).endVertex();
				renderer.pos(-0.04, 0.04, fullDist).endVertex();
				renderer.pos(0.04, -0.04, fullDist).endVertex();
				tessellator.draw();
				renderer.begin(7, DefaultVertexFormat.POSITION);
				renderer.pos(0.04, -0.04, fullDist).endVertex();
				renderer.pos(-0.04, 0.04, fullDist).endVertex();
				renderer.pos(-0.04, 0.04, 0).endVertex();
				renderer.pos(0.04, -0.04, 0).endVertex();
				tessellator.draw();
				GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
				GL11.glDisable(GL11.GL_BLEND);
				GlStateManager.enableLighting();
				GlStateManager.enableTexture2D();
				GlStateManager.popMatrix();
			}
	}
}
