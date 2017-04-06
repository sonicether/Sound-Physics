package com.sonicether.soundphysics;

import java.util.Iterator;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

//import cpw.mods.fml.client.event.ConfigChangedEvent;
//import cpw.mods.fml.common.FMLCommonHandler;
//import cpw.mods.fml.common.Mod;
//import cpw.mods.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
//import cpw.mods.fml.common.event.FMLInitializationEvent;
//import cpw.mods.fml.common.event.FMLPreInitializationEvent;
//import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod(modid = SoundPhysicsCore.modid, version = SoundPhysicsCore.version, guiFactory = "com.sonicether.soundphysics.SPGUIFactory")
public class SoundPhysicsCore implements IClassTransformer {

	public static Configuration configFile;

	public static final String modid = "soundphysics";
	public static final String version = "1.0.0";

	// Config variables
	public static class Config {
		// general
		public static float rolloffFactor = 1.0f;
		public static float globalReverbGain = 1.0f;
		public static float globalReverbBrightness = 1.0f;
		public static double soundDistanceAllowance = 4.0f;
		public static float globalBlockAbsorption = 1.0f;
		public static float globalBlockReflectance = 1.0f;
		public static float airAbsorption = 1.0f;
		public static float underwaterFilter = 0.8f;

		// performance
		public static boolean skipRainOcclusionTracing = true;
		public static int environmentEvaluationRays = 32;
		public static boolean simplerSharedAirspaceSimulation = false;

		// block properties
		public static float stoneReflectivity = 1.0f;
		public static float woodReflectivity = 0.4f;
		public static float groundReflectivity = 0.3f;
		public static float plantReflectivity = 0.5f;
		public static float metalReflectivity = 1.0f;
		public static float glassReflectivity = 0.5f;
		public static float clothReflectivity = 0.05f;
		public static float sandReflectivity = 0.2f;
		public static float snowReflectivity = 0.2f;

		// misc
		public static boolean debugLogging = false;
		public static boolean occlusionLogging = false;
		public static boolean environmentLogging = false;
		public static boolean performanceLogging = false;

		public static final String categoryGeneral = "general";
		public static final String categoryPerformance = "performance";
		public static final String categoryMaterialProperties = "material properties";
		public static final String categoryMisc = "misc";
	}

	@Mod.EventHandler
	public void preInit(final FMLPreInitializationEvent event) {
		configFile = new Configuration(event.getSuggestedConfigurationFile());
		syncConfig();
	}

	@Mod.EventHandler
	public void init(final FMLInitializationEvent event) {
		MinecraftForge.EVENT_BUS.register(this);
	}

	@SubscribeEvent
	public void onConfigChanged(final ConfigChangedEvent.OnConfigChangedEvent eventArgs) {
		if (eventArgs.getModID().equals(modid)) {
			syncConfig();
		}
	}

	public static void syncConfig() {
		// General
		Config.rolloffFactor = configFile.getFloat("Attenuation Factor", Configuration.CATEGORY_GENERAL, 1.0f, 0.2f,
				1.0f,
				"Affects how quiet a sound gets based on distance. Lower values mean distant sounds are louder. 1.0 is the physically correct value.");
		Config.globalReverbGain = configFile.getFloat("Global Reverb Gain", Configuration.CATEGORY_GENERAL, 1.0f, 0.1f,
				2.0f, "The global volume of simulated reverberations.");
		Config.globalReverbBrightness = configFile.getFloat("Global Reverb Brightness", Configuration.CATEGORY_GENERAL,
				1.0f, 0.1f, 2.0f,
				"The brightness of reverberation. Higher values result in more high frequencies in reverberation. Lower values give a more muffled sound to the reverb.");
		Config.globalBlockAbsorption = configFile.getFloat("Global Block Absorption", Configuration.CATEGORY_GENERAL,
				1.0f, 0.1f, 4.0f, "The global amount of sound that will be absorbed when traveling through blocks.");
		Config.globalBlockReflectance = configFile.getFloat("Global Block Reflectance", Configuration.CATEGORY_GENERAL,
				1.0f, 0.1f, 4.0f,
				"The global amount of sound reflectance energy of all blocks. Lower values result in more conservative reverb simulation with shorter reverb tails. Higher values result in more generous reverb simulation with higher reverb tails.");
		Config.soundDistanceAllowance = configFile.getFloat("Sound Distance Allowance", Configuration.CATEGORY_GENERAL,
				4.0f, 1.0f, 6.0f,
				"Minecraft won't allow sounds to play past a certain distance. This parameter is a multiplier for how far away a sound source is allowed to be in order for it to actually play. Values too high can cause polyphony issues.");
		Config.airAbsorption = configFile.getFloat("Air Absorption", Configuration.CATEGORY_GENERAL, 1.0f, 0.0f, 5.0f,
				"A value controlling the amount that air absorbs high frequencies with distance. A value of 1.0 is physically correct for air with normal humidity and temperature. Higher values mean air will absorb more high frequencies with distance. 0 disables this effect.");
		Config.underwaterFilter = configFile.getFloat("Underwater Filter", Configuration.CATEGORY_GENERAL, 0.8f, 0.0f,
				1.0f,
				"How much sound is filtered when the player is underwater. 0.0 means no filter. 1.0 means fully filtered.");

		// performance
		Config.skipRainOcclusionTracing = configFile.getBoolean("Skip Rain Occlusion Tracing",
				Config.categoryPerformance, true,
				"If true, rain sound sources won't trace for sound occlusion. This can help performance during rain.");
		Config.environmentEvaluationRays = configFile.getInt("Environment Evaluation Rays", Config.categoryPerformance,
				32, 8, 64,
				"The number of rays to trace to determine reverberation for each sound source. More rays provides more consistent tracing results but takes more time to calculate. Decrease this value if you experience lag spikes when sounds play.");
		Config.simplerSharedAirspaceSimulation = configFile.getBoolean("Simpler Shared Airspace Simulation",
				Config.categoryPerformance, false,
				"If true, enables a simpler technique for determining when the player and a sound source share airspace. Might sometimes miss recognizing shared airspace, but it's faster to calculate.");

		// material properties
		Config.stoneReflectivity = configFile.getFloat("Stone Reflectivity", Config.categoryMaterialProperties, 1.0f,
				0.0f, 1.0f, "Sound reflectivity for stone blocks.");
		Config.woodReflectivity = configFile.getFloat("Wood Reflectivity", Config.categoryMaterialProperties, 0.4f,
				0.0f, 1.0f, "Sound reflectivity for wooden blocks.");
		Config.groundReflectivity = configFile.getFloat("Ground Reflectivity", Config.categoryMaterialProperties, 0.3f,
				0.0f, 1.0f, "Sound reflectivity for ground blocks (dirt, gravel, etc).");
		Config.plantReflectivity = configFile.getFloat("Foliage Reflectivity", Config.categoryMaterialProperties, 0.5f,
				0.0f, 1.0f, "Sound reflectivity for foliage blocks (leaves, grass, etc.).");
		Config.metalReflectivity = configFile.getFloat("Metal Reflectivity", Config.categoryMaterialProperties, 1.0f,
				0.0f, 1.0f, "Sound reflectivity for metal blocks.");
		Config.glassReflectivity = configFile.getFloat("Glass Reflectivity", Config.categoryMaterialProperties, 0.5f,
				0.0f, 1.0f, "Sound reflectivity for glass blocks.");
		Config.clothReflectivity = configFile.getFloat("Cloth Reflectivity", Config.categoryMaterialProperties, 0.05f,
				0.0f, 1.0f, "Sound reflectivity for cloth blocks (carpet, wool, etc).");
		Config.sandReflectivity = configFile.getFloat("Sand Reflectivity", Config.categoryMaterialProperties, 0.2f,
				0.0f, 1.0f, "Sound reflectivity for sand blocks.");
		Config.snowReflectivity = configFile.getFloat("Snow Reflectivity", Config.categoryMaterialProperties, 0.2f,
				0.0f, 1.0f, "Sound reflectivity for snow blocks.");

		// misc
		Config.debugLogging = configFile.getBoolean("Debug Logging", Config.categoryMisc, false,
				"General debug logging");
		Config.occlusionLogging = configFile.getBoolean("Occlusion Logging", Config.categoryMisc, false,
				"Occlusion tracing information logging");
		Config.environmentLogging = configFile.getBoolean("Environment Logging", Config.categoryMisc, false,
				"Environment evaluation information logging");
		Config.performanceLogging = configFile.getBoolean("Performance Logging", Config.categoryMisc, false,
				"Performance information logging");

		if (configFile.hasChanged()) {
			configFile.save();
			SoundPhysics.applyConfigChanges();
		}
	}

	private void log(final String message) {
		if (Config.debugLogging)
			System.out.println(message);
	}

	@Override
	public byte[] transform(final String obfuscated, final String deobfuscated, byte[] bytes) {
		InsnList toInject = new InsnList();
		InsnList helperList2 = new InsnList();

		// SoundPhysics.init() in SoundManager.SoundSystemStarterThread
		toInject.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "com/sonicether/soundphysics/SoundPhysics", "init", "()V",
				false));
		bytes = patchMethodInClass(obfuscated, bytes,
				new String[] { "net.minecraft.client.audio.SoundManager$SoundSystemStarterThread", "ccq$a" },
				new String[] { "<init>", "<init>" },
				new String[] { "(Lnet/minecraft/client/audio/SoundManager;)V", "(Lccq;)V" }, Opcodes.INVOKESPECIAL,
				AbstractInsnNode.METHOD_INSN, new String[] { "<init>", "<init>" }, null,
				new InsnList[] { toInject, toInject }, false, 0, 0, false, 0);

		// setLastSoundCategory(var6) in SoundManager.playSound()
		toInject = new InsnList();
		toInject.add(new VarInsnNode(Opcodes.ALOAD, 7));
		toInject.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "com/sonicether/soundphysics/SoundPhysics",
				"setLastSoundCategory", "(Lnet/minecraft/util/SoundCategory;)V", false));
		helperList2.add(new VarInsnNode(Opcodes.ALOAD, 7));
		helperList2.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "com/sonicether/soundphysics/SoundPhysics",
				"setLastSoundCategory", "(Lno;)V", false));
		bytes = patchMethodInClass(obfuscated, bytes, new String[] { "net.minecraft.client.audio.SoundManager", "ccq" },
				new String[] { "playSound", "c" },
				new String[] { "(Lnet/minecraft/client/audio/ISound;)V", "(Lccc;)V" }, Opcodes.INVOKEVIRTUAL,
				AbstractInsnNode.METHOD_INSN, new String[] { "setVolume", "setVolume" }, null,
				new InsnList[] { toInject, helperList2 }, false, 0, 0, false, 0);

		// setLastSoundName(name) in SoundManager.playSound()
		toInject = new InsnList();
		toInject.add(new VarInsnNode(Opcodes.ALOAD, 1));
		toInject.add(new MethodInsnNode(Opcodes.INVOKEINTERFACE, "net/minecraft/client/audio/ISound",
				"getSoundLocation", "()Lnet/minecraft/util/ResourceLocation;", true));
		toInject.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "net/minecraft/util/ResourceLocation", "toString",
				"()Ljava/lang/String;", false));
		toInject.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "com/sonicether/soundphysics/SoundPhysics",
				"setLastSoundName", "(Ljava/lang/String;)V", false));
		helperList2 = new InsnList();
		helperList2.add(new VarInsnNode(Opcodes.ALOAD, 1));
		helperList2.add(new MethodInsnNode(Opcodes.INVOKEINTERFACE, "ccc", "a", "()Lkq;", true));
		helperList2.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "kq", "toString", "()Ljava/lang/String;", false));
		helperList2.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "com/sonicether/soundphysics/SoundPhysics",
				"setLastSoundName", "(Ljava/lang/String;)V", false));
		bytes = patchMethodInClass(obfuscated, bytes, new String[] { "net.minecraft.client.audio.SoundManager", "ccq" },
				new String[] { "playSound", "c" },
				new String[] { "(Lnet/minecraft/client/audio/ISound;)V", "(Lccc;)V" }, Opcodes.INVOKEVIRTUAL,
				AbstractInsnNode.METHOD_INSN, new String[] { "setVolume", "setVolume" }, null,
				new InsnList[] { toInject, helperList2 }, false, 0, 0, false, 0);

		// Global volume multiplier
		toInject = new InsnList();
		toInject.add(new FieldInsnNode(Opcodes.GETSTATIC, "com/sonicether/soundphysics/SoundPhysics",
				"globalVolumeMultiplier", "F"));
		toInject.add(new InsnNode(Opcodes.FMUL));
		bytes = patchMethodInClass(obfuscated, bytes, new String[] { "net.minecraft.client.audio.SoundManager", "ccq" },
				new String[] { "playSound", "c" },
				new String[] { "(Lnet/minecraft/client/audio/ISound;)V", "(Lccc;)V" }, Opcodes.INVOKESPECIAL,
				AbstractInsnNode.METHOD_INSN, new String[] { "getClampedVolume", "e" },
				new String[] { "(Lnet/minecraft/client/audio/ISound;)F", "(Lccc;)F" }, new InsnList[] { toInject },
				false, 0, 0, false, 0);

		// onPlaySound(var6) in paulscode.libraries.SourceLWJGLOpenAL.play()
		toInject = new InsnList();
		toInject.add(new VarInsnNode(Opcodes.ALOAD, 0));
		toInject.add(new FieldInsnNode(Opcodes.GETFIELD, "paulscode/sound/libraries/SourceLWJGLOpenAL", "position",
				"Lpaulscode/sound/Vector3D;"));
		toInject.add(new FieldInsnNode(Opcodes.GETFIELD, "paulscode/sound/Vector3D", "x", "F"));
		toInject.add(new VarInsnNode(Opcodes.ALOAD, 0));
		toInject.add(new FieldInsnNode(Opcodes.GETFIELD, "paulscode/sound/libraries/SourceLWJGLOpenAL", "position",
				"Lpaulscode/sound/Vector3D;"));
		toInject.add(new FieldInsnNode(Opcodes.GETFIELD, "paulscode/sound/Vector3D", "y", "F"));
		toInject.add(new VarInsnNode(Opcodes.ALOAD, 0));
		toInject.add(new FieldInsnNode(Opcodes.GETFIELD, "paulscode/sound/libraries/SourceLWJGLOpenAL", "position",
				"Lpaulscode/sound/Vector3D;"));
		toInject.add(new FieldInsnNode(Opcodes.GETFIELD, "paulscode/sound/Vector3D", "z", "F"));
		toInject.add(new VarInsnNode(Opcodes.ALOAD, 0));
		toInject.add(new FieldInsnNode(Opcodes.GETFIELD, "paulscode/sound/libraries/SourceLWJGLOpenAL", "channelOpenAL",
				"Lpaulscode/sound/libraries/ChannelLWJGLOpenAL;"));
		toInject.add(new FieldInsnNode(Opcodes.GETFIELD, "paulscode/sound/libraries/ChannelLWJGLOpenAL", "ALSource",
				"Ljava/nio/IntBuffer;"));
		toInject.add(new InsnNode(Opcodes.ICONST_0));
		toInject.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/nio/IntBuffer", "get", "(I)I", false));
		toInject.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "com/sonicether/soundphysics/SoundPhysics", "onPlaySound",
				"(FFFI)V", false));
		bytes = patchMethodInClass(obfuscated, bytes, new String[] { "paulscode.sound.libraries.SourceLWJGLOpenAL" },
				new String[] { "play" }, new String[] { "(Lpaulscode/sound/Channel;)V" }, Opcodes.INVOKEVIRTUAL,
				AbstractInsnNode.METHOD_INSN, new String[] { "play" }, null, new InsnList[] { toInject }, false, 0, 0,
				false, 0);

		// attenuation model and rolloff factor
		toInject = new InsnList();
		toInject.add(new FieldInsnNode(Opcodes.GETSTATIC, "com/sonicether/soundphysics/SoundPhysics",
				"attenuationModel", "I"));
		toInject.add(new FieldInsnNode(Opcodes.GETSTATIC, "com/sonicether/soundphysics/SoundPhysics",
				"globalRolloffFactor", "F"));
		bytes = patchMethodInClass(obfuscated, bytes, new String[] { "paulscode.sound.SoundSystem" },
				new String[] { "newSource" },
				new String[] { "(ZLjava/lang/String;Ljava/net/URL;Ljava/lang/String;ZFFFIF)V" }, Opcodes.INVOKESPECIAL,
				AbstractInsnNode.METHOD_INSN, new String[] { "<init>" }, null, new InsnList[] { toInject }, true, 2, 0,
				false, 0);

		// Multiply sound distance volume play decision by
		// SoundPhysics.soundDistanceAllowance
		toInject = new InsnList();
		toInject.add(new FieldInsnNode(Opcodes.GETSTATIC, "com/sonicether/soundphysics/SoundPhysics",
				"soundDistanceAllowance", "D"));
		toInject.add(new InsnNode(Opcodes.DMUL));
		bytes = patchMethodInClass(obfuscated, bytes,
				new String[] { "net.minecraft.server.management.PlayerList", "mt" },
				new String[] { "sendToAllNearExcept", "a" },
				new String[] { "(Lnet/minecraft/entity/player/EntityPlayer;DDDDILnet/minecraft/network/Packet;)V",
						"(Laay;DDDDILfm;)V" },
				Opcodes.DCMPG, AbstractInsnNode.INSN, new String[] { "", "" }, new String[] { "", "" },
				new InsnList[] { toInject }, true, 0, 0, false, 0);

		// Offset entity sound by their eye height
		toInject = new InsnList();
		toInject.add(new VarInsnNode(Opcodes.ALOAD, 1));
		toInject.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "com/sonicether/soundphysics/SoundPhysics",
				"calculateEntitySoundOffset", "(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/SoundEvent;)D",
				false));
		toInject.add(new InsnNode(Opcodes.DADD));
		toInject.add(new VarInsnNode(Opcodes.ALOAD, 0));
		helperList2 = new InsnList();
		helperList2.add(new VarInsnNode(Opcodes.ALOAD, 1));
		helperList2.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "com/sonicether/soundphysics/SoundPhysics",
				"calculateEntitySoundOffset", "(Lsn;Lnm;)D", false));
		helperList2.add(new InsnNode(Opcodes.DADD));
		helperList2.add(new VarInsnNode(Opcodes.ALOAD, 0));
		bytes = patchMethodInClass(obfuscated, bytes, new String[] { "net.minecraft.entity.Entity", "sn" },
				new String[] { "playSound", "a" }, new String[] { "(Lnet/minecraft/util/SoundEvent;FF)V", "(Lnm;FF)V" },
				Opcodes.INVOKEVIRTUAL, AbstractInsnNode.METHOD_INSN, new String[] { "getSoundCategory", "bC" }, null,
				new InsnList[] { toInject, helperList2 }, true, 0, 0, false, -3);

		return bytes;
	}

	private byte[] patchMethodInClass(final String currentClassName, final byte[] bytes,
			final String[] targetClassNames, final String[] targetMethodNames, final String[] targetMethodSignatures,
			final int targetNodeOpcode, final int targetNodeType, final String[] targetInvocationMethodNames,
			final String[] targetInvocationMethodSignatures, final InsnList[] instructionsToInjects,
			final boolean insertBefore, final int nodesToDeleteBefore, final int nodesToDeleteAfter,
			final boolean deleteTargetNode, final int targetNodeOffset) {
		String targetClassName = targetClassNames[0];
		String targetMethodName = targetMethodNames[0];
		String targetInvocationMethodName = targetInvocationMethodNames[0];

		String targetMethodSignature = targetMethodSignatures[0];
		InsnList instructionsToInject = instructionsToInjects[0];
		boolean obfuscated = false;

		if (targetClassNames.length == 2) {
			if (currentClassName.equals(targetClassNames[1])) {
				targetClassName = targetClassNames.length == 2 ? targetClassNames[1] : targetClassNames[0];
				targetMethodName = targetMethodNames.length == 2 ? targetMethodNames[1] : targetClassNames[0];
				targetInvocationMethodName = targetInvocationMethodNames.length == 2 ? targetInvocationMethodNames[1]
						: targetInvocationMethodNames[0];
				targetMethodSignature = targetMethodSignatures.length == 2 ? targetMethodSignatures[1]
						: targetMethodSignatures[0];
				instructionsToInject = instructionsToInjects.length == 2 ? instructionsToInjects[1]
						: instructionsToInjects[0];
				obfuscated = true;
			}
		}

		String targetInvocationMethodSignature = null;
		if (targetInvocationMethodSignatures != null) {
			targetInvocationMethodSignature = targetInvocationMethodSignatures[0];
			if (obfuscated) {
				targetInvocationMethodSignature = targetInvocationMethodSignatures.length == 2
						? targetInvocationMethodSignatures[1] : targetInvocationMethodSignatures[0];
			}
		}

		// If this isn't the target class, leave!
		if (!currentClassName.equals(targetClassName)) {
			return bytes;
		}

		log("#################################################################   Patching Class: " + targetClassName);

		// Setup ASM class manipulation stuff
		final ClassNode classNode = new ClassNode();
		final ClassReader classReader = new ClassReader(bytes);
		classReader.accept(classNode, 0);

		// Now we loop over all of the methods declared inside the class until
		// we get to the target method name
		final Iterator<MethodNode> methods = classNode.methods.iterator();
		while (methods.hasNext()) {
			final MethodNode m = methods.next();
			log("********* Method Name: " + m.name + " Desc: " + m.desc);

			// Check if this is the method name and the signature matches
			if (m.name.equals(targetMethodName) && m.desc.equals(targetMethodSignature)) {
				log("*************************************** Inside target method: " + targetMethodName);

				AbstractInsnNode currentNode = null;
				AbstractInsnNode targetNode = null;

				final Iterator<AbstractInsnNode> iter = m.instructions.iterator();

				// Loop over the instruction set
				while (iter.hasNext()) {
					currentNode = iter.next();

					if (currentNode.getOpcode() == targetNodeOpcode) {
						if (targetNodeType == AbstractInsnNode.METHOD_INSN) // If
																			// we're
																			// looking
																			// for
																			// a
																			// method
																			// opcode
						{
							if (currentNode.getType() == AbstractInsnNode.METHOD_INSN) {
								final MethodInsnNode method = (MethodInsnNode) currentNode;
								// log("Method found: " + method.name);
								if (method.name.equals(targetInvocationMethodName)) {
									if (method.desc.equals(targetInvocationMethodSignature)
											|| targetInvocationMethodSignature == null) {
										log("Found target method invocation for injection: "
												+ targetInvocationMethodName);
										targetNode = currentNode;
									}

								}
							}
						} else {
							if (currentNode.getType() == targetNodeType) {
								log("Found target node for injection: " + targetNodeOpcode);
								targetNode = currentNode;
							}
						}

					}
				}

				// Offset the target node by the supplied offset value
				if (targetNodeOffset > 0) {
					for (int i = 0; i < targetNodeOffset; i++) {
						targetNode = targetNode.getNext();
					}
				} else if (targetNodeOffset < 0) {
					for (int i = 0; i < -targetNodeOffset; i++) {
						targetNode = targetNode.getPrevious();
					}
				}

				if (targetNode != null) // If we've found the target, inject the
										// instructions!
				{
					for (int i = 0; i < nodesToDeleteBefore; i++) {
						final AbstractInsnNode previousNode = targetNode.getPrevious();
						log("Removing Node " + previousNode.getOpcode());
						m.instructions.remove(previousNode);
					}

					for (int i = 0; i < nodesToDeleteAfter; i++) {
						final AbstractInsnNode nextNode = targetNode.getNext();
						log("Removing Node " + nextNode.getOpcode());
						m.instructions.remove(nextNode);
					}

					if (insertBefore) {
						m.instructions.insertBefore(targetNode, instructionsToInject);
					} else {
						m.instructions.insert(targetNode, instructionsToInject);
					}

					if (deleteTargetNode) {
						m.instructions.remove(targetNode);
					}

					log("Patching complete!----------------------------------------------------------------------------------------");
				}
				break;
			}
		}

		final ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
		classNode.accept(writer);
		return writer.toByteArray();
	}
}
