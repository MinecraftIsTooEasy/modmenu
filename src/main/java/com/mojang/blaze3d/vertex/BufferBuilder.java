package com.mojang.blaze3d.vertex;

import com.mojang.blaze3d.platform.GLX;
import com.mojang.blaze3d.platform.MemoryTracker;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Tessellator;
import org.lwjgl.opengl.ARBVertexBufferObject;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLContext;

@Environment(EnvType.CLIENT)
public class BufferBuilder extends Tessellator {
	private static boolean f_5537920;
	private static boolean f_6972226;
	private ByteBuffer byteBuffer;
	private IntBuffer intBuffer;
	private FloatBuffer floatBuffer;
	private ShortBuffer shortBuffer;
	private int[] buffer;
	private int vertexCount;
	private double u;
	private double v;
	private int brightness;
	private int color;
	private boolean colored;
	private boolean textured;
	private boolean hasBrightness;
	private boolean hasNormals;
	private int index;
	private int nextVertexCount;
	private boolean uncolored;
	private int drawMode;
	private double offsetX;
	private double offsetY;
	private double offsetZ;
	private int normals;
	public static final BufferBuilder INSTANCE = new BufferBuilder(2097152);
	private boolean tessellating;
	private boolean f_4060651;
	private IntBuffer f_1177867;
	private int f_4098633;
	private int f_3098389 = 10;
	private int size;

	private BufferBuilder(int size) {
		this.size = size;
		this.byteBuffer = MemoryTracker.createByteBuffer(size * 4);
		this.intBuffer = this.byteBuffer.asIntBuffer();
		this.floatBuffer = this.byteBuffer.asFloatBuffer();
		this.shortBuffer = this.byteBuffer.asShortBuffer();
		this.buffer = new int[size];
		this.f_4060651 = f_6972226 && GLContext.getCapabilities().GL_ARB_vertex_buffer_object;
		if (this.f_4060651) {
			this.f_1177867 = MemoryTracker.createIntBuffer(this.f_3098389);
			ARBVertexBufferObject.glGenBuffersARB(this.f_1177867);
		}

	}

	public int end() {
		if (!this.tessellating) {
			throw new IllegalStateException("Not tesselating!");
		} else {
			this.tessellating = false;
			if (this.vertexCount > 0) {
				this.intBuffer.clear();
				this.intBuffer.put(this.buffer, 0, this.index);
				this.byteBuffer.position(0);
				this.byteBuffer.limit(this.index * 4);
				if (this.f_4060651) {
					this.f_4098633 = (this.f_4098633 + 1) % this.f_3098389;
					ARBVertexBufferObject.glBindBufferARB(34962, this.f_1177867.get(this.f_4098633));
					ARBVertexBufferObject.glBufferDataARB(34962, this.byteBuffer, 35040);
				}

				if (this.textured) {
					if (this.f_4060651) {
						GL11.glTexCoordPointer(2, 5126, 32, 12L);
					} else {
						this.floatBuffer.position(3);
						GL11.glTexCoordPointer(2, 32, this.floatBuffer);
					}

					GL11.glEnableClientState(32888);
				}

				if (this.hasBrightness) {
					GLX.clientActiveTexture(GLX.GL_TEXTURE1);
					if (this.f_4060651) {
						GL11.glTexCoordPointer(2, 5122, 32, 28L);
					} else {
						this.shortBuffer.position(14);
						GL11.glTexCoordPointer(2, 32, this.shortBuffer);
					}

					GL11.glEnableClientState(32888);
					GLX.clientActiveTexture(GLX.GL_TEXTURE0);
				}

				if (this.colored) {
					if (this.f_4060651) {
						GL11.glColorPointer(4, 5121, 32, 20L);
					} else {
						this.byteBuffer.position(20);
						GL11.glColorPointer(4, true, 32, this.byteBuffer);
					}

					GL11.glEnableClientState(32886);
				}

				if (this.hasNormals) {
					if (this.f_4060651) {
						GL11.glNormalPointer(5121, 32, 24L);
					} else {
						this.byteBuffer.position(24);
						GL11.glNormalPointer(32, this.byteBuffer);
					}

					GL11.glEnableClientState(32885);
				}

				if (this.f_4060651) {
					GL11.glVertexPointer(3, 5126, 32, 0L);
				} else {
					this.floatBuffer.position(0);
					GL11.glVertexPointer(3, 32, this.floatBuffer);
				}

				GL11.glEnableClientState(32884);
				if (this.drawMode == 7 && f_5537920) {
					GL11.glDrawArrays(4, 0, this.vertexCount);
				} else {
					GL11.glDrawArrays(this.drawMode, 0, this.vertexCount);
				}

				GL11.glDisableClientState(32884);
				if (this.textured) {
					GL11.glDisableClientState(32888);
				}

				if (this.hasBrightness) {
					GLX.clientActiveTexture(GLX.GL_TEXTURE1);
					GL11.glDisableClientState(32888);
					GLX.clientActiveTexture(GLX.GL_TEXTURE0);
				}

				if (this.colored) {
					GL11.glDisableClientState(32886);
				}

				if (this.hasNormals) {
					GL11.glDisableClientState(32885);
				}
			}

			int var1 = this.index * 4;
			this.clear();
			return var1;
		}
	}

	private void clear() {
		this.vertexCount = 0;
		this.byteBuffer.clear();
		this.index = 0;
		this.nextVertexCount = 0;
	}

	public void start() {
		this.start(7);
	}

	public void start(int drawMode) {
		if (this.tessellating) {
			throw new IllegalStateException("Already tesselating!");
		} else {
			this.tessellating = true;
			this.clear();
			this.drawMode = drawMode;
			this.hasNormals = false;
			this.colored = false;
			this.textured = false;
			this.hasBrightness = false;
			this.uncolored = false;
		}
	}

	public void texture(double u, double v) {
		this.textured = true;
		this.u = u;
		this.v = v;
	}

	public void brightness(int brightness) {
		this.hasBrightness = true;
		this.brightness = brightness;
	}

	public void color(float r, float g, float b) {
		this.color((int)(r * 255.0F), (int)(g * 255.0F), (int)(b * 255.0F));
	}

	public void color(float r, float g, float b, float a) {
		this.color((int)(r * 255.0F), (int)(g * 255.0F), (int)(b * 255.0F), (int)(a * 255.0F));
	}

	public void color(int r, int g, int b) {
		this.color(r, g, b, 255);
	}

	public void color(int r, int g, int b, int a) {
		if (!this.uncolored) {
			if (r > 255) {
				r = 255;
			}

			if (g > 255) {
				g = 255;
			}

			if (b > 255) {
				b = 255;
			}

			if (a > 255) {
				a = 255;
			}

			if (r < 0) {
				r = 0;
			}

			if (g < 0) {
				g = 0;
			}

			if (b < 0) {
				b = 0;
			}

			if (a < 0) {
				a = 0;
			}

			this.colored = true;
			if (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN) {
				this.color = a << 24 | b << 16 | g << 8 | r;
			} else {
				this.color = r << 24 | g << 16 | b << 8 | a;
			}

		}
	}

	public void vertex(double x, double y, double z, double u, double v) {
		this.texture(u, v);
		this.vertex(x, y, z);
	}

	public void vertex(double x, double y, double z) {
		++this.nextVertexCount;
		if (this.drawMode == 7 && f_5537920 && this.nextVertexCount % 4 == 0) {
			for(int var7 = 0; var7 < 2; ++var7) {
				int var8 = 8 * (3 - var7);
				if (this.textured) {
					this.buffer[this.index + 3] = this.buffer[this.index - var8 + 3];
					this.buffer[this.index + 4] = this.buffer[this.index - var8 + 4];
				}

				if (this.hasBrightness) {
					this.buffer[this.index + 7] = this.buffer[this.index - var8 + 7];
				}

				if (this.colored) {
					this.buffer[this.index + 5] = this.buffer[this.index - var8 + 5];
				}

				this.buffer[this.index + 0] = this.buffer[this.index - var8 + 0];
				this.buffer[this.index + 1] = this.buffer[this.index - var8 + 1];
				this.buffer[this.index + 2] = this.buffer[this.index - var8 + 2];
				++this.vertexCount;
				this.index += 8;
			}
		}

		if (this.textured) {
			this.buffer[this.index + 3] = Float.floatToRawIntBits((float)this.u);
			this.buffer[this.index + 4] = Float.floatToRawIntBits((float)this.v);
		}

		if (this.hasBrightness) {
			this.buffer[this.index + 7] = this.brightness;
		}

		if (this.colored) {
			this.buffer[this.index + 5] = this.color;
		}

		if (this.hasNormals) {
			this.buffer[this.index + 6] = this.normals;
		}

		this.buffer[this.index + 0] = Float.floatToRawIntBits((float)(x + this.offsetX));
		this.buffer[this.index + 1] = Float.floatToRawIntBits((float)(y + this.offsetY));
		this.buffer[this.index + 2] = Float.floatToRawIntBits((float)(z + this.offsetZ));
		this.index += 8;
		++this.vertexCount;
		if (this.vertexCount % 4 == 0 && this.index >= this.size - 32) {
			this.end();
			this.tessellating = true;
		}

	}

	public void color(int rgb) {
		int var2 = rgb >> 16 & 255;
		int var3 = rgb >> 8 & 255;
		int var4 = rgb & 255;
		this.color(var2, var3, var4);
	}

	public void color(int rgb, int a) {
		int var3 = rgb >> 16 & 255;
		int var4 = rgb >> 8 & 255;
		int var5 = rgb & 255;
		this.color(var3, var4, var5, a);
	}

	public void uncolored() {
		this.uncolored = true;
	}

	public void normal(float x, float y, float z) {
		this.hasNormals = true;
		byte var4 = (byte)((int)(x * 127.0F));
		byte var5 = (byte)((int)(y * 127.0F));
		byte var6 = (byte)((int)(z * 127.0F));
		this.normals = var4 & 255 | (var5 & 255) << 8 | (var6 & 255) << 16;
	}

	public void offset(double offsetX, double offsetY, double offsetZ) {
		this.offsetX = offsetX;
		this.offsetY = offsetY;
		this.offsetZ = offsetZ;
	}

	public void addOffset(float offsetX, float offsetY, float offsetZ) {
		this.offsetX += (double)offsetX;
		this.offsetY += (double)offsetY;
		this.offsetZ += (double)offsetZ;
	}
}

