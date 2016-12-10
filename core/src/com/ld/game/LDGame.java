package com.ld.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Rectangle;

enum PlayerState {
	GROUND,
	AIR,
	WALL_LEFT,
	WALL_RIGHT;
}
public class LDGame extends ApplicationAdapter {

	private TiledMap tileMap;
	private OrthogonalTiledMapRenderer tileMapRenderer;
	private SpriteBatch batch;
	private OrthographicCamera cam;

	private static final float GAME_WIDTH = 1280;
	private static final float GAME_HEIGHT = 800;
	
	private static final double FALL_ACCELERATION = 0.2;
	
	private static final double PLAYER_GROUND_MOVESPEED = 6;
	private static final double PLAYER_AIR_INFLUENCE = 0.3;
	private static final double PLAYER_AIR_MAX_MOVESPEED = 5;
	
	private static final double WALL_FRICTION = 0.08;
	
	Rectangle player = new Rectangle(200, 200, 112, 112);
	Texture playerImg;
	PlayerState playerState = PlayerState.AIR;
	double playerHorizVelocity = 0.0;
	double playerFallingVelocity = 0.0;
	
	@Override
	public void create () {
		batch = new SpriteBatch();
		cam = new OrthographicCamera();
		cam.setToOrtho(false, GAME_WIDTH, GAME_HEIGHT);
		batch.setProjectionMatrix(cam.combined);
		tileMap = new TmxMapLoader().load("test_level.tmx");
		tileMapRenderer = new OrthogonalTiledMapRenderer(tileMap);
		playerImg = new Texture("mayuri.jpg");
	}

	@Override
	public void render () {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		

		tileMapRenderer.setView(cam);
		tileMapRenderer.render();
		
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
			else if (player.x > GAME_WIDTH - player.width) {
				player.x = GAME_WIDTH - player.width;
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
			else if (player.x > GAME_WIDTH - player.width) {
				player.x = GAME_WIDTH - player.width;
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
