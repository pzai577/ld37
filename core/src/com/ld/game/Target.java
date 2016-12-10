package com.ld.game;

public class Target extends RectangleObject {
    private static final float DEFAULT_TARGET_WIDTH = 32;
    private static final float DEFAULT_TARGET_HEIGHT = 32;
    
    public Target(float x, float y) {
        this(x, y, DEFAULT_TARGET_WIDTH, DEFAULT_TARGET_HEIGHT);
    }
    
    public Target(float x, float y, float width, float height) {
        super(x, y, width, height);
    }
    
    @Override
    public String toString() {
        return x+" "+y;
    }
}
