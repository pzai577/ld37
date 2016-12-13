package com.ld.game;

import com.badlogic.gdx.math.Rectangle;

@SuppressWarnings("serial")
public class Checkpoint extends Rectangle {
    private static final float DEFAULT_CP_WIDTH = 32;
    private static final float DEFAULT_CP_HEIGHT = 32;
    
    public Checkpoint(float x, float y) {
        this(x, y, DEFAULT_CP_WIDTH, DEFAULT_CP_HEIGHT);
    }
    
    public Checkpoint(float x, float y, float height, float width) {
        super(x, y, width, height);
    }
}
