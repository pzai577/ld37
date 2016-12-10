package com.ld.game;

import java.util.HashMap;

enum AnimationType {
	GROUND_FSMASH,
	GROUND_DSMASH,
	GROUND_USMASH,
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
		hurtboxLocationMap.put(AnimationType.GROUND_DSMASH, new float[][]{
			{3, -30 + 28, 70, 25, 4},
			{7, 0 + 28, 76.15f, 25, 4},
			{11, 30 + 28, 70, 25, 4},
		});
		durationMap.put(AnimationType.GROUND_DSMASH, 16);
		
		hurtboxLocationMap.put(AnimationType.GROUND_FSMASH, new float[][]{
			{5, 80, 28, 20, 22},
			{8, 100, 28, 20, 16},
			{11, 120, 28, 20, 10},
			{15, 140, 28, 20, 4},
		});
		durationMap.put(AnimationType.GROUND_FSMASH, 32);
	}
}
