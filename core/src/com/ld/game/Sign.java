package com.ld.game;

import com.badlogic.gdx.math.Rectangle;

public class Sign extends Rectangle {
    public String displayText;
    public boolean active;
    
    public Sign(float x, float y, float width, float height, String text) {
        super(x, y, width, height);
        displayText = text;
        active = false;
    }
}
