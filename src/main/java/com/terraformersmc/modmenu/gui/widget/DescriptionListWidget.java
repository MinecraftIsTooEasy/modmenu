package com.terraformersmc.modmenu.gui.widget;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.terraformersmc.modmenu.config.ModMenuConfig;
import com.terraformersmc.modmenu.gui.ModsScreen;
import com.terraformersmc.modmenu.gui.widget.entries.EntryListWidget;
import com.terraformersmc.modmenu.gui.widget.entries.ModListEntry;
import com.terraformersmc.modmenu.util.GlUtil;
import com.terraformersmc.modmenu.util.ScreenUtil;
import com.terraformersmc.modmenu.util.VersionUtil;
import com.terraformersmc.modmenu.util.mod.Mod;
import net.minecraft.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

public class DescriptionListWidget extends EntryListWidget {

	private static final ChatMessageComponent HAS_UPDATE_TEXT = ChatMessageComponent.createFromTranslationKey("modmenu.hasUpdate");
	private static final ChatMessageComponent EXPERIMENTAL_TEXT = ChatMessageComponent.createFromTranslationKey("modmenu.experimental").setColor(EnumChatFormatting.GOLD);
	private static final ChatMessageComponent MODRINTH_TEXT = ChatMessageComponent.createFromTranslationKey("modmenu.modrinth");
	private static final ChatMessageComponent CHILD_HAS_UPDATE_TEXT = ChatMessageComponent.createFromTranslationKey("modmenu.childHasUpdate");
	private static final ChatMessageComponent LINKS_TEXT = ChatMessageComponent.createFromTranslationKey("modmenu.links");
	private static final ChatMessageComponent SOURCE_TEXT = ChatMessageComponent.createFromTranslationKey("modmenu.source").setColor(EnumChatFormatting.BLUE).setUnderline(true);
	private static final ChatMessageComponent LICENSE_TEXT = ChatMessageComponent.createFromTranslationKey("modmenu.license");
	private static final ChatMessageComponent VIEW_CREDITS_TEXT = ChatMessageComponent.createFromTranslationKey("modmenu.viewCredits").setColor(EnumChatFormatting.BLUE).setUnderline(true);
	private static final ChatMessageComponent CREDITS_TEXT = ChatMessageComponent.createFromTranslationKey("modmenu.credits");

	private final Minecraft minecraft;
	private final ModsScreen parent;
	private final FontRenderer textRenderer;
	private final List<DescriptionEntry> entries = new ArrayList<>();
	private ModListEntry lastSelected = null;

	public DescriptionListWidget(Minecraft client, int width, int height, int top, int bottom, int slotHeight, ModsScreen parent) {
		super(client, width, height, top, bottom, slotHeight);
		this.minecraft = client;
		this.parent = parent;
		this.textRenderer = client.fontRenderer;
	}

	@Override
	public int getRowWidth() {
		return this.width - 10;
	}

	@Override
	protected int getScrollBarX() {
		return this.width - 6 + this.left;
	}

	public boolean isMouseInList(int mouseX, int mouseY) {
		return mouseY >= this.bottom && mouseY <= this.top && mouseX >= this.left && mouseX <= this.right;
	}

	@Override
	public int getSize() {
		return this.entries.size();
	}

	@Override
	protected void drawSlot(int i, int j, int k, int l, Tessellator tessellator) {

	}

	public void clear() {
		this.entries.clear();
	}

	@Override
	public DescriptionEntry getEntry(int index) {
		return this.entries.get(index);
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float delta) {
		int getSize = this.getSize();
		int scrollbarMinX = this.getScrollBarX();
		int scrollbarMaxX = scrollbarMinX + 6;
		if (mouseX > this.left && mouseX < this.right && mouseY > this.top && mouseY < this.bottom) {
			if (Mouse.isButtonDown(0)) {
				if (this.initialClickY == -1.0f) {
					int mouseClickMode = 1;
					if (mouseY >= this.top && mouseY <= this.bottom) {
						int rowMinX = this.width / 2 - this.getRowWidth() / 2;
						int rowMaxX = this.width / 2 + this.getRowWidth() / 2;
						int selectedY = mouseY - this.top - this.field_77242_t + (int) this.amountScrolled - 4;
						int selectedPos = selectedY / this.slotHeight;
						if (mouseX >= rowMinX && mouseX <= rowMaxX && selectedPos >= 0 && selectedY >= 0 && selectedPos < getSize) {
							int selectedIndex = selectedPos == this.selectedElement && Minecraft.getSystemTime() - this.lastClicked < 250L ? 1 : 0;
							this.elementClicked(selectedPos, selectedIndex != 0);
							this.selectedElement = selectedPos;
							this.lastClicked = Minecraft.getSystemTime();
						} else if (mouseX >= rowMinX && mouseX <= rowMaxX && selectedY < 0) {
							this.func_77224_a(mouseX - rowMinX, mouseY - this.top + (int) this.amountScrolled - 4);
							mouseClickMode = 0;
						}
						if (mouseX >= scrollbarMinX && mouseX <= scrollbarMaxX) {
							this.scrollMultiplier = -1.0f;
							int maxScroll = this.func_77209_d();
							if (maxScroll < 1) {
								maxScroll = 1;
							}
							int heightForScrolling;
							if ((heightForScrolling = (int) ((float) ((this.bottom - this.top) * (this.bottom - this.top)) / (float) this.getContentHeight())) < 32) {
								heightForScrolling = 32;
							}
							if (heightForScrolling > this.bottom - this.top - 8) {
								heightForScrolling = this.bottom - this.top - 8;
							}
							this.scrollMultiplier /= (float) (this.bottom - this.top - heightForScrolling) / (float) maxScroll;
						} else {
							this.scrollMultiplier = 1.0f;
						}
						this.initialClickY = mouseClickMode != 0 ? (float) mouseY : -2.0f;
					} else {
						this.initialClickY = -2.0f;
					}
				} else if (this.initialClickY >= 0.0f) {
					this.amountScrolled -= ((float) mouseY - this.initialClickY) * this.scrollMultiplier;
					this.initialClickY = mouseY;
				}
			} else {
				while (!this.minecraft.gameSettings.touchscreen && Mouse.next()) {
					int dwheel = Mouse.getEventDWheel();
					if (dwheel != 0) {
						if (dwheel > 0) {
							dwheel = -1;
						} else if (dwheel < 0) {
							dwheel = 1;
						}
						this.amountScrolled += dwheel * this.slotHeight / 2;
					}
					this.minecraft.currentScreen.handleMouseInput();
				}
				this.initialClickY = -1.0f;
			}
		}
		this.bindAmountScrolled();

		this.mouseX = mouseX;
		this.mouseY = mouseY;
		bindAmountScrolled();
		ModListEntry selectedEntry = parent.getSelectedEntry();
		if (selectedEntry != lastSelected) {
			lastSelected = selectedEntry;
			clear();
			func_77208_b(-Integer.MAX_VALUE);
			if (lastSelected != null) {
				DescriptionEntry emptyEntry = new DescriptionEntry("");
				int wrapWidth = getRowWidth() - 5;

				Mod mod = lastSelected.getMod();
				String description = mod.getTranslatedDescription();
				if (!description.isEmpty()) {
					for (Object line : textRenderer.listFormattedStringToWidth(description.replaceAll("\n", "\n\n"), wrapWidth)) {
						this.entries.add(new DescriptionEntry((String) line));
					}
				}

				if (ModMenuConfig.UPDATE_CHECKER.getValue() && !ModMenuConfig.DISABLE_UPDATE_CHECKER.getValue().contains(mod.getId())) {
					if (mod.getModrinthData() != null) {
						this.entries.add(emptyEntry);

						int index = 0;
						for (Object line : textRenderer.listFormattedStringToWidth(HAS_UPDATE_TEXT.toStringWithFormatting(true), wrapWidth - 11)) {
							DescriptionEntry entry = new DescriptionEntry((String) line);
							if (index == 0) entry.setUpdateTextEntry();

							this.entries.add(entry);
							index += 1;
						}

						for (Object line : textRenderer.listFormattedStringToWidth(EXPERIMENTAL_TEXT.toStringWithFormatting(true), wrapWidth - 16)) {
							this.entries.add(new DescriptionEntry((String) line, 8));
						}

						ChatMessageComponent updateText = ChatMessageComponent.createFromTranslationWithSubstitutions("modmenu.updateText", VersionUtil.stripPrefix(mod.getModrinthData().versionNumber()), MODRINTH_TEXT)
							.setColor(EnumChatFormatting.BLUE).setUnderline(true);

						String versionLink = String.format("https://modrinth.com/project/%s/version/%s", mod.getModrinthData().projectId(), mod.getModrinthData().versionId());

						for (Object line : textRenderer.listFormattedStringToWidth(updateText.toStringWithFormatting(true), wrapWidth - 16)) {
							this.entries.add(new LinkEntry((String) line, versionLink, 8));
						}
					}
					if (mod.getChildHasUpdate()) {
						this.entries.add(emptyEntry);

						int index = 0;
						for (Object line : textRenderer.listFormattedStringToWidth(CHILD_HAS_UPDATE_TEXT.toStringWithFormatting(true), wrapWidth - 11)) {
							DescriptionEntry entry = new DescriptionEntry((String) line);
							if (index == 0) entry.setUpdateTextEntry();

							this.entries.add(entry);
							index += 1;
						}
					}
				}

				Map<String, String> links = mod.getLinks();
				String sourceLink = mod.getSource();
				if ((!links.isEmpty() || sourceLink != null) && !ModMenuConfig.HIDE_MOD_LINKS.getValue()) {
					this.entries.add(emptyEntry);

					for (Object line : textRenderer.listFormattedStringToWidth(LINKS_TEXT.toStringWithFormatting(true), wrapWidth)) {
						this.entries.add(new DescriptionEntry((String) line));
					}

					if (sourceLink != null) {
						int indent = 8;
						for (Object line : textRenderer.listFormattedStringToWidth(SOURCE_TEXT.toStringWithFormatting(true), wrapWidth - 16)) {
							this.entries.add(new LinkEntry((String) line, sourceLink, indent));
							indent = 16;
						}
					}

					links.forEach((key, value) -> {
						int indent = 8;
						for (Object line : textRenderer.listFormattedStringToWidth(ChatMessageComponent.createFromTranslationKey(key).setColor(EnumChatFormatting.BLUE).setUnderline(true).toStringWithFormatting(true), wrapWidth - 16)) {
							this.entries.add(new LinkEntry((String) line, value, indent));
							indent = 16;
						}
					});
				}

				Set<String> licenses = mod.getLicense();
				if (!ModMenuConfig.HIDE_MOD_LICENSE.getValue() && !licenses.isEmpty()) {
					this.entries.add(emptyEntry);

					for (Object line : textRenderer.listFormattedStringToWidth(LICENSE_TEXT.toStringWithFormatting(true), wrapWidth)) {
						this.entries.add(new DescriptionEntry((String) line));
					}

					for (String license : licenses) {
						int indent = 8;
						for (Object line : textRenderer.listFormattedStringToWidth(license, wrapWidth - 16)) {
							this.entries.add(new DescriptionEntry((String) line, indent));
							indent = 16;
						}
					}
				}

				if (!ModMenuConfig.HIDE_MOD_CREDITS.getValue()) {
					if ("minecraft".equals(mod.getId())) {
						this.entries.add(emptyEntry);

						for (Object line : textRenderer.listFormattedStringToWidth(VIEW_CREDITS_TEXT.toStringWithFormatting(true), wrapWidth)) {
							this.entries.add(new MojangCreditsEntry((String) line));
						}
					} else if (!"java".equals(mod.getId())) {
						List<String> credits = mod.getCredits();
						if (!credits.isEmpty()) {
							this.entries.add(emptyEntry);

							for (Object line : textRenderer.listFormattedStringToWidth(CREDITS_TEXT.toStringWithFormatting(true), wrapWidth)) {
								this.entries.add(new DescriptionEntry((String) line));
							}

							for (String credit : credits) {
								int indent = 8;
								for (Object line : textRenderer.listFormattedStringToWidth(credit, wrapWidth - 16)) {
									this.entries.add(new DescriptionEntry((String) line, indent));
									indent = 16;
								}
							}
						}
					}
				}
			}
		}

		BufferBuilder bufferBuilder = BufferBuilder.INSTANCE;

		{
			this.minecraft.getTextureManager().bindTexture(GuiScreen.optionsBackground);
			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
			bufferBuilder.start(GL11.GL_QUADS);
			bufferBuilder.color(0x20, 0x20, 0x20);
            bufferBuilder.vertex(this.left, this.bottom, 0.0, (this.left / 32.0F), ((this.bottom + this.amountScrolled) / 32.0F));
            bufferBuilder.vertex(this.right, this.bottom, 0.0, (this.right / 32.0F), ((this.bottom + this.amountScrolled) / 32.0F));
            bufferBuilder.vertex(this.right, this.top, 0.0, (this.right / 32.0F), ((this.top + this.amountScrolled) / 32.0F));
            bufferBuilder.vertex(this.left, this.top, 0.0, (this.left / 32.0F), ((this.top + this.amountScrolled) / 32.0F));
			bufferBuilder.end();
		}

		int listX = this.left + this.width / 2 - this.getRowWidth() / 2 + 2;
		int listY = this.top + 4 - (int)this.amountScrolled;
		this.renderList(listX, listY, mouseX, mouseY);

		GL11.glDepthFunc(GL11.GL_LEQUAL);
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		GL11.glEnable(GL11.GL_BLEND);
		GlUtil.blendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ZERO, GL11.GL_ONE);
		GL11.glDisable(GL11.GL_ALPHA_TEST);
		GL11.glShadeModel(GL11.GL_SMOOTH);
		GL11.glDisable(GL11.GL_TEXTURE_2D);

		bufferBuilder.start(GL11.GL_QUADS);
		bufferBuilder.color(0, 0, 0, 0);
		bufferBuilder.vertex(this.left, this.top + 4, 0.0, 0.0, 1.0);
		bufferBuilder.vertex(this.right, this.top + 4, 0.0, 1.0, 1.0);
		bufferBuilder.color(0, 0, 0, 255);
		bufferBuilder.vertex(this.right, this.top, 0.0, 1.0, 0.0);
		bufferBuilder.vertex(this.left, this.top, 0.0, 0.0, 0.0);
		bufferBuilder.end();
		bufferBuilder.start(GL11.GL_QUADS);
		bufferBuilder.color(0, 0, 0, 255);
		bufferBuilder.vertex(this.left, this.bottom, 0.0, 0.0, 1.0);
		bufferBuilder.vertex(this.right, this.bottom, 0.0, 1.0, 1.0);
		bufferBuilder.color(0, 0, 0, 0);
		bufferBuilder.vertex(this.right, this.bottom - 4, 0.0, 1.0, 0.0);
		bufferBuilder.vertex(this.left, this.bottom - 4, 0.0, 0.0, 0.0);
		bufferBuilder.end();

		this.renderScrollBar(bufferBuilder);

		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glShadeModel(GL11.GL_FLAT);
		GL11.glEnable(GL11.GL_ALPHA_TEST);
		GL11.glDisable(GL11.GL_BLEND);
	}

	public void handleMouse() {
		int getSize = this.getSize();
		int scrollbarMinX = this.getScrollBarX();
		int scrollbarMaxX = scrollbarMinX + 6;
		if (mouseX > this.left && mouseX < this.right && mouseY > this.top && mouseY < this.bottom) {
			if (Mouse.isButtonDown(0)) {
				if (this.initialClickY == -1.0f) {
					int mouseClickMode = 1;
					if (mouseY >= this.top && mouseY <= this.bottom) {
						int rowMinX = this.width / 2 - this.getRowWidth() / 2;
						int rowMaxX = this.width / 2 + this.getRowWidth() / 2;
						int selectedY = mouseY - this.top - this.field_77242_t + (int) this.amountScrolled - 4;
						int selectedPos = selectedY / this.slotHeight;
						if (mouseX >= rowMinX && mouseX <= rowMaxX && selectedPos >= 0 && selectedY >= 0 && selectedPos < getSize) {
							int selectedIndex = selectedPos == this.selectedElement && Minecraft.getSystemTime() - this.lastClicked < 250L ? 1 : 0;
							this.elementClicked(selectedPos, selectedIndex != 0);
							this.selectedElement = selectedPos;
							this.lastClicked = Minecraft.getSystemTime();
						} else if (mouseX >= rowMinX && mouseX <= rowMaxX && selectedY < 0) {
							this.func_77224_a(mouseX - rowMinX, mouseY - this.top + (int) this.amountScrolled - 4);
							mouseClickMode = 0;
						}
						if (mouseX >= scrollbarMinX && mouseX <= scrollbarMaxX) {
							this.scrollMultiplier = -1.0f;
							int maxScroll = this.func_77209_d();
							if (maxScroll < 1) {
								maxScroll = 1;
							}
							int heightForScrolling;
							if ((heightForScrolling = (int) ((float) ((this.bottom - this.top) * (this.bottom - this.top)) / (float) this.getContentHeight())) < 32) {
								heightForScrolling = 32;
							}
							if (heightForScrolling > this.bottom - this.top - 8) {
								heightForScrolling = this.bottom - this.top - 8;
							}
							this.scrollMultiplier /= (float) (this.bottom - this.top - heightForScrolling) / (float) maxScroll;
						} else {
							this.scrollMultiplier = 1.0f;
						}
						this.initialClickY = mouseClickMode != 0 ? (float) mouseY : -2.0f;
					} else {
						this.initialClickY = -2.0f;
					}
				} else if (this.initialClickY >= 0.0f) {
					this.amountScrolled -= ((float) mouseY - this.initialClickY) * this.scrollMultiplier;
					this.initialClickY = mouseY;
				}
			} else {
				while (!this.minecraft.gameSettings.touchscreen && Mouse.next()) {
					int dwheel = Mouse.getEventDWheel();
					if (dwheel != 0) {
						if (dwheel > 0) {
							dwheel = -1;
						} else if (dwheel < 0) {
							dwheel = 1;
						}
						this.amountScrolled += dwheel * this.slotHeight / 2;
					}
					this.minecraft.currentScreen.handleMouseInput();
				}
				this.initialClickY = -1.0f;
			}
		}
		this.bindAmountScrolled();
	}

	@Override
	protected void drawSlot(int index, int x, int y, int height, BufferBuilder bufferBuilder) {
		if (y >= this.top && y + height <= this.bottom) {
			super.drawSlot(index, x, y, height, bufferBuilder);
		}
	}

	public void renderScrollBar(BufferBuilder bufferBuilder) {
		int scrollbarStartX = this.getScrollBarX();
		int scrollbarEndX = scrollbarStartX + 6;
		int maxScroll = this.func_77209_d();
		if (maxScroll > 0) {
			int p = (int) ((float) ((this.bottom - this.top) * (this.bottom - this.top)) / (float) this.func_77209_d());
			p = MathHelper.clamp_int(p, 32, this.bottom - this.top - 8);
			int q = (int) this.getScrollAmount() * (this.bottom - this.top - p) / maxScroll + this.top;
			if (q < this.top) {
				q = this.top;
			}

			bufferBuilder.start(GL11.GL_QUADS);
			bufferBuilder.color(0, 0, 0, 0xFF);
			bufferBuilder.vertex(scrollbarStartX, this.bottom, 0.0, 0.0, 1.0);
			bufferBuilder.vertex(scrollbarEndX, this.bottom, 0.0, 1.0, 1.0);
			bufferBuilder.vertex(scrollbarEndX, this.top, 0.0, 1.0, 0.0);
			bufferBuilder.vertex(scrollbarStartX, this.top, 0.0, 0.0, 0.0);
			bufferBuilder.end();
			bufferBuilder.start(GL11.GL_QUADS);
			bufferBuilder.color(0x80, 0x80, 0x80, 0xFF);
			bufferBuilder.vertex(scrollbarStartX, q + p, 0.0, 0.0, 1.0);
			bufferBuilder.vertex(scrollbarEndX, q + p, 0.0, 1.0, 1.0);
			bufferBuilder.vertex(scrollbarEndX, q, 0.0, 1.0, 0.0);
			bufferBuilder.vertex(scrollbarStartX, q, 0.0, 0.0, 0.0);
			bufferBuilder.end();
			bufferBuilder.start(GL11.GL_QUADS);
			bufferBuilder.color(0xC0, 0xC0, 0xC0, 0xFF);
			bufferBuilder.vertex(scrollbarStartX, q + p - 1, 0.0, 0.0, 1.0);
			bufferBuilder.vertex(scrollbarEndX - 1, q + p - 1, 0.0, 1.0, 1.0);
			bufferBuilder.vertex(scrollbarEndX - 1, q, 0.0, 1.0, 0.0);
			bufferBuilder.vertex(scrollbarStartX, q, 0.0, 0.0, 0.0);
			bufferBuilder.end();
		}
	}

	public void confirmResult(boolean result, int id) {
		if (result) {
			int index = id - ModsScreen.MODS_LIST_CONFIRM_ID_OFFSET;
			List<DescriptionEntry> entries = this.entries;

			if (index >= 0 && index < entries.size()) {
				DescriptionEntry entry = entries.get(index);

				if (entry instanceof LinkEntry) {
					String link = ((LinkEntry) entry).link;
					ScreenUtil.openLink(parent, link, parent.getSelectedEntry().mod.getId() + "/link");
				}
			}
		}

		minecraft.displayGuiScreen(this.parent);
	}

	protected class DescriptionEntry implements EntryListWidget.Entry {
		protected String text;
		protected int indent;
		public boolean updateTextEntry = false;

		public DescriptionEntry(String text, int indent) {
			this.text = text;
			this.indent = indent;
		}

		public DescriptionEntry(String text) {
			this(text, 0);
		}

		public DescriptionEntry setUpdateTextEntry() {
			this.updateTextEntry = true;
			return this;
		}

		@Override
		public void render(int index, int x, int y, int width, int height, BufferBuilder bufferBuilder, int mouseX, int mouseY, boolean hovered) {
			if (updateTextEntry) {
				UpdateAvailableBadge.renderBadge(x + indent, y);
				x+=11;
			}
			textRenderer.drawStringWithShadow(text, x + indent, y, 0xAAAAAA);
		}

		@Override
		public boolean mouseClicked(int index, int mouseX, int mouseY, int button, int entryMouseX, int entryMouseY) {
			return false;
		}

		@Override
		public void mouseReleased(int index, int mouseX, int mouseY, int button, int entryMouseX, int entryMouseY) {
		}
	}

	protected class MojangCreditsEntry extends DescriptionEntry {
		public MojangCreditsEntry(String text) {
			super(text);
		}

		@Override
		public boolean mouseClicked(int index, int mouseX, int mouseY, int button, int entryMouseX, int entryMouseY) {
			if (isMouseInList(mouseX, mouseY)) {
				minecraft.displayGuiScreen(new GuiWinGame());
			}
			return super.mouseClicked(index, mouseX, mouseY, button, entryMouseX, entryMouseY);
		}
	}

	protected class LinkEntry extends DescriptionEntry {
		private final String link;

		public LinkEntry(String text, String link, int indent) {
			super(text, indent);
			this.link = link;
		}

		public LinkEntry(String text, String link) {
			this(text, link, 0);
		}

		@Override
		public boolean mouseClicked(int index, int mouseX, int mouseY, int button, int entryMouseX, int entryMouseY) {
			if (isMouseInList(mouseX, mouseY)) {
				minecraft.displayGuiScreen(new GuiConfirmOpenLink(DescriptionListWidget.this.parent, link, ModsScreen.MODS_LIST_CONFIRM_ID_OFFSET + index, false));
			}
			return super.mouseClicked(index, mouseX, mouseY, button, entryMouseX, entryMouseY);
		}
	}

}
