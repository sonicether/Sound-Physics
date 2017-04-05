package com.sonicether.soundphysics;

import java.util.Set;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.client.IModGuiFactory;

public class SPGUIFactory implements IModGuiFactory {

	@Override
	public void initialize(final Minecraft minecraftInstance) {
		// TODO Auto-generated method stub

	}

	@Override
	public Class<? extends GuiScreen> mainConfigGuiClass() {
		return SPConfigGUI.class;
	}

	@Override
	public Set<RuntimeOptionCategoryElement> runtimeGuiCategories() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RuntimeOptionGuiHandler getHandlerFor(final RuntimeOptionCategoryElement element) {
		// TODO Auto-generated method stub
		return null;
	}

}
