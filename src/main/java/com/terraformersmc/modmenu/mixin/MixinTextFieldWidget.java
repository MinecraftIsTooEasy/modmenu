package com.terraformersmc.modmenu.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.terraformersmc.modmenu.gui.widget.Controller;
import com.terraformersmc.modmenu.gui.widget.TextFieldAccess;

import net.minecraft.GuiTextField;

@Mixin(GuiTextField.class)
public class MixinTextFieldWidget implements TextFieldAccess {

	@Shadow
	private String text;
	@Unique
	private Controller controller;

	@Inject(
		method = "writeText",
		at = @At(
			value = "TAIL"
		)
	)
	private void onWrite(CallbackInfo ci) {
		if (this.controller != null) {
			this.controller.setValue(this.text);
		}
	}

	@Inject(
		method = "deleteFromCursor",
		at = @At(
			value = "TAIL"
		)
	)
	private void onEraseCharacters(CallbackInfo ci) {
		if (this.controller != null) {
			this.controller.setValue(this.text);
		}
	}

	@Override
	public void setController(Controller controller) {
		this.controller = controller;
	}
}
