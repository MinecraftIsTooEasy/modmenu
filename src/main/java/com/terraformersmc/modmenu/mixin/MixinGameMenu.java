package com.terraformersmc.modmenu.mixin;

import com.terraformersmc.modmenu.ModMenu;
import com.terraformersmc.modmenu.api.ModMenuApi;
import com.terraformersmc.modmenu.config.ModMenuConfig;
import com.terraformersmc.modmenu.gui.ModsScreen;
import com.terraformersmc.modmenu.gui.widget.ModMenuButtonWidget;
import com.terraformersmc.modmenu.gui.widget.UpdateCheckerTexturedButtonWidget;

import net.minecraft.GuiIngameMenu;
import net.minecraft.GuiScreen;
import net.minecraft.GuiButton;
import net.minecraft.ResourceLocation;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiIngameMenu.class)
public abstract class MixinGameMenu extends GuiScreen {
	/** button id for gui.advancements button */
	private static final int ADVANCEMENTS = 5;
	/** button id for gui.stats button */
	private static final int STATS = 6;
	/** button id for menu.shareToLan button */
	private static final int SHARE_TO_LAN = 7;
	/** button id for modmenu.title button */
	private static final int MODS = 69;
	private static final ResourceLocation FABRIC_ICON_BUTTON_LOCATION = new ResourceLocation("textures/gui/mods_button.png");
	@Inject(method = "initGui", at = @At(value = "TAIL"))
	private void onInit(CallbackInfo ci) {
		if (ModMenuConfig.MODIFY_GAME_MENU.getValue()) {
			final ModMenuConfig.GameMenuButtonStyle style = ModMenuConfig.GAME_MENU_BUTTON_STYLE.getValue();
			final int spacing = 4;
			int modsButtonIndex = -1;
			int modsButtonX = -1;
			int modsButtonY = -1;
			int modsButtonWidth = -1;
			int modsButtonHeight = 20;
			for (int i = 0; i < this.buttonList.size(); i++) {
				final GuiButton button = (GuiButton) this.buttonList.get(i);
				if (style == ModMenuConfig.GameMenuButtonStyle.BELOW_ADVANCEMENTS && button.id == ADVANCEMENTS) {
					modsButtonX = button.xPosition;
					modsButtonWidth = ((AccessorButtonWidget) button).getWidth();
				}
				if (style == ModMenuConfig.GameMenuButtonStyle.BELOW_STATISTICS && button.id == STATS) {
					modsButtonX = button.xPosition;
					modsButtonWidth = ((AccessorButtonWidget) button).getWidth();
				}
				if (style == ModMenuConfig.GameMenuButtonStyle.BELOW_ADVANCEMENTS_AND_STATISTICS && button.id == ADVANCEMENTS) {
					modsButtonX = button.xPosition;
					modsButtonWidth = 2 * ((AccessorButtonWidget) button).getWidth() + spacing;
				}
				if (style == ModMenuConfig.GameMenuButtonStyle.ICON && button.id == STATS) {
					modsButtonX = button.xPosition + ((AccessorButtonWidget) button).getWidth() + spacing;
					modsButtonWidth = modsButtonHeight;
				}
				if (button.id == SHARE_TO_LAN) {
					modsButtonIndex = i + 1;
					if (style == ModMenuConfig.GameMenuButtonStyle.ICON) {
						modsButtonY = button.yPosition;
					} else {
						modsButtonY = button.yPosition - spacing - modsButtonHeight;
					}
				}
			}
			if (modsButtonIndex != -1) {
				if (style == ModMenuConfig.GameMenuButtonStyle.ICON) {
					this.buttonList.add(new UpdateCheckerTexturedButtonWidget(MODS, modsButtonX, modsButtonY, modsButtonWidth, modsButtonHeight, 0, 0, 20, FABRIC_ICON_BUTTON_LOCATION, 32, 64));
				} else {
					this.buttonList.add(new ModMenuButtonWidget(MODS, modsButtonX, modsButtonY, modsButtonWidth, modsButtonHeight, ModMenuApi.createModsButtonText()));
				}
			}
		}
	}

	@Inject(method = "actionPerformed", at = @At(value = "HEAD"))
	private void onButtonClicked(GuiButton button, CallbackInfo ci) {
		if (button.id == MODS) {
			this.mc.displayGuiScreen(new ModsScreen(this));
		}
	}
}
