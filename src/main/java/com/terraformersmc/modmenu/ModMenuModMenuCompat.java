package com.terraformersmc.modmenu;

import com.google.common.collect.ImmutableMap;
import com.terraformersmc.modmenu.gui.ModMenuOptionsScreen;
import io.github.prospector.modmenu.api.ConfigScreenFactory;
import io.github.prospector.modmenu.api.ModMenuApi;
import net.minecraft.GuiOptions;
import net.minecraft.Minecraft;

import java.util.Map;

public class ModMenuModMenuCompat implements ModMenuApi {

	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return ModMenuOptionsScreen::new;
	}

	@Override
	public Map<String, ConfigScreenFactory<?>> getProvidedConfigScreenFactories() {
		return ImmutableMap.of("minecraft", parent -> new GuiOptions(parent, Minecraft.getMinecraft().gameSettings));
	}
}
