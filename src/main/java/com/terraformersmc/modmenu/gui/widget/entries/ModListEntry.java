package com.terraformersmc.modmenu.gui.widget.entries;

import com.mojang.blaze3d.vertex.BufferBuilder;
import org.lwjgl.opengl.GL11;

import com.terraformersmc.modmenu.ModMenu;
import com.terraformersmc.modmenu.config.ModMenuConfig;
import com.terraformersmc.modmenu.gui.widget.ModListWidget;
import com.terraformersmc.modmenu.gui.widget.UpdateAvailableBadge;
import com.terraformersmc.modmenu.util.DrawingUtil;
import com.terraformersmc.modmenu.util.mod.Mod;
import com.terraformersmc.modmenu.util.mod.ModBadgeRenderer;
import net.minecraft.Minecraft;
import net.minecraft.Gui;
import net.minecraft.FontRenderer;
import net.minecraft.DynamicTexture;
import net.minecraft.ResourceLocation;
import net.minecraft.EnumChatFormatting;
import net.minecraft.ChatMessageComponent;

import java.util.Collections;

public class ModListEntry implements EntryListWidget.Entry {
	public static final ResourceLocation UNKNOWN_ICON = new ResourceLocation("textures/misc/unknown_pack.png");
	private static final ResourceLocation MOD_CONFIGURATION_ICON = new ResourceLocation("textures/gui/mod_configuration.png");
	private static final ResourceLocation ERROR_ICON = new ResourceLocation("textures/gui/world_selection.png");

	protected final Minecraft client;
	public final Mod mod;
	protected final ModListWidget list;
	protected ResourceLocation iconLocation;
	protected static final int FULL_ICON_SIZE = 32;
	protected static final int COMPACT_ICON_SIZE = 19;
	protected long sinceLastClick;

	public ModListEntry(Mod mod, ModListWidget list) {
		this.mod = mod;
		this.list = list;
		this.client = Minecraft.getMinecraft();
	}

	@Override
	public void render(int index, int x, int y, int rowWidth, int rowHeight, BufferBuilder bufferBuilder, int mouseX, int mouseY, boolean hovered) {
		x += getXOffset();
		rowWidth -= getXOffset();
		int iconSize = ModMenuConfig.COMPACT_LIST.getValue() ? COMPACT_ICON_SIZE : FULL_ICON_SIZE;
		String modId = mod.getId();
		if ("java".equals(modId)) {
			DrawingUtil.drawRandomVersionBackground(mod, x, y, iconSize, iconSize);
		}
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		GL11.glEnable(GL11.GL_BLEND);
		this.client.getTextureManager().bindTexture(this.getIconTexture());
		DrawingUtil.drawTexture(x, y, 0.0F, 0.0F, iconSize, iconSize, iconSize, iconSize);
		GL11.glDisable(GL11.GL_BLEND);
		ChatMessageComponent name = ChatMessageComponent.createFromText(mod.getTranslatedName());
		ChatMessageComponent trimmedName = name;
		int maxNameWidth = rowWidth - iconSize - 3;
		FontRenderer font = this.client.fontRenderer;
		if (font.getStringWidth(name.toStringWithFormatting(true)) > maxNameWidth) {
			ChatMessageComponent ellipsis = ChatMessageComponent.createFromText("...");
			trimmedName = ChatMessageComponent.createFromText("").addText(font.trimStringToWidth(name.toStringWithFormatting(true), maxNameWidth - font.getStringWidth(ellipsis.toStringWithFormatting(true)))).appendComponent(ellipsis);
		}
		font.drawString(trimmedName.toStringWithFormatting(true), x + iconSize + 3, y + 1, 0xFFFFFF);
		int updateBadgeXOffset = 0;
		if (ModMenuConfig.UPDATE_CHECKER.getValue() && !ModMenuConfig.DISABLE_UPDATE_CHECKER.getValue().contains(modId) && (mod.getModrinthData() != null || mod.getChildHasUpdate())) {
			UpdateAvailableBadge.renderBadge(x + iconSize + 3 + font.getStringWidth(name.toStringWithFormatting(true)) + 2, y);
			updateBadgeXOffset = 11;
		}
		if (!ModMenuConfig.HIDE_BADGES.getValue()) {
			new ModBadgeRenderer(x + iconSize + 3 + font.getStringWidth(name.toStringWithFormatting(true)) + 2 + updateBadgeXOffset, y, x + rowWidth, mod, list.getParent()).draw(mouseX, mouseY);
		}
		if (!ModMenuConfig.COMPACT_LIST.getValue()) {
			String summary = mod.getSummary();
			DrawingUtil.drawWrappedString(summary, (x + iconSize + 3 + 4), (y + client.fontRenderer.FONT_HEIGHT + 2), rowWidth - iconSize - 7, 2, 0x808080);
		} else {
			DrawingUtil.drawWrappedString(mod.getPrefixedVersion(), (x + iconSize + 3), (y + client.fontRenderer.FONT_HEIGHT + 2), rowWidth - iconSize - 7, 2, 0x808080);
		}

		if (!(this instanceof ParentEntry) && ModMenuConfig.QUICK_CONFIGURE.getValue() && (this.list.getParent().getModHasConfigScreen().get(modId) || this.list.getParent().modScreenErrors.containsKey(modId))) {
			final int textureSize = ModMenuConfig.COMPACT_LIST.getValue() ? (int) (256 / (FULL_ICON_SIZE / (double) COMPACT_ICON_SIZE)) : 256;
			if (this.client.gameSettings.touchscreen || hovered) {
				Gui.drawRect(x, y, x + iconSize, y + iconSize, -1601138544);
				boolean hoveringIcon = mouseX - x < iconSize;
				int v = hoveringIcon ? iconSize : 0;
				if (this.list.getParent().modScreenErrors.containsKey(modId)) {
					this.client.getTextureManager().bindTexture(ERROR_ICON);
					DrawingUtil.drawTexture(x, y, 96.0F, (float) v, iconSize, iconSize, textureSize, textureSize);
					if (hoveringIcon) {
						Throwable e = this.list.getParent().modScreenErrors.get(modId);
						this.list.getParent().setTooltip(Collections.singletonList(this.client.fontRenderer.trimStringToWidth(ChatMessageComponent.createFromTranslationWithSubstitutions("modmenu.configure.error", modId, modId).addText("\n\n").addText(e.toString()).setColor(EnumChatFormatting.RED).toStringWithFormatting(true), 175)));
					}
				} else {
					this.client.getTextureManager().bindTexture(MOD_CONFIGURATION_ICON);
					DrawingUtil.drawTexture(x, y, 0.0F, (float) v, iconSize, iconSize, textureSize, textureSize);
				}
			}
		}
	}

	@Override
	public boolean mouseClicked(int index, int mouseX, int mouseY, int button, int entryMouseX, int entryMouseY) {
		list.select(this);
		if (ModMenuConfig.QUICK_CONFIGURE.getValue() && this.list.getParent().getModHasConfigScreen().get(this.mod.getId())) {
			int iconSize = ModMenuConfig.COMPACT_LIST.getValue() ? COMPACT_ICON_SIZE : FULL_ICON_SIZE;
			if (mouseX - list.getRowLeft() <= iconSize) {
				this.openConfig();
			} else if (Minecraft.getSystemTime() - this.sinceLastClick < 250) {
				this.openConfig();
			}
		}
		this.sinceLastClick = Minecraft.getSystemTime();
		return true;
	}

	@Override
	public void mouseReleased(int index, int mouseX, int mouseY, int button, int entryMouseX, int entryMouseY) {
	}

	public boolean keyPressed(char chr, int key) {
		return false;
	}

	public void openConfig() {
		client.displayGuiScreen(ModMenu.getConfigScreen(mod.getId(), list.getParent()));
	}

	public Mod getMod() {
		return mod;
	}

	public ResourceLocation getIconTexture() {
		if (this.iconLocation == null) {
			this.iconLocation = new ResourceLocation(ModMenu.MOD_ID, mod.getId() + "_icon");
			DynamicTexture icon = mod.getIcon(list.getFabricIconHandler(), 64 * this.client.gameSettings.guiScale);
			if (icon != null) {
				this.client.getTextureManager().loadTexture(this.iconLocation, icon);
			} else {
				this.iconLocation = UNKNOWN_ICON;
			}
		}
		return iconLocation;
	}

	public int getXOffset() {
		return 0;
	}
}
