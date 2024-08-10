package com.terraformersmc.modmenu.gui.widget;

import com.terraformersmc.modmenu.ModMenu;
import com.terraformersmc.modmenu.config.ModMenuConfig;

import net.minecraft.Minecraft;
import net.minecraft.GuiButton;
import net.minecraft.ChatMessageComponent;

public class ModMenuButtonWidget extends GuiButton {
	public ModMenuButtonWidget(int id, int x, int y, int width, int height, ChatMessageComponent text) {
		super(id, x, y, width, height, text.toStringWithFormatting(true));
	}

	@Override
	public void drawButton(Minecraft minecraft, int mouseX, int mouseY) {
		super.drawButton(minecraft, mouseX, mouseY);
		if (ModMenuConfig.BUTTON_UPDATE_BADGE.getValue() && ModMenu.areModUpdatesAvailable()) {
			UpdateAvailableBadge.renderBadge(this.width + this.xPosition - 16, this.height / 2 + this.yPosition - 4);
		}
	}
}
