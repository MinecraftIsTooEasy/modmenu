package com.terraformersmc.modmenu.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.GuiButton;

@Mixin(GuiButton.class)
public interface AccessorButtonWidget {

	@Accessor("width")
	int getWidth();

	@Accessor("height")
	int getHeight();

	@Accessor("width")
	void setWidth(int width);

}
