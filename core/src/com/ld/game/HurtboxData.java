package com.ld.game;

import java.util.HashMap;

enum AnimationType {
	GROUND_FSMASH,
	GROUND_DSMASH,
	GROUND_USMASH,
	
	AIR_NAIR,
	AIR_FAIR,
	AIR_UAIR,
	AIR_DAIR,
}
public class HurtboxData {
	// Singleton
	private static HurtboxData hurtboxData = null;
	
	private HashMap<AnimationType, float[][]> hurtboxLocationMap;
	private HashMap<AnimationType, Integer> durationMap;
	
	public HurtboxData() {
		hurtboxLocationMap = new HashMap<AnimationType, float[][]>();
		durationMap = new HashMap<AnimationType, Integer>();
		initializeData();
	}
	
	public static HurtboxData getInstance() {
		if (hurtboxData == null) {
			hurtboxData = new HurtboxData();
		}
		return hurtboxData;
	}
	
	public static float[][] getAnimationFrames(AnimationType key) {
		return getInstance().hurtboxLocationMap.get(key);
	}
	
	public static int getDuration(AnimationType key) {
		return getInstance().durationMap.get(key);
	}

	public void initializeData() {
		// Format is { frame delay, X position, Y position, radius, duration }
		hurtboxLocationMap.put(AnimationType.GROUND_DSMASH, new float[][]{
			{3, -30, 70, 25, 4},
			{7, 0, 76.15f, 25, 4},
			{11, 30, 70, 25, 4},
		});
		durationMap.put(AnimationType.GROUND_DSMASH, 16);
		
		hurtboxLocationMap.put(AnimationType.GROUND_FSMASH, new float[][]{
			{5, 80, 0, 20, 22},
			{8, 100, 0, 20, 16},
			{11, 120, 0, 20, 10},
			{15, 140, 0, 20, 4},
		});
		durationMap.put(AnimationType.GROUND_FSMASH, 32);

		hurtboxLocationMap.put(AnimationType.AIR_NAIR, new float[][]{
			{6, 20, 8, 15, 9},
			{8, 40, 16, 15, 6},
			{9, 60, 24, 15, 3},
			{13, -20, -8, 15, 9},
			{15, -40, -16, 15, 6},
			{16, -60, -24, 15, 3},
		});
		durationMap.put(AnimationType.AIR_NAIR, 20);
		
		hurtboxLocationMap.put(AnimationType.AIR_UAIR, new float[][]{
				{3, 63.02f, 11.11f, 15, 4},
				{6, 41.13f, 49.02f, 15, 4},
				{9, 0, 64.15f, 15, 4},
				{12, -41.13f, 49.02f, 15, 4},
				{15, -63.02f, 11.11f, 15, 4},
				
				{3, 63.02f/2, 11.11f/2, 15, 4},
				{6, 41.13f/2, 60.14f/2, 15, 4},
				{9, 0, 64.15f/2, 15, 4},
				{12, -41.13f/2, 49.02f/2, 15, 4},
				{15, -63.02f/2, 11.11f/2, 15, 4},
			});
			durationMap.put(AnimationType.AIR_UAIR, 21);
	}
}
