package com.ld.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;

public class Sounds {

	Sound swordSound, laserSound, jumpSound, dblJumpSound, targetBreakSound, checkpointSound, runSound, climbSound, deathSound, landingSound, dialogueSound, legFinishSound;
	
	public Sounds() {		
		swordSound = Gdx.audio.newSound(Gdx.files.internal("swoosh.mp3"));
		
		laserSound = Gdx.audio.newSound(Gdx.files.internal("laser.wav"));
		
		targetBreakSound = Gdx.audio.newSound(Gdx.files.internal("target.wav"));
		
		checkpointSound = Gdx.audio.newSound(Gdx.files.internal("checkpoint.wav"));
		
		jumpSound = Gdx.audio.newSound(Gdx.files.internal("jump.wav"));
		
		dblJumpSound = Gdx.audio.newSound(Gdx.files.internal("double_jump.wav"));
		
		deathSound = Gdx.audio.newSound(Gdx.files.internal("death.wav"));
		
		landingSound = Gdx.audio.newSound(Gdx.files.internal("land.wav"));
		
		dialogueSound = Gdx.audio.newSound(Gdx.files.internal("dialogue2.wav"));
		
		legFinishSound = Gdx.audio.newSound(Gdx.files.internal("finish_leg.wav"));
	}

}
