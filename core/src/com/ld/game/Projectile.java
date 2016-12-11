package com.ld.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Rectangle;

public class Projectile {
	
	protected Rectangle[] hurtboxes;
	
	public Color color;
	
	protected double horizVelocity;
	protected double vertVelocity;
	protected double horizAccel;
	protected double vertAccel;
	
	public Projectile() {
		this.hurtboxes = new Rectangle[0];
		this.color = null;
		this.horizVelocity = 0;
		this.vertVelocity = 0;
		this.horizAccel = 0;
		this.vertAccel = 0;
	}
	
	public void update() {
		for(Rectangle box: hurtboxes) {
			box.x += this.horizVelocity;
			box.y -= this.vertVelocity;
		}
		horizVelocity += horizAccel;
		vertVelocity += vertAccel;
	}

}