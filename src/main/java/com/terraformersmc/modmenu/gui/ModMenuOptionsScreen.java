package com.terraformersmc.modmenu.gui;

import com.terraformersmc.modmenu.config.ModMenuConfig;
import com.terraformersmc.modmenu.config.ModMenuConfigManager;
import com.terraformersmc.modmenu.gui.widget.ConfigOptionListWidget;
import net.minecraft.GuiScreen;
import net.minecraft.GuiButton;
import net.minecraft.I18n;

public class ModMenuOptionsScreen extends GuiScreen {

	private static final int DONE = 0;

	private GuiScreen previous;
	private String title;
	private ConfigOptionListWidget list;
	private int mouseX;
	private int mouseY;

	public ModMenuOptionsScreen(GuiScreen previous) {
		this.previous = previous;
		this.title = I18n.getString("modmenu.options");
	}

	@Override
	public void initGui() {
		this.list = new ConfigOptionListWidget(this.mc, this.width, this.height, 32, this.height - 32, 25, ModMenuConfig.asOptions());
		this.buttonList.add(new GuiButton(DONE, this.width / 2 - 100, this.height - 27, 200, 20, I18n.getString("gui.done")));
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float delta) {
		this.mouseX = mouseX;
		this.mouseY = mouseY;
		this.drawDefaultBackground();
		this.list.drawScreen(mouseX, mouseY, delta);
		this.drawCenteredString(this.fontRenderer, this.title, this.width / 2, 5, 0xffffff);
		super.drawScreen(mouseX, mouseY, delta);
	}

	@Override
	public void handleMouseInput() {
		super.handleMouseInput();
//		if (this.list.isMouseInList(mouseX, mouseY)) {
//			this.list.handleMouse();
//		}
	}

	@Override
	public void mouseClicked(int mouseX, int mouseY, int button) {
		this.list.mouseClicked(mouseX, mouseY, button);
		super.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	public void actionPerformed(GuiButton button) {
		switch (button.id) {
		case DONE:
			ModMenuConfigManager.save();
			ModMenuOptionsScreen.this.mc.displayGuiScreen(ModMenuOptionsScreen.this.previous);
			break;
		}
	}

	@Override
	public void onGuiClosed() {
		ModMenuConfigManager.save();
	}
}
