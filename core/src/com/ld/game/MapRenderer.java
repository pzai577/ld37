package com.ld.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;

public class MapRenderer {
    Map map;
    SpriteBatch batch;
    OrthographicCamera cam;
    
    OrthogonalTiledMapRenderer tileMapRenderer;
    static final float GAME_WIDTH = 1280;
    static final float GAME_HEIGHT = 800;
    Texture targetImg;
    Texture playerImg;
    TextureRegion imgRegion;
    
    
    public MapRenderer (Map map, SpriteBatch batch) {
        this.map = map;
        this.batch = batch;
        this.cam = new OrthographicCamera();
        this.cam.setToOrtho(false, GAME_WIDTH, GAME_HEIGHT);
        batch.setProjectionMatrix(cam.combined);
        
        tileMapRenderer = new OrthogonalTiledMapRenderer(map.tileMap);
        targetImg = new Texture(Gdx.files.internal("target.png"));
        playerImg = new Texture(Gdx.files.internal("mayuri.jpg"));
        imgRegion = new TextureRegion(playerImg);
    }
    
    public void render() {
        cam.update();
        
        tileMapRenderer.setView(cam);
        tileMapRenderer.render();
        
        int width = 56;
        int height = 56;
        
        batch.begin();
        batch.draw(imgRegion, map.player.getX(), map.player.getY(), width/2, height/2,
                    width, height, 1f, 1f, map.player.getRotation());
        for (Target t: map.targets) {
            batch.draw(targetImg, t.x, t.y, t.width, t.height);
        }
        batch.end();
        ShapeRenderer r = new ShapeRenderer();
        r.begin(ShapeType.Filled);
        r.setColor(Color.RED);
        for (Hurtbox box : map.player.getActiveHurtboxes()) {
            r.arc(map.player.getX() + box.x, map.player.getY() + box.y, box.radius, 0, 360);
        }
        r.end();
    }
}
