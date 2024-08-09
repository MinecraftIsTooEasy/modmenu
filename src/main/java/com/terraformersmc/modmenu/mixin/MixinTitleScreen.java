package com.terraformersmc.modmenu.mixin;

import com.terraformersmc.modmenu.ModMenu;
import com.terraformersmc.modmenu.api.ModMenuApi;
import com.terraformersmc.modmenu.config.ModMenuConfig;
import com.terraformersmc.modmenu.event.ModMenuEventHandler;
import com.terraformersmc.modmenu.gui.ModsScreen;
import com.terraformersmc.modmenu.gui.widget.ModMenuButtonWidget;
import com.terraformersmc.modmenu.gui.widget.UpdateCheckerTexturedButtonWidget;
import com.terraformersmc.modmenu.util.TranslationUtil;

import net.minecraft.GuiScreen;
import net.minecraft.GuiMainMenu;
import net.minecraft.GuiButton;
import net.minecraft.ResourceLocation;
import net.minecraft.I18n;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiMainMenu.class)
public abstract class MixinTitleScreen extends GuiScreen {
	/** button id for menu.multiplayer button */
	private static final int MULTIPLAYER = 2;
	/** button id for menu.online button */
	private static final int ONLINE = 14;
	/** button id for modmenu.title button */
	private static final int MODS = 69;
	private static final ResourceLocation FABRIC_ICON_BUTTON_LOCATION = new ResourceLocation(ModMenu.MOD_ID, "textures/gui/mods_button.png");

	@Inject(at = @At(value = "TAIL"), method = "init")
	private void onInit(CallbackInfo ci) {
		final List<GuiButton> buttons = this.buttons;
		if (ModMenuConfig.MODIFY_TITLE_SCREEN.getValue()) {
			int modsButtonIndex = -1;
			final int spacing = 24;
			int buttonsY = this.height / 4 + 48;
			for (int i = 0; i < buttons.size(); i++) {
				GuiButton button = buttons.get(i);
				if (ModMenuConfig.MODS_BUTTON_STYLE.getValue() == ModMenuConfig.TitleMenuButtonStyle.CLASSIC) {
					if (button.visible) {
						ModMenuEventHandler.shiftButtons(button, modsButtonIndex == -1, spacing);
						if (modsButtonIndex == -1) {
							buttonsY = button.y;
						}
					}
				}
				if (button.id == ONLINE) {
					if (ModMenuConfig.MODS_BUTTON_STYLE.getValue() == ModMenuConfig.TitleMenuButtonStyle.REPLACE_REALMS) {
						buttons.set(i, new ModMenuButtonWidget(MODS, button.x, button.y, ((AccessorButtonWidget) button).getWidth(), ((AccessorButtonWidget) button).getHeight(), ModMenuApi.createModsButtonText()));
					} else {
						if (ModMenuConfig.MODS_BUTTON_STYLE.getValue() == ModMenuConfig.TitleMenuButtonStyle.SHRINK) {
							((AccessorButtonWidget) button).setWidth(98);
						}
						modsButtonIndex = i + 1;
						if (button.visible) {
							buttonsY = button.y;
						}
					}
				}

			}
			if (modsButtonIndex != -1) {
				if (ModMenuConfig.MODS_BUTTON_STYLE.getValue() == ModMenuConfig.TitleMenuButtonStyle.CLASSIC) {
					this.buttons.add(new ModMenuButtonWidget(MODS, this.width / 2 - 100, buttonsY + spacing, 200, 20, ModMenuApi.createModsButtonText()));
				} else if (ModMenuConfig.MODS_BUTTON_STYLE.getValue() == ModMenuConfig.TitleMenuButtonStyle.SHRINK) {
					this.buttons.add(new ModMenuButtonWidget(MODS, this.width / 2 + 2, buttonsY, 98, 20, ModMenuApi.createModsButtonText()));
				} else if (ModMenuConfig.MODS_BUTTON_STYLE.getValue() == ModMenuConfig.TitleMenuButtonStyle.ICON) {
					this.buttons.add(new UpdateCheckerTexturedButtonWidget(MODS, this.width / 2 + 104, buttonsY, 20, 20, 0, 0, 20, FABRIC_ICON_BUTTON_LOCATION, 32, 64));
				}
			}
		}
	}

	@Inject(method = "buttonClicked", at = @At(value = "HEAD"))
	private void onButtonClicked(GuiButton button, CallbackInfo ci) {
		if (button.id == MODS) {
			this.minecraft.openScreen(new ModsScreen(this));
		}
	}

	@ModifyArg(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/GuiMainMenu;drawString(Lnet/minecraft/client/render/TextRenderer;Ljava/lang/String;III)V", ordinal = 0))
	private String onRender(String string) {
		if (ModMenuConfig.MODIFY_TITLE_SCREEN.getValue() && ModMenuConfig.MOD_COUNT_LOCATION.getValue().isOnTitleScreen()) {
			String count = ModMenu.getDisplayedModCount();
			String specificKey = "modmenu.mods." + count;
			String replacementKey = TranslationUtil.hasTranslation(specificKey) ? specificKey : "modmenu.mods.n";
			if (ModMenuConfig.EASTER_EGGS.getValue() && TranslationUtil.hasTranslation(specificKey + ".secret")) {
				replacementKey = specificKey + ".secret";
			}
			return string.replace(I18n.translate(I18n.translate("menu.modded")), I18n.translate(replacementKey, count));
		}
		return string;
	}
}
