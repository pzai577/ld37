package com.ld.game;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Shape2D;
import com.badlogic.gdx.utils.Array;

enum PlayerState {
	GROUND,
	GROUND_PREJUMP,
	GROUND_ANIM,
	AIR,
	AIR_ANIM,
	AIR_ANIM_RECOVER,
	WALL_LEFT,
	WALL_RIGHT;
}

enum PlayerFrame {
	// TODO: maybe get rid of this enum
	STAND,
	RUN,
	RUN_NOARMS,
	PREJUMP,
	CLIMB,
	CYCLONE,
	TWIST,
}

public class Player {
	// TODO: Maybe make all of the EnhancedCells class variables to avoid slowdown?
    // TODO: Use delta time to account for varying frame rates

	private static final double FALL_ACCELERATION = 0.35;
	
	// note: if PLAYER_GROUND_MOVESPEED>PLAYER_AIR_MAX_MOVESPEED, things look weird if you run off a platform
	private static final double PLAYER_GROUND_MAX_MOVESPEED = 5;
	private static final double PLAYER_AIR_INFLUENCE = 0.35;
	private static final double PLAYER_AIR_MAX_MOVESPEED = 5;
	
	private static final double PLAYER_MAX_SLOWFALL_SPEED = 8.3;
	private static final double PLAYER_FASTFALL_SPEED = 9.3;
	
	private static final double PLAYER_JUMP_SPEED = 9.5;
	private static final double PLAYER_SHORTHOP_SPEED = 6.5;
	private static final double PLAYER_PREJUMP_FRAMES = 5;

	private static final double PLAYER_WALL_INFLUENCE = 0.16;
	private static final double PLAYER_WALL_SCALE_SPEED = 5.5;
	private static final double WALL_FRICTION = 0.08;
	private static final double FLOOR_FRICTION = 0.12;
	
	public static final int PLAYER_WIDTH = 40;
	public static final int PLAYER_HEIGHT = 56;
	
	private boolean pause;
	public boolean isAlive;
	public Rectangle position;
	
	private PlayerState playerState = PlayerState.AIR;
	public double playerHorizVelocity = 0.0; // making these two public so Map can access it
	public double playerVertVelocity = 0.0;
	private float playerRotation = 0.0f;
	
	private boolean playerFacingLeft = true;
	public boolean playerHasDoubleJump = false;
	private boolean playerRotating = false;
	private boolean playerRotatingLeft = false;
	private boolean playerFastFalling = false;
	
	public boolean playerSwordVisible = false;
	public boolean playerFlipSword = false;
	public float playerSwordRotation = 0;
	
	private boolean currentAnimationIsFlipped;
	private float[][] currentAnimationFrames;
	private int currentDuration;
	private AnimationType currentAnimationType;
	
	private Sound weaponSound;
	private Sound laserSound;
	
	private PlayerFrame playerFrame;
	
	private int stateFrameDuration = 0;
	
	private Map map;
	private TiledMapTileLayer collisionLayer;
	// TODO: maybe change to Array to avoid excessive garbage collection? only applies for lots of hurtboxes
	private Array<HurtboxCircle> activeHurtboxes;
	private Array<HurtboxRectangle> activeHurtboxRects;
	
	public Player(Map map, TiledMapTileLayer collisionLayer) {
	    isAlive = true;
		position = new Rectangle(200, 200, PLAYER_WIDTH, PLAYER_HEIGHT);
		this.map = map;
		this.collisionLayer = collisionLayer;
		this.activeHurtboxes = new Array<HurtboxCircle>();
		this.activeHurtboxRects = new Array<HurtboxRectangle>();
		this.playerFrame = PlayerFrame.STAND;
		
		weaponSound = Gdx.audio.newSound(Gdx.files.internal("swoosh.mp3"));
		//http://soundbible.com/706-Swoosh-3.html
		laserSound = Gdx.audio.newSound(Gdx.files.internal("laser.mp3"));
		//http://soundbible.com/472-Laser-Blasts.html
	}
	
	public void updateState() {
		if (pause) return;
		if (playerState == PlayerState.AIR || playerState == PlayerState.AIR_ANIM) {
			updatePlayerAir();
		}
		else if (playerState == PlayerState.GROUND || playerState == PlayerState.GROUND_ANIM
				|| playerState == PlayerState.GROUND_PREJUMP) {
			updatePlayerGround();
		}
		else if (playerState == PlayerState.WALL_LEFT) {
			updatePlayerWallLeft();
		}
		else if (playerState == PlayerState.WALL_RIGHT) {
			updatePlayerWallRight();
		}
		
		// shoot laser gun
		if(Gdx.input.isKeyJustPressed(Keys.C)) {
			shootLaser();
		}
		
		++stateFrameDuration;
	}
	
	private void updatePlayerAir(){
		if (Gdx.input.isKeyJustPressed(Keys.DOWN) && playerVertVelocity > 0) {
			playerFastFalling = true;
		}
		if (playerState == PlayerState.AIR) {
			if (Gdx.input.isKeyPressed(Keys.LEFT)) {
				playerHorizVelocity -= PLAYER_AIR_INFLUENCE;
				playerHorizVelocity = Math.max(playerHorizVelocity, -PLAYER_AIR_MAX_MOVESPEED);
	            playerFacingLeft = true;
			}
			if (Gdx.input.isKeyPressed(Keys.RIGHT)) {
				playerHorizVelocity += PLAYER_AIR_INFLUENCE;
				playerHorizVelocity = Math.min(playerHorizVelocity, PLAYER_AIR_MAX_MOVESPEED);
	            playerFacingLeft = false;
			}
		}
		boolean wasInAirAnim = true;
		if (playerState == PlayerState.AIR) {
			if (Gdx.input.isKeyJustPressed(Keys.Z) && playerHasDoubleJump) {
				if (Gdx.input.isKeyPressed(Keys.LEFT)) {
					playerHorizVelocity = -PLAYER_AIR_MAX_MOVESPEED;
					playerFacingLeft = true;
				}
				else if (Gdx.input.isKeyPressed(Keys.RIGHT)) {
					playerHorizVelocity = PLAYER_AIR_MAX_MOVESPEED;
					playerFacingLeft = false;
				}
				playerHasDoubleJump = false;
				map.particles.add(new Particle(position.x, position.y,
						new Point[]{ new Point(0, 0), new Point(1, 0), new Point(2, 0), new Point(2, 0) }));
				//playerRotating = true;
				playerFastFalling = false;
				playerRotatingLeft = (playerHorizVelocity <= 0);
				playerVertVelocity = -8;
			}
			wasInAirAnim = false;
		}
		//update y position of player
		if (playerFastFalling) {
			position.y -= PLAYER_FASTFALL_SPEED;
		}
		else {
			position.y -= playerVertVelocity;
			playerVertVelocity = Math.min(playerVertVelocity + FALL_ACCELERATION,
										  PLAYER_MAX_SLOWFALL_SPEED);
		}
		//check whether you're intersecting something vertically
		EnhancedCell topCell = getCollidingTopCell();
		EnhancedCell bottomCell = getCollidingBottomCell();

		if (topCell != null) {
			if (playerVertVelocity < 0) {
				position.y = (topCell.y) * collisionLayer.getTileHeight() - PLAYER_HEIGHT - 1;
				playerVertVelocity = 0;
			}
		}
		
		if (bottomCell != null) {
			position.y = (bottomCell.y+1)*collisionLayer.getTileHeight();
			playerState = PlayerState.GROUND;
			playerRotation = 0;
			playerHasDoubleJump = true;
			playerSwordVisible = false;
			playerFlipSword = false;
		}
		//update x position of player
		position.x += playerHorizVelocity;
		//check whether you're intersecting something horizontally
		EnhancedCell leftCell = getCollidingLeftCell();
		EnhancedCell rightCell = getCollidingRightCell();
		if (leftCell != null) {
			position.x = (leftCell.x+1)*collisionLayer.getTileWidth();
			playerHorizVelocity = 0;
			if (playerState != PlayerState.GROUND) {
				playerState = PlayerState.WALL_LEFT;
				playerRotation = 0;
				playerRotating = false;
				playerSwordVisible = false;
				playerFlipSword = false;
			}
		}
		else if (rightCell != null) {
			position.x = (rightCell.x)*collisionLayer.getTileWidth() - PLAYER_WIDTH - 1;
			playerHorizVelocity = 0;
			if (playerState != PlayerState.GROUND) {
				playerState = PlayerState.WALL_RIGHT;
				playerRotation = 0;
				playerRotating = false;
				playerSwordVisible = false;
				playerFlipSword = false;
			}
		}
		if (playerState == PlayerState.AIR && Gdx.input.isKeyJustPressed(Keys.X)) {
			setState(PlayerState.AIR_ANIM);
			boolean isFrontKeyPressed = (playerFacingLeft && Gdx.input.isKeyPressed(Keys.LEFT))
					|| (!playerFacingLeft && Gdx.input.isKeyPressed(Keys.RIGHT));
			if (isFrontKeyPressed) {
				loadHurtboxData(AnimationType.AIR_FAIR);
				playerFrame = PlayerFrame.RUN_NOARMS;
				playerSwordVisible = true;
				playerSwordRotation = -90;
			}
			else if (Gdx.input.isKeyPressed(Keys.UP)) {
				loadHurtboxData(AnimationType.AIR_UAIR);
				playerFrame = PlayerFrame.TWIST;
				playerSwordVisible = true;
				playerRotation = 175;
				playerFlipSword = true;
				playerSwordRotation = -140;
			}
			else if (Gdx.input.isKeyPressed(Keys.DOWN)) {
				loadHurtboxData(AnimationType.AIR_DAIR);
				playerFrame = PlayerFrame.CYCLONE;
				playerSwordVisible = true;
				playerSwordRotation = -75;
				playerRotating = true;
			}
			else {
				loadHurtboxData(AnimationType.AIR_FAIR);
				playerFrame = PlayerFrame.RUN_NOARMS;
				playerSwordVisible = true;
				playerSwordRotation = -90;
			}
		}
		if (wasInAirAnim) {
			updateAnimationFramesIfInState(PlayerState.AIR_ANIM, PlayerState.AIR);
		}
	}
	
	private void updatePlayerGround(){
		if (playerState == PlayerState.GROUND) {
			if (Gdx.input.isKeyPressed(Keys.LEFT)) {
				playerHorizVelocity = (1. - FLOOR_FRICTION) * playerHorizVelocity
									+ FLOOR_FRICTION * -PLAYER_GROUND_MAX_MOVESPEED;
				playerFacingLeft = true;
				playerFrame = PlayerFrame.RUN;
			}
			else if (Gdx.input.isKeyPressed(Keys.RIGHT)) {
				playerHorizVelocity = (1. - FLOOR_FRICTION) * playerHorizVelocity
						+ FLOOR_FRICTION * PLAYER_GROUND_MAX_MOVESPEED;
				playerFacingLeft = false;
				playerFrame = PlayerFrame.RUN;
			}
			else {
				playerHorizVelocity = (1. - FLOOR_FRICTION) * playerHorizVelocity;
				if (Math.abs(playerHorizVelocity) < 1) {
					playerHorizVelocity = 0;
				}
				playerFrame = PlayerFrame.STAND;
			}
			if (Gdx.input.isKeyJustPressed(Keys.Z)) {
				setState(PlayerState.GROUND_PREJUMP);
				playerFrame = PlayerFrame.PREJUMP;
				playerFastFalling = false;
			}
			position.x += playerHorizVelocity;

			EnhancedCell leftCell = getCollidingLeftCell();
			EnhancedCell rightCell = getCollidingRightCell();
			EnhancedCell bottomCell = getCollidingBottomCell();
			
			if (leftCell != null) {
				position.x = (leftCell.x+1) * collisionLayer.getTileWidth();
				if (Gdx.input.isKeyPressed(Keys.UP)) {
					playerVertVelocity = -PLAYER_WALL_SCALE_SPEED;
					setState(PlayerState.WALL_LEFT);
					playerFrame = PlayerFrame.CLIMB;
					playerSwordVisible = false;
					playerFlipSword = false;
				}
			}
			else if (rightCell != null) {
				position.x = (rightCell.x) * collisionLayer.getTileWidth() - PLAYER_WIDTH - 1;
				if (Gdx.input.isKeyPressed(Keys.UP)) {
					playerVertVelocity = -PLAYER_WALL_SCALE_SPEED;
					setState(PlayerState.WALL_RIGHT);
					playerFrame = PlayerFrame.CLIMB;
					playerSwordVisible = false;
					playerFlipSword = false;
				}
			}
			else if (bottomCell == null) {
				setState(PlayerState.AIR);
				playerFastFalling = false;
				playerHasDoubleJump = true;
				playerFrame = PlayerFrame.STAND;
				playerVertVelocity = 0;
			}
			
			if (playerState == PlayerState.GROUND && Gdx.input.isKeyJustPressed(Keys.X)) {
				setState(PlayerState.GROUND_ANIM);
				loadHurtboxData(AnimationType.AIR_FAIR);
				playerFrame = PlayerFrame.RUN_NOARMS;
				playerSwordVisible = true;
				playerSwordRotation = -90;
			}
		}
		else if (playerState == PlayerState.GROUND_PREJUMP) {
			// TODO: this is copied from the if/else branch above, de-duplicate
			if (Gdx.input.isKeyPressed(Keys.LEFT)) {
				playerHorizVelocity = (1. - FLOOR_FRICTION) * playerHorizVelocity
									+ FLOOR_FRICTION * -PLAYER_GROUND_MAX_MOVESPEED;
				playerFacingLeft = true;
			}
			else if (Gdx.input.isKeyPressed(Keys.RIGHT)) {
				playerHorizVelocity = (1. - FLOOR_FRICTION) * playerHorizVelocity
						+ FLOOR_FRICTION * PLAYER_GROUND_MAX_MOVESPEED;
				playerFacingLeft = false;
			}
			else {
				playerHorizVelocity = (1. - FLOOR_FRICTION) * playerHorizVelocity;
				if (Math.abs(playerHorizVelocity) < 1) {
					playerHorizVelocity = 0;
				}
			}
			position.x += playerHorizVelocity;

			EnhancedCell leftCell = getCollidingLeftCell();
			EnhancedCell rightCell = getCollidingRightCell();
			EnhancedCell bottomCell = getCollidingBottomCell();
			
			if (leftCell != null) {
				position.x = (leftCell.x+1) * collisionLayer.getTileWidth();
			}
			else if (rightCell != null) {
				position.x = (rightCell.x) * collisionLayer.getTileWidth() - PLAYER_WIDTH - 1;
			}
			else if (bottomCell == null) {
				position.x -= playerHorizVelocity; // TODO: make notion of 'teeter' precise?
				playerHorizVelocity = 0;
			}
			if (stateFrameDuration == PLAYER_PREJUMP_FRAMES) {
				if (Gdx.input.isKeyPressed(Keys.Z)) {
					playerVertVelocity = -PLAYER_JUMP_SPEED;
				}
				else {
					playerVertVelocity = -PLAYER_SHORTHOP_SPEED;
				}
				setState(PlayerState.AIR);
				playerFrame = PlayerFrame.STAND;
			}
		}
		else { // PlayerState.GROUND_ANIM
			// TODO: this is copied from the if/else branch above, de-duplicate
			if (Gdx.input.isKeyPressed(Keys.LEFT)) {
				playerHorizVelocity = (1. - FLOOR_FRICTION) * playerHorizVelocity
									+ FLOOR_FRICTION * -PLAYER_GROUND_MAX_MOVESPEED;
			}
			else if (Gdx.input.isKeyPressed(Keys.RIGHT)) {
				playerHorizVelocity = (1. - FLOOR_FRICTION) * playerHorizVelocity
						+ FLOOR_FRICTION * PLAYER_GROUND_MAX_MOVESPEED;
			}
			else {
				playerHorizVelocity = (1. - FLOOR_FRICTION) * playerHorizVelocity;
				if (Math.abs(playerHorizVelocity) < 1) {
					playerHorizVelocity = 0;
				}
			}
			position.x += playerHorizVelocity;

			EnhancedCell leftCell = getCollidingLeftCell();
			EnhancedCell rightCell = getCollidingRightCell();
			EnhancedCell bottomCell = getCollidingBottomCell();
			
			if (leftCell != null) {
				position.x = (leftCell.x+1) * collisionLayer.getTileWidth();
			}
			else if (rightCell != null) {
				position.x = (rightCell.x) * collisionLayer.getTileWidth() - PLAYER_WIDTH - 1;
			}
			else if (bottomCell == null) {
				position.x -= playerHorizVelocity; // TODO: make notion of 'teeter' precise?
				playerHorizVelocity = 0;
			}
			
			updateAnimationFramesIfInState(PlayerState.GROUND_ANIM, PlayerState.GROUND);
		}
	}
	
	private void updatePlayerWallLeft(){
		playerHorizVelocity = 0;
		playerFacingLeft = true;
		playerFrame = PlayerFrame.CLIMB;
		if (Gdx.input.isKeyPressed(Keys.RIGHT)) {
			playerHorizVelocity = 2 * PLAYER_AIR_INFLUENCE;
			playerState = PlayerState.AIR;
			playerFrame = PlayerFrame.STAND;
		}
		else if (Gdx.input.isKeyJustPressed(Keys.Z)) {
			playerHorizVelocity = PLAYER_AIR_MAX_MOVESPEED;
			playerVertVelocity = -6;
			setState(PlayerState.AIR);
			playerFrame = PlayerFrame.STAND;
			playerFacingLeft = false;
			playerFastFalling = false;
			playerHasDoubleJump = true;
		}
		else if (Gdx.input.isKeyPressed(Keys.UP)) {
			playerVertVelocity = (1. - PLAYER_WALL_INFLUENCE) * playerVertVelocity
					+ PLAYER_WALL_INFLUENCE * -PLAYER_WALL_SCALE_SPEED;
			position.y -= playerVertVelocity;
		}
		else {
			position.y -= playerVertVelocity;
			playerVertVelocity = (1. - WALL_FRICTION) * playerVertVelocity + WALL_FRICTION * 3;
		}
		
		position.x += playerHorizVelocity;
		
		EnhancedCell leftCell = getCollidingLeftCell();
		EnhancedCell bottomCell = getCollidingBottomCell();
		
		if (bottomCell != null) {
			position.y = (bottomCell.y+1) * collisionLayer.getTileHeight();
			playerState = PlayerState.GROUND;
			playerRotation = 0;
			playerRotating = false;
			playerHasDoubleJump = true;
			playerSwordVisible = false;
			playerFlipSword = false;
		}
		else if (leftCell==null) {
			// TODO: make the player snap to ground?
			setState(PlayerState.AIR);
			playerFrame = PlayerFrame.STAND;
		}
	}
	
	private void updatePlayerWallRight(){
		playerHorizVelocity = 0;
		playerFacingLeft = false;
		playerFrame = PlayerFrame.CLIMB;
		if (Gdx.input.isKeyPressed(Keys.LEFT)) {
			playerHorizVelocity = -2 * PLAYER_AIR_INFLUENCE;
			playerFrame = PlayerFrame.STAND;
			playerState = PlayerState.AIR;
		}
		else if (Gdx.input.isKeyJustPressed(Keys.Z)) {
			playerHorizVelocity = -PLAYER_AIR_MAX_MOVESPEED;
			playerVertVelocity = -6;
			setState(PlayerState.AIR);
			playerFacingLeft = true;
			playerFrame = PlayerFrame.STAND;
			playerFastFalling = false;
			playerHasDoubleJump = true;
		}
		else if (Gdx.input.isKeyPressed(Keys.UP)) {
			playerVertVelocity = (1. - PLAYER_WALL_INFLUENCE) * playerVertVelocity
					+ PLAYER_WALL_INFLUENCE * -PLAYER_WALL_SCALE_SPEED;
			position.y -= playerVertVelocity;
		}
		else {
			position.y -= playerVertVelocity;
			playerVertVelocity = (1. - WALL_FRICTION) * playerVertVelocity + WALL_FRICTION * 3;
		}
		
		position.x += playerHorizVelocity;
		
		EnhancedCell rightCell = getCollidingRightCell();
		EnhancedCell bottomCell = getCollidingBottomCell();
		
		if (bottomCell != null) {
			position.y = (bottomCell.y+1) * collisionLayer.getTileHeight();
			playerState = PlayerState.GROUND;
			playerRotation = 0;
			playerRotating = false;
			playerHasDoubleJump = true;
			playerSwordVisible = false;
			playerFlipSword = false;
		}
		else if (rightCell==null) {
			setState(PlayerState.AIR);
			playerFrame = PlayerFrame.STAND;
		}
	}
	
	public void setState(PlayerState state) {
		playerState = state;
		stateFrameDuration = 0;
	}
	
	public void die() {
	    isAlive = false;
	    // TODO: probably want to add sound effects or do other things here too
	}
	
	/*
	 * Some collision code adapted from https://www.youtube.com/watch?v=TLZbC9brH1c
	 * 
	 * These four methods return a colliding cell in the corresponding direction
	 * An "EnhancedCell" is just a cell with extra information about the x and y coordinates.
	 */
	public EnhancedCell getCollidingLeftCell() {
        for (float step = 1f; step < PLAYER_HEIGHT; step += collisionLayer.getTileHeight() / 2) {        
        	EnhancedCell cell = getEnhancedCell(getX() - 1, getY() + step);
        	if (cell != null) {
        		return cell;
        	}
        }
        return getEnhancedCell(getX() - 1, getY() + PLAYER_HEIGHT);
	}
	
	public EnhancedCell getCollidingRightCell() {
        for (float step = 1f; step < PLAYER_HEIGHT; step += collisionLayer.getTileHeight() / 2) {        
        	EnhancedCell cell = getEnhancedCell(getX() + PLAYER_WIDTH + 1, getY() + step);
        	if (cell != null) {
        		return cell;
        	}
        }
        return getEnhancedCell(getX() + PLAYER_WIDTH + 1, getY() + PLAYER_HEIGHT);
	}
	
	public EnhancedCell getCollidingTopCell() {
        for (float step = 0.1f; step < PLAYER_WIDTH; step += collisionLayer.getTileWidth() / 2) {        
        	EnhancedCell cell = getEnhancedCell(getX() + step, getY() + PLAYER_HEIGHT);
        	if (cell != null) {
        		return cell;
        	}
        }
		return getEnhancedCell(getX() + PLAYER_WIDTH, getY() + PLAYER_HEIGHT);
	}
	
	public EnhancedCell getCollidingBottomCell() {
		for (float step = 0.5f; step < PLAYER_WIDTH; step += collisionLayer.getTileWidth() / 2) {        
        	EnhancedCell cell = getEnhancedCell(getX() + step, getY() - 1);
        	if (cell != null) {
        		return cell;
        	}
        }
		return getEnhancedCell(getX() + PLAYER_WIDTH, getY() - 5);
	}
	
	private EnhancedCell getEnhancedCell(float x, float y) {
		int xCoord = (int) (x/collisionLayer.getTileWidth());
		int yCoord = (int) (y/collisionLayer.getTileHeight());
		Cell cell = collisionLayer.getCell(xCoord, yCoord);
		if(cell == null) {
			return null;
		}
		return new EnhancedCell(cell, xCoord, yCoord);		
	}
	
	public float getX() {
		return position.x;
	}
	
	public float getY() {
		return position.y;
	}
	
	public boolean getFacingLeft() {
		return playerFacingLeft;
	}
	
	public float getRotation() {
		return playerRotation;
	}
	
	public PlayerFrame getPlayerFrame() {
		return playerFrame;
	}
	
	public void loadHurtboxData(AnimationType type) {
		currentAnimationType = type;
		currentAnimationFrames = HurtboxData.getAnimationFrames(type);
		currentDuration = HurtboxData.getDuration(type);
		currentAnimationIsFlipped = playerFacingLeft;
		weaponSound.play();
	}
	
	public Array<HurtboxCircle> getActiveHurtboxes() {
		return activeHurtboxes;
	}
	
	public void updateAnimationFramesIfInState(PlayerState state, PlayerState endState) {
		if (playerState == state) {
			updateActiveHurtboxes();
			if (currentAnimationType == AnimationType.AIR_FAIR) {
				playerSwordRotation += 17;
			}
			else if (currentAnimationType == AnimationType.AIR_DAIR && stateFrameDuration < 20) {
				playerSwordRotation += 16;
				playerRotation += 16;
			}
			else if (currentAnimationType == AnimationType.AIR_UAIR && stateFrameDuration < 21) {
				playerSwordRotation -= 14;
				playerRotation -= 14;
				
				//pause = true;
			}
			for (float[] hurtboxData : currentAnimationFrames) {
				if (Math.abs(hurtboxData[0] - stateFrameDuration) < 1e-6) {
					float adjustedXPosition = PLAYER_WIDTH / 2 + hurtboxData[1] * (currentAnimationIsFlipped ? -1 : 1);
					activeHurtboxes.add(new HurtboxCircle(adjustedXPosition,
													hurtboxData[2] + PLAYER_HEIGHT / 2,
													hurtboxData[3],
													(int)hurtboxData[4]));
				}
			}
			if (currentAnimationType == AnimationType.AIR_FAIR && stateFrameDuration == 11) {
				playerFrame = PlayerFrame.STAND;
				playerSwordVisible = false;
			}
			if (currentAnimationType == AnimationType.AIR_DAIR && stateFrameDuration == 20) {
				playerSwordVisible = false;
				playerRotation = 0;
			}
			if (currentAnimationType == AnimationType.AIR_UAIR && stateFrameDuration == 11) {
				playerFrame = PlayerFrame.CYCLONE;
			}
			else if (currentAnimationType == AnimationType.AIR_UAIR && stateFrameDuration == 15) {
				playerFlipSword = false;
				playerSwordVisible = false;
			}
			if (stateFrameDuration == currentDuration) {
				playerState = endState;
				if (currentAnimationType == AnimationType.AIR_DAIR ||
					currentAnimationType == AnimationType.AIR_UAIR ||
					state == PlayerState.GROUND_ANIM) {
					playerFrame = PlayerFrame.STAND;
					playerRotation = 0;
				}
				activeHurtboxes.clear();
			}
		}
		else {
			activeHurtboxes.clear();
		}
	}
	
	public void updateActiveHurtboxes() {
		for (int i = 0; i < activeHurtboxes.size; ++i) {
			--activeHurtboxes.get(i).duration;
			if (activeHurtboxes.get(i).duration == 0) {
				activeHurtboxes.removeIndex(i);
				--i;
			}
		}
	}
	
	private void shootLaser() {
		// TODO: make map.projectiles private?
		LaserPulse laser = new LaserPulse(this, !this.playerFacingLeft);
		map.projectiles.add(laser);
		this.activeHurtboxRects.add(laser.hitbox);
		laserSound.play();
	}
	
	public Array<Circle> getHurtboxCircles() {
	    Array<Circle> allHurtboxes = new Array<Circle>();
        //convert hurtboxes to circles for Intersector
        for (HurtboxCircle hb: getActiveHurtboxes()) {
            allHurtboxes.add(new Circle(getX()+hb.x, getY()+hb.y, hb.radius));
        }
        return allHurtboxes;
	}
	
	public Array<HurtboxRectangle> getHurtboxRects() {
		return activeHurtboxRects;
	}
	
//	public Array<Shape2D> getHurtboxes() {
//		Array<Shape2D> hurtboxes = new Array<Shape2D>();
//		for(HurtboxCircle hb: getActiveHurtboxes()) {
//			hurtboxes.add(new Circle(getX()+hb.x, getY()+hb.y, hb.radius));
//		}
//		
//		return hurtboxes;
//	}
}
