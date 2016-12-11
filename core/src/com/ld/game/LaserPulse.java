package com.ld.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Rectangle;
import com.ld.game.Player;

public class LaserPulse extends Projectile {
	
	Player player;
	
	public LaserPulse(Player player, boolean shootingRight){
		super();
		
		this.player = player;
		
		float laserLength = 50;
		float laserThickness = 2;
		float speed = 5;
		Rectangle hitbox;
		if(shootingRight){
			hitbox = new Rectangle(player.position.x + Player.PLAYER_WIDTH, player.position.y + 20, laserLength, laserThickness);
			this.horizVelocity = speed;
		}
		else{
			hitbox = new Rectangle(player.position.x - laserLength, player.position.y + 20, laserLength, laserThickness);
			this.horizVelocity = -speed;
		}
		
		this.color = Color.CYAN;
		this.hitboxes.add(hitbox);
		
	}

}