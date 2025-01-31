package com.terraformersmc.modmenu.util;

import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.entrypoint.EntrypointContainer;
import net.xiaoyu233.fml.FishModLoader;

import java.io.File;
import java.text.NumberFormat;
import java.util.List;
import java.util.Optional;

public class FabricUtils {

	public static List<ModContainer> getAllMods() {
		return FishModLoader.getModsMap().values().stream().map(x -> (ModContainer) x).toList();
	}

	public static String getModSizeFormatted() {
		return NumberFormat.getInstance().format(getModSize());
	}

	public static int getModSize() {
		return FishModLoader.getModsMap().size();
	}

	public static Optional<ModContainer> getModContainer(String id) {
		return FishModLoader.getModContainer(id);
	}

	public static boolean isModLoaded(String modid) {
		return FishModLoader.hasMod(modid);
	}

	public static String getGameDirectory() {
		return "";
	}

	public static File getModsDirectory() {
		return FishModLoader.MOD_DIR;
	}

	public static String getConfigDirectory() {
		return "config" + File.separator;
	}

//    public static <T> List<T> getEntrypoints(String key, Class<T> type) {
//        return FishModLoader.getEntrypointContainers(key, type)
//                .stream().map(EntrypointContainer::getEntrypoint).toList();
//    }

	public static <T> List<EntrypointContainer<T>> getEntrypointContainers(String key, Class<T> type) {
		return FishModLoader.getEntrypointContainers(key, type);
	}
}
