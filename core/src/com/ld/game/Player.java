package com.ld.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
import com.badlogic.gdx.math.Rectangle;

enum PlayerState {
	GROUND,
	AIR,
	WALL_LEFT,
	WALL_RIGHT;
}

public class Player {
	// TODO: Maybe make all of the EnhancedCells class variables to avoid slowdown?

	private static final double FALL_ACCELERATION = 0.2;
	
	// note: if PLAYER_GROUND_MOVESPEED>PLAYER_AIR_MAX_MOVESPEED, things look weird if you run off a platform
	private static final double PLAYER_GROUND_MOVESPEED = 5;
	private static final double PLAYER_AIR_INFLUENCE = 0.3;
	private static final double PLAYER_AIR_MAX_MOVESPEED = 5;
	
	private static final double PLAYER_JUMP_SPEED = 8;
	
	private static final double WALL_FRICTION = 0.08;

	private static final float GAME_WIDTH = 1280;
	private static final float GAME_HEIGHT = 800;
	
	private static final int PLAYER_WIDTH = 56;
	private static final int PLAYER_HEIGHT = 56;
	
	private Rectangle position;
	private PlayerState playerState = PlayerState.AIR;
	private double playerHorizVelocity = 0.0;
	private double playerFallingVelocity = 0.0;
	private float playerRotation = 0.0f;
	private boolean playerHasDoubleJump = false;
	private boolean playerRotating = false;
	private boolean playerRotatingLeft = false;
	
	private TiledMapTileLayer collisionLayer;
	
	public Player(TiledMapTileLayer collisionLayer) {
		position = new Rectangle(200, 200, PLAYER_WIDTH, PLAYER_HEIGHT);
		this.collisionLayer = collisionLayer;
	}
	
	public void updateState() {
		
		if (playerState == PlayerState.AIR) {
			if (Gdx.input.isKeyPressed(Keys.LEFT)) {
				playerHorizVelocity -= PLAYER_AIR_INFLUENCE;
				playerHorizVelocity = Math.max(playerHorizVelocity, -PLAYER_AIR_MAX_MOVESPEED);
			}
			if (Gdx.input.isKeyPressed(Keys.RIGHT)) {
				playerHorizVelocity += PLAYER_AIR_INFLUENCE;
				playerHorizVelocity = Math.min(playerHorizVelocity, PLAYER_AIR_MAX_MOVESPEED);
			}
			if (Gdx.input.isKeyJustPressed(Keys.UP) && playerHasDoubleJump) {
				playerHasDoubleJump = false;
				playerRotating = true;
				playerRotatingLeft = (playerHorizVelocity <= 0);
				playerFallingVelocity = -8;
			}
			if (playerRotating) {
				playerRotation += 15 * (playerRotatingLeft ? 1 : -1);
				if (Math.abs(playerRotation) >= 360) {
					playerRotation = 0;
					playerRotating = false;
				}
			}
			position.x += playerHorizVelocity;
			position.y -= playerFallingVelocity;
			playerFallingVelocity += FALL_ACCELERATION;
			
			EnhancedCell leftCell = getCollidingLeftCell();
			EnhancedCell rightCell = getCollidingRightCell();
			EnhancedCell topCell = getCollidingTopCell();
			EnhancedCell bottomCell = getCollidingBottomCell();
			// the extra isCellBlocked condition is there to (hopefully) prevent weird stuff happening if the player runs into a wall too fast
			if (bottomCell != null && !isCellBlocked(bottomCell.x, bottomCell.y+1)) {
				position.y = (bottomCell.y+1)*collisionLayer.getTileHeight();
				playerState = PlayerState.GROUND;
				playerRotation = 0;
				playerRotating = false;
				playerHasDoubleJump = true;
				System.out.println("bottom!");
			}
			else if (leftCell != null) {
				position.x = (leftCell.x+1)*collisionLayer.getTileWidth();
				if (!Gdx.input.isKeyPressed(Keys.RIGHT))
					playerHorizVelocity = 0;
				if (Gdx.input.isKeyPressed(Keys.LEFT))
					playerState = PlayerState.WALL_LEFT;
				System.out.println("left!");
			}
			else if (rightCell != null) {
				position.x = (rightCell.x)*collisionLayer.getTileWidth()-PLAYER_WIDTH;
				if (!Gdx.input.isKeyPressed(Keys.LEFT))
					playerHorizVelocity = 0;
				if (Gdx.input.isKeyPressed(Keys.RIGHT))
					playerState = PlayerState.WALL_RIGHT;
				System.out.println("right!");
			}
			
			
			//TODO: Implement collisions going up
		}
		else if (playerState == PlayerState.GROUND) {
			playerHorizVelocity = 0;
			if (Gdx.input.isKeyPressed(Keys.LEFT)) {
				playerHorizVelocity = -PLAYER_GROUND_MOVESPEED;
			}
			if (Gdx.input.isKeyPressed(Keys.RIGHT)) {
				playerHorizVelocity = PLAYER_GROUND_MOVESPEED;
			}
			if (Gdx.input.isKeyPressed(Keys.UP)) {
				playerFallingVelocity = -PLAYER_JUMP_SPEED;
				playerState = PlayerState.AIR;
			}
			position.x += playerHorizVelocity;

			EnhancedCell leftCell = getCollidingLeftCell();
			EnhancedCell rightCell = getCollidingRightCell();
			EnhancedCell bottomCell = getCollidingBottomCell();
			
			if (leftCell != null) {
				position.x = (leftCell.x+1)*collisionLayer.getTileWidth();
			}
			else if (rightCell != null) {
				position.x = (rightCell.x)*collisionLayer.getTileWidth()-PLAYER_WIDTH;
			}
			else if (bottomCell == null) {
				playerState = PlayerState.AIR;
				playerFallingVelocity = 0;
				System.out.println("falling!");
			}
		}
		else if (playerState == PlayerState.WALL_LEFT) {
			playerHorizVelocity = 0;
			if (Gdx.input.isKeyPressed(Keys.RIGHT)) {
				playerHorizVelocity = 2 * PLAYER_AIR_INFLUENCE;
				playerState = PlayerState.AIR;
			}
			else if (Gdx.input.isKeyJustPressed(Keys.UP)) {
				playerHorizVelocity = PLAYER_AIR_MAX_MOVESPEED;
				playerFallingVelocity = -6;
				playerState = PlayerState.AIR;
				playerHasDoubleJump = true;
			}
			else if (!Gdx.input.isKeyPressed(Keys.LEFT)) {
				position.y -= playerFallingVelocity;
				playerState = PlayerState.AIR;
			}
			else { // only slide on wall if key is held
				position.y -= playerFallingVelocity;
				playerFallingVelocity = (1. - WALL_FRICTION) * playerFallingVelocity + WALL_FRICTION * 3;
			}
			
			position.x += playerHorizVelocity;
			
			EnhancedCell leftCell = getCollidingLeftCell();
			EnhancedCell rightCell = getCollidingRightCell();
			EnhancedCell bottomCell = getCollidingBottomCell();
			
			if (bottomCell != null) {
				position.y = (bottomCell.y+1)*collisionLayer.getTileHeight();
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
			if (Gdx.input.isKeyPressed(Keys.LEFT)) {
				playerHorizVelocity = -2 * PLAYER_AIR_INFLUENCE;
				playerState = PlayerState.AIR;
			}
			else if (Gdx.input.isKeyJustPressed(Keys.UP)) {
				playerHorizVelocity = -PLAYER_AIR_MAX_MOVESPEED;
				playerFallingVelocity = -6;
				playerState = PlayerState.AIR;
				playerHasDoubleJump = true;
			}
			else if (!Gdx.input.isKeyPressed(Keys.RIGHT)) {
				position.y -= playerFallingVelocity;
				playerState = PlayerState.AIR;
			}
			else { // only slide on wall if key is held
				position.y -= playerFallingVelocity;
				playerFallingVelocity = (1. - WALL_FRICTION) * playerFallingVelocity + WALL_FRICTION * 3;
			}
			
			position.x += playerHorizVelocity;
			
			EnhancedCell rightCell = getCollidingRightCell();
			EnhancedCell bottomCell = getCollidingBottomCell();
			
			if (bottomCell != null) {
				position.y = (bottomCell.y+1)*collisionLayer.getTileHeight();
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
 * An "EnhancedCell" is just a cell with extra information about the x and y coordinates, this makes it easier to do some calculations
 */
	public EnhancedCell getCollidingLeftCell() {
        for(float step = 1f; step < PLAYER_HEIGHT; step += collisionLayer.getTileHeight() / 2) {        
        	EnhancedCell cell = getCell(getX()-1, getY()+step);
        	if(cell!=null)
                        return cell;
        }
        return null;
	}
	
	public EnhancedCell getCollidingRightCell() {
        for(float step = 1f; step < PLAYER_HEIGHT; step += collisionLayer.getTileHeight() / 2) {        
        	EnhancedCell cell = getCell(getX()+PLAYER_WIDTH+1, getY()+step);
        	if(cell!=null)
                        return cell;
        }
        return null;
	}
	
	public EnhancedCell getCollidingTopCell() {
        for(float step = 0.1f; step < PLAYER_WIDTH; step += collisionLayer.getTileWidth() / 2) {        
        	EnhancedCell cell = getCell(getX() + step, getY()+PLAYER_HEIGHT);
        	if(cell!=null)
                        return cell;
        }
        return null;
	}
	
	public EnhancedCell getCollidingBottomCell() {
		for(float step = 0.5f; step < PLAYER_WIDTH; step += collisionLayer.getTileWidth() / 2) {        
        	EnhancedCell cell = getCell(getX() + step, getY()-5);
        	if(cell!=null)
                        return cell;
        }
        return null;
	}
	
	private EnhancedCell getCell(float x, float y) {
		//System.out.println("checking x: "+ x+"y: "+y);
		//System.out.println("height: "+collisionLayer.getHeight()+" width: "+collisionLayer.getWidth());
		//System.out.println("tilex: "+ (int) (x/collisionLayer.getTileWidth())+" tiley: "+(int) (y/collisionLayer.getTileHeight()));
		int xCoord = (int) (x/collisionLayer.getTileWidth());
		int yCoord = (int) (y/collisionLayer.getTileHeight());
		Cell cell = collisionLayer.getCell(xCoord, yCoord);
		if(cell==null)
			return null;
		else
			return new EnhancedCell(cell, xCoord, yCoord);
					
	}
	
	private boolean isCellBlocked(int x, int y) {
		Cell cell = collisionLayer.getCell(x, y);
		return cell!=null;
	}
	
	public float getX() {
		return position.x;
	}
	
	public float getY() {
		return position.y;
	}
	
	public float getRotation() {
		return playerRotation;
	}
}
