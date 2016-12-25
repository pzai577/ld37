package com.ld.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class GameScreen extends ScreenAdapter {
    LDGame game;
    Map map;
    MapRenderer renderer;
    Music music;
    float player_time;
    
    public GameScreen(LDGame game) {
        this.game = game;
        
//        map = new Map("test_level.tmx");
//        map = new Map("wide_level.tmx");
        map = new Map("actual_game_maybe.tmx", false);
        renderer = new MapRenderer(map, game.batch);
        
        music = Gdx.audio.newMusic(Gdx.files.internal("music.mp3"));
        music.setLooping(true);
        //music.play();
        
        player_time = 0;
    }
    
    @Override
    public void render(float delta) {
    	// TODO: use delta
        map.update();
        
        Gdx.gl.glClearColor(0.2f, 0, 0.1f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        renderer.render();
        
        player_time += delta;
        checkGameCompletion();
        
        if (Gdx.input.isKeyJustPressed(Keys.ESCAPE)) {
            game.batch.dispose();
            game.batch = new SpriteBatch();
            game.setScreen(new MenuScreen(game));
        }
    }
    
    public void refreshGame() {
    	this.game.refresh();
    }
    
    private void checkGameCompletion() {
    	if(map.isGameFinished()){
    		game.setScreen(new CreditsScreen(game, player_time, map.playerDeaths));
    	}
    }
}
