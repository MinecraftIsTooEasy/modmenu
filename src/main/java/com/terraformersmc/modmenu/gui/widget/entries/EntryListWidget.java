package com.terraformersmc.modmenu.gui.widget.entries;

import com.mojang.blaze3d.vertex.BufferBuilder;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import com.terraformersmc.modmenu.util.GlUtil;

import net.minecraft.Minecraft;
import net.minecraft.Gui;
import net.minecraft.GuiSlot;

public abstract class EntryListWidget extends GuiSlot {

	private boolean scrolling;

	public EntryListWidget(Minecraft minecraft, int i, int j, int k, int l, int m) {
		super(minecraft, i, j, k, l, m);
	}

	@Override
	protected void elementClicked(int index, boolean doubleClick) {
	}

	@Override
	protected boolean isSelected(int index) {
		return false;
	}

	@Override
	protected void drawBackground() {
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float tickDelta) {
		int n;
		int n2;
		int n3;
		int n4;
		int n5;
		this.mouseX = mouseX;
		this.mouseY = mouseY;
		this.drawBackground();
		int n6 = this.getSize();
		int n7 = this.getScrollBarX();
		int n8 = n7 + 6;
		if (mouseX > this.left && mouseX < this.right && mouseY > this.top && mouseY < this.bottom) {
			int n9;
			if (Mouse.isButtonDown(0) && this.isScrolling()) {
				if (this.initialClickY == -1.0f) {
					n9 = 1;
					if (mouseY >= this.top && mouseY <= this.bottom) {
						int n10 = this.width / 2 - this.getRowWidth() / 2;
						n5 = this.width / 2 + this.getRowWidth() / 2;
						n4 = mouseY - this.top - this.field_77242_t + (int) this.amountScrolled - 4;
						n3 = n4 / this.slotHeight;
						if (mouseX >= n10 && mouseX <= n5 && n3 >= 0 && n4 >= 0 && n3 < n6) {
							n2 = n3 == this.selectedElement && Minecraft.getSystemTime() - this.lastClicked < 250L ? 1 : 0;
							this.elementClicked(n3, n2 != 0);
							this.selectedElement = n3;
							this.lastClicked = Minecraft.getSystemTime();
						} else if (mouseX >= n10 && mouseX <= n5 && n4 < 0) {
							this.func_77224_a(mouseX - n10, mouseY - this.top + (int) this.amountScrolled - 4);
							n9 = 0;
						}
						if (mouseX >= n7 && mouseX <= n8) {
							this.scrollMultiplier = -1.0f;
							n2 = this.func_77209_d();
							if (n2 < 1) {
								n2 = 1;
							}
							if ((n = (int) ((float) ((this.bottom - this.top) * (this.bottom - this.top))
									/ (float) this.getContentHeight())) < 32) {
								n = 32;
							}
							if (n > this.bottom - this.top - 8) {
								n = this.bottom - this.top - 8;
							}
							this.scrollMultiplier /= (float) (this.bottom - this.top - n) / (float) n2;
						} else {
							this.scrollMultiplier = 1.0f;
						}
						this.initialClickY = n9 != 0 ? (float) mouseY : -2.0f;
					} else {
						this.initialClickY = -2.0f;
					}
				} else if (this.initialClickY >= 0.0f) {
					this.amountScrolled -= ((float) mouseY - this.initialClickY) * this.scrollMultiplier;
					this.initialClickY = mouseY;
				}
			} else {
				while (!this.mc.gameSettings.touchscreen && Mouse.next()) {
					n9 = Mouse.getEventDWheel();
					if (n9 != 0) {
						if (n9 > 0) {
							n9 = -1;
						} else if (n9 < 0) {
							n9 = 1;
						}
						this.amountScrolled += (float) (n9 * this.slotHeight / 2);
					}
					this.mc.currentScreen.handleMouseInput();
				}
				this.initialClickY = -1.0f;
			}
		}
		this.bindAmountScrolled();
		GL11.glDisable(2896);
		GL11.glDisable(2912);
		BufferBuilder bufferBuilder = BufferBuilder.INSTANCE;
		this.mc.getTextureManager().bindTexture(Gui.optionsBackground);
		GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
		float f = 32.0f;
		bufferBuilder.start();
		bufferBuilder.color(0x202020);
		bufferBuilder.vertex(this.left, this.bottom, 0.0, (float) this.left / f,
				(float) (this.bottom + (int) this.amountScrolled) / f);
		bufferBuilder.vertex(this.right, this.bottom, 0.0, (float) this.right / f,
				(float) (this.bottom + (int) this.amountScrolled) / f);
		bufferBuilder.vertex(this.right, this.top, 0.0, (float) this.right / f,
				(float) (this.top + (int) this.amountScrolled) / f);
		bufferBuilder.vertex(this.left, this.top, 0.0, (float) this.left / f,
				(float) (this.top + (int) this.amountScrolled) / f);
		bufferBuilder.end();
		n5 = this.left + (this.width / 2 - this.getRowWidth() / 2 + 2);
		n4 = this.top + 4 - (int) this.amountScrolled;
		if (this.field_77243_s) {
			this.func_77222_a(n5, n4, bufferBuilder);
		}
		this.renderList(n5, n4, mouseX, mouseY);
		GL11.glDisable(2929);
		n3 = 4;
		this.renderHoleBackground(0, this.top, 255, 255);
		this.renderHoleBackground(this.bottom, this.height, 255, 255);
		GL11.glEnable(3042);
		GlUtil.blendFuncSeparate(770, 771, 0, 1);
		GL11.glDisable(3008);
		GL11.glShadeModel(7425);
		GL11.glDisable(3553);
		bufferBuilder.start();
		bufferBuilder.color(0, 0);
		bufferBuilder.vertex(this.left, this.top + n3, 0.0, 0.0, 1.0);
		bufferBuilder.vertex(this.right, this.top + n3, 0.0, 1.0, 1.0);
		bufferBuilder.color(0, 255);
		bufferBuilder.vertex(this.right, this.top, 0.0, 1.0, 0.0);
		bufferBuilder.vertex(this.left, this.top, 0.0, 0.0, 0.0);
		bufferBuilder.end();
		bufferBuilder.start();
		bufferBuilder.color(0, 255);
		bufferBuilder.vertex(this.left, this.bottom, 0.0, 0.0, 1.0);
		bufferBuilder.vertex(this.right, this.bottom, 0.0, 1.0, 1.0);
		bufferBuilder.color(0, 0);
		bufferBuilder.vertex(this.right, this.bottom - n3, 0.0, 1.0, 0.0);
		bufferBuilder.vertex(this.left, this.bottom - n3, 0.0, 0.0, 0.0);
		bufferBuilder.end();
		n2 = this.func_77209_d();
		if (n2 > 0) {
			int n11;
			n = (this.bottom - this.top) * (this.bottom - this.top) / this.getContentHeight();
			if (n < 32) {
				n = 32;
			}
			if (n > this.bottom - this.top - 8) {
				n = this.bottom - this.top - 8;
			}
			if ((n11 = (int) this.amountScrolled * (this.bottom - this.top - n) / n2 + this.top) < this.top) {
				n11 = this.top;
			}
			bufferBuilder.start();
			bufferBuilder.color(0, 255);
			bufferBuilder.vertex(n7, this.bottom, 0.0, 0.0, 1.0);
			bufferBuilder.vertex(n8, this.bottom, 0.0, 1.0, 1.0);
			bufferBuilder.vertex(n8, this.top, 0.0, 1.0, 0.0);
			bufferBuilder.vertex(n7, this.top, 0.0, 0.0, 0.0);
			bufferBuilder.end();
			bufferBuilder.start();
			bufferBuilder.color(0x808080, 255);
			bufferBuilder.vertex(n7, n11 + n, 0.0, 0.0, 1.0);
			bufferBuilder.vertex(n8, n11 + n, 0.0, 1.0, 1.0);
			bufferBuilder.vertex(n8, n11, 0.0, 1.0, 0.0);
			bufferBuilder.vertex(n7, n11, 0.0, 0.0, 0.0);
			bufferBuilder.end();
			bufferBuilder.start();
			bufferBuilder.color(0xC0C0C0, 255);
			bufferBuilder.vertex(n7, n11 + n - 1, 0.0, 0.0, 1.0);
			bufferBuilder.vertex(n8 - 1, n11 + n - 1, 0.0, 1.0, 1.0);
			bufferBuilder.vertex(n8 - 1, n11, 0.0, 1.0, 0.0);
			bufferBuilder.vertex(n7, n11, 0.0, 0.0, 0.0);
			bufferBuilder.end();
		}
		this.func_77215_b(mouseX, mouseY);
		GL11.glEnable(3553);
		GL11.glShadeModel(7424);
		GL11.glEnable(3008);
		GL11.glDisable(3042);
	}

	protected void renderList(int x, int y, int mouseX, int mouseY) {
		int getSize = this.getSize();
		BufferBuilder bufferBuilder = BufferBuilder.INSTANCE;
		for (int i = 0; i < getSize; ++i) {
			int entryY = y + i * this.slotHeight + this.field_77242_t;
			int slotHeight = this.slotHeight - 4;
			if (entryY > this.bottom || entryY + slotHeight < this.top)
				continue;
			if (this.showSelectionBox && this.isSelected(i)) {
				int n4 = this.left + (this.width / 2 - this.getRowWidth() / 2);
				int n5 = this.left + (this.width / 2 + this.getRowWidth() / 2);
				GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
				GL11.glDisable(3553);
				bufferBuilder.start();
				bufferBuilder.color(0x808080);
				bufferBuilder.vertex(n4, entryY + slotHeight + 2, 0.0, 0.0, 1.0);
				bufferBuilder.vertex(n5, entryY + slotHeight + 2, 0.0, 1.0, 1.0);
				bufferBuilder.vertex(n5, entryY - 2, 0.0, 1.0, 0.0);
				bufferBuilder.vertex(n4, entryY - 2, 0.0, 0.0, 0.0);
				bufferBuilder.color(0);
				bufferBuilder.vertex(n4 + 1, entryY + slotHeight + 1, 0.0, 0.0, 1.0);
				bufferBuilder.vertex(n5 - 1, entryY + slotHeight + 1, 0.0, 1.0, 1.0);
				bufferBuilder.vertex(n5 - 1, entryY - 1, 0.0, 1.0, 0.0);
				bufferBuilder.vertex(n4 + 1, entryY - 1, 0.0, 0.0, 0.0);
				bufferBuilder.end();
				GL11.glEnable(3553);
			}
			this.drawSlot(i, x, entryY, slotHeight, bufferBuilder);
		}
	}

	private void renderHoleBackground(int top, int bottom, int topAlpha, int bottomAlpha) {
		BufferBuilder bufferBuilder = BufferBuilder.INSTANCE;
		this.mc.getTextureManager().bindTexture(Gui.optionsBackground);
		GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
		float f = 32.0f;
		bufferBuilder.start();
		bufferBuilder.color(0x404040, bottomAlpha);
		bufferBuilder.vertex(this.left, bottom, 0.0, 0.0, (float) bottom / f);
		bufferBuilder.vertex(this.left + this.width, bottom, 0.0, (float) this.width / f, (float) bottom / f);
		bufferBuilder.color(0x404040, topAlpha);
		bufferBuilder.vertex(this.left + this.width, top, 0.0, (float) this.width / f, (float) top / f);
		bufferBuilder.vertex(this.left, top, 0.0, 0.0, (float) top / f);
		bufferBuilder.end();
	}

//	@Override
	protected void drawSlot(int index, int x, int y, int slotHeight, BufferBuilder bufferBuilder) {
		this.getEntry(index).render(index, x, y, this.getRowWidth(), slotHeight, bufferBuilder, mouseX, mouseY, this.func_77210_c(mouseX, mouseY) == index);
	}

	public boolean mouseClicked(int mouseX, int mouseY, int button) {
		int n;
		if (this.isMouseInList(mouseY) && (n = this.func_77210_c(mouseX, mouseY)) >= 0) {
			int n2 = this.left + (this.width / 2 - this.getRowWidth() / 2 + 2);
			int n3 = this.top + 4 - this.getScrollAmount() + (n * this.slotHeight + this.field_77242_t);
			int n4 = mouseX - n2;
			int n5 = mouseY - n3;
			if (this.getEntry(n).mouseClicked(n, mouseX, mouseY, button, n4, n5)) {
				this.setScrolling(false);
				return true;
			}
		}
		return false;
	}

	public boolean mouseReleased(int mouseX, int mouseY, int button) {
		for (int i = 0; i < this.getSize(); ++i) {
			int n = this.left + (this.width / 2 - this.getRowWidth() / 2 + 2);
			int n2 = this.top + 4 - this.getScrollAmount() + (i * this.slotHeight + this.field_77242_t);
			int n3 = mouseX - n;
			int n4 = mouseY - n2;
			this.getEntry(i).mouseReleased(i, mouseX, mouseY, button, n3, n4);
		}
		this.setScrolling(true);
		return false;
	}

	public abstract Entry getEntry(int var1);

	public int getScrollAmount() {
		return (int) this.amountScrolled;
	}

	public boolean isMouseInList(int mouseY) {
		return mouseY >= this.top && mouseY <= this.bottom;
	}

	public void setScrolling(boolean scrolling) {
		this.scrolling = scrolling;
	}

	public boolean isScrolling() {
		return this.scrolling;
	}

	public void setX(int x) {
		this.left = x;
		this.right = x + this.width;
	}

	public int getRowWidth() {
		return 220;
	}

	public static interface Entry {

		void render(int var1, int var2, int var3, int var4, int var5, BufferBuilder var6, int var7, int var8, boolean var9);

		boolean mouseClicked(int var1, int var2, int var3, int var4, int var5, int var6);

		void mouseReleased(int var1, int var2, int var3, int var4, int var5, int var6);

	}
}
