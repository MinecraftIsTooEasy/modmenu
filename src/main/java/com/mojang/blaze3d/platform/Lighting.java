package com.mojang.blaze3d.platform;

import java.nio.FloatBuffer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Vec3;
import org.lwjgl.opengl.GL11;

@Environment(EnvType.CLIENT)
public class Lighting {
	private static FloatBuffer BUFFER = MemoryTracker.createFloatBuffer(16);
	private static final Vec3 LIGHT_0 = Vec3.createVectorHelper(0.20000000298023224, 1.0, -0.699999988079071).normalize();
	private static final Vec3 LIGHT_1 = Vec3.createVectorHelper(-0.20000000298023224, 1.0, 0.699999988079071).normalize();

	public static void turnOff() {
		GL11.glDisable(2896);
		GL11.glDisable(16384);
		GL11.glDisable(16385);
		GL11.glDisable(2903);
	}

	public static void turnOn() {
		GL11.glEnable(2896);
		GL11.glEnable(16384);
		GL11.glEnable(16385);
		GL11.glEnable(2903);
		GL11.glColorMaterial(1032, 5634);
		float var0 = 0.4F;
		float var1 = 0.6F;
		float var2 = 0.0F;
		GL11.glLight(16384, 4611, getBuffer(LIGHT_0.xCoord, LIGHT_0.yCoord, LIGHT_0.zCoord, 0.0));
		GL11.glLight(16384, 4609, getBuffer(var1, var1, var1, 1.0F));
		GL11.glLight(16384, 4608, getBuffer(0.0F, 0.0F, 0.0F, 1.0F));
		GL11.glLight(16384, 4610, getBuffer(var2, var2, var2, 1.0F));
		GL11.glLight(16385, 4611, getBuffer(LIGHT_1.xCoord, LIGHT_1.yCoord, LIGHT_1.zCoord, 0.0));
		GL11.glLight(16385, 4609, getBuffer(var1, var1, var1, 1.0F));
		GL11.glLight(16385, 4608, getBuffer(0.0F, 0.0F, 0.0F, 1.0F));
		GL11.glLight(16385, 4610, getBuffer(var2, var2, var2, 1.0F));
		GL11.glShadeModel(7424);
		GL11.glLightModel(2899, getBuffer(var0, var0, var0, 1.0F));
	}

	private static FloatBuffer getBuffer(double r, double g, double b, double a) {
		return getBuffer((float)r, (float)g, (float)b, (float)a);
	}

	private static FloatBuffer getBuffer(float r, float g, float b, float a) {
		BUFFER.clear();
		BUFFER.put(r).put(g).put(b).put(a);
		BUFFER.flip();
		return BUFFER;
	}

	public static void turnOnGui() {
		GL11.glPushMatrix();
		GL11.glRotatef(-30.0F, 0.0F, 1.0F, 0.0F);
		GL11.glRotatef(165.0F, 1.0F, 0.0F, 0.0F);
		turnOn();
		GL11.glPopMatrix();
	}
}

