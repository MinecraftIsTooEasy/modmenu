package com.terraformersmc.modmenu.gui.widget;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.vertex.BufferBuilder;
import net.minecraft.*;
import org.jetbrains.annotations.Nullable;

import com.terraformersmc.modmenu.config.option.ConfigOption;
import com.terraformersmc.modmenu.gui.widget.entries.EntryListWidget;
import com.terraformersmc.modmenu.mixin.AccessorButtonWidget;

public class ConfigOptionListWidget extends EntryListWidget {
	private final Minecraft minecraft;
	private final List<Entry> entries = new ArrayList<>();

	private int nextId;

	public ConfigOptionListWidget(Minecraft minecraft, int width, int height, int yStart, int yEnd, int entryHeight, ConfigOption ... options) {
        super(minecraft, width, height, yStart, yEnd, entryHeight);
        this.minecraft = minecraft;
//        this.centerAlongY = false;
        for (int i = 0; i < options.length; i += 2) {
            ConfigOption option = options[i];
            ConfigOption option2 = i < options.length - 1 ? options[i + 1] : null;
            this.entries.add(new Entry(width, option, option2));
        }
    }

	@Nullable
	private static GuiButton createWidget(final Minecraft minecraft, int id, int x, int y, int width, final @Nullable ConfigOption option) {
		if (option == null) {
			return null;
		}
		GuiButton button = new GuiSmallButton(id, x, y, null, option.getValueLabel());
		((AccessorButtonWidget) button).setWidth(width);
		return button;
	}

	@Override
	public Entry getEntry(int i) {
		return this.entries.get(i);
	}

	@Override
	protected int getSize() {
		return this.entries.size();
	}

	@Override
	public int getRowWidth() {
		return 400;
	}

	@Override
	protected int getScrollBarX() {
		return super.getScrollBarX() + 32;
	}

	public boolean isMouseInList(int mouseX, int mouseY) {
		return mouseY >= this.top && mouseY <= this.bottom && mouseX >= this.right && mouseX <= this.left;
	}

	@Override
	protected void drawSlot(int i, int j, int k, int l, Tessellator tessellator) {

	}

	public final class Entry implements EntryListWidget.Entry {
		@Nullable
		private final ConfigOption leftOption;
		@Nullable
		private final GuiButton left;
		@Nullable
		private final ConfigOption rightOption;
		@Nullable
		private final GuiButton right;

		public Entry(@Nullable GuiButton left, @Nullable ConfigOption leftOption, GuiButton right, @Nullable ConfigOption rightOption) {
			this.left = left;
			this.leftOption = leftOption;
			this.right = right;
			this.rightOption = rightOption;
		}

		public Entry(int x, ConfigOption option) {
			this(ConfigOptionListWidget.createWidget(minecraft, nextId++, x / 2 - 155, 0, 310, option), option, null, null);
		}

		public Entry(int x, @Nullable ConfigOption option, ConfigOption option2) {
			this(ConfigOptionListWidget.createWidget(minecraft, nextId++, x / 2 - 155, 0, 150, option), option,
					ConfigOptionListWidget.createWidget(minecraft, nextId++, x / 2 - 155 + 160, 0, 150, option2), option2);
		}

		@Override
		public void render(int index, int x, int y, int width, int height, BufferBuilder bufferBuilder, int mouseX, int mouseY, boolean hovered) {
			if (this.left != null) {
				this.left.yPosition = y;
				this.left.drawButton(minecraft, mouseX, mouseY);
			}
			if (this.right != null) {
				this.right.yPosition = y;
				this.right.drawButton(minecraft, mouseX, mouseY);
			}
		}

		@Override
		public boolean mouseClicked(int index, int mouseX, int mouseY, int button, int entryMouseX, int entryMouseY) {
			if (button == 0) {
				if (this.left != null && this.left.mousePressed(minecraft, mouseX, mouseY)) {
					this.leftOption.click();
					minecraft.sndManager.playSoundFX("random.click", 1.0f, 1.0f);
					this.left.displayString = this.leftOption.getValueLabel();
					return true;
				}
				if (this.right != null && this.right.mousePressed(minecraft, mouseX, mouseY)) {
					this.rightOption.click();
					minecraft.sndManager.playSoundFX("random.click", 1.0f, 1.0f);
					this.right.displayString = this.rightOption.getValueLabel();
					return true;
				}
			}
			return false;
		}

		@Override
		public void mouseReleased(int index, int mouseX, int mouseY, int button, int entryMouseX, int entryMouseY) {
			this.left.mouseReleased(mouseX, mouseY);
			this.right.mouseReleased(mouseX, mouseY);
		}
	}
}
