package com.ld.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.ld.game.Player;

public class LaserPulse extends Projectile {
	
	Player player;
	Rectangle pulsebox;
	
	float laserLength = 50;
	float laserThickness = 2;
	float speed = 7;
	float pulseSize = 2;
	
	Color laserColor = Color.CYAN;
	Color pulseColor = new Color(0f, 0.5f, 0.5f, 1f);
	
	public LaserPulse(Player player, boolean shootingRight){
		super();
		
		this.player = player;
		
		if(shootingRight){
			this.hitbox = new Rectangle(player.position.x + Player.PLAYER_WIDTH, player.position.y + 20, laserLength, laserThickness);
			this.horizVelocity = speed;
		}
		else{
			this.hitbox = new Rectangle(player.position.x - laserLength, player.position.y + 20, laserLength, laserThickness);
			this.horizVelocity = -speed;
		}
		
		this.pulsebox = new Rectangle(hitbox.x - pulseSize, hitbox.y - pulseSize, laserLength + 2 * pulseSize, laserThickness + 2 * pulseSize);
		
//		this.colors.add();
//		this.hitboxes.add(pulsebox);
//		this.colors.add(Color.CYAN);
//		this.hitboxes.add(hitbox);
	}
	
	@Override
	public void update() {
		super.update();
		updateBox(pulsebox);
	}
	
	@Override
	public void render(ShapeRenderer r) {
		// TODO: make pulsebox rounded and pulse
		r.setColor(pulseColor);
		renderRectHelper(r, pulsebox);
		r.setColor(laserColor);
		renderRectHelper(r, hitbox);
	}

}