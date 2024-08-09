package com.terraformersmc.modmenu.util;

import com.terraformersmc.modmenu.config.ModMenuConfig;
import com.terraformersmc.modmenu.util.mod.Mod;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatMessageComponent;
import net.minecraft.Minecraft;
import net.minecraft.Gui;

import java.util.List;
import java.util.Random;

import org.lwjgl.opengl.GL11;

@Environment(EnvType.CLIENT)
public class DrawingUtil {
	private static final Minecraft CLIENT = Minecraft.getMinecraft();

	public static void drawRandomVersionBackground(Mod mod, int x, int y, int width, int height) {
		int seed = mod.getName().hashCode() + mod.getVersion().hashCode();
		Random random = new Random(seed);
		int color = 0xFF000000 | MathUtil.toRgb(MathUtil.nextFloat(random, 0f, 1f), MathUtil.nextFloat(random, 0.7f, 0.8f), 0.9f);
		if (!ModMenuConfig.RANDOM_JAVA_COLORS.getValue()) {
			color = 0xFFDD5656;
		}
		GL11.glColor4f(1f, 1f, 1f, 1f);
		Gui.fill(x, y, x + width, y + height, color);
	}

	public static void drawWrappedString(String string, int x, int y, int wrapWidth, int lines, int color) {
		while (string != null && string.endsWith("\n")) {
			string = string.substring(0, string.length() - 1);
		}
		List<String> strings = CLIENT.fontRenderer.split(string, wrapWidth);
		for (int i = 0; i < strings.size(); i++) {
			if (i >= lines) {
				break;
			}
			String renderable = strings.get(i);
			if (i == lines - 1 && strings.size() > lines) {
				renderable += "...";
			}
			int x1 = x;
			if (CLIENT.fontRenderer.isBidirectional()) {
				int width = CLIENT.fontRenderer.getStringWidth(renderable);
				x1 += (float) (wrapWidth - width);
			}
			CLIENT.fontRenderer.drawString(renderable, x1, y + i * CLIENT.fontRenderer.FONT_HEIGHT, color);
		}
	}

	public static void drawBadge(int x, int y, int tagWidth, ChatMessageComponent text, int outlineColor, int fillColor, int textColor) {
		Gui.drawRect(x + 1, y - 1, x + tagWidth, y, outlineColor);
		Gui.drawRect(x, y, x + 1, y + CLIENT.fontRenderer.FONT_HEIGHT, outlineColor);
		Gui.drawRect(x + 1, y + 1 + CLIENT.fontRenderer.FONT_HEIGHT - 1, x + tagWidth, y + CLIENT.fontRenderer.FONT_HEIGHT + 1, outlineColor);
		Gui.drawRect( x + tagWidth, y, x + tagWidth + 1, y + CLIENT.fontRenderer.FONT_HEIGHT, outlineColor);
		Gui.drawRect( x + 1, y, x + tagWidth, y + CLIENT.fontRenderer.FONT_HEIGHT, fillColor);
		String s = text.buildString(true);
		CLIENT.fontRenderer.drawString(s, (int) (x + 1 + (tagWidth - CLIENT.fontRenderer.getStringWidth(s)) / (float) 2), y + 1, textColor);
	}

	public static void drawTexture(int x, int y, float u, float v, int width, int height, float scaleU, float scaleV) {
		float invertedScaleU = 1.0f / scaleU;
		float invertedScaleV = 1.0f / scaleV;
		BufferBuilder bufferBuilder = BufferBuilder.INSTANCE;
		bufferBuilder.start();
		bufferBuilder.vertex(x, y + height, 0.0, u * invertedScaleU, (v + (float) height) * invertedScaleV);
		bufferBuilder.vertex(x + width, y + height, 0.0, (u + (float) width) * invertedScaleU, (v + (float) height) * invertedScaleV);
		bufferBuilder.vertex(x + width, y, 0.0, (u + (float) width) * invertedScaleU, v * invertedScaleV);
		bufferBuilder.vertex(x, y, 0.0, u * invertedScaleU, v * invertedScaleV);
		bufferBuilder.end();
	}
}
