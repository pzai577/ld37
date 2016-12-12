package com.ld.game;

import com.badlogic.gdx.math.Circle;

public class HurtboxCircle extends Circle implements Hurtbox {
	private static final long serialVersionUID = 1L;
	//	public float x, y, radius;
	public int duration;
	public HurtboxCircle(float x, float y, float radius, int duration) {
		super(x, y, radius);
//		this.x = x;
//		this.y = y;
//		this.radius = radius;
		this.duration = duration;
	}
	
}
