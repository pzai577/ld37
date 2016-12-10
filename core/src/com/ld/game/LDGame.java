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

	public static final float FALL_ACCELERATION = 0.2f;
	
	public static final float PLAYER_GROUND_MOVESPEED = 6;
	public static final float PLAYER_AIR_MOVESPEED = 5;
	
	Rectangle player = new Rectangle(200, 200, 112, 112);
	Texture playerImg;
	PlayerState playerState = PlayerState.AIR;
	float playerFallingSpeed = 0.0f;
	
	@Override
	public void create () {
		batch = new SpriteBatch();
		cam = new OrthographicCamera();
		cam.setToOrtho(false, 480, 320);
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
				player.x -= PLAYER_AIR_MOVESPEED;
			}
			if (Gdx.input.isKeyPressed(Keys.RIGHT)) {
				player.x += PLAYER_AIR_MOVESPEED;
			}
			player.y -= playerFallingSpeed;
			playerFallingSpeed += FALL_ACCELERATION;
			
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
			if (Gdx.input.isKeyPressed(Keys.LEFT)) {
				player.x -= PLAYER_GROUND_MOVESPEED;
			}
			if (Gdx.input.isKeyPressed(Keys.RIGHT)) {
				player.x += PLAYER_GROUND_MOVESPEED;
			}
			if (Gdx.input.isKeyPressed(Keys.UP)) {
				playerFallingSpeed = -11;
				playerState = PlayerState.AIR;
			}
		}
		else if (playerState == PlayerState.WALL_LEFT) {
			if (Gdx.input.isKeyPressed(Keys.RIGHT)) {
				player.x += PLAYER_AIR_MOVESPEED;
			}
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
