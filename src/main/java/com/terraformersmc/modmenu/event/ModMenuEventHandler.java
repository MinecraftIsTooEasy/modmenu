package com.terraformersmc.modmenu.event;

import com.terraformersmc.modmenu.gui.ModsScreen;
import net.minecraft.GuiButton;
import net.minecraft.Minecraft;
import net.minecraft.KeyBinding;
import net.ornithemc.osl.keybinds.api.KeyBindingEvents;
import net.ornithemc.osl.lifecycle.api.client.MinecraftClientEvents;
import org.lwjgl.input.Keyboard;

public class ModMenuEventHandler {
	private static KeyBinding MENU_KEY_BIND;

	public static void register() {
		KeyBindingEvents.REGISTER_KEYBINDS.register(registry -> MENU_KEY_BIND = registry.register(
				"key.modmenu.open_menu",
				Keyboard.KEY_NONE
		));
		MinecraftClientEvents.TICK_END.register(ModMenuEventHandler::onClientEndTick);
	}

	private static void onClientEndTick(Minecraft client) {
		while (MENU_KEY_BIND.isPressed()) {
			client.displayGuiScreen(new ModsScreen(client.currentScreen));
		}
	}

	public static void shiftButtons(GuiButton button, boolean shiftUp, int spacing) {
		if (shiftUp) {
			button.yPosition -= spacing / 2;
		} else {
			button.yPosition += spacing / 2;
		}
	}
}
