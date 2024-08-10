package com.terraformersmc.modmenu.gui.widget;

import org.lwjgl.opengl.GL11;

import com.terraformersmc.modmenu.util.DrawingUtil;

import net.minecraft.Minecraft;
import net.minecraft.ResourceLocation;

public class UpdateAvailableBadge {
	private static final ResourceLocation UPDATE_ICON = new ResourceLocation("textures/gui/realms/trial_icon.png");

	public static void renderBadge(int x, int y) {
		GL11.glColor4f(1f, 1f, 1f, 1f);
		int animOffset = 0;
		if ((Minecraft.getSystemTime() / 800L & 1L) == 1L) {
			animOffset = 8;
		}
		Minecraft.getMinecraft().getTextureManager().bindTexture(UPDATE_ICON);
		DrawingUtil.drawTexture(x, y, 0f, animOffset, 8, 8, 8, 16);
	}
}
