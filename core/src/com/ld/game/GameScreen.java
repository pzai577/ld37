package com.ld.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;

public class GameScreen extends ScreenAdapter {
    LDGame game;
    Map map;
    MapRenderer renderer;
    
    public GameScreen(LDGame game) {
        this.game = game;
        
        map = new Map("test_level.tmx");
//        map = new Map("wide_level.tmx");
        renderer = new MapRenderer(map, game.batch);
    }
    
    @Override
    public void render(float delta) {
        map.update();
        
        Gdx.gl.glClearColor(0.2f, 0, 0.1f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        renderer.render();
        
        checkGameCompletion();
    }
    
    public void refreshGame() {
    	this.game.refresh();
    }
    
    private void checkGameCompletion() {
    	if(map.isGameFinished()){
    		refreshGame();
    	}
    }
}
