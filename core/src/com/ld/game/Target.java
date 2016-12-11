package com.ld.game;

import com.badlogic.gdx.math.Rectangle;

public class Target extends Rectangle {
    private static final float DEFAULT_TARGET_WIDTH = 32;
    private static final float DEFAULT_TARGET_HEIGHT = 32;
    
    public Target(float x, float y) {
        this(x, y, DEFAULT_TARGET_WIDTH, DEFAULT_TARGET_HEIGHT);
    }
    
    public Target(float x, float y, float width, float height) {
        super(x, y, width, height);
    }
    
    
    
    //toString is just for debug purposes
    @Override
    public String toString() {
        return x+" "+y;
    }
}
