package com.hbm.forgefluid;

import com.hbm.inventory.gui.GuiInfoContainer;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.IIcon;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;

public class FFUtils {
	public static void drawLiquid(FluidTank tank, int guiLeft, int guiTop,
			float zLevel, int sizeX, int sizeY, int offsetX, int offsetY) {
		if (tank.getFluid() != null) {
			IIcon liquidIcon = tank.getFluid().getFluid().getStillIcon();
			if (liquidIcon != null) {
				int level = (int) (((double) tank.getFluidAmount() / (double) tank
						.getCapacity()) * 52.0D);

				drawFull(tank, guiLeft, guiTop, zLevel, liquidIcon, level,
						sizeX, sizeY, offsetX, offsetY);
			}
		}
	}

	public static void drawFull(FluidTank tank, int guiLeft, int guiTop,
			float zLevel, IIcon liquidIcon, int level, int sizeX, int sizeY,
			int offsetX, int offsetY) {
		int color = tank.getFluid().getFluid().getColor();

		float left = liquidIcon.getMinU();
		float right = liquidIcon.getMaxU();
		float up = liquidIcon.getMinV();
		float down = liquidIcon.getMaxV();
		float right2 = liquidIcon.getInterpolatedU(2);
		float up2 = liquidIcon.getInterpolatedV(16 - (level % 16));
		int number = Math.floorDiv(level, 16);

		Tessellator tes = Tessellator.instance;
		tes.startDrawingQuads();
		tes.setColorOpaque_I(color);
		// pixels 17 to 69 (-52)

		for (int i = 0; i < number; i++) {
			tes.addVertexWithUV(guiLeft + offsetX, guiTop + offsetY - 16
					- (i * 16), zLevel, left, up);
			tes.addVertexWithUV(guiLeft + offsetX, guiTop + offsetY - (i * 16),
					zLevel, left, down);
			tes.addVertexWithUV(guiLeft + offsetX + 16, guiTop + offsetY
					- (i * 16), zLevel, right, down);
			tes.addVertexWithUV(guiLeft + offsetX + 16, guiTop + offsetY - 16
					- (i * 16), zLevel, right, up);

			/*
			 * tes.addVertexWithUV(guiLeft + 71 + 16, guiTop + 69 - 16 - (i *
			 * 16), zLevel, left, up); tes.addVertexWithUV(guiLeft + 71 + 16,
			 * guiTop + 69 - (i * 16), zLevel, left, down);
			 * tes.addVertexWithUV(guiLeft + 71 + 32, guiTop + 69 - (i * 16),
			 * zLevel, right, down); tes.addVertexWithUV(guiLeft + 71 + 32,
			 * guiTop + 69 - 16 - (i * 16), zLevel, right, up);
			 * 
			 * tes.addVertexWithUV(guiLeft + 71 + 32, guiTop + 69 - 16 - (i *
			 * 16), zLevel, left, up); tes.addVertexWithUV(guiLeft + 71 + 32,
			 * guiTop + 69 - (i * 16), zLevel, left, down);
			 * tes.addVertexWithUV(guiLeft + 71 + 34, guiTop + 69 - (i * 16),
			 * zLevel, right2, down); tes.addVertexWithUV(guiLeft + 71 + 34,
			 * guiTop + 69 - 16 - (i * 16), zLevel, right2, up);
			 */
		}
		tes.addVertexWithUV(guiLeft + offsetX, guiTop + offsetY - (number * 16)
				- (level % 16), zLevel, left, up2);
		tes.addVertexWithUV(guiLeft + offsetX,
				guiTop + offsetY - (number * 16), zLevel, left, down);
		tes.addVertexWithUV(guiLeft + offsetX + 16, guiTop + offsetY
				- (number * 16), zLevel, right, down);
		tes.addVertexWithUV(guiLeft + offsetX + 16, guiTop + offsetY
				- (number * 16) - (level % 16), zLevel, right, up2);

		/*
		 * tes.addVertexWithUV(guiLeft + 71 + 16, guiTop + 69 - (number * 16) -
		 * (level % 16), zLevel, left, up2); tes.addVertexWithUV(guiLeft + 71 +
		 * 16, guiTop + 69 - (number * 16), zLevel, left, down);
		 * tes.addVertexWithUV(guiLeft + 71 + 32, guiTop + 69 - (number * 16),
		 * zLevel, right, down); tes.addVertexWithUV(guiLeft + 71 + 32, guiTop +
		 * 69 - (number * 16) - (level % 16), zLevel, right, up2);
		 * 
		 * tes.addVertexWithUV(guiLeft + 71 + 32, guiTop + 69 - (number * 16) -
		 * (level % 16), zLevel, left, up2); tes.addVertexWithUV(guiLeft + 71 +
		 * 32, guiTop + 69 - (number * 16), zLevel, left, down);
		 * tes.addVertexWithUV(guiLeft + 71 + 34, guiTop + 69 - (number * 16),
		 * zLevel, right2, down); tes.addVertexWithUV(guiLeft + 71 + 34, guiTop
		 * + 69 - (number * 16) - (level % 16), zLevel, right2, up2);
		 */

		tes.draw();

	}

	public static void renderTankInfo(GuiInfoContainer gui, int mouseX,
			int mouseY, int x, int y, int width, int height, FluidTank fluidTank) {
		if (x <= mouseX && x + width > mouseX && y < mouseY
				&& y + height >= mouseY) {
			if (fluidTank.getFluid() != null) {
				gui.drawFluidInfo(
						new String[] {
								I18n.format(fluidTank.getFluid().getFluid()
										.getUnlocalizedName()),
								fluidTank.getFluidAmount() + "/"
										+ fluidTank.getCapacity() + "mB" },
						mouseX, mouseY);
			} else {
				gui.drawFluidInfo(new String[] {I18n.format("None"), fluidTank.getFluidAmount() + "/" + fluidTank.getCapacity() + "mB" }, mouseX, mouseY);
			}
		}
	}
}