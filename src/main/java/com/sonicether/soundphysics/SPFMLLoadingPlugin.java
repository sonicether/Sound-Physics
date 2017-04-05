package com.sonicether.soundphysics;

import java.util.Map;

import net.minecraftforge.fml.relauncher.IFMLCallHook;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.MCVersion;



@MCVersion(value="1.11")
public class SPFMLLoadingPlugin implements IFMLLoadingPlugin, IFMLCallHook
{

	@Override
	public String[] getASMTransformerClass() 
	{
		return new String[]{SoundPhysicsCore.class.getName()};
	}

	@Override
	public String getModContainerClass() {
		//return SPDummyContainer.class.getName();
		return null;
	}

	@Override
	public String getSetupClass() {
		return this.getClass().getName();
	}

	@Override
	public void injectData(Map<String, Object> data) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getAccessTransformerClass() {
		// TODO Auto-generated method stub
		return null;
	}
	
	// This is the method that FML calls in the main thread before Minecraft
	// begins loading.
	@Override
	public Void call() throws Exception {
		// This method is called directly after FML injects the data in the method below.
		// TODO Auto-generated method stub
		return null;
	}

}
