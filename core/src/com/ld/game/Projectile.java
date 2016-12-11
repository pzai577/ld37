package com.ld.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;

public class Projectile {
	
	protected Array<Rectangle> hitboxes;
	
	public Color color;
	
	protected double horizVelocity;
	protected double vertVelocity;
	protected double horizAccel;
	protected double vertAccel;
	
	public Projectile() {
		this.hitboxes = new Array<Rectangle>();
		this.color = null;
		this.horizVelocity = 0;
		this.vertVelocity = 0;
		this.horizAccel = 0;
		this.vertAccel = 0;
	}
	
	public void update() {
		for(Rectangle box: hitboxes) {
			box.x += this.horizVelocity;
			box.y -= this.vertVelocity;
		}
		horizVelocity += horizAccel;
		vertVelocity += vertAccel;
	}

}