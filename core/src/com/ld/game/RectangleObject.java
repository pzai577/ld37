package com.ld.game;

import com.badlogic.gdx.math.Rectangle;

public class RectangleObject {
    public float x;
    public float y;
    public float width;
    public float height;
    public Rectangle rect;
    
    public RectangleObject (float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        rect = new Rectangle(x, y, width, height);
    }
}
