package com.ld.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;

public class Sounds {

	Sound swordSound, laserSound, jumpSound, dblJumpSound, targetBreakSound, checkpointSound, runSound, climbSound;
	
	public Sounds() {
		
		swordSound = Gdx.audio.newSound(Gdx.files.internal("swoosh.mp3"));
		//http://soundbible.com/706-Swoosh-3.html
		
		laserSound = Gdx.audio.newSound(Gdx.files.internal("laser.mp3"));
		//http://soundbible.com/472-Laser-Blasts.html
		
		targetBreakSound = Gdx.audio.newSound(Gdx.files.internal("item.mp3"));
		//https://www.freesound.org/people/Kastenfrosch/sounds/162467/
		
		checkpointSound = Gdx.audio.newSound(Gdx.files.internal("tada.mp3"));
		//http://soundbible.com/1003-Ta-Da.html
		
		jumpSound = Gdx.audio.newSound(Gdx.files.internal("mario-jump.mp3"));
		//http://soundbible.com/1601-Mario-Jumping.html
		
		dblJumpSound = Gdx.audio.newSound(Gdx.files.internal("jump.mp3"));
		//http://soundbible.com/1343-Jump.html
	}

}
