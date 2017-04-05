package com.sonicether.soundphysics;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.config.DummyConfigElement;
//import cpw.mods.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.IConfigElement;

public class SPConfigGUI extends GuiConfig {
	public SPConfigGUI(final GuiScreen parent) {
		super(parent, getConfigElements(), SoundPhysicsCore.modid, false, false, "Sound Physics Configuration");
	}

	/** Compiles a list of config elements */
	private static List<IConfigElement> getConfigElements() {
		final List<IConfigElement> list = new ArrayList<>();

		// Add categories to config GUI
		list.add(categoryElement(Configuration.CATEGORY_GENERAL, "General", "soundphysics.configgui.ctgy.general"));
		list.add(categoryElement(SoundPhysicsCore.Config.categoryPerformance, "Performance",
				"soundphysics.configgui.ctgy.performance"));
		list.add(categoryElement(SoundPhysicsCore.Config.categoryMaterialProperties, "Material Properties",
				"soundphysics.configgui.ctgy.materialProperties"));
		list.add(categoryElement(SoundPhysicsCore.Config.categoryMisc, "Misc", "soundphysics.configgui.ctgy.misc"));

		return list;
	}

	/**
	 * Creates a button linking to another screen where all options of the
	 * category are available
	 */
	private static IConfigElement categoryElement(final String category, final String name, final String tooltip_key) {
		return new DummyConfigElement.DummyCategoryElement(name, tooltip_key,
				new ConfigElement(SoundPhysicsCore.configFile.getCategory(category)).getChildElements());
	}
}