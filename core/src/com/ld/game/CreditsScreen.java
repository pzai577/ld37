package com.ld.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.utils.Align;

public class CreditsScreen extends ScreenAdapter {
    LDGame game;
    BitmapFont creditsFont;
    float time;
    int deaths;
    static final float TEXT_WIDTH = 1200;
    static final float TEXT_VERT = 500;
    
    public CreditsScreen(LDGame game, float time, int deaths) {
        this.game = game;
        this.time = time;
        this.deaths = deaths;

        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("PixelFJVerdana12pt.ttf"));
        FreeTypeFontParameter parameter = new FreeTypeFontParameter();
        parameter.size = 20;
        creditsFont = generator.generateFont(parameter);
        generator.dispose();
    }
    
    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.2f, 0, 0.1f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        
        game.batch.begin();
        creditsFont.draw(game.batch, "Thanks for playing\nUnited Parcel Samurai!\nYour time:\n"+time+" seconds.\nNumber of deaths:\n"+deaths+"\n\nPress space to return to menu.", 640-TEXT_WIDTH/2, TEXT_VERT, TEXT_WIDTH, Align.center, true);
        game.batch.end();
        
        if (Gdx.input.isKeyJustPressed(Keys.SPACE)) {
            game.setScreen(new MenuScreen(game));
        }
    }
}
