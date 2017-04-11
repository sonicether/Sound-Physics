package com.sonicether.soundphysics;

import java.util.Iterator;
import java.util.ListIterator;

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

import net.minecraft.launchwrapper.IClassTransformer;

public class CoreModInjector implements IClassTransformer {

	@Override
	public byte[] transform(final String obfuscated, final String deobfuscated, byte[] bytes) {
		InsnList toInject = new InsnList();

		// SoundPhysics.init() in SoundManager.SoundSystemStarterThread
		toInject.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "com/sonicether/soundphysics/SoundPhysics", "init", "()V",
				false));

		bytes = patchMethodInClass(obfuscated, bytes, "ccq$a", "<init>", "(Lccq;)V", Opcodes.INVOKESPECIAL,
				AbstractInsnNode.METHOD_INSN, "<init>", null, toInject, false, 0, 0, false, 0);

		// setLastSoundCategory(var6) in SoundManager.playSound()
		toInject = new InsnList();
		toInject.add(new VarInsnNode(Opcodes.ALOAD, 7));
		toInject.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "com/sonicether/soundphysics/SoundPhysics",
				"setLastSoundCategory", "(Lno;)V", false));

		bytes = patchMethodInClass(obfuscated, bytes, "ccq", "c", "(Lccc;)V", Opcodes.INVOKEVIRTUAL,
				AbstractInsnNode.METHOD_INSN, "setVolume", null, toInject, false, 0, 0, false, 0);

		// setLastSoundName(name) in SoundManager.playSound()
		toInject = new InsnList();
		toInject.add(new VarInsnNode(Opcodes.ALOAD, 1));
		toInject.add(new MethodInsnNode(Opcodes.INVOKEINTERFACE, "ccc", "a", "()Lkq;", true));
		toInject.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "kq", "toString", "()Ljava/lang/String;", false));
		toInject.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "com/sonicether/soundphysics/SoundPhysics",
				"setLastSoundName", "(Ljava/lang/String;)V", false));

		bytes = patchMethodInClass(obfuscated, bytes, "ccq", "c", "(Lccc;)V", Opcodes.INVOKEVIRTUAL,
				AbstractInsnNode.METHOD_INSN, "setVolume", null, toInject, false, 0, 0, false, 0);

		// Global volume multiplier
		toInject = new InsnList();
		toInject.add(new FieldInsnNode(Opcodes.GETSTATIC, "com/sonicether/soundphysics/SoundPhysics",
				"globalVolumeMultiplier", "F"));
		toInject.add(new InsnNode(Opcodes.FMUL));

		bytes = patchMethodInClass(obfuscated, bytes, "ccq", "c", "(Lccc;)V", Opcodes.INVOKESPECIAL,
				AbstractInsnNode.METHOD_INSN, "e", "(Lccc;)F", toInject, false, 0, 0, false, 0);

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

		bytes = patchMethodInClass(obfuscated, bytes, "paulscode.sound.libraries.SourceLWJGLOpenAL", "play",
				"(Lpaulscode/sound/Channel;)V", Opcodes.INVOKEVIRTUAL, AbstractInsnNode.METHOD_INSN, "play", null,
				toInject, false, 0, 0, false, 0);

		// attenuation model and rolloff factor
		toInject = new InsnList();
		toInject.add(new FieldInsnNode(Opcodes.GETSTATIC, "com/sonicether/soundphysics/SoundPhysics",
				"attenuationModel", "I"));
		toInject.add(new FieldInsnNode(Opcodes.GETSTATIC, "com/sonicether/soundphysics/SoundPhysics",
				"globalRolloffFactor", "F"));

		bytes = patchMethodInClass(obfuscated, bytes, "paulscode.sound.SoundSystem", "newSource",
				"(ZLjava/lang/String;Ljava/net/URL;Ljava/lang/String;ZFFFIF)V", Opcodes.INVOKESPECIAL,
				AbstractInsnNode.METHOD_INSN, "<init>", null, toInject, true, 2, 0, false, 0);

		// Multiply sound distance volume play decision by
		// SoundPhysics.soundDistanceAllowance
		toInject = new InsnList();
		toInject.add(new FieldInsnNode(Opcodes.GETSTATIC, "com/sonicether/soundphysics/SoundPhysics",
				"soundDistanceAllowance", "D"));
		toInject.add(new InsnNode(Opcodes.DMUL));

		bytes = patchMethodInClass(obfuscated, bytes, "mt", "a", "(Laay;DDDDILfm;)V", Opcodes.DCMPG,
				AbstractInsnNode.INSN, "", "", toInject, true, 0, 0, false, 0);

		// Offset entity sound by their eye height
		toInject = new InsnList();
		toInject.add(new VarInsnNode(Opcodes.ALOAD, 1));
		toInject.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "com/sonicether/soundphysics/SoundPhysics",
				"calculateEntitySoundOffset", "(Lsn;Lnm;)D", false));
		toInject.add(new InsnNode(Opcodes.DADD));
		toInject.add(new VarInsnNode(Opcodes.ALOAD, 0));

		bytes = patchMethodInClass(obfuscated, bytes, "sn", "a", "(Lnm;FF)V", Opcodes.INVOKEVIRTUAL,
				AbstractInsnNode.METHOD_INSN, "bC", null, toInject, true, 0, 0, false, -3);

		return bytes;
	}

	private byte[] patchMethodInClass(final String obfuscatedClassName, final byte[] bytes, final String targetClass,
			final String targetMethod, final String targetMethodSignature, final int targetNodeOpcode,
			final int targetNodeType, final String targetInvocationMethodName,
			final String targetInvocationMethodSignature, final InsnList instructionsToInject,
			final boolean insertBefore, final int nodesToDeleteBefore, final int nodesToDeleteAfter,
			final boolean deleteTargetNode, final int targetNodeOffset) {

		// If this is not the target class, leave!
		if (!obfuscatedClassName.equals(targetClass)) {
			return bytes;
		}

		log("################################################################# Patching Class: " + targetClass);

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
			if (m.name.equals(targetMethod) && m.desc.equals(targetMethodSignature)) {
				log("*************************************** Inside target method: " + targetMethod);

				AbstractInsnNode currentNode = null;
				AbstractInsnNode targetNode = null;

				final ListIterator<AbstractInsnNode> iter = m.instructions.iterator();

				// Loop over the instruction set
				while (iter.hasNext()) {
					currentNode = iter.next();

					if (currentNode.getOpcode() == targetNodeOpcode) {

						if (targetNodeType == AbstractInsnNode.METHOD_INSN) {
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

						// TODO: BREAK STATEMENTS! BUT MAY NOT WORK IF THERE ARE
						// MORE THAN ONE MATCHING TARGET NODES! (offsets have to
						// be corrected)

					}
				}

				if (targetNode == null) {
					break;
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

				if (targetNode == null) {
					break;
				}

				// If we've found the target, inject the instructions!
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

				break;
			}
		}

		final ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
		classNode.accept(writer);
		return writer.toByteArray();
	}

	private void log(final String message) {
		if (Config.debugLogging) {
			System.out.println(message);
		}
	}

}
