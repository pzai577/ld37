package com.ld.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.utils.Align;

public class MenuScreen extends ScreenAdapter {
    LDGame game;
    BitmapFont titleFont;
    BitmapFont pressFont;
    
    public MenuScreen(LDGame game) {
        this.game = game;

        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("PixelFJVerdana12pt.ttf"));
        FreeTypeFontParameter parameter = new FreeTypeFontParameter();
        parameter.size = 30;
        parameter.borderWidth = 1;
        parameter.borderColor = Color.LIGHT_GRAY;
        parameter.color = Color.GRAY;
        parameter.shadowOffsetX = 3;
        parameter.shadowColor = Color.DARK_GRAY;
        titleFont = generator.generateFont(parameter);
        
        FreeTypeFontParameter pressParameter = new FreeTypeFontParameter();
        parameter.size = 20;
        pressFont = generator.generateFont(pressParameter);
        generator.dispose();
    }
    
    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        
        game.batch.begin();
        titleFont.draw(game.batch, "UNITED PARCEL SAMURAI", 0, 700, 1280, Align.center, true);
        pressFont.draw(game.batch, "Press Z to begin.", 0, 300, 1280, Align.center, true);
        game.batch.end();
        
        if (Gdx.input.isKeyJustPressed(Keys.Z)) {
            game.setScreen(new GameScreen(game));
        }
    }
}
