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
    BitmapFont pressGrayFont;
    int currChoice;
    
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
        pressParameter.size = 20;
        pressParameter.color = Color.GREEN;
        pressFont = generator.generateFont(pressParameter);
        
        FreeTypeFontParameter pressGrayParameter = new FreeTypeFontParameter();
        pressGrayParameter.size = 20;
        pressGrayParameter.color = Color.GRAY;
        pressGrayFont = generator.generateFont(pressGrayParameter);
        
        generator.dispose();
        
        currChoice = 0;
    }
    
    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        
        game.batch.begin();
        titleFont.draw(game.batch, "UNITED PARCEL SAMURAI", 0, 700, 1280, Align.center, true);
        if (currChoice==0) {
            pressFont.draw(game.batch, "Play the game!", 0, 300, 1280, Align.center, true);
            pressGrayFont.draw(game.batch, "Speedrun mode", 0, 200, 1280, Align.center, true);
        }
        else if (currChoice==1) {
            pressGrayFont.draw(game.batch, "Play the game!", 0, 300, 1280, Align.center, true);
            pressFont.draw(game.batch, "Speedrun mode", 0, 200, 1280, Align.center, true);
        }
        game.batch.end();
        
        if (Gdx.input.isKeyJustPressed(Keys.UP) || Gdx.input.isKeyJustPressed(Keys.DOWN)) {
            currChoice = 1-currChoice;
        }
        if (Gdx.input.isKeyJustPressed(Keys.Z)) {
            game.setScreen(new GameScreen(game, currChoice==1));
        }
    }
}
