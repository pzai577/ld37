package com.ld.game;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
import com.badlogic.gdx.math.Rectangle;

enum PlayerState {
	GROUND,
	GROUND_ANIM,
	AIR,
	AIR_ANIM,
	WALL_LEFT,
	WALL_RIGHT;
}

public class Player {
	// TODO: Maybe make all of the EnhancedCells class variables to avoid slowdown?
    // TODO: Use delta time to account for varying frame rates

	private static final double FALL_ACCELERATION = 0.33;
	
	// note: if PLAYER_GROUND_MOVESPEED>PLAYER_AIR_MAX_MOVESPEED, things look weird if you run off a platform
	private static final double PLAYER_GROUND_MAX_MOVESPEED = 5;
	private static final double PLAYER_AIR_INFLUENCE = 0.6;
	private static final double PLAYER_AIR_MAX_MOVESPEED = 5;
	private static final double PLAYER_MAX_SLOWFALL_SPEED = 7.8;
	private static final double PLAYER_FASTFALL_SPEED = 11.8;
	
	private static final double PLAYER_JUMP_SPEED = 8;
	
	private static final double WALL_FRICTION = 0.08;
	private static final double FLOOR_FRICTION = 0.12;
	
	private static final int PLAYER_WIDTH = 56;
	private static final int PLAYER_HEIGHT = 56;
	
	private Rectangle position;
	private PlayerState playerState = PlayerState.AIR;
	private double playerHorizVelocity = 0.0;
	private double playerVertVelocity = 0.0;
	private float playerRotation = 0.0f;
	
	private boolean playerFacingLeft = false;
	private boolean playerHasDoubleJump = false;
	private boolean playerRotating = false;
	private boolean playerRotatingLeft = false;
	private boolean playerFastFalling = false;
	
	private boolean currentAnimationIsFlipped;
	private float[][] currentAnimationFrames;
	private int currentDuration;
	
	// TODO: setState() function that resets frameNumber
	private int frameNumber = 0;
	
	// quick and dirty hitbox system
	// All hitboxes are circles.
	// arrays of { frame offset from start, X position, Y position (offset from center),
	//			   radius, duration }.
	private boolean isDSmash;
	
	private TiledMapTileLayer collisionLayer;
	
	private ArrayList<Hurtbox> activeHurtboxes;
	
	public Player(TiledMapTileLayer collisionLayer) {
		position = new Rectangle(200, 200, PLAYER_WIDTH, PLAYER_HEIGHT);
		this.collisionLayer = collisionLayer;
		this.activeHurtboxes = new ArrayList<Hurtbox>();
	}
	
	public void updateState() {
		
		if (playerState == PlayerState.AIR || playerState == PlayerState.AIR_ANIM) {
			if (Gdx.input.isKeyJustPressed(Keys.DOWN) && playerVertVelocity > 0) {
				playerFastFalling = true;
			}
			if (Gdx.input.isKeyPressed(Keys.LEFT)) {
				playerHorizVelocity -= PLAYER_AIR_INFLUENCE;
				playerHorizVelocity = Math.max(playerHorizVelocity, -PLAYER_AIR_MAX_MOVESPEED);
			}
			if (Gdx.input.isKeyPressed(Keys.RIGHT)) {
				playerHorizVelocity += PLAYER_AIR_INFLUENCE;
				playerHorizVelocity = Math.min(playerHorizVelocity, PLAYER_AIR_MAX_MOVESPEED);
			}
			boolean wasInAirAnim = true;
			if (playerState == PlayerState.AIR) {
				if (Gdx.input.isKeyJustPressed(Keys.UP) && playerHasDoubleJump) {
					if (Gdx.input.isKeyPressed(Keys.LEFT)) {
						playerHorizVelocity = -PLAYER_AIR_MAX_MOVESPEED;
					}
					else if (Gdx.input.isKeyPressed(Keys.RIGHT)) {
						playerHorizVelocity = PLAYER_AIR_MAX_MOVESPEED;
					}
					playerHasDoubleJump = false;
					playerRotating = true;
					playerFastFalling = false;
					playerRotatingLeft = (playerHorizVelocity <= 0);
					playerVertVelocity = -8;
				}
				wasInAirAnim = false;
			}
			if (playerRotating) {
				playerRotation += 15 * (playerRotatingLeft ? 1 : -1);
				if (Math.abs(playerRotation) >= 360) {
					playerRotation = 0;
					playerRotating = false;
				}
			}
			if (playerFastFalling) {
				position.y -= PLAYER_FASTFALL_SPEED;
			}
			else {
				position.y -= playerVertVelocity;
				playerVertVelocity = Math.min(playerVertVelocity + FALL_ACCELERATION,
											  PLAYER_MAX_SLOWFALL_SPEED);
			}
			
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
				playerRotating = false;
				playerHasDoubleJump = true;
			}
			position.x += playerHorizVelocity;
			EnhancedCell leftCell = getCollidingLeftCell();
			EnhancedCell rightCell = getCollidingRightCell();
			if (leftCell != null) {
				position.x = (leftCell.x+1)*collisionLayer.getTileWidth();
				playerHorizVelocity = 0;
				if (playerState != PlayerState.GROUND) {
					playerState = PlayerState.WALL_LEFT;
				}
			}
			else if (rightCell != null) {
				position.x = (rightCell.x)*collisionLayer.getTileWidth() - PLAYER_WIDTH - 1;
				playerHorizVelocity = 0;
				if (playerState != PlayerState.GROUND) {
					playerState = PlayerState.WALL_RIGHT;
				}
			}
			if (playerState == PlayerState.AIR && Gdx.input.isKeyPressed(Keys.Z)) {
				playerState = PlayerState.AIR_ANIM;
				frameNumber = 0;
				loadHurtboxData(AnimationType.GROUND_DSMASH);
			}
			if (wasInAirAnim) {
				updateAnimationFramesIfInState(PlayerState.AIR_ANIM, PlayerState.AIR);
			}
		}
		else if (playerState == PlayerState.GROUND || playerState == PlayerState.GROUND_ANIM) {
			if (playerState == PlayerState.GROUND) {
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
				if (Gdx.input.isKeyPressed(Keys.UP)) {
					playerVertVelocity = -PLAYER_JUMP_SPEED;
					playerState = PlayerState.AIR;
					playerFastFalling = false;
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
					playerState = PlayerState.AIR;
					playerFastFalling = false;
					playerHasDoubleJump = true;
					playerVertVelocity = 0;
				}
				if (playerState == PlayerState.GROUND && Gdx.input.isKeyPressed(Keys.Z)) {
					playerState = PlayerState.GROUND_ANIM;
					frameNumber = 0;
					loadHurtboxData(Gdx.input.isKeyPressed(Keys.DOWN) ? AnimationType.GROUND_DSMASH
							: AnimationType.GROUND_FSMASH);
				}
			}
			else {
				// TODO: this is copied from the if/else branch above, de-duplicate
				playerHorizVelocity = (1. - FLOOR_FRICTION) * playerHorizVelocity;
				if (Math.abs(playerHorizVelocity) < 1) {
					playerHorizVelocity = 0;
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
		else if (playerState == PlayerState.WALL_LEFT) {
			playerHorizVelocity = 0;
			playerFacingLeft = false;
			if (Gdx.input.isKeyPressed(Keys.RIGHT)) {
				playerHorizVelocity = 2 * PLAYER_AIR_INFLUENCE;
				playerState = PlayerState.AIR;
			}
			else if (Gdx.input.isKeyJustPressed(Keys.UP)) {
				playerHorizVelocity = PLAYER_AIR_MAX_MOVESPEED;
				playerVertVelocity = -6;
				playerState = PlayerState.AIR;
				playerFastFalling = false;
				playerHasDoubleJump = true;
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
			}
			else if (leftCell==null) {
				playerState = PlayerState.AIR;
			}
		}
		else if (playerState == PlayerState.WALL_RIGHT) {
			playerHorizVelocity = 0;
			playerFacingLeft = true;
			if (Gdx.input.isKeyPressed(Keys.LEFT)) {
				playerHorizVelocity = -2 * PLAYER_AIR_INFLUENCE;
				playerState = PlayerState.AIR;
			}
			else if (Gdx.input.isKeyJustPressed(Keys.UP)) {
				playerHorizVelocity = -PLAYER_AIR_MAX_MOVESPEED;
				playerVertVelocity = -6;
				playerState = PlayerState.AIR;
				playerHasDoubleJump = true;
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
			}
			else if (rightCell==null) {
				playerState = PlayerState.AIR;
			}
		}
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
        	EnhancedCell cell = getEnhancedCell(getX() + step, getY() - 5);
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
	
	public void loadHurtboxData(AnimationType type) {
		currentAnimationFrames = HurtboxData.getAnimationFrames(type);
		currentDuration = HurtboxData.getDuration(type);
		currentAnimationIsFlipped = playerFacingLeft;
	}
	
	public List<Hurtbox> getActiveHurtboxes() {
		return activeHurtboxes;
	}
	
	public void updateAnimationFramesIfInState(PlayerState state, PlayerState endState) {
		if (playerState == state) {
			updateActiveHurtboxes();
			for (float[] hurtboxData : currentAnimationFrames) {
				if (hurtboxData[0] == frameNumber) {
					float adjustedXPosition = PLAYER_WIDTH / 2 + hurtboxData[1] * (currentAnimationIsFlipped ? -1 : 1);
					activeHurtboxes.add(new Hurtbox(adjustedXPosition,
													hurtboxData[2] + PLAYER_HEIGHT / 2,
													hurtboxData[3],
													(int)hurtboxData[4]));
				}
			}
			++frameNumber;
			if (frameNumber == currentDuration) {
				playerState = endState;
			}
		}
		else {
			activeHurtboxes.clear();
		}
	}
	
	public void updateActiveHurtboxes() {
		for (int i = 0; i < activeHurtboxes.size(); ++i) {
			--activeHurtboxes.get(i).duration;
			if (activeHurtboxes.get(i).duration == 0) {
				activeHurtboxes.remove(i);
				--i;
			}
		}
	}
}
