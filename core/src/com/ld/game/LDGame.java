package com.ld.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

enum PlayerState {
	GROUND,
	AIR,
	WALL_LEFT,
	WALL_RIGHT;
}
public class LDGame extends ApplicationAdapter {
	public static final double FALL_ACCELERATION = 0.2;
	
	public static final double PLAYER_GROUND_MOVESPEED = 6;
	public static final double PLAYER_AIR_INFLUENCE = 0.3;
	public static final double PLAYER_AIR_MAX_MOVESPEED = 5;
	
	public static final double WALL_FRICTION = 0.08;
	
	SpriteBatch batch;
	
	Rectangle player = new Rectangle(200, 200, 112, 112);
	Texture playerImg;
	PlayerState playerState = PlayerState.AIR;
	double playerHorizVelocity = 0.0;
	double playerFallingVelocity = 0.0;
	
	@Override
	public void create () {
		batch = new SpriteBatch();
		playerImg = new Texture("mayuri.jpg");
	}

	@Override
	public void render () {
		Gdx.gl.glClearColor(1, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		if (playerState == PlayerState.AIR) {
			if (Gdx.input.isKeyPressed(Keys.LEFT)) {
				playerHorizVelocity -= PLAYER_AIR_INFLUENCE;
				playerHorizVelocity = Math.max(playerHorizVelocity, -PLAYER_AIR_MAX_MOVESPEED);
			}
			if (Gdx.input.isKeyPressed(Keys.RIGHT)) {
				playerHorizVelocity += PLAYER_AIR_INFLUENCE;
				playerHorizVelocity = Math.min(playerHorizVelocity, PLAYER_AIR_MAX_MOVESPEED);
			}
			player.x += playerHorizVelocity;
			player.y -= playerFallingVelocity;
			playerFallingVelocity += FALL_ACCELERATION;
			
			if (player.y < 0) {
				player.y = 0;
				playerState = PlayerState.GROUND;
			}
			else if (player.x < 0) {
				player.x = 0;
				playerState = PlayerState.WALL_LEFT;
			}
			else if (player.x > 1280 - player.width) {
				player.x = 1280 - player.width;
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
			player.x += playerHorizVelocity;

			if (player.x < 0) {
				player.x = 0;
			}
			else if (player.x > 1280 - player.width) {
				player.x = 1280 - player.width;
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
			}
			else {
				player.y -= playerFallingVelocity;
				playerFallingVelocity = (1. - WALL_FRICTION) * playerFallingVelocity + WALL_FRICTION * 3;
			}
			if (player.y < 0) {
				player.y = 0;
				playerState = PlayerState.GROUND;
			}
			player.x += playerHorizVelocity;
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
			}
			else {
				player.y -= playerFallingVelocity;
				playerFallingVelocity = (1. - WALL_FRICTION) * playerFallingVelocity + WALL_FRICTION * 3;
			}
			if (player.y < 0) {
				player.y = 0;
				playerState = PlayerState.GROUND;
			}
			player.x += playerHorizVelocity;
		}

		batch.begin();
		batch.draw(playerImg, player.x, player.y);
		batch.end();
	}
	
	@Override
	public void dispose () {
		batch.dispose();
		playerImg.dispose();
	}
}
