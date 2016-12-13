package com.ld.game;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class Particle {
	// This class is for displaying animated particles whose source sprites come entirely
	// from 'particles.png'.
	// TODO: make more general?
	
	public Point[] frameData;
	private int frame = 0;
	private float x, y;
	
	public Particle(float x, float y, Point[] frameData) {
		this.x = x;
		this.y = y;
		this.frameData = frameData;
	}
	
	public int getFrame() {
		return frame;
	}
	
	public Point getFrameData() {
		return frameData[frame];
	}
	
	public void tick() {
		++frame;
	}
	
	public boolean readyToDie() {
		return frame == frameData.length;
	}
	
	// This is really messy and should be refactored, but it probably won't be :^)
	public void render(SpriteBatch batch, TextureRegion[][] particleSprites) {
		Point p = frameData[frame];
		batch.draw(particleSprites[p.y][p.x], x, y, 56, 56);
	}
}