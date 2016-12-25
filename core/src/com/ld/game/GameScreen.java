package com.ld.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.utils.Align;

public class GameScreen extends ScreenAdapter {
    LDGame game;
    boolean speedrunMode;
    Map map;
    MapRenderer renderer;
    Music music;
    float player_time;
    
    BitmapFont timeFont;
    SpriteBatch timeBatch;
    
    public GameScreen(LDGame game, boolean speedrunMode) {
        this.game = game;
        this.speedrunMode = speedrunMode;
//        map = new Map("test_level.tmx");
//        map = new Map("wide_level.tmx");
        map = new Map("actual_game_maybe.tmx", speedrunMode);
        renderer = new MapRenderer(map, game.batch);
        
        music = Gdx.audio.newMusic(Gdx.files.internal("music.mp3"));
        music.setLooping(true);
        //music.play();
        
        player_time = 0;

        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("PixelFJVerdana12pt.ttf"));
        FreeTypeFontParameter parameter = new FreeTypeFontParameter();
        parameter.size = 20;
        timeFont = generator.generateFont(parameter);
        generator.dispose();
        
        timeBatch = new SpriteBatch();
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
        
        if (speedrunMode) {
            timeBatch.begin();
            timeFont.draw(timeBatch, "Time: "+String.format("%.2f", player_time), 50, 750, 900, Align.left, true);
            timeFont.draw(timeBatch, "Deaths: "+map.playerDeaths, 50, 700, 900, Align.left, true);
            timeBatch.end();
        }
        
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
