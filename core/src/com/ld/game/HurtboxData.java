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
			{6, 25, 8, 20, 9},
			{8, 50, 16, 20, 6},
			{9, 70, 24, 20, 3},
			{13, -25, -8, 20, 9},
			{15, -50, -16, 20, 6},
			{16, -70, -24, 20, 3},
		});
		durationMap.put(AnimationType.AIR_NAIR, 20);
		
		float[][] airUairData = new float[22][5];
		for (int i = -5; i <= 5; ++i) {
			airUairData[2*(i + 5)] = new float[]{10 + i, (float) (65 * Math.sin(-i*Math.PI/12)), (float) (65 * Math.cos(i*Math.PI/12)), 15, 2};
			airUairData[2*(i + 5) + 1] = new float[]{10 + i, (float) (65 * Math.sin(-i*Math.PI/12) / 2), (float) (65 * Math.cos(i*Math.PI/12) / 2), 15, 2};
		}
		
		hurtboxLocationMap.put(AnimationType.AIR_UAIR, airUairData);
		durationMap.put(AnimationType.AIR_UAIR, 18);
		
		float[][] airDairData = new float[60][5];
		for (int i = 0; i < 14; ++i) {
			airDairData[2*i] = new float[]{6 + i, (float) (65 * Math.sin((i+2)*Math.PI/8)), (float) (65 * Math.cos((i+2)*Math.PI/8)), 15, 1};
			airDairData[2*i + 1] = new float[]{6 + i, (float) (65 * Math.sin((i+2)*Math.PI/8) / 2), (float) (65 * Math.cos((i+2)*Math.PI/8) / 2), 15, 1};
		}
		
		hurtboxLocationMap.put(AnimationType.AIR_DAIR, airDairData);
		durationMap.put(AnimationType.AIR_DAIR, 23);
	}
}
