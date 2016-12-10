package com.ld.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;

public class LDGame extends ApplicationAdapter {

	private TiledMap tileMap;
	private OrthogonalTiledMapRenderer tileMapRenderer;
	private TiledMapTileLayer collisionLayer;
	private SpriteBatch batch;
	private OrthographicCamera cam;

	private static final float GAME_WIDTH = 1280;
	private static final float GAME_HEIGHT = 800;
	
	private static final double FALL_ACCELERATION = 0.2;
	
	private static final double PLAYER_GROUND_MOVESPEED = 6;
	private static final double PLAYER_AIR_INFLUENCE = 0.3;
	private static final double PLAYER_AIR_MAX_MOVESPEED = 5;
	
	private static final double WALL_FRICTION = 0.08;
	
	Player player;

	Texture playerImg;
	TextureRegion imgRegion;
	
	@Override
	public void create () {
		batch = new SpriteBatch();
		cam = new OrthographicCamera();
		cam.setToOrtho(false, GAME_WIDTH, GAME_HEIGHT);
		batch.setProjectionMatrix(cam.combined);
		tileMap = new TmxMapLoader().load("test_level.tmx");
		collisionLayer = (TiledMapTileLayer) tileMap.getLayers().get("Collision Tile Layer");
		System.out.println("layer name: " + collisionLayer.getName());
		tileMapRenderer = new OrthogonalTiledMapRenderer(tileMap);
		playerImg = new Texture("mayuri.jpg");
		imgRegion = new TextureRegion(playerImg);
		//(TiledMapTileLayer)tileMap.getLayers().get(0);
		
		player = new Player(collisionLayer);
	}

	@Override
	public void render () {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		

		tileMapRenderer.setView(cam);
		tileMapRenderer.render();
		
		int width = 56;
		int height = 56;
		
		player.updateState();
		
		batch.begin();
		batch.draw(imgRegion, player.getX(), player.getY(), width/2, height/2,
					width, height, 1f, 1f, player.getRotation());
		batch.end();

	}
	
	@Override
	public void dispose () {
		batch.dispose();
		playerImg.dispose();
	}
}
