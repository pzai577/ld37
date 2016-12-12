package com.ld.game;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;

public class Projectile {
	
//	protected Array<Rectangle> hitboxes;
//	
//	public Array<Color> colors;
	public HurtboxRectangle hitbox;
	
	protected double horizVelocity;
	protected double vertVelocity;
	protected double horizAccel;
	protected double vertAccel;
	
	public Projectile() {
//		this.hitboxes = new Array<Rectangle>();
//		this.colors = new Array<Color>();
		this.hitbox = new HurtboxRectangle(new Rectangle());
		this.horizVelocity = 0;
		this.vertVelocity = 0;
		this.horizAccel = 0;
		this.vertAccel = 0;
	}
	
	public void update() {
//		for(Rectangle box: hitboxes) {
//			box.x += this.horizVelocity;
//			box.y -= this.vertVelocity;
//		}
		updateBox(hitbox);
		horizVelocity += horizAccel;
		vertVelocity += vertAccel;
	}
	
	protected void updateBox(Rectangle rect) {
		rect.x += this.horizVelocity;
		rect.y -= this.vertVelocity;
	}
	
	public void render(ShapeRenderer r){
		return;
	}
	
	protected void renderRectHelper(ShapeRenderer r, Rectangle rect) {
		r.rect(rect.x, rect.y, rect.width, rect.height);
	}

}