package com.sonicether.soundphysics;

import com.google.common.collect.Lists;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import net.minecraftforge.fml.common.DummyModContainer;
import net.minecraftforge.fml.common.LoadController;
import net.minecraftforge.fml.common.ModMetadata;
import net.minecraftforge.fml.common.event.FMLConstructionEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
/*
import cpw.mods.fml.common.DummyModContainer;
import cpw.mods.fml.common.LoadController;
import cpw.mods.fml.common.ModMetadata;
import cpw.mods.fml.common.event.FMLConstructionEvent;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
*/

public class SPDummyContainer extends DummyModContainer
{
	public SPDummyContainer() 
	{
		super(new ModMetadata());
		ModMetadata meta = getMetadata();
		meta.modId = "SoundPhysics";
		meta.name = "Sound Physics";
		meta.version = "@VERSION@";
		meta.credits = "Manoog";
		meta.authorList = Lists.newArrayList();
		meta.authorList.add("sonicether");
		meta.description = "Adds realistic sound attenuation, obstruction, and reverberation.";
		meta.url = ""; // TODO: Update this!
		meta.updateUrl = "";
		meta.screenshots = new String[0];
		meta.logoFile = "";
	}
	
	@Override
	public boolean registerBus(EventBus bus, LoadController controller)
	{
		bus.register(this);
		return true;
	}
	
	@Subscribe
	public void modConstruction(FMLConstructionEvent event)
	{
		
	}
	
	@Subscribe
	public void init(FMLInitializationEvent event) 
	{

	}
	
	@Subscribe
	public void preInit(FMLPreInitializationEvent event) 
	{

	}

	@Subscribe
	public void postInit(FMLPostInitializationEvent event) 
	{

	}
}
