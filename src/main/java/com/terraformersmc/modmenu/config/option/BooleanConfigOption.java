package com.terraformersmc.modmenu.config.option;

import com.terraformersmc.modmenu.util.TranslationUtil;

import net.minecraft.ChatMessageComponent;

public class BooleanConfigOption implements ConfigOption {
	private final String key, translationKey;
	private final boolean defaultValue;
	private final ChatMessageComponent enabledText;
	private final ChatMessageComponent disabledText;

	public BooleanConfigOption(String key, boolean defaultValue, String enabledKey, String disabledKey) {
		ConfigOptionStorage.setBoolean(key, defaultValue);
		this.key = key;
		this.translationKey = TranslationUtil.translationKeyOf("option", key);
		this.defaultValue = defaultValue;
		this.enabledText = ChatMessageComponent.createFromTranslationKey(translationKey + "." + enabledKey);
		this.disabledText = ChatMessageComponent.createFromTranslationKey(translationKey + "." + disabledKey);
	}

	public BooleanConfigOption(String key, boolean defaultValue) {
		this(key, defaultValue, "true", "false");
	}

	public String getKey() {
		return key;
	}

	public boolean getValue() {
		return ConfigOptionStorage.getBoolean(key);
	}

	public void setValue(boolean value) {
		ConfigOptionStorage.setBoolean(key, value);
	}

	public void toggleValue() {
		ConfigOptionStorage.toggleBoolean(key);
	}

	public boolean getDefaultValue() {
		return defaultValue;
	}

	@Override
	public String getValueLabel() {
		return TranslationUtil.translateOptionLabel(ChatMessageComponent.createFromTranslationKey(translationKey), getValue() ? enabledText : disabledText);
	}

	@Override
	public void click() {
		toggleValue();
	}
}
