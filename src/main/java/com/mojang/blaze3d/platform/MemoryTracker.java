package com.mojang.blaze3d.platform;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.lwjgl.opengl.GL11;

@Environment(EnvType.CLIENT)
public class MemoryTracker {
	private static final Map LIST_RANGES = new HashMap();
	private static final List LISTS = new ArrayList();

	public static synchronized int getLists(int s) {
		int var1 = GL11.glGenLists(s);
		LIST_RANGES.put(var1, s);
		return var1;
	}

	public static synchronized void releaseList(int list) {
		GL11.glDeleteLists(list, (Integer)LIST_RANGES.remove(list));
	}

	public static synchronized void m_3478385() {
		for(int var0 = 0; var0 < LISTS.size(); ++var0) {
			GL11.glDeleteTextures((Integer)LISTS.get(var0));
		}

		LISTS.clear();
	}

	public static synchronized void releaseLists() {
		Iterator var0 = LIST_RANGES.entrySet().iterator();

		while(var0.hasNext()) {
			Map.Entry var1 = (Map.Entry)var0.next();
			GL11.glDeleteLists((Integer)var1.getKey(), (Integer)var1.getValue());
		}

		LIST_RANGES.clear();
		m_3478385();
	}

	public static synchronized ByteBuffer createByteBuffer(int capacity) {
		return ByteBuffer.allocateDirect(capacity).order(ByteOrder.nativeOrder());
	}

	public static IntBuffer createIntBuffer(int capacity) {
		return createByteBuffer(capacity << 2).asIntBuffer();
	}

	public static FloatBuffer createFloatBuffer(int capacity) {
		return createByteBuffer(capacity << 2).asFloatBuffer();
	}
}

