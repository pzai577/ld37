package com.ld.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.ld.game.Player;

public class LaserPulse extends Projectile {
	
	Player player;
	Rectangle pulsebox;
	
	float laserLength = 50;
	float laserThickness = 2;
	float speed = 7;
//	float speed = 0;
	float pulseSize = 2;
//	float pulseSize = 10;
	int pulseHalfPeriod = 15; // in frames, which should be 1/60 s
	int currentPulseTime;
	boolean pulsingDown;
	
	Color laserColor = Color.CYAN;
	Color pulseColor = new Color(0f, 0.5f, 0.5f, 0.1f);
	
	public LaserPulse(Player player, boolean shootingRight){
		super();
		
		this.player = player;
		
		if(shootingRight){
			this.hitbox = new HurtboxRectangle(new Rectangle(player.position.x + Player.PLAYER_WIDTH, player.position.y + 20, laserLength, laserThickness));
			this.head = new Vector2(hitbox.x + laserLength, hitbox.y);
			this.horizVelocity = speed;
		}
		else{
			this.hitbox = new HurtboxRectangle(new Rectangle(player.position.x - laserLength, player.position.y + 20, laserLength, laserThickness));
			this.head = new Vector2(hitbox.x, hitbox.y);
			this.horizVelocity = -speed;
		}
		
		this.laserColor.a = 0.5f;
		
		this.pulsebox = new Rectangle(hitbox.x, hitbox.y - pulseSize, laserLength, laserThickness + 2 * pulseSize);
		currentPulseTime = pulseHalfPeriod;
		pulsingDown = true;
		
//		this.colors.add();
//		this.hitboxes.add(pulsebox);
//		this.colors.add(Color.CYAN);
//		this.hitboxes.add(hitbox);
	}
	
	@Override
	public void update() {
		super.update();
		
		updateBox(pulsebox);
		if(pulsingDown) {
			currentPulseTime--;
			if(currentPulseTime == 0) {
				pulsingDown = !pulsingDown;
			}
		}
		else {
			currentPulseTime++;
			if(currentPulseTime == pulseHalfPeriod) {
				pulsingDown = !pulsingDown;
			}
		}
		this.pulseColor.a = 1.0f * currentPulseTime / pulseHalfPeriod;
	}
	
	@Override
	public void render(ShapeRenderer r) {
		r.setColor(pulseColor);
		renderRectHelper(r, pulsebox);
		r.arc(hitbox.x, hitbox.y+hitbox.height/2, hitbox.height/2 + pulseSize, 90, 180);
		r.arc(hitbox.x + laserLength, hitbox.y+hitbox.height/2, hitbox.height/2 + pulseSize, 270, 180);
		r.setColor(laserColor);
		renderRectHelper(r, hitbox);
	}
	
	@Override
	public void destroy() {
		super.destroy();
	}

}