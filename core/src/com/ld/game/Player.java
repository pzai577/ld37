package com.ld.game;

import java.awt.Point;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.math.Rectangle;

enum PlayerState {
	GROUND,
	AIR,
	WALL_LEFT,
	WALL_RIGHT;
}
public class Player {
	
	private static final double FALL_ACCELERATION = 0.2;
	
	private static final double PLAYER_GROUND_MOVESPEED = 6;
	private static final double PLAYER_AIR_INFLUENCE = 0.3;
	private static final double PLAYER_AIR_MAX_MOVESPEED = 5;
	
	private static final double WALL_FRICTION = 0.08;

	private static final float GAME_WIDTH = 1280;
	private static final float GAME_HEIGHT = 800;
	
	private Rectangle position;
	private PlayerState playerState = PlayerState.AIR;
	private double playerHorizVelocity = 0.0;
	private double playerFallingVelocity = 0.0;
	private float playerRotation = 0.0f;
	private boolean playerHasDoubleJump = false;
	private boolean playerRotating = false;
	private boolean playerRotatingLeft = false;
	
	public Player() {
		position = new Rectangle(200, 200, 56, 56);
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
			
			if (position.y < 0) {
				position.y = 0;
				playerState = PlayerState.GROUND;
				playerRotation = 0;
				playerRotating = false;
				playerHasDoubleJump = true;
			}
			else if (position.x < 0) {
				position.x = 0;
				playerState = PlayerState.WALL_LEFT;
			}
			else if (position.x > GAME_WIDTH - position.width) {
				position.x = GAME_WIDTH - position.width;
				playerState = PlayerState.WALL_RIGHT;
			}
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
				playerFallingVelocity = -11;
				playerState = PlayerState.AIR;
			}
			position.x += playerHorizVelocity;

			if (position.x < 0) {
				position.x = 0;
			}
			else if (position.x > GAME_WIDTH - position.width) {
				position.x = GAME_WIDTH - position.width;
			}
		}
		else if (playerState == PlayerState.WALL_LEFT) {
			playerHorizVelocity = 0;
			if (Gdx.input.isKeyPressed(Keys.RIGHT)) {
				playerHorizVelocity = 2 * PLAYER_AIR_INFLUENCE;
				playerState = PlayerState.AIR;
			}
			else if (Gdx.input.isKeyPressed(Keys.UP)) {
				playerHorizVelocity = PLAYER_AIR_MAX_MOVESPEED;
				playerFallingVelocity = -6;
				playerState = PlayerState.AIR;
				playerHasDoubleJump = true;
			}
			else {
				position.y -= playerFallingVelocity;
				playerFallingVelocity = (1. - WALL_FRICTION) * playerFallingVelocity + WALL_FRICTION * 3;
			}
			if (position.y < 0) {
				position.y = 0;
				playerState = PlayerState.GROUND;
				playerRotation = 0;
				playerRotating = false;
				playerHasDoubleJump = true;
			}
			position.x += playerHorizVelocity;
		}
		else if (playerState == PlayerState.WALL_RIGHT) {
			playerHorizVelocity = 0;
			if (Gdx.input.isKeyPressed(Keys.LEFT)) {
				playerHorizVelocity = -2 * PLAYER_AIR_INFLUENCE;
				playerState = PlayerState.AIR;
			}
			else if (Gdx.input.isKeyPressed(Keys.UP)) {
				playerHorizVelocity = -PLAYER_AIR_MAX_MOVESPEED;
				playerFallingVelocity = -6;
				playerState = PlayerState.AIR;
				playerHasDoubleJump = true;
			}
			else {
				position.y -= playerFallingVelocity;
				playerFallingVelocity = (1. - WALL_FRICTION) * playerFallingVelocity + WALL_FRICTION * 3;
			}
			if (position.y < 0) {
				position.y = 0;
				playerState = PlayerState.GROUND;
				playerRotation = 0;
				playerRotating = false;
				playerHasDoubleJump = true;
			}
			position.x += playerHorizVelocity;
		}
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
