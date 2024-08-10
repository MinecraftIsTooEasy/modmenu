package com.terraformersmc.modmenu.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.I18n;
import net.minecraft.Locale;

@Mixin(I18n.class)
public interface AccessorI18n {

	@Accessor("i18nLocale")
	public static Locale getTranslations() {
		throw new UnsupportedOperationException();
	}
}
