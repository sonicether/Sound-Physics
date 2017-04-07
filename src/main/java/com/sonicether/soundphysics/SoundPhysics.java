package com.sonicether.soundphysics;

import org.lwjgl.openal.AL10;
import org.lwjgl.openal.AL11;
import org.lwjgl.openal.ALC10;
import org.lwjgl.openal.ALCcontext;
import org.lwjgl.openal.ALCdevice;
import org.lwjgl.openal.EFX10;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import paulscode.sound.SoundSystemConfig;

public class SoundPhysics {

	private static final String logPrefix = "[SOUND PHYSICS]";
	private static int auxFXSlot0;
	private static int auxFXSlot1;
	private static int auxFXSlot2;
	private static int auxFXSlot3;
	private static int reverb0;
	private static int reverb1;
	private static int reverb2;
	private static int reverb3;
	private static int directFilter0;
	private static int sendFilter0;
	private static int sendFilter1;
	private static int sendFilter2;
	private static int sendFilter3;

	private static Minecraft mc;

	private static SoundCategory lastSoundCategory;
	private static String lastSoundName;

	// THESE VARIABLES ARE CONSTANTLY ACCESSED AND USED BY ASM INJECTED CODE! DO
	// NOT REMOVE!
	public static int attenuationModel = SoundSystemConfig.ATTENUATION_ROLLOFF;
	public static float globalRolloffFactor = SoundPhysicsCore.Config.rolloffFactor;
	public static float globalVolumeMultiplier = 4.0f;
	public static float globalReverbMultiplier = 0.7f * SoundPhysicsCore.Config.globalReverbGain;
	public static double soundDistanceAllowance = SoundPhysicsCore.Config.soundDistanceAllowance;

	/**
	 * CALLED BY ASM INJECTED CODE!
	 */
	public static void init() {
		log("Initializing Sound Physics...");
		setupEFX();
		mc = Minecraft.getMinecraft();
	}

	public static void applyConfigChanges() {
		globalRolloffFactor = SoundPhysicsCore.Config.rolloffFactor;
		globalReverbMultiplier = 0.7f * SoundPhysicsCore.Config.globalReverbGain;
		soundDistanceAllowance = SoundPhysicsCore.Config.soundDistanceAllowance;

		if (auxFXSlot0 != 0) {
			// Set the global reverb parameters and apply them to the effect and
			// effectslot
			setReverbParams(ReverbParams.getReverb0(), auxFXSlot0, reverb0);
			setReverbParams(ReverbParams.getReverb1(), auxFXSlot1, reverb1);
			setReverbParams(ReverbParams.getReverb2(), auxFXSlot2, reverb2);
			setReverbParams(ReverbParams.getReverb3(), auxFXSlot3, reverb3);
		}

	}

	private static void setupEFX() {
		// Get current context and device
		final ALCcontext currentContext = ALC10.alcGetCurrentContext();
		final ALCdevice currentDevice = ALC10.alcGetContextsDevice(currentContext);

		if (ALC10.alcIsExtensionPresent(currentDevice, "ALC_EXT_EFX")) {
			log("EFX Extension recognized.");
		} else {
			logError("EFX Extension not found on current device. Aborting.");
			return;
		}

		// Create auxiliary effect slots
		auxFXSlot0 = EFX10.alGenAuxiliaryEffectSlots();
		log("Aux slot " + auxFXSlot0 + " created");
		EFX10.alAuxiliaryEffectSloti(auxFXSlot0, EFX10.AL_EFFECTSLOT_AUXILIARY_SEND_AUTO, AL10.AL_TRUE);

		auxFXSlot1 = EFX10.alGenAuxiliaryEffectSlots();
		log("Aux slot " + auxFXSlot1 + " created");
		EFX10.alAuxiliaryEffectSloti(auxFXSlot1, EFX10.AL_EFFECTSLOT_AUXILIARY_SEND_AUTO, AL10.AL_TRUE);

		auxFXSlot2 = EFX10.alGenAuxiliaryEffectSlots();
		log("Aux slot " + auxFXSlot2 + " created");
		EFX10.alAuxiliaryEffectSloti(auxFXSlot2, EFX10.AL_EFFECTSLOT_AUXILIARY_SEND_AUTO, AL10.AL_TRUE);

		auxFXSlot3 = EFX10.alGenAuxiliaryEffectSlots();
		log("Aux slot " + auxFXSlot3 + " created");
		EFX10.alAuxiliaryEffectSloti(auxFXSlot3, EFX10.AL_EFFECTSLOT_AUXILIARY_SEND_AUTO, AL10.AL_TRUE);
		checkErrorLog("Failed creating auxiliary effect slots!");

		reverb0 = EFX10.alGenEffects();
		EFX10.alEffecti(reverb0, EFX10.AL_EFFECT_TYPE, EFX10.AL_EFFECT_EAXREVERB);
		checkErrorLog("Failed creating reverb effect slot 0!");
		reverb1 = EFX10.alGenEffects();
		EFX10.alEffecti(reverb1, EFX10.AL_EFFECT_TYPE, EFX10.AL_EFFECT_EAXREVERB);
		checkErrorLog("Failed creating reverb effect slot 1!");
		reverb2 = EFX10.alGenEffects();
		EFX10.alEffecti(reverb2, EFX10.AL_EFFECT_TYPE, EFX10.AL_EFFECT_EAXREVERB);
		checkErrorLog("Failed creating reverb effect slot 2!");
		reverb3 = EFX10.alGenEffects();
		EFX10.alEffecti(reverb3, EFX10.AL_EFFECT_TYPE, EFX10.AL_EFFECT_EAXREVERB);
		checkErrorLog("Failed creating reverb effect slot 3!");

		// Create filters
		directFilter0 = EFX10.alGenFilters();
		EFX10.alFilteri(directFilter0, EFX10.AL_FILTER_TYPE, EFX10.AL_FILTER_LOWPASS);

		sendFilter0 = EFX10.alGenFilters();
		EFX10.alFilteri(sendFilter0, EFX10.AL_FILTER_TYPE, EFX10.AL_FILTER_LOWPASS);

		sendFilter1 = EFX10.alGenFilters();
		EFX10.alFilteri(sendFilter1, EFX10.AL_FILTER_TYPE, EFX10.AL_FILTER_LOWPASS);

		sendFilter2 = EFX10.alGenFilters();
		EFX10.alFilteri(sendFilter2, EFX10.AL_FILTER_TYPE, EFX10.AL_FILTER_LOWPASS);

		sendFilter3 = EFX10.alGenFilters();
		EFX10.alFilteri(sendFilter3, EFX10.AL_FILTER_TYPE, EFX10.AL_FILTER_LOWPASS);
		checkErrorLog("Error creating lowpass filters!");

		applyConfigChanges();
	}

	/**
	 * CALLED BY ASM INJECTED CODE!
	 */
	public static void setLastSoundCategory(final SoundCategory sc) {
		lastSoundCategory = sc;
	}

	/**
	 * CALLED BY ASM INJECTED CODE!
	 */
	public static void setLastSoundName(final String name) {
		lastSoundName = name;
	}

	/**
	 * CALLED BY ASM INJECTED CODE!
	 */
	public static void onPlaySound(final float posX, final float posY, final float posZ, final int sourceID) {
		logGeneral("onPlaySound, Source ID: " + sourceID + " " + posX + ", " + posY + ", " + posZ
				+ "    Sound category: " + lastSoundCategory.toString() + "    Sound name: " + lastSoundName);

		long startTime = 0;
		long endTime = 0;

		if (SoundPhysicsCore.Config.performanceLogging) {
			startTime = System.nanoTime();
		}

		evaluateEnvironment(sourceID, posX, posY, posZ);

		if (SoundPhysicsCore.Config.performanceLogging) {
			endTime = System.nanoTime();
			log("Total calculation time for sound " + lastSoundName + ": "
					+ (double) (endTime - startTime) / (double) 1000000 + " milliseconds");
		}
	}

	/**
	 * CALLED BY ASM INJECTED CODE!
	 */
	public static double calculateEntitySoundOffset(final Entity entity, final SoundEvent sound) {
		if (sound.getSoundName().getResourcePath().matches(".*step.*"))
			return 0;

		return entity.getEyeHeight();
	}

	@SuppressWarnings("deprecation")
	private static float getBlockReflectivity(final BlockPos blockPos) {
		final Block block = mc.world.getBlockState(blockPos).getBlock();
		final SoundType soundType = block.getSoundType();

		float reflectivity = 0.5f;

		if (soundType == SoundType.STONE) {
			reflectivity = SoundPhysicsCore.Config.stoneReflectivity;
		} else if (soundType == SoundType.WOOD) {
			reflectivity = SoundPhysicsCore.Config.woodReflectivity;
		} else if (soundType == SoundType.GROUND) {
			reflectivity = SoundPhysicsCore.Config.groundReflectivity;
		} else if (soundType == SoundType.PLANT) {
			reflectivity = SoundPhysicsCore.Config.plantReflectivity;
		} else if (soundType == SoundType.METAL) {
			reflectivity = SoundPhysicsCore.Config.metalReflectivity;
		} else if (soundType == SoundType.GLASS) {
			reflectivity = SoundPhysicsCore.Config.glassReflectivity;
		} else if (soundType == SoundType.CLOTH) {
			reflectivity = SoundPhysicsCore.Config.clothReflectivity;
		} else if (soundType == SoundType.SAND) {
			reflectivity = SoundPhysicsCore.Config.sandReflectivity;
		} else if (soundType == SoundType.SNOW) {
			reflectivity = SoundPhysicsCore.Config.snowReflectivity;
		} else if (soundType == SoundType.LADDER) {
			reflectivity = SoundPhysicsCore.Config.woodReflectivity;
		} else if (soundType == SoundType.ANVIL) {
			reflectivity = SoundPhysicsCore.Config.metalReflectivity;
		}

		reflectivity *= SoundPhysicsCore.Config.globalBlockReflectance;

		return reflectivity;
	}

	private static Vec3d getNormalFromFacing(final EnumFacing sideHit) {
		return new Vec3d(sideHit.getDirectionVec());
	}

	private static Vec3d reflect(final Vec3d dir, final Vec3d normal) {
		// dir - 2.0 * dot(normal, dir) * normal
		final double dot = dir.dotProduct(normal);

		final double x = dir.xCoord - 2.0 * dot * normal.xCoord;
		final double y = dir.yCoord - 2.0 * dot * normal.yCoord;
		final double z = dir.zCoord - 2.0 * dot * normal.zCoord;

		return new Vec3d(x, y, z);
	}

	private static Vec3d offsetSoundByName(double soundX, double soundY, double soundZ, final Vec3d playerPos,
			final String name, final SoundCategory category) {
		double offsetX = 0.0;
		double offsetY = 0.0;
		double offsetZ = 0.0;
		double offsetTowardsPlayer = 0.0;

		double tempNormX = 0;
		double tempNormY = 0;
		double tempNormZ = 0;

		if (soundY % 1.0 < 0.001 || name.matches(".*step.*")) {
			offsetY = 0.1;
		}

		if (category == SoundCategory.BLOCKS || category == SoundCategory.RECORDS || name.matches(".*block.*")) {
			// The ray will probably hit the block that it's emitting from
			// before
			// escaping. Offset the ray start position towards the player by the
			// diagonal half length of a cube

			tempNormX = playerPos.xCoord - soundX;
			tempNormY = playerPos.yCoord - soundY;
			tempNormZ = playerPos.zCoord - soundZ;
			double length = Math.sqrt(tempNormX * tempNormX + tempNormY * tempNormY + tempNormZ * tempNormZ);
			tempNormX /= length;
			tempNormY /= length;
			tempNormZ /= length;
			// 0.867 > square root of 0.5^2 * 3
			offsetTowardsPlayer = 0.867;
			offsetX += tempNormX * offsetTowardsPlayer;
			offsetY += tempNormY * offsetTowardsPlayer;
			offsetZ += tempNormZ * offsetTowardsPlayer;
		}

		return new Vec3d(soundX + offsetX, soundY + offsetY, soundZ + offsetZ);
	}

	@SuppressWarnings("deprecation")
	private static void evaluateEnvironment(final int sourceID, final float posX, final float posY, final float posZ) {
		if (posX < 0.01f && posY < 0.01f && posZ < 0.01f) {
			// Supposed to catch menu sounds, but quite crude
			setEnvironment(sourceID, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f);
			return;
		}

		if (mc.player == null || mc.world == null) {
			return;
		}

		final boolean isRain = lastSoundName.matches(".*rain.*");

		if (SoundPhysicsCore.Config.skipRainOcclusionTracing && isRain) {
			setEnvironment(sourceID, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f);
			return;
		}

		float directCutoff = 1.0f;
		final float absorptionCoeff = SoundPhysicsCore.Config.globalBlockAbsorption * 3.0f;

		final Vec3d playerPos = new Vec3d(mc.player.posX, mc.player.posY + mc.player.getEyeHeight(), mc.player.posZ);
		final Vec3d soundPos = offsetSoundByName(posX, posY, posZ, playerPos, lastSoundName, lastSoundCategory);
		final Vec3d normalToPlayer = playerPos.subtract(soundPos).normalize();

		logGeneral("Player pos: " + playerPos.xCoord + ", " + playerPos.yCoord + ", " + playerPos.zCoord
				+ "      Sound Pos: " + soundPos.xCoord + ", " + soundPos.yCoord + ", " + soundPos.zCoord
				+ "       To player vector: " + normalToPlayer.xCoord + ", " + normalToPlayer.yCoord + ", "
				+ normalToPlayer.zCoord);

		Vec3d rayOrigin = soundPos;

		float occlusionAccumulation = 0.0f;

		for (int i = 0; i < 10; i++) {
			final RayTraceResult rayHit = mc.world.rayTraceBlocks(rayOrigin, playerPos, true);

			if (rayHit == null)
				break;

			final Block blockHit = mc.world.getBlockState(rayHit.getBlockPos()).getBlock();

			float blockOcclusion = 1.0f;

			if (!blockHit.isOpaqueCube(blockHit.getDefaultState())) {
				// log("not a solid block!");
				blockOcclusion *= 0.15f;
			}

			logOcclusion(blockHit.getUnlocalizedName() + "    " + rayHit.hitVec.xCoord + ", " + rayHit.hitVec.yCoord
					+ ", " + rayHit.hitVec.zCoord);

			occlusionAccumulation += blockOcclusion;

			rayOrigin = new Vec3d(rayHit.hitVec.xCoord + normalToPlayer.xCoord * 0.1,
					rayHit.hitVec.yCoord + normalToPlayer.yCoord * 0.1,
					rayHit.hitVec.zCoord + normalToPlayer.zCoord * 0.1);

			logOcclusion("New trace position: " + rayOrigin.xCoord + ", " + rayOrigin.yCoord + ", " + rayOrigin.zCoord);
		}

		directCutoff = (float) Math.exp(-occlusionAccumulation * absorptionCoeff);
		float directGain = (float) Math.pow(directCutoff, 0.1);

		logOcclusion("direct cutoff: " + directCutoff + "  direct gain:" + directGain);

		// Calculate reverb parameters for this sound
		float sendGain0 = 0.0f;
		float sendGain1 = 0.0f;
		float sendGain2 = 0.0f;
		float sendGain3 = 0.0f;

		float sendCutoff0 = 1.0f;
		float sendCutoff1 = 1.0f;
		float sendCutoff2 = 1.0f;
		float sendCutoff3 = 1.0f;

		if (mc.player.isInsideOfMaterial(Material.WATER)) {
			directCutoff *= 1.0f - SoundPhysicsCore.Config.underwaterFilter;
		}

		if (isRain) {
			setEnvironment(sourceID, sendGain0, sendGain1, sendGain2, sendGain3, sendCutoff0, sendCutoff1, sendCutoff2,
					sendCutoff3, directCutoff, directGain);
			return;
		}

		// Shoot rays around sound
		final float phi = 1.618033988f;
		final float gAngle = phi * (float) Math.PI * 2.0f;
		final float maxDistance = 256.0f;

		final int numRays = SoundPhysicsCore.Config.environmentEvaluationRays;
		final int rayBounces = 4;

		final float[] bounceReflectivityRatio = new float[rayBounces];

		float sharedAirspace = 0.0f;

		final float rcpTotalRays = 1.0f / (numRays * rayBounces);
		final float rcpPrimaryRays = 1.0f / numRays;

		for (int i = 0; i < numRays; i++) {
			final float fi = i;
			final float fiN = fi / numRays;
			final float longitude = gAngle * fi;
			final float latitude = (float) Math.asin(fiN * 2.0f - 1.0f);

			Vec3d rayDir = new Vec3d(0.0, 0.0, 0.0);
			// rayDir.xCoord = Math.cos(latitude) * Math.cos(longitude);
			// rayDir.zCoord = Math.cos(latitude) * Math.sin(longitude);
			// rayDir.yCoord = Math.sin(latitude);
			{
				final double x = Math.cos(latitude) * Math.cos(longitude);
				final double y = Math.cos(latitude) * Math.sin(longitude);
				final double z = Math.sin(latitude);
				rayDir = new Vec3d(x, y, z);
			}

			// Vec3d rayStart = Vec3d.createVectorHelper(soundPos.xCoord +
			// rayDir.xCoord * 0.867, soundPos.yCoord + rayDir.yCoord * 0.867,
			// soundPos.zCoord + rayDir.zCoord * 0.867);
			final Vec3d rayStart = new Vec3d(soundPos.xCoord, soundPos.yCoord, soundPos.zCoord);

			final Vec3d rayEnd = new Vec3d(rayStart.xCoord + rayDir.xCoord * maxDistance,
					rayStart.yCoord + rayDir.yCoord * maxDistance, rayStart.zCoord + rayDir.zCoord * maxDistance);

			final RayTraceResult rayHit = mc.world.rayTraceBlocks(rayStart, rayEnd, true);

			if (rayHit != null) {
				final double rayLength = soundPos.distanceTo(rayHit.hitVec);

				// Additional bounces
				BlockPos lastHitBlock = rayHit.getBlockPos();
				Vec3d lastHitPos = rayHit.hitVec;
				Vec3d lastHitNormal = getNormalFromFacing(rayHit.sideHit);
				Vec3d lastRayDir = rayDir;

				float totalRayDistance = (float) rayLength;

				// Secondary ray bounces
				for (int j = 0; j < rayBounces; j++) {
					final Vec3d newRayDir = reflect(lastRayDir, lastHitNormal);
					// Vec3d newRayDir = lastHitNormal;
					final Vec3d newRayStart = new Vec3d(lastHitPos.xCoord + lastHitNormal.xCoord * 0.01,
							lastHitPos.yCoord + lastHitNormal.yCoord * 0.01,
							lastHitPos.zCoord + lastHitNormal.zCoord * 0.01);
					final Vec3d newRayEnd = new Vec3d(newRayStart.xCoord + newRayDir.xCoord * maxDistance,
							newRayStart.yCoord + newRayDir.yCoord * maxDistance,
							newRayStart.zCoord + newRayDir.zCoord * maxDistance);

					final RayTraceResult newRayHit = mc.world.rayTraceBlocks(newRayStart, newRayEnd, true);

					float energyTowardsPlayer = 0.25f;
					final float blockReflectivity = getBlockReflectivity(lastHitBlock);
					energyTowardsPlayer *= blockReflectivity * 0.75f + 0.25f;

					if (newRayHit == null) {
						totalRayDistance += lastHitPos.distanceTo(playerPos);
					} else {
						final double newRayLength = lastHitPos.distanceTo(newRayHit.hitVec);

						bounceReflectivityRatio[j] += (float) blockReflectivity;

						totalRayDistance += newRayLength;

						lastHitPos = newRayHit.hitVec;
						lastHitNormal = getNormalFromFacing(newRayHit.sideHit);
						lastRayDir = newRayDir;
						lastHitBlock = newRayHit.getBlockPos();

						// Cast one final ray towards the player. If it's
						// unobstructed, then the sound source and the player
						// share airspace.
						if (SoundPhysicsCore.Config.simplerSharedAirspaceSimulation && j == rayBounces - 1
								|| !SoundPhysicsCore.Config.simplerSharedAirspaceSimulation) {
							final Vec3d finalRayStart = new Vec3d(lastHitPos.xCoord + lastHitNormal.xCoord * 0.01,
									lastHitPos.yCoord + lastHitNormal.yCoord * 0.01,
									lastHitPos.zCoord + lastHitNormal.zCoord * 0.01);

							final RayTraceResult finalRayHit = mc.world.rayTraceBlocks(finalRayStart, playerPos, true);

							if (finalRayHit == null) {
								// log("Secondary ray hit the player!");
								sharedAirspace += 1.0f;
							}
						}
					}

					final float reflectionDelay = (float) Math.max(totalRayDistance, 0.0) * 0.12f * blockReflectivity;

					final float cross0 = 1.0f - MathHelper.clamp(Math.abs(reflectionDelay - 0.0f), 0.0f, 1.0f);
					final float cross1 = 1.0f - MathHelper.clamp(Math.abs(reflectionDelay - 1.0f), 0.0f, 1.0f);
					final float cross2 = 1.0f - MathHelper.clamp(Math.abs(reflectionDelay - 2.0f), 0.0f, 1.0f);
					final float cross3 = MathHelper.clamp(reflectionDelay - 2.0f, 0.0f, 1.0f);

					sendGain0 += cross0 * energyTowardsPlayer * 6.4f * rcpTotalRays;
					sendGain1 += cross1 * energyTowardsPlayer * 12.8f * rcpTotalRays;
					sendGain2 += cross2 * energyTowardsPlayer * 12.8f * rcpTotalRays;
					sendGain3 += cross3 * energyTowardsPlayer * 12.8f * rcpTotalRays;

					// Nowhere to bounce off of, stop bouncing!
					if (newRayHit == null) {
						break;
					}
				}
			}

		}

		// log("total reflectivity ratio: " + totalReflectivityRatio);

		bounceReflectivityRatio[0] = bounceReflectivityRatio[0] / numRays;
		bounceReflectivityRatio[1] = bounceReflectivityRatio[1] / numRays;
		bounceReflectivityRatio[2] = bounceReflectivityRatio[2] / numRays;
		bounceReflectivityRatio[3] = bounceReflectivityRatio[3] / numRays;

		sharedAirspace *= 64.0f;

		if (SoundPhysicsCore.Config.simplerSharedAirspaceSimulation) {
			sharedAirspace *= rcpPrimaryRays;
		} else {
			sharedAirspace *= rcpTotalRays;
		}

		final float sharedAirspaceWeight0 = MathHelper.clamp(sharedAirspace / 20.0f, 0.0f, 1.0f);
		final float sharedAirspaceWeight1 = MathHelper.clamp(sharedAirspace / 15.0f, 0.0f, 1.0f);
		final float sharedAirspaceWeight2 = MathHelper.clamp(sharedAirspace / 10.0f, 0.0f, 1.0f);
		final float sharedAirspaceWeight3 = MathHelper.clamp(sharedAirspace / 10.0f, 0.0f, 1.0f);

		sendCutoff0 = (float) Math.exp(-occlusionAccumulation * absorptionCoeff * 1.0f) * (1.0f - sharedAirspaceWeight0)
				+ sharedAirspaceWeight0;
		sendCutoff1 = (float) Math.exp(-occlusionAccumulation * absorptionCoeff * 1.0f) * (1.0f - sharedAirspaceWeight1)
				+ sharedAirspaceWeight1;
		sendCutoff2 = (float) Math.exp(-occlusionAccumulation * absorptionCoeff * 1.5f) * (1.0f - sharedAirspaceWeight2)
				+ sharedAirspaceWeight2;
		sendCutoff3 = (float) Math.exp(-occlusionAccumulation * absorptionCoeff * 1.5f) * (1.0f - sharedAirspaceWeight3)
				+ sharedAirspaceWeight3;

		// attempt to preserve directionality when airspace is shared by
		// allowing some of the dry signal through but filtered
		final float averageSharedAirspace = (sharedAirspaceWeight0 + sharedAirspaceWeight1 + sharedAirspaceWeight2
				+ sharedAirspaceWeight3) * 0.25f;
		directCutoff = Math.max((float) Math.pow(averageSharedAirspace, 0.5) * 0.2f, directCutoff);

		directGain = (float) Math.pow(directCutoff, 0.1);

		logEnvironment("Bounce reflectivity 0: " + bounceReflectivityRatio[0] + " bounce reflectivity 1: "
				+ bounceReflectivityRatio[1] + " bounce reflectivity 2: " + bounceReflectivityRatio[2]
				+ " bounce reflectivity 3: " + bounceReflectivityRatio[3]);

		sendGain1 *= (float) bounceReflectivityRatio[1];
		sendGain2 *= (float) Math.pow(bounceReflectivityRatio[2], 3.0);
		sendGain3 *= (float) Math.pow(bounceReflectivityRatio[3], 4.0);

		sendGain0 = MathHelper.clamp(sendGain0, 0.0f, 1.0f);
		sendGain1 = MathHelper.clamp(sendGain1, 0.0f, 1.0f);
		sendGain2 = MathHelper.clamp(sendGain2 * 1.05f - 0.05f, 0.0f, 1.0f);
		sendGain3 = MathHelper.clamp(sendGain3 * 1.05f - 0.05f, 0.0f, 1.0f);

		sendGain0 *= (float) Math.pow(sendCutoff0, 0.1);
		sendGain1 *= (float) Math.pow(sendCutoff1, 0.1);
		sendGain2 *= (float) Math.pow(sendCutoff2, 0.1);
		sendGain3 *= (float) Math.pow(sendCutoff3, 0.1);

		logEnvironment("Final environment settings:   " + sendGain0 + ",   " + sendGain1 + ",   " + sendGain2 + ",   "
				+ sendGain3);

		if (mc.player.isInWater()) {
			sendCutoff0 *= 0.4f;
			sendCutoff1 *= 0.4f;
			sendCutoff2 *= 0.4f;
			sendCutoff3 *= 0.4f;
		}

		setEnvironment(sourceID, sendGain0, sendGain1, sendGain2, sendGain3, sendCutoff0, sendCutoff1, sendCutoff2,
				sendCutoff3, directCutoff, directGain);
	}

	private static void setEnvironment(final int sourceID, final float sendGain0, final float sendGain1,
			final float sendGain2, final float sendGain3, final float sendCutoff0, final float sendCutoff1,
			final float sendCutoff2, final float sendCutoff3, final float directCutoff, final float directGain) {
		// Set reverb send filter values and set source to send to all reverb fx
		// slots
		EFX10.alFilterf(sendFilter0, EFX10.AL_LOWPASS_GAIN, sendGain0);
		EFX10.alFilterf(sendFilter0, EFX10.AL_LOWPASS_GAINHF, sendCutoff0);
		AL11.alSource3i(sourceID, EFX10.AL_AUXILIARY_SEND_FILTER, auxFXSlot0, 0, sendFilter0);

		EFX10.alFilterf(sendFilter1, EFX10.AL_LOWPASS_GAIN, sendGain1);
		EFX10.alFilterf(sendFilter1, EFX10.AL_LOWPASS_GAINHF, sendCutoff1);
		AL11.alSource3i(sourceID, EFX10.AL_AUXILIARY_SEND_FILTER, auxFXSlot1, 1, sendFilter1);

		EFX10.alFilterf(sendFilter2, EFX10.AL_LOWPASS_GAIN, sendGain2);
		EFX10.alFilterf(sendFilter2, EFX10.AL_LOWPASS_GAINHF, sendCutoff2);
		AL11.alSource3i(sourceID, EFX10.AL_AUXILIARY_SEND_FILTER, auxFXSlot2, 2, sendFilter2);

		EFX10.alFilterf(sendFilter3, EFX10.AL_LOWPASS_GAIN, sendGain3);
		EFX10.alFilterf(sendFilter3, EFX10.AL_LOWPASS_GAINHF, sendCutoff3);
		AL11.alSource3i(sourceID, EFX10.AL_AUXILIARY_SEND_FILTER, auxFXSlot3, 3, sendFilter3);

		EFX10.alFilterf(directFilter0, EFX10.AL_LOWPASS_GAIN, directGain);
		EFX10.alFilterf(directFilter0, EFX10.AL_LOWPASS_GAINHF, directCutoff);
		AL10.alSourcei(sourceID, EFX10.AL_DIRECT_FILTER, directFilter0);

		AL10.alSourcef(sourceID, EFX10.AL_AIR_ABSORPTION_FACTOR, SoundPhysicsCore.Config.airAbsorption);
	}

	/**
	 * Applies the parameters in the enum ReverbParams to the main reverb
	 * effect.
	 */
	protected static void setReverbParams(final ReverbParams r, final int auxFXSlot, final int reverbSlot) {
		EFX10.alEffectf(reverbSlot, EFX10.AL_EAXREVERB_DENSITY, r.density);
		checkErrorLog("Error while assigning reverb density: " + r.density);

		EFX10.alEffectf(reverbSlot, EFX10.AL_EAXREVERB_DIFFUSION, r.diffusion);
		checkErrorLog("Error while assigning reverb diffusion: " + r.diffusion);

		EFX10.alEffectf(reverbSlot, EFX10.AL_EAXREVERB_GAIN, r.gain);
		checkErrorLog("Error while assigning reverb gain: " + r.gain);

		EFX10.alEffectf(reverbSlot, EFX10.AL_EAXREVERB_GAINHF, r.gainHF);
		checkErrorLog("Error while assigning reverb gainHF: " + r.gainHF);

		EFX10.alEffectf(reverbSlot, EFX10.AL_EAXREVERB_DECAY_TIME, r.decayTime);
		checkErrorLog("Error while assigning reverb decayTime: " + r.decayTime);

		EFX10.alEffectf(reverbSlot, EFX10.AL_EAXREVERB_DECAY_HFRATIO, r.decayHFRatio);
		checkErrorLog("Error while assigning reverb decayHFRatio: " + r.decayHFRatio);

		EFX10.alEffectf(reverbSlot, EFX10.AL_EAXREVERB_REFLECTIONS_GAIN, r.reflectionsGain);
		checkErrorLog("Error while assigning reverb reflectionsGain: " + r.reflectionsGain);

		EFX10.alEffectf(reverbSlot, EFX10.AL_EAXREVERB_LATE_REVERB_GAIN, r.lateReverbGain);
		checkErrorLog("Error while assigning reverb lateReverbGain: " + r.lateReverbGain);

		EFX10.alEffectf(reverbSlot, EFX10.AL_EAXREVERB_LATE_REVERB_DELAY, r.lateReverbDelay);
		checkErrorLog("Error while assigning reverb lateReverbDelay: " + r.lateReverbDelay);

		EFX10.alEffectf(reverbSlot, EFX10.AL_EAXREVERB_AIR_ABSORPTION_GAINHF, r.airAbsorptionGainHF);
		checkErrorLog("Error while assigning reverb airAbsorptionGainHF: " + r.airAbsorptionGainHF);

		EFX10.alEffectf(reverbSlot, EFX10.AL_EAXREVERB_ROOM_ROLLOFF_FACTOR, r.roomRolloffFactor);
		checkErrorLog("Error while assigning reverb roomRolloffFactor: " + r.roomRolloffFactor);

		// Attach updated effect object
		EFX10.alAuxiliaryEffectSloti(auxFXSlot, EFX10.AL_EFFECTSLOT_EFFECT, reverbSlot);
	}

	protected static void log(final String message) {
		System.out.println(logPrefix + ": " + message);
	}

	protected static void logOcclusion(final String message) {
		if (SoundPhysicsCore.Config.occlusionLogging)
			System.out.println(logPrefix + " [OCCLUSION] " + ": " + message);
	}

	protected static void logEnvironment(final String message) {
		if (SoundPhysicsCore.Config.environmentLogging)
			System.out.println(logPrefix + " [ENVIRONMENT] " + ": " + message);
	}

	protected static void logGeneral(final String message) {
		if (SoundPhysicsCore.Config.debugLogging)
			System.out.println(logPrefix + ": " + message);
	}

	protected static void logError(final String errorMessage) {
		System.out.println(logPrefix + " [ERROR]: " + errorMessage);
	}

	protected static boolean checkErrorLog(final String errorMessage) {
		final int error = AL10.alGetError();
		if (error == 0)
			return false;

		String errorName;

		switch (error) {
		case AL10.AL_INVALID_NAME:
			errorName = "AL_INVALID_NAME";
			break;
		case AL10.AL_INVALID_ENUM:
			errorName = "AL_INVALID_ENUM";
			break;
		case AL10.AL_INVALID_VALUE:
			errorName = "AL_INVALID_VALUE";
			break;
		case AL10.AL_INVALID_OPERATION:
			errorName = "AL_INVALID_OPERATION";
			break;
		case AL10.AL_OUT_OF_MEMORY:
			errorName = "AL_OUT_OF_MEMORY";
			break;
		default:
			errorName = Integer.toString(error);
			break;
		}

		System.out.println(logPrefix + " [ERROR]: " + errorMessage + " OpenAL error " + errorName);
		return true;
	}

}
