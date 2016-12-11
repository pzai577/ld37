package com.ld.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Rectangle;
import com.ld.game.Player;

public class LaserPulse extends Projectile {
	
	Player player;
	
	public LaserPulse(Player player){
		super();
		
		this.player = player;
		
		Rectangle[] boxes = {new Rectangle(player.position.x + 10, player.position.y + 20, 50, 2)};
		this.color = Color.CYAN;
		this.hurtboxes = boxes;
		this.horizVelocity = 5;
		
	}

}