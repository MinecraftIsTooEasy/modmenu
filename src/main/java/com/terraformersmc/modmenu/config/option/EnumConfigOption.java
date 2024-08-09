package com.terraformersmc.modmenu.config.option;

import com.terraformersmc.modmenu.util.TranslationUtil;
import net.minecraft.ChatMessageComponent;

import java.util.Locale;

public class EnumConfigOption<E extends Enum<E>> implements ConfigOption {
	private final String key, translationKey;
	private final Class<E> enumClass;
	private final E defaultValue;

	public EnumConfigOption(String key, E defaultValue) {
		ConfigOptionStorage.setEnum(key, defaultValue);
		this.key = key;
		this.translationKey = TranslationUtil.translationKeyOf("option", key);
		this.enumClass = defaultValue.getDeclaringClass();
		this.defaultValue = defaultValue;
	}

	public String getKey() {
		return key;
	}

	public E getValue() {
		return ConfigOptionStorage.getEnum(key, enumClass);
	}

	public void setValue(E value) {
		ConfigOptionStorage.setEnum(key, value);
	}

	public void cycleValue() {
		ConfigOptionStorage.cycleEnum(key, enumClass);
	}

	public void cycleValue(int amount) {
		ConfigOptionStorage.cycleEnum(key, enumClass, amount);
	}

	public E getDefaultValue() {
		return defaultValue;
	}

	private static <E extends Enum<E>> ChatMessageComponent getValueText(EnumConfigOption<E> option, E value) {
		return ChatMessageComponent.createFromTranslationKey(option.translationKey + "." + value.name().toLowerCase(Locale.ROOT));
	}

	@Override
	public String getValueLabel() {
		return TranslationUtil.translateOptionLabel(ChatMessageComponent.createFromTranslationKey(translationKey), getValueText(this, getValue()));
	}

	@Override
	public void click() {
		cycleValue();
	}
}
