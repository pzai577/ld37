package com.ld.game;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class Projectile {
	
//	protected Array<Rectangle> hitboxes;
//	
//	public Array<Color> colors;
	public HurtboxRectangle hitbox;
	public Vector2 head;
	
	protected float horizVelocity;
	protected float vertVelocity;
	protected float horizAccel;
	protected float vertAccel;
	
	public Projectile() {
//		this.hitboxes = new Array<Rectangle>();
//		this.colors = new Array<Color>();
		this.hitbox = new HurtboxRectangle(new Rectangle());
		this.head = new Vector2();
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
		this.head.add(horizVelocity, vertVelocity);
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
	
	public void destroy() {
		
	}

}