package com.terraformersmc.modmenu.mixin;

import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.Locale;

@Mixin(Locale.class)
public interface AccessorTranslationStorage {

	@Accessor("field_135032_a")
	Map<String, String> getTranslations();

}
