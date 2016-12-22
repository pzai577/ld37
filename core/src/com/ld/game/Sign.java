package com.ld.game;

import com.badlogic.gdx.math.Rectangle;

@SuppressWarnings("serial")
public class Sign extends Rectangle {
    public String displayText;
    public boolean active;
    public int leg;
    
    public Sign(float x, float y, float width, float height, String text, int leg) {
        super(x, y, width, height);
        displayText = text;
        active = false;
        this.leg = leg;
    }
}
