package com.mojang.blaze3d.platform;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.IntBuffer;
import javax.imageio.ImageIO;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Minecraft;
import net.minecraft.DynamicTexture;
import net.minecraft.ResourceLocation;
import net.minecraft.ResourceManager;
import org.lwjgl.opengl.GL11;

@Environment(EnvType.CLIENT)
public class TextureUtil {
	private static final IntBuffer BUFFER = MemoryTracker.createIntBuffer(4194304);
	public static final DynamicTexture MISSING_TEXTURE = new DynamicTexture(16, 16);
	public static final int[] MISSING_DATA;

	public static int m_9352345() {
		return GL11.glGenTextures();
	}

	public static int uploadTexture(int id, BufferedImage image) {
		return upload(id, image, false, false);
	}

	public static void uploadTexture(int id, int[] pixels, int width, int height) {
		bind(id);
		upload(pixels, width, height, 0, 0, false, false);
	}

	public static void upload(int[] is, int i, int j, int k, int l, boolean bl, boolean bl2) {
		int var7 = 4194304 / i;
		setTextureFilter(bl);
		setTextureClamp(bl2);

		int var10;
		for(int var8 = 0; var8 < i * j; var8 += i * var10) {
			int var9 = var8 / i;
			var10 = Math.min(var7, j - var9);
			int var11 = i * var10;
			putInBufferAt(is, var8, var11);
			GL11.glTexSubImage2D(3553, 0, k, l + var9, i, var10, 32993, 33639, BUFFER);
		}

	}

	public static int upload(int texture, BufferedImage image, boolean blur, boolean mipmap) {
		prepare(texture, image.getWidth(), image.getHeight());
		return upload(texture, image, 0, 0, blur, mipmap);
	}

	public static void prepare(int id, int width, int height) {
		bind(id);
		GL11.glTexImage2D(3553, 0, 6408, width, height, 0, 32993, 33639, (IntBuffer)null);
	}

	public static int upload(int id, BufferedImage texture, int xOffset, int yOffset, boolean blur, boolean mipmap) {
		bind(id);
		upload(texture, xOffset, yOffset, blur, mipmap);
		return id;
	}

	private static void upload(BufferedImage texture, int xOffset, int yOffset, boolean blur, boolean mipmap) {
		int var5 = texture.getWidth();
		int var6 = texture.getHeight();
		int var7 = 4194304 / var5;
		int[] var8 = new int[var7 * var5];
		setTextureFilter(blur);
		setTextureClamp(mipmap);

		for(int var9 = 0; var9 < var5 * var6; var9 += var5 * var7) {
			int var10 = var9 / var5;
			int var11 = Math.min(var7, var6 - var10);
			int var12 = var5 * var11;
			texture.getRGB(0, var10, var5, var11, var8, 0, var5);
			putInBuffer(var8, var12);
			GL11.glTexSubImage2D(3553, 0, xOffset, yOffset + var10, var5, var11, 32993, 33639, BUFFER);
		}

	}

	private static void setTextureClamp(boolean clamp) {
		if (clamp) {
			GL11.glTexParameteri(3553, 10242, 10496);
			GL11.glTexParameteri(3553, 10243, 10496);
		} else {
			GL11.glTexParameteri(3553, 10242, 10497);
			GL11.glTexParameteri(3553, 10243, 10497);
		}

	}

	private static void setTextureFilter(boolean bl) {
		if (bl) {
			GL11.glTexParameteri(3553, 10241, 9729);
			GL11.glTexParameteri(3553, 10240, 9729);
		} else {
			GL11.glTexParameteri(3553, 10241, 9728);
			GL11.glTexParameteri(3553, 10240, 9728);
		}

	}

	private static void putInBuffer(int[] texture, int size) {
		putInBufferAt(texture, 0, size);
	}

	private static void putInBufferAt(int[] texture, int offset, int size) {
		int[] var3 = texture;
		if (Minecraft.getMinecraft().gameSettings.anaglyph) {
			var3 = getAnaglyphColors(texture);
		}

		BUFFER.clear();
		BUFFER.put(var3, offset, size);
		BUFFER.position(0).limit(size);
	}

	static void bind(int texture) {
		GL11.glBindTexture(3553, texture);
	}

	public static int[] getPixels(ResourceManager resourceManager, ResourceLocation location) throws IOException {
		BufferedImage var2 = ImageIO.read(resourceManager.getResource(location).getInputStream());
		int var3 = var2.getWidth();
		int var4 = var2.getHeight();
		int[] var5 = new int[var3 * var4];
		var2.getRGB(0, 0, var3, var4, var5, 0, var3);
		return var5;
	}

	public static int[] getAnaglyphColors(int[] colors) {
		int[] var1 = new int[colors.length];

		for(int var2 = 0; var2 < colors.length; ++var2) {
			int var3 = colors[var2] >> 24 & 255;
			int var4 = colors[var2] >> 16 & 255;
			int var5 = colors[var2] >> 8 & 255;
			int var6 = colors[var2] & 255;
			int var7 = (var4 * 30 + var5 * 59 + var6 * 11) / 100;
			int var8 = (var4 * 30 + var5 * 70) / 100;
			int var9 = (var4 * 30 + var6 * 70) / 100;
			var1[var2] = var3 << 24 | var7 << 16 | var8 << 8 | var9;
		}

		return var1;
	}

	static {
		MISSING_DATA = MISSING_TEXTURE.getTextureData();
		int var0 = -16777216;
		int var1 = -524040;
		int[] var2 = new int[]{-524040, -524040, -524040, -524040, -524040, -524040, -524040, -524040};
		int[] var3 = new int[]{-16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216, -16777216};
		int var4 = var2.length;

		for(int var5 = 0; var5 < 16; ++var5) {
			System.arraycopy(var5 < var4 ? var2 : var3, 0, MISSING_DATA, 16 * var5, var4);
			System.arraycopy(var5 < var4 ? var3 : var2, 0, MISSING_DATA, 16 * var5 + var4, var4);
		}

		MISSING_TEXTURE.updateDynamicTexture();
	}
}

