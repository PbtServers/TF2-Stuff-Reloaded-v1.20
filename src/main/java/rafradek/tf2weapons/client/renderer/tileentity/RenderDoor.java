package rafradek.tf2weapons.client.renderer.BlockEntity;


import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.BlockEntity.TileEntitySpecialRenderer;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import rafradek.tf2weapons.TF2weapons;
import rafradek.tf2weapons.block.BlockOverheadDoor;
import rafradek.tf2weapons.tileentity.TileEntityOverheadDoor;

public class RenderDoor extends TileEntitySpecialRenderer<TileEntityOverheadDoor> {

	@Override
	public void render(TileEntityOverheadDoor te, double x, double y, double z, float partialTicks, int destroyStage,
			float alpha) {
		BlockPos orig = te.getPos();
		if (!te.getWorld().isAirBlock(new BlockPos(orig.getX(), te.minBounds.getY(), orig.getZ()))
				|| te.getWorld().getBlockState(orig).getBlock() != TF2weapons.blockOverheadDoor)
			return;
		BlockState state = te.getWorld().getBlockState(orig).withProperty(BlockOverheadDoor.HOLDER, false)
				.withProperty(BlockOverheadDoor.SLIDING, false);
		this.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		float amountScrolled = te.amountScrolled - te.motion + partialTicks * te.motion;
		float off = amountScrolled - (int) amountScrolled;
		for (int i = 0; i <= amountScrolled; i++) {
			GlStateManager.pushMatrix();
			GlStateManager.disableLighting();
			Tesselator tessellator = Tesselator.getInstance();
			BufferBuilder bufferbuilder = tessellator.getBuffer();
			BlockPos blockpos = orig.down(i);
			// GlStateManager.translate(-blockpos.getX(), -blockpos.getY(),
			// -blockpos.getZ());

			float yscale = 1;
			if (i == 0) {
				GlStateManager.scale(1f, off, 1f);
				yscale = 1 / off;
			}
			GlStateManager.translate((float) x, (float) (y - i + 1 - off) * yscale, (float) z);
			bufferbuilder.begin(7, DefaultVertexFormat.BLOCK);

			bufferbuilder.setTranslation(-blockpos.getX(), -blockpos.getY(), -blockpos.getZ());
			BlockRendererDispatcher blockrendererdispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();
			blockrendererdispatcher.getBlockModelRenderer().renderModel(te.getWorld(),
					blockrendererdispatcher.getModelForState(state), state, blockpos, bufferbuilder, false,
					Mth.getPositionRandom(blockpos));
			tessellator.draw();
			bufferbuilder.setTranslation(0, 0, 0);
			GlStateManager.enableLighting();
			GlStateManager.popMatrix();
		}
		super.render(te, x, y, z, partialTicks, destroyStage, alpha);
	}
}
