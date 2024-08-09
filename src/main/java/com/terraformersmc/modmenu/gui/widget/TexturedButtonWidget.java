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
	public void render(Minecraft minecraft, int mouseX, int mouseY) {
		if (this.visible) {
			minecraft.getTextureManager().bind(this.texture);
			GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
			boolean hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
			int u = this.u;
			int v = this.v;
			if (hovered) {
				v += this.vOff;
			}
			DrawingUtil.drawTexture(this.x, this.y, u, v, this.width, this.height, this.textureWidth, this.textureHeight);
		}
	}
}
