package rafradek.tf2weapons.client.gui.inventory;


import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.resources.ResourceLocation;
import rafradek.tf2weapons.TF2weapons;
import rafradek.tf2weapons.inventory.ContainerRobotDeploy;
import rafradek.tf2weapons.tileentity.TileEntityRobotDeploy;

public class GuiRobotDeploy extends GuiContainer {

	private static final ResourceLocation ROBOT_DEPLOY_TEXTURE = new ResourceLocation(TF2weapons.MOD_ID, "textures/gui/container/robotdeploy.png");

	TileEntityRobotDeploy BlockEntity;
	InventoryPlayer player;

	public GuiRobotDeploy(InventoryPlayer player, TileEntityRobotDeploy BlockEntity) {
		super(new ContainerRobotDeploy(player, BlockEntity));
		this.BlockEntity = BlockEntity;
		this.player = player;
		ySize = 193;
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		drawDefaultBackground();
		super.drawScreen(mouseX, mouseY, partialTicks);
		renderHoveredToolTip(mouseX, mouseY);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		mc.getTextureManager().bindTexture(ROBOT_DEPLOY_TEXTURE);
		int i = (width - xSize) / 2;
		int j = (height - ySize) / 2;
		drawTexturedModalRect(i, j, 0, 0, xSize, ySize);
		float pr = (float) ((ContainerRobotDeploy) inventorySlots).progress / ((ContainerRobotDeploy) inventorySlots).maxprogress;
		if (BlockEntity.produceGiant()) {
			drawTexturedModalRect(i + 142, j + 23, 192, 31, 24, 48);
			drawTexturedModalRect(i + 142, j + 23 + (int) (48 * (1 - pr)), 216, 31 + (int) (48 * (1 - pr)), 24, (int) (48 * pr));
		} else drawTexturedModalRect(i + 146, j + 38 + (int) (32 * (1 - pr)), 176, 31 + (int) (32 * (1 - pr)), 16, (int) (32 * pr));
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		String s = "container.robotdeploy";
		fontRenderer.drawString(s, xSize / 2 - fontRenderer.getStringWidth(s) / 2, 6, 4210752);
		fontRenderer.drawString(player.getDisplayName().getUnformattedText(), 8, ySize - 96 + 2,
				4210752);
		fontRenderer.drawString("x" + BlockEntity.getRequirement(0), 64, 18, 4210752);
		fontRenderer.drawString("x" + BlockEntity.getRequirement(1), 46, 39, 4210752);
		fontRenderer.drawString("x" + BlockEntity.getRequirement(2), 46, 60, 4210752);
		fontRenderer.drawString("x" + BlockEntity.getCurrencyRequirement(), 64, 81, 4210752);
		/*
		 * if (this.BlockEntity.produceGiant())
		 * this.fontRenderer.drawString("x2"+this.BlockEntity.getRequirement(2), 135, 18,
		 * 4210752);
		 */
	}
}
