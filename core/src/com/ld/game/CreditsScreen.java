package com.ld.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.utils.Align;

public class CreditsScreen extends ScreenAdapter {
    LDGame game;
    BitmapFont creditsFont;
    float time;
    int deaths;
    static final float TEXT_WIDTH = 500;
    static final float TEXT_VERT = 400;
    
    public CreditsScreen(LDGame game, float time, int deaths) {
        this.game = game;
        this.time = time;
        this.deaths = deaths;
        creditsFont = new BitmapFont();
    }
    
    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.2f, 0, 0.1f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        
        game.batch.begin();
        creditsFont.draw(game.batch, "Thanks for playing United Parcel Samurai! \n Your time was "+time+" seconds and you had "+deaths+" deaths!", 640-TEXT_WIDTH/2, TEXT_VERT, TEXT_WIDTH, Align.center, true);
        game.batch.end();
    }
}
