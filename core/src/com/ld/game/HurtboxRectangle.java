package com.ld.game;

import com.badlogic.gdx.math.Rectangle;

@SuppressWarnings("serial")
public class HurtboxRectangle extends Rectangle implements Hurtbox {
	public Projectile ownerProjectile;
    
	public HurtboxRectangle(Rectangle rect, Projectile ownerProjectile) {
		super(rect);
		this.ownerProjectile = ownerProjectile;
	}
}
