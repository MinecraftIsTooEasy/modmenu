package com.terraformersmc.modmenu;

import com.google.common.collect.LinkedListMultimap;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.terraformersmc.modmenu.config.ModMenuConfig;
import com.terraformersmc.modmenu.config.ModMenuConfig.GameMenuButtonStyle;
import com.terraformersmc.modmenu.config.ModMenuConfig.TitleMenuButtonStyle;
import com.terraformersmc.modmenu.config.ModMenuConfigManager;
import com.terraformersmc.modmenu.util.FabricUtils;
import com.terraformersmc.modmenu.util.TranslationUtil;
import com.terraformersmc.modmenu.util.mod.Mod;
import com.terraformersmc.modmenu.util.mod.fabric.FabricDummyParentMod;
import com.terraformersmc.modmenu.util.mod.fabric.FabricMod;
import io.github.prospector.modmenu.api.ConfigScreenFactory;
import io.github.prospector.modmenu.api.ModMenuApi;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.GuiScreen;
import net.minecraft.ChatMessageComponent;
import net.xiaoyu233.fml.FishModLoader;
import net.xiaoyu233.fml.ModResourceManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.NumberFormat;
import java.util.*;

public class ModMenu implements ClientModInitializer {
	public static final String MOD_ID = "modmenu";
	public static final String GITHUB_REF = "TerraformersMC/ModMenu";
	public static final Logger LOGGER = LogManager.getLogger("Mod Menu");
	public static final Gson GSON = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).setPrettyPrinting().create();
	public static final Gson GSON_MINIFIED = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
	public static Mod mod = null;

	public static final Map<String, Mod> MODS = new HashMap<>();
	public static final Map<String, Mod> ROOT_MODS = new HashMap<>();
	public static final LinkedListMultimap<Mod, Mod> PARENT_MAP = LinkedListMultimap.create();

	private static Map<String, ConfigScreenFactory<?>> configScreenFactories = new HashMap<>();
	private static List<Map<String, ConfigScreenFactory<?>>> delayedScreenFactoryProviders = new ArrayList<>();

	private static int cachedDisplayedModCount = -1;
	//	public static boolean runningQuilt = FabricLoader.getInstance().isModLoaded("quilt_loader");
	public static boolean devEnvironment = FishModLoader.isDevelopmentEnvironment();

	public static GuiScreen getConfigScreen(String modid, GuiScreen menuScreen) {
		if (!delayedScreenFactoryProviders.isEmpty()) {
			delayedScreenFactoryProviders.forEach(map -> map.forEach(configScreenFactories::putIfAbsent));
			delayedScreenFactoryProviders.clear();
		}
		if (ModMenuConfig.HIDDEN_CONFIGS.getValue().contains(modid)) {
			return null;
		}
		ConfigScreenFactory<?> factory = configScreenFactories.get(modid);
		if (factory != null) {
			return factory.create(menuScreen);
		}
		return null;
	}

	@Override
	public void onInitializeClient() {
		ModResourceManager.addResourcePackDomain("modmenu");
		ModMenuConfigManager.initializeConfig();
		Set<String> modpackMods = new HashSet<>();
		FishModLoader.getEntrypointContainers("modmenu", ModMenuApi.class).forEach(entrypoint -> {
			ModMetadata metadata = entrypoint.getProvider().getMetadata();
			String modId = metadata.getId();
			try {
				ModMenuApi api = entrypoint.getEntrypoint();
				configScreenFactories.put(modId, api.getModConfigScreenFactory());
				delayedScreenFactoryProviders.add(api.getProvidedConfigScreenFactories());
				api.attachModpackBadges(modpackMods::add);
			} catch (Throwable e) {
				LOGGER.error("Mod {} provides a broken implementation of ModMenuApi", modId, e);
			}
		});
		// Fill mods map
		for (ModContainer modContainer : FabricUtils.getAllMods()) {
			Mod mod = new FabricMod(modContainer, modpackMods);

			MODS.put(mod.getId(), mod);
		}

//		ModrinthUtil.checkForUpdates();

		Map<String, Mod> dummyParents = new HashMap<>();

		// Initialize parent map
		for (Mod mod : MODS.values()) {
			String parentId = mod.getParent();
			if (parentId != null) {
				Mod parent = MODS.getOrDefault(parentId, dummyParents.get(parentId));
				if (parent == null) {
					if (mod instanceof FabricMod) {
						parent = new FabricDummyParentMod((FabricMod) mod, parentId);
						dummyParents.put(parentId, parent);
					}
				}
				PARENT_MAP.put(parent, mod);
			} else {
				ROOT_MODS.put(mod.getId(), mod);
			}
		}
		MODS.putAll(dummyParents);
//		ModMenuEventHandler.register();
	}

	public static void clearModCountCache() {
		cachedDisplayedModCount = -1;
	}

	public static boolean areModUpdatesAvailable() {
		if (!ModMenuConfig.UPDATE_CHECKER.getValue()) {
			return false;
		}

		for (Mod mod : MODS.values()) {
			if (mod.isHidden()) {
				continue;
			}

			if (!ModMenuConfig.SHOW_LIBRARIES.getValue() && mod.getBadges().contains(Mod.Badge.LIBRARY)) {
				continue;
			}

			if (mod.getModrinthData() != null || mod.getChildHasUpdate()) {
				return true; // At least one currently visible mod has an update
			}
		}

		return false;
	}

	public static String getDisplayedModCount() {
		if (cachedDisplayedModCount == -1) {
			// listen, if you have >= 2^32 mods then that's on you
			cachedDisplayedModCount = Math.toIntExact(MODS.values().stream().filter(mod ->
					(ModMenuConfig.COUNT_CHILDREN.getValue() || mod.getParent() == null) &&
							(ModMenuConfig.COUNT_LIBRARIES.getValue() || !mod.getBadges().contains(Mod.Badge.LIBRARY)) &&
							(ModMenuConfig.COUNT_HIDDEN_MODS.getValue() || !mod.isHidden())
			).count());
		}
		return NumberFormat.getInstance().format(cachedDisplayedModCount);
	}

	public static ChatMessageComponent createModsButtonText(boolean title) {
		TitleMenuButtonStyle titleStyle = ModMenuConfig.MODS_BUTTON_STYLE.getValue();
		GameMenuButtonStyle gameMenuStyle = ModMenuConfig.GAME_MENU_BUTTON_STYLE.getValue();
		boolean isIcon = title ? titleStyle == TitleMenuButtonStyle.ICON : gameMenuStyle == GameMenuButtonStyle.ICON;
		boolean isShort = /*title ? titleStyle == ModMenuConfig.TitleMenuButtonStyle.SHRINK :*/ false;
		ChatMessageComponent modsText = ChatMessageComponent.createFromTranslationKey("modmenu.title");
		if (ModMenuConfig.MOD_COUNT_LOCATION.getValue().isOnModsButton() && !isIcon) {
			String count = ModMenu.getDisplayedModCount();
			if (isShort) {
				modsText.appendComponent(ChatMessageComponent.createFromText(" ")).addFormatted("modmenu.loaded.short", count);
			} else {
				String specificKey = "modmenu.loaded." + count;
				String key = TranslationUtil.hasTranslation(specificKey) ? specificKey : "modmenu.loaded.none";
				if (ModMenuConfig.EASTER_EGGS.getValue() && TranslationUtil.hasTranslation(specificKey + ".secret")) {
					key = specificKey + ".secret";
				}
				modsText.appendComponent(ChatMessageComponent.createFromText(" ")).addFormatted(key, count);
			}
		}
		return modsText;
	}
}
