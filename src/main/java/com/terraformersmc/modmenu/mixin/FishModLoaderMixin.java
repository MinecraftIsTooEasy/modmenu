package com.terraformersmc.modmenu.mixin;

import com.terraformersmc.modmenu.api.IFishModLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.impl.ModContainerImpl;
import net.xiaoyu233.fml.FishModLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

@Mixin(FishModLoader.class)
public class FishModLoaderMixin implements IFishModLoader {
	@Shadow private static final ArrayList<ModContainerImpl> mods = new ArrayList<>();

	@Override
	public Collection<ModContainer> getAllMods() {
		return Collections.unmodifiableList(mods);
	}
}
