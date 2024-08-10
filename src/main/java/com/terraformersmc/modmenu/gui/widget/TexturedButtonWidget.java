package com.terraformersmc.modmenu.gui.widget;

import org.lwjgl.opengl.GL11;

import com.terraformersmc.modmenu.util.DrawingUtil;

import net.minecraft.Minecraft;
import net.minecraft.GuiButton;
import net.minecraft.ResourceLocation;

public class TexturedButtonWidget extends GuiButton {
	protected final ResourceLocation texture;
	protected final int u;
	protected final int v;
	protected final int vOff;
	protected final int textureWidth;
	protected final int textureHeight;

	public TexturedButtonWidget(int id, int x, int y, int width, int height, int u, int v, int hoveredVOffset, ResourceLocation texture, int textureWidth, int textureHeight) {
		super(id, x, y, width, height, "");
		this.texture = texture;
		this.u = u;
		this.v = v;
		this.vOff = hoveredVOffset;
		this.textureWidth = textureWidth;
		this.textureHeight = textureHeight;
	}

	@Override
	public void drawButton(Minecraft minecraft, int mouseX, int mouseY) {
		if (this.drawButton) {
			minecraft.getTextureManager().bindTexture(this.texture);
			GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
			boolean hovered = mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;
			int u = this.u;
			int v = this.v;
			if (hovered) {
				v += this.vOff;
			}
			DrawingUtil.drawTexture(this.xPosition, this.yPosition, u, v, this.width, this.height, this.textureWidth, this.textureHeight);
		}
	}
}
