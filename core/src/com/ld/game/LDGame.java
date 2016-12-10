package com.ld.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;

public class LDGame extends ApplicationAdapter {
	private TiledMap tileMap;
	private OrthogonalTiledMapRenderer tileMapRenderer;
	private SpriteBatch batch;
	private OrthographicCamera cam;
	
	@Override
	public void create () {
		batch = new SpriteBatch();
		cam = new OrthographicCamera();
		cam.setToOrtho(false, 480, 320);
		batch.setProjectionMatrix(cam.combined);
		tileMap = new TmxMapLoader().load("test_level.tmx");
		tileMapRenderer = new OrthogonalTiledMapRenderer(tileMap);
	}

	@Override
	public void render () {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		tileMapRenderer.setView(cam);
		tileMapRenderer.render();
		
		cam.update();
	}
	
	@Override
	public void dispose () {

	}
}
