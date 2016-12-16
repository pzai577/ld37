package com.ld.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;

public class Sounds {

	Sound swordSound, laserSound, jumpSound, dblJumpSound, targetBreakSound, checkpointSound, runSound, climbSound, deathSound, landingSound, dialogueSound;
	
	public Sounds() {
		
		swordSound = Gdx.audio.newSound(Gdx.files.internal("swoosh.mp3"));
		//http://soundbible.com/706-Swoosh-3.html
		
		laserSound = Gdx.audio.newSound(Gdx.files.internal("laser.wav"));
		//http://soundbible.com/472-Laser-Blasts.html
		
		targetBreakSound = Gdx.audio.newSound(Gdx.files.internal("target.wav"));
		//https://www.freesound.org/people/Kastenfrosch/sounds/162467/
		
		checkpointSound = Gdx.audio.newSound(Gdx.files.internal("checkpoint.wav"));
		//http://soundbible.com/1003-Ta-Da.html
		
		jumpSound = Gdx.audio.newSound(Gdx.files.internal("jump.wav"));
		//http://soundbible.com/1601-Mario-Jumping.html
		
		dblJumpSound = Gdx.audio.newSound(Gdx.files.internal("double_jump.wav"));
		//http://soundbible.com/1343-Jump.html
		
		deathSound = Gdx.audio.newSound(Gdx.files.internal("death.wav"));
		
		landingSound = Gdx.audio.newSound(Gdx.files.internal("land.wav"));
		
		dialogueSound = Gdx.audio.newSound(Gdx.files.internal("dialogue2.wav"));
	}

}
