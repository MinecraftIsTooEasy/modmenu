package com.terraformersmc.modmenu.api;

import net.fabricmc.loader.api.ModContainer;

import java.util.Collection;

public interface IFishModLoader {
	default Collection<ModContainer> getAllMods() {
		return null;
	}
}
