package com.mojang.blaze3d.platform;

import com.terraformersmc.modmenu.util.GlUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.lwjgl.opengl.ARBMultitexture;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GLContext;

@Environment(EnvType.CLIENT)
public class GLX {
	public static int GL_TEXTURE0;
	public static int GL_TEXTURE1;
	private static boolean useMultitextureArb;

	public static void init() {
		GlUtil.init();
		useMultitextureArb = GLContext.getCapabilities().GL_ARB_multitexture && !GLContext.getCapabilities().OpenGL13;
		if (useMultitextureArb) {
			GL_TEXTURE0 = 33984;
			GL_TEXTURE1 = 33985;
		} else {
			GL_TEXTURE0 = 33984;
			GL_TEXTURE1 = 33985;
		}

	}

	public static void activeTexture(int texture) {
		if (useMultitextureArb) {
			ARBMultitexture.glActiveTextureARB(texture);
		} else {
			GL13.glActiveTexture(texture);
		}

	}

	public static void clientActiveTexture(int texture) {
		if (useMultitextureArb) {
			ARBMultitexture.glClientActiveTextureARB(texture);
		} else {
			GL13.glClientActiveTexture(texture);
		}

	}

	public static void multiTexCoord2f(int texture, float s, float t) {
		if (useMultitextureArb) {
			ARBMultitexture.glMultiTexCoord2fARB(texture, s, t);
		} else {
			GL13.glMultiTexCoord2f(texture, s, t);
		}

	}
}

