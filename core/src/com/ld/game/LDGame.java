package com.ld.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

public class LDGame extends ApplicationAdapter {
	SpriteBatch batch;
	
	Rectangle player = new Rectangle(200, 200, 112, 112);
	Texture playerImg;
	
	@Override
	public void create () {
		batch = new SpriteBatch();
		playerImg = new Texture("mayuri.jpg");
	}

	@Override
	public void render () {
		Gdx.gl.glClearColor(1, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		if (Gdx.input.isKeyPressed(Keys.LEFT)) {
			player.x -= 3;
		}
		if (Gdx.input.isKeyPressed(Keys.RIGHT)) {
			player.x += 3;
		}
		if (Gdx.input.isKeyPressed(Keys.UP)) {
			player.y += 3;
		}
		if (Gdx.input.isKeyPressed(Keys.DOWN)) {
			player.y -= 3;
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
