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
    private Map map;
    private SpriteBatch batch;
    private OrthographicCamera cam;
    
    private OrthogonalTiledMapRenderer tileMapRenderer;
    private static final float GAME_WIDTH = 1280;
    private static final float GAME_HEIGHT = 800;
    private Texture playerImg;
    private TextureRegion imgRegion;
    
    public MapRenderer (Map map, SpriteBatch batch) {
        this.map = map;
        this.batch = batch;
        this.cam = new OrthographicCamera();
        this.cam.setToOrtho(false, GAME_WIDTH, GAME_HEIGHT);
        batch.setProjectionMatrix(cam.combined);
        
        tileMapRenderer = new OrthogonalTiledMapRenderer(map.tileMap);
        playerImg = new Texture("mayuri.jpg");
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
                    width, height, (map.player.getFacingLeft() ? 1 : -1), 1f, map.player.getRotation());
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
