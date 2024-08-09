package com.terraformersmc.modmenu.gui.widget;

import com.terraformersmc.modmenu.ModMenu;
import com.terraformersmc.modmenu.config.ModMenuConfig;

import net.minecraft.Minecraft;
import net.minecraft.GuiButton;
import net.minecraft.ChatMessageComponent;

public class ModMenuButtonWidget extends GuiButton {
	public ModMenuButtonWidget(int id, int x, int y, int width, int height, ChatMessageComponent text) {
		super(id, x, y, width, height, text.buildString(true));
	}

	@Override
	public void render(Minecraft minecraft, int mouseX, int mouseY) {
		super.render(minecraft, mouseX, mouseY);
		if (ModMenuConfig.BUTTON_UPDATE_BADGE.getValue() && ModMenu.areModUpdatesAvailable()) {
			UpdateAvailableBadge.renderBadge(this.width + this.x - 16, this.height / 2 + this.y - 4);
		}
	}
}
