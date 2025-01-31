package com.terraformersmc.modmenu.gui;

import com.google.common.base.Joiner;
import com.mojang.blaze3d.platform.Lighting;
import com.terraformersmc.modmenu.ModMenu;
import com.terraformersmc.modmenu.config.ModMenuConfig;
import com.terraformersmc.modmenu.config.ModMenuConfigManager;
import com.terraformersmc.modmenu.gui.widget.Controller;
import com.terraformersmc.modmenu.gui.widget.DescriptionListWidget;
import com.terraformersmc.modmenu.gui.widget.ModListWidget;
import com.terraformersmc.modmenu.gui.widget.TextFieldAccess;
import com.terraformersmc.modmenu.gui.widget.TexturedButtonWidget;
import com.terraformersmc.modmenu.gui.widget.entries.ModListEntry;
import com.terraformersmc.modmenu.util.DrawingUtil;
import com.terraformersmc.modmenu.util.OsUtil;
import com.terraformersmc.modmenu.util.ScreenUtil;
import com.terraformersmc.modmenu.util.TranslationUtil;
import com.terraformersmc.modmenu.util.mod.Mod;
import com.terraformersmc.modmenu.util.mod.ModBadgeRenderer;
import net.minecraft.*;
import net.minecraft.GuiConfirmOpenLink;
import net.minecraft.GuiScreen;
import net.xiaoyu233.fml.FishModLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL11;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

public class ModsScreen extends GuiScreen implements Controller {
	private static final ResourceLocation FILTERS_BUTTON_LOCATION = new ResourceLocation(ModMenu.MOD_ID, "textures/gui/filters_button.png");
	private static final ResourceLocation CONFIGURE_BUTTON_LOCATION = new ResourceLocation(ModMenu.MOD_ID, "textures/gui/configure_button.png");

	private static final int SEARCH_BOX = 0;
	private static final int DESCRIPTION_LIST = 1;
	private static final int WEBSITE = 2;
	private static final int ISSUES = 3;
	private static final int FILTERS = 4;
	private static final int SORTING = 5;
	private static final int LIBRARIES = 6;
	private static final int MODS_FOLDER = 7;
	private static final int DONE = 8;
	public static final int MODS_LIST_CONFIRM_ID_OFFSET = 10;
	private static final ChatMessageComponent TOGGLE_FILTER_OPTIONS = ChatMessageComponent.createFromTranslationKey("modmenu.toggleFilterOptions");
	private static final ChatMessageComponent CONFIGURE = ChatMessageComponent.createFromTranslationKey("modmenu.configure");
	private static final Logger LOGGER = LogManager.getLogger("Mod Menu | ModsScreen");
	private static RenderItem itemRenderer = new RenderItem();
	private GuiTextField searchBox;
	private DescriptionListWidget descriptionListWidget;
	private final ChatMessageComponent title;
	private final GuiScreen previousScreen;
	private ModListWidget modList;
	private ModListEntry selected;
	private ModBadgeRenderer modBadgeRenderer;
	private float scrollPercent = 0;
	private boolean init = false;
	private boolean filterOptionsShown = false;
	private int paneY;
	private static final int RIGHT_PANE_Y = 48;
	private int paneWidth;
	private int rightPaneX;
	private int searchBoxX;
	private int filtersX;
	private int filtersWidth;
	private int searchRowWidth;
	public final Set<String> showModChildren = new HashSet<>();
	private int mouseX;
	private int mouseY;
	private List<String> tooltip;

	public final Map<String, Boolean> modHasConfigScreen = new HashMap<>();
	public final Map<String, Throwable> modScreenErrors = new HashMap<>();

	public ModsScreen(GuiScreen previousScreen) {
		this.title = ChatMessageComponent.createFromTranslationKey("modmenu.title");
		this.previousScreen = previousScreen;
	}

	@Override
	public void setValue(String value) {
		modList.filter(value, false);
	}

	@Override
	public void handleMouseInput() {
		super.handleMouseInput();
		if (modList.isMouseInList(mouseX, mouseY)) {
//			this.modList.handleMouse();
		} else if (descriptionListWidget.isMouseInList(mouseX, mouseY)) {
			this.descriptionListWidget.handleMouse();
		}
	}

	@Override
	public void updateScreen() {
		this.searchBox.updateCursorCounter();
	}

	@Override
	public void initGui() {
		paneY = ModMenuConfig.CONFIG_MODE.getValue() ? 48 : 48 + 19;
		paneWidth = this.width / 2 - 8;
		rightPaneX = width - paneWidth;

		int filtersButtonSize = (ModMenuConfig.CONFIG_MODE.getValue() ? 0 : 22);
		int searchWidthMax = paneWidth - 32 - filtersButtonSize;
		int searchBoxWidth = ModMenuConfig.CONFIG_MODE.getValue() ? Math.min(200, searchWidthMax) : searchWidthMax;
		searchBoxX = paneWidth / 2 - searchBoxWidth / 2 - filtersButtonSize / 2;
		this.searchBox = new GuiTextField(this.fontRenderer, searchBoxX, 22, searchBoxWidth, 20);
		((TextFieldAccess) this.searchBox).setController(this);

		for (Mod mod : ModMenu.MODS.values()) {
			String id = mod.getId();
			if (!modHasConfigScreen.containsKey(id)) {
				try {
					GuiScreen configScreen = ModMenu.getConfigScreen(id, this);
					modHasConfigScreen.put(id, configScreen != null);
				} catch (java.lang.NoClassDefFoundError e) {
					LOGGER.warn("The '" + id + "' mod config screen is not available because " + e.getLocalizedMessage() + " is missing.");
					modScreenErrors.put(id, e);
					modHasConfigScreen.put(id, false);
				} catch (Throwable e) {
					LOGGER.error("Error from mod '" + id + "'", e);
					modScreenErrors.put(id, e);
					modHasConfigScreen.put(id, false);
				}
			}
		}

		this.modList = new ModListWidget(this.mc, paneWidth, this.height, paneY, this.height - 36, ModMenuConfig.COMPACT_LIST.getValue() ? 23 : 36, this.searchBox.getText(), this.modList, this);
		this.modList.setX(0);
		modList.reloadFilters();

		this.descriptionListWidget = new DescriptionListWidget(this.mc, paneWidth, this.height, RIGHT_PANE_Y + 60, this.height - 36, fontRenderer.FONT_HEIGHT + 1, this);
		this.descriptionListWidget.setX(rightPaneX);
		GuiButton configureButton = new TexturedButtonWidget(DESCRIPTION_LIST, width - 24, RIGHT_PANE_Y, 20, 20, 0, 0, 20, CONFIGURE_BUTTON_LOCATION, 32, 64) {
			private ChatMessageComponent tooltip;

			@Override
			public void drawButton(Minecraft minecraft, int mouseX, int mouseY) {
				if (selected != null) {
					String modId = selected.getMod().getId();
					if (selected != null) {
						enabled = modHasConfigScreen.get(modId);
					} else {
						enabled = false;
						drawButton = false;
					}
					drawButton = selected != null && modHasConfigScreen.get(modId) || modScreenErrors.containsKey(modId);
					if (modScreenErrors.containsKey(modId)) {
						Throwable e = modScreenErrors.get(modId);
						this.tooltip = ChatMessageComponent.createFromTranslationWithSubstitutions("modmenu.configure.error", modId, modId).addText("\n\n").addText(e.toString()).setColor(EnumChatFormatting.RED);
					} else {
						this.tooltip = CONFIGURE;
					}
				}
				super.drawButton(minecraft, mouseX, mouseY);
			}

			@Override
			public void func_82251_b(int mouseX, int mouseY) {
				ModsScreen.this.renderTooltip(this.tooltip.toStringWithFormatting(true), mouseX, mouseY);
			}
		};
		int urlButtonWidths = paneWidth / 2 - 2;
		int cappedButtonWidth = Math.min(urlButtonWidths, 200);
		GuiButton websiteButton = new GuiButton(WEBSITE, rightPaneX + (urlButtonWidths / 2) - (cappedButtonWidth / 2), RIGHT_PANE_Y + 36, Math.min(urlButtonWidths, 200), 20, I18n.getString("modmenu.website")) {

			@Override
			public void drawButton(Minecraft minecraft, int mouseX, int mouseY) {
				drawButton = selected != null;
				enabled = drawButton && selected.getMod().getWebsite() != null;
				super.drawButton(minecraft, mouseX, mouseY);
			}
		};
		GuiButton issuesButton = new GuiButton(ISSUES, rightPaneX + urlButtonWidths + 4 + (urlButtonWidths / 2) - (cappedButtonWidth / 2), RIGHT_PANE_Y + 36, Math.min(urlButtonWidths, 200), 20, I18n.getString("modmenu.issues")) {
			@Override
			public void drawButton(Minecraft minecraft, int mouseX, int mouseY) {
				drawButton = selected != null;
				enabled = drawButton && selected.getMod().getIssueTracker() != null;
				super.drawButton(minecraft, mouseX, mouseY);
			}
		};
		GuiButton filtersButton = new TexturedButtonWidget(FILTERS, paneWidth / 2 + searchBoxWidth / 2 - 20 / 2 + 2, 22, 20, 20, 0, 0, 20, FILTERS_BUTTON_LOCATION, 32, 64) {
			@Override
			public void func_82251_b(int mouseX, int mouseY) {
				ModsScreen.this.renderTooltip(TOGGLE_FILTER_OPTIONS.toStringWithFormatting(true), mouseX, mouseY);
			}
		};
		if (!ModMenuConfig.CONFIG_MODE.getValue()) {
			this.buttonList.add(filtersButton);
		}
		String showLibrariesText = ModMenuConfig.SHOW_LIBRARIES.getValueLabel();
		String sortingText = ModMenuConfig.SORTING.getValueLabel();
		int showLibrariesWidth = fontRenderer.getStringWidth(showLibrariesText) + 20;
		int sortingWidth = fontRenderer.getStringWidth(sortingText) + 20;
		filtersWidth = showLibrariesWidth + sortingWidth + 2;
		searchRowWidth = searchBoxX + searchBoxWidth + 22;
		updateFiltersX();
		this.buttonList.add(new GuiButton(SORTING, filtersX, 45, sortingWidth, 20, sortingText) {
			@Override
			public void drawButton(Minecraft minecraft, int mouseX, int mouseY) {
				drawButton = filterOptionsShown;
				this.displayString = ModMenuConfig.SORTING.getValueLabel();
				super.drawButton(minecraft, mouseX, mouseY);
			}
		});
		this.buttonList.add(new GuiButton(LIBRARIES, filtersX + sortingWidth + 2, 45, showLibrariesWidth, 20, showLibrariesText) {
			@Override
			public void drawButton(Minecraft minecraft, int mouseX, int mouseY) {
				drawButton = filterOptionsShown;
				this.displayString = ModMenuConfig.SHOW_LIBRARIES.getValueLabel();
				super.drawButton(minecraft, mouseX, mouseY);
			}
		});
		if (!ModMenuConfig.HIDE_CONFIG_BUTTONS.getValue()) {
			this.buttonList.add(configureButton);
		}
		this.buttonList.add(websiteButton);
		this.buttonList.add(issuesButton);
		this.buttonList.add(new GuiButton(MODS_FOLDER, this.width / 2 - 154, this.height - 28, 150, 20, I18n.getString("modmenu.modsFolder")));
		this.buttonList.add(new GuiButton(DONE, this.width / 2 + 4, this.height - 28, 150, 20, I18n.getString("gui.done")));
		this.searchBox.setFocused(true);

		init = true;
	}

	@Override
	public void actionPerformed(GuiButton button) {
		switch (button.id) {
		case DESCRIPTION_LIST: {
			final String id = Objects.requireNonNull(selected).getMod().getId();
			if (modHasConfigScreen.get(id)) {
				GuiScreen configScreen = ModMenu.getConfigScreen(id, this);
				mc.displayGuiScreen(configScreen);
			} else {
				button.enabled = false;
			}
			break;
		}
		case WEBSITE: {
			final Mod mod = Objects.requireNonNull(selected).getMod();
			mc.displayGuiScreen(new GuiConfirmOpenLink(this, mod.getWebsite(), WEBSITE, false));
			break;
		}
		case ISSUES: {
			final Mod mod = Objects.requireNonNull(selected).getMod();
			mc.displayGuiScreen(new GuiConfirmOpenLink(this, mod.getIssueTracker(), ISSUES, false));
			break;
		}
		case FILTERS: {
			filterOptionsShown = !filterOptionsShown;
			break;
		}
		case SORTING: {
			ModMenuConfig.SORTING.cycleValue();
			ModMenuConfigManager.save();
			modList.reloadFilters();
			break;
		}
		case LIBRARIES: {
			ModMenuConfig.SHOW_LIBRARIES.toggleValue();
			ModMenuConfigManager.save();
			modList.reloadFilters();
			break;
		}
		case MODS_FOLDER: {
			OsUtil.openFolder(new File(String.valueOf(FishModLoader.MOD_DIR)));
			break;
		}
		case DONE: {
			mc.displayGuiScreen(previousScreen);
			break;
		}
		}
	}

	@Override
	public void keyTyped(char chr, int key) {
		this.searchBox.textboxKeyTyped(chr, key);
		this.modList.reloadFilters();
		super.keyTyped(chr, key);
	}

	@Override
	public void mouseClicked(int mouseX, int mouseY, int button) {
		if (this.modList.isMouseInList(mouseX, mouseY)) {
			this.modList.mouseClicked(mouseX, mouseY, button);
		}
		if (this.descriptionListWidget.isMouseInList(mouseX, mouseY)) {
			this.descriptionListWidget.mouseClicked(mouseX, mouseY, button);
		}
		super.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float delta) {
		this.mouseX = mouseX;
		this.mouseY = mouseY;
		this.tooltip = null;
		this.drawBackground(0);
		ModListEntry selectedEntry = selected;
		if (selectedEntry != null) {
			this.descriptionListWidget.drawScreen(mouseX, mouseY, delta);
		}
		this.modList.drawScreen(mouseX, mouseY, delta);
		this.searchBox.drawTextBox();
		GL11.glDisable(GL11.GL_BLEND);
		this.drawCenteredString(this.fontRenderer, this.title.toStringWithFormatting(true), this.modList.getWidth() / 2, 8, 16777215);
		if (!ModMenuConfig.CONFIG_MODE.getValue()) {
			ChatMessageComponent fullModCount = computeModCountText(true);
			if (!ModMenuConfig.CONFIG_MODE.getValue() && updateFiltersX()) {
				if (filterOptionsShown) {
					if (!ModMenuConfig.SHOW_LIBRARIES.getValue() || fontRenderer.getStringWidth(fullModCount.toStringWithFormatting(true)) <= filtersX - 5) {
						this.fontRenderer.drawString(fullModCount.toStringWithFormatting(true), searchBoxX, 52, 0xFFFFFF);
					} else {
						this.fontRenderer.drawString(computeModCountText(false).toStringWithFormatting(true), searchBoxX, 46, 0xFFFFFF);
						this.fontRenderer.drawString(computeLibraryCountText().toStringWithFormatting(true), searchBoxX, 57, 0xFFFFFF);
					}
				} else {
					if (!ModMenuConfig.SHOW_LIBRARIES.getValue() || fontRenderer.getStringWidth(fullModCount.toStringWithFormatting(true)) <= modList.getWidth() - 5) {
						this.fontRenderer.drawString(fullModCount.toStringWithFormatting(true), searchBoxX, 52, 0xFFFFFF);
					} else {
						this.fontRenderer.drawString(computeModCountText(false).toStringWithFormatting(true), searchBoxX, 46, 0xFFFFFF);
						this.fontRenderer.drawString(computeLibraryCountText().toStringWithFormatting(true), searchBoxX, 57, 0xFFFFFF);
					}
				}
			}
		}
		if (selectedEntry != null) {
			Mod mod = selectedEntry.getMod();
			int x = rightPaneX;
			if ("java".equals(mod.getId())) {
				DrawingUtil.drawRandomVersionBackground(mod, x, RIGHT_PANE_Y, 32, 32);
			}
			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
			GL11.glEnable(GL11.GL_BLEND);
			this.mc.getTextureManager().bindTexture(this.selected.getIconTexture());
			DrawingUtil.drawTexture(x, RIGHT_PANE_Y, 0.0F, 0.0F, 32, 32, 32, 32);
			GL11.glDisable(GL11.GL_BLEND);
			int lineSpacing = fontRenderer.FONT_HEIGHT + 1;
			int imageOffset = 36;
			ChatMessageComponent name = ChatMessageComponent.createFromText(mod.getTranslatedName());
			ChatMessageComponent trimmedName = name;
			int maxNameWidth = this.width - (x + imageOffset);
			if (fontRenderer.getStringWidth(name.toStringWithFormatting(true)) > maxNameWidth) {
				ChatMessageComponent ellipsis = ChatMessageComponent.createFromText("...");
				trimmedName = ChatMessageComponent.createFromText("").addText(fontRenderer.trimStringToWidth(name.toStringWithFormatting(true), maxNameWidth - fontRenderer.getStringWidth(ellipsis.toStringWithFormatting(true)))).appendComponent(ellipsis);
			}
			this.fontRenderer.drawString(trimmedName.toStringWithFormatting(true), x + imageOffset, RIGHT_PANE_Y + 1, 0xFFFFFF);
			if (mouseX > x + imageOffset && mouseY > RIGHT_PANE_Y + 1 && mouseY < RIGHT_PANE_Y + 1 + fontRenderer.FONT_HEIGHT && mouseX < x + imageOffset + fontRenderer.getStringWidth(trimmedName.toStringWithFormatting(true))) {
				setTooltip(Arrays.asList(I18n.getStringParams("modmenu.modIdToolTip", mod.getId())));
			}
			if (init || modBadgeRenderer == null || modBadgeRenderer.getMod() != mod) {
				modBadgeRenderer = new ModBadgeRenderer(x + imageOffset + this.mc.fontRenderer.getStringWidth(trimmedName.toStringWithFormatting(true)) + 2, RIGHT_PANE_Y, width - 28, selectedEntry.mod, this);
				init = false;
			}
			if (!ModMenuConfig.HIDE_BADGES.getValue()) {
				modBadgeRenderer.draw(mouseX, mouseY);
			}
			if (mod.isReal()) {
				this.fontRenderer.drawString(mod.getPrefixedVersion(), x + imageOffset, RIGHT_PANE_Y + 2 + lineSpacing, 0x808080);
			}
			String authors;
			List<String> names = mod.getAuthors();

			if (!names.isEmpty()) {
				if (names.size() > 1) {
					authors = Joiner.on(", ").join(names);
				} else {
					authors = names.get(0);
				}
				DrawingUtil.drawWrappedString(I18n.getStringParams("modmenu.authorPrefix", authors), x + imageOffset, RIGHT_PANE_Y + 2 + lineSpacing * 2, paneWidth - imageOffset - 4, 1, 0x808080);
			}
		}
		super.drawScreen(mouseX, mouseY, delta);
		if (this.tooltip != null && !this.tooltip.isEmpty()) {
			this.renderTooltip(this.tooltip, mouseX, mouseY);
		}
	}

	private ChatMessageComponent computeModCountText(boolean includeLibs) {
		int[] rootMods = formatModCount(ModMenu.ROOT_MODS.values().stream().filter(mod -> !mod.isHidden() && !mod.getBadges().contains(Mod.Badge.LIBRARY)).map(Mod::getId).collect(Collectors.toSet()));

		if (includeLibs && ModMenuConfig.SHOW_LIBRARIES.getValue()) {
			int[] rootLibs = formatModCount(ModMenu.ROOT_MODS.values().stream().filter(mod -> !mod.isHidden() && mod.getBadges().contains(Mod.Badge.LIBRARY)).map(Mod::getId).collect(Collectors.toSet()));
			return TranslationUtil.translateNumeric("modmenu.showingModsLibraries", rootMods, rootLibs);
		} else {
			return TranslationUtil.translateNumeric("modmenu.showingMods", rootMods);
		}
	}

	private ChatMessageComponent computeLibraryCountText() {
		if (ModMenuConfig.SHOW_LIBRARIES.getValue()) {
			int[] rootLibs = formatModCount(ModMenu.ROOT_MODS.values().stream().filter(mod -> !mod.isHidden() && mod.getBadges().contains(Mod.Badge.LIBRARY)).map(Mod::getId).collect(Collectors.toSet()));
			return TranslationUtil.translateNumeric("modmenu.showingLibraries", rootLibs);
		} else {
			return ChatMessageComponent.createFromText(null);
		}
	}

	private int[] formatModCount(Set<String> set) {
		int drawButton = modList.getDisplayedCountFor(set);
		int total = set.size();
		if (drawButton == total) {
			return new int[]{total};
		}
		return new int[]{drawButton, total};
	}

	public void setTooltip(List<String> tooltip) {
		this.tooltip = tooltip;
	}

	public ModListEntry getSelectedEntry() {
		return selected;
	}

	public void updateSelectedEntry(ModListEntry entry) {
		if (entry != null) {
			this.selected = entry;
		}
	}

	public float getScrollPercent() {
		return scrollPercent;
	}

	public void updateScrollPercent(float scrollPercent) {
		this.scrollPercent = scrollPercent;
	}

	public String getSearchInput() {
		return searchBox.getText();
	}

	private boolean updateFiltersX() {
		if ((filtersWidth + fontRenderer.getStringWidth(computeModCountText(true).toStringWithFormatting(true)) + 20) >= searchRowWidth && ((filtersWidth + fontRenderer.getStringWidth(computeModCountText(false).toStringWithFormatting(true)) + 20) >= searchRowWidth || (filtersWidth + fontRenderer.getStringWidth(computeLibraryCountText().toStringWithFormatting(true)) + 20) >= searchRowWidth)) {
			filtersX = paneWidth / 2 - filtersWidth / 2;
			return !filterOptionsShown;
		} else {
			filtersX = searchRowWidth - filtersWidth + 1;
			return true;
		}
	}

	private static boolean isFabricMod(Path mod) {
		try (JarFile jarFile = new JarFile(mod.toFile())) {
			return jarFile.getEntry("fml.mod.json") != null;
		} catch (IOException e) {
			return false;
		}
	}

	public Map<String, Boolean> getModHasConfigScreen() {
		return modHasConfigScreen;
	}

	@Override
	public void confirmClicked(boolean result, int id) {
		if (id < MODS_LIST_CONFIRM_ID_OFFSET) {
			super.confirmClicked(result, id);
			if (result && this.selected != null) {
				switch (id) {
				case WEBSITE:
					ScreenUtil.openLink(this, this.selected.mod.getWebsite(), this.selected.mod.getId() + " /website");
					break;
				case ISSUES:
					ScreenUtil.openLink(this, this.selected.mod.getIssueTracker(), this.selected.mod.getId() + "/issues");
					break;
				}
			}

			this.mc.displayGuiScreen(this);
		} else {
			this.descriptionListWidget.confirmResult(result, id);
		}
	}

	protected void renderTooltip(String text, int x, int y) {
		this.renderTooltip(Arrays.asList(text), x, y);
	}

	protected void renderTooltip(List<String> text, int x, int y) {
		int n;
		if (text.isEmpty()) {
			return;
		}
		GL11.glDisable(32826);
		Lighting.turnOff();
		GL11.glDisable(2896);
		GL11.glDisable(2929);
		int n2 = 0;
		for (String string : text) {
			n = this.fontRenderer.getStringWidth(string);
			if (n <= n2)
				continue;
			n2 = n;
		}
		int n3 = x + 12;
		int n4 = y - 12;
		n = n2;
		int n5 = 8;
		if (text.size() > 1) {
			n5 += 2 + (text.size() - 1) * 10;
		}
		if (n3 + n2 > this.width) {
			n3 -= 28 + n2;
		}
		if (n4 + n5 + 6 > this.height) {
			n4 = this.height - n5 - 6;
		}
		this.zLevel = 300.0f;
		ModsScreen.itemRenderer.zLevel = 300.0f;
		int n6 = -267386864;
		this.drawGradientRect(n3 - 3, n4 - 4, n3 + n + 3, n4 - 3, n6, n6);
		this.drawGradientRect(n3 - 3, n4 + n5 + 3, n3 + n + 3, n4 + n5 + 4, n6, n6);
		this.drawGradientRect(n3 - 3, n4 - 3, n3 + n + 3, n4 + n5 + 3, n6, n6);
		this.drawGradientRect(n3 - 4, n4 - 3, n3 - 3, n4 + n5 + 3, n6, n6);
		this.drawGradientRect(n3 + n + 3, n4 - 3, n3 + n + 4, n4 + n5 + 3, n6, n6);
		int n7 = 0x505000FF;
		int n8 = (n7 & 0xFEFEFE) >> 1 | n7 & 0xFF000000;
		this.drawGradientRect(n3 - 3, n4 - 3 + 1, n3 - 3 + 1, n4 + n5 + 3 - 1, n7, n8);
		this.drawGradientRect(n3 + n + 2, n4 - 3 + 1, n3 + n + 3, n4 + n5 + 3 - 1, n7, n8);
		this.drawGradientRect(n3 - 3, n4 - 3, n3 + n + 3, n4 - 3 + 1, n7, n7);
		this.drawGradientRect(n3 - 3, n4 + n5 + 2, n3 + n + 3, n4 + n5 + 3, n8, n8);
		for (int i = 0; i < text.size(); ++i) {
			String string = (String) text.get(i);
			this.fontRenderer.drawStringWithShadow(string, n3, n4, -1);
			if (i == 0) {
				n4 += 2;
			}
			n4 += 10;
		}
		this.zLevel = 0.0f;
		ModsScreen.itemRenderer.zLevel = 0.0f;
		GL11.glEnable(2896);
		GL11.glEnable(2929);
		Lighting.turnOn();
		GL11.glEnable(32826);
	}
}
