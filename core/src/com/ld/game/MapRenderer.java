package com.ld.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;

public class MapRenderer {
    Map map;
    SpriteBatch batch, batch2;
    ShapeRenderer r;
    OrthographicCamera cam;
    
    OrthogonalTiledMapRenderer tileMapRenderer;
    static final float GAME_WIDTH = 1280;
    static final float GAME_HEIGHT = 800;
    static final float CAM_BORDER = 150;
    static final float LOWER_CAM_BOUNDARY = GAME_HEIGHT/2;
    
    Texture targetImg;
    Texture playerImg;
    Texture sageImg;
    TextureRegion imgRegion;
    TextureRegion playerStand, playerRun, playerPrejump, playerClimb;
    BitmapFont font;
    Matrix4 fontRotation;
    
    public MapRenderer (Map map, SpriteBatch batch) {
        this.map = map;
        this.batch = batch;
        this.r = new ShapeRenderer();
        this.cam = new OrthographicCamera(GAME_WIDTH, GAME_HEIGHT);
        this.cam.setToOrtho(false, GAME_WIDTH, GAME_HEIGHT);
        
        tileMapRenderer = new OrthogonalTiledMapRenderer(map.tileMap);

        targetImg = new Texture(Gdx.files.internal("target.png"));

        playerImg = new Texture("samurai.png");
        sageImg = new Texture("sage.png");
        
        playerStand = new TextureRegion(playerImg, 8, 0,
        		Player.PLAYER_WIDTH,Player.PLAYER_HEIGHT);
        playerRun = new TextureRegion(playerImg, playerImg.getWidth() / 4 + 8, 0,
        		Player.PLAYER_WIDTH, Player.PLAYER_HEIGHT);
        playerPrejump = new TextureRegion(playerImg, 2 * playerImg.getWidth() / 4 + 8, 0,
        		Player.PLAYER_WIDTH, Player.PLAYER_HEIGHT);
        playerClimb = new TextureRegion(playerImg, 3 * playerImg.getWidth() / 4 + 8, 0,
        		Player.PLAYER_WIDTH, Player.PLAYER_HEIGHT);
        
        batch2 = new SpriteBatch();
        font = new BitmapFont();
        fontRotation = new Matrix4();
        fontRotation.setToRotation(new Vector3(0, 0, 1), 10);
        batch2.setTransformMatrix(fontRotation);
    }
    
    public void render() {
    	moveCamera();
        cam.update();
        
        tileMapRenderer.setView(cam);
        tileMapRenderer.render();
        
        int width = 40;
        int height = 56;
        
        batch.setProjectionMatrix(cam.combined);
        batch.begin();
        for (Target t: map.targets) {
            batch.draw(targetImg, t.x, t.y, t.width, t.height);
        }
        TextureRegion personTexture = determinePlayerTexture();
        batch.draw(personTexture, map.player.getX(), map.player.getY(), width/2, height/2,
                    width, height, (map.player.getFacingLeft() ? 1 : -1), 1f, map.player.getRotation());
        
        batch.draw(sageImg, 3 * 32, 1 * 32 - 2, sageImg.getWidth(), sageImg.getHeight());
        batch.end();
        
        if (Math.abs(map.player.getX() - 3*32) + Math.abs(map.player.getY() - 30) <= 150) {
	        batch2.setProjectionMatrix(cam.combined);
	        batch2.begin();
	        font.draw(batch2, "Take my sword to my\nbrother across the forest", 120, 100);
	        batch2.end();
        }
        
        r.setProjectionMatrix(cam.combined);
        r.begin(ShapeType.Filled);
        r.setColor(Color.RED);
        for (Hurtbox box : map.player.getActiveHurtboxes()) {
            r.arc(map.player.getX() + box.x, map.player.getY() + box.y, box.radius, 0, 360);
        }
        r.end();
    }
    
    private TextureRegion determinePlayerTexture(){
    	if (map.player.getPlayerFrame() == PlayerFrame.RUN) {
        	return playerRun;
        }
        else if (map.player.getPlayerFrame() == PlayerFrame.PREJUMP) {
        	return playerPrejump;
        }
        else if (map.player.getPlayerFrame() == PlayerFrame.CLIMB) {
        	return playerClimb;
        }
        else {
            return playerStand;
        }
    }
    
    private void moveCamera(){
    	if(map.player.position.x + Player.PLAYER_WIDTH > cam.position.x + GAME_WIDTH/2 - CAM_BORDER)
    		cam.position.x = map.player.position.x + Player.PLAYER_WIDTH + CAM_BORDER - GAME_WIDTH/2;
    	if(map.player.position.x < cam.position.x - GAME_WIDTH/2 + CAM_BORDER)
    		cam.position.x = map.player.position.x - CAM_BORDER + GAME_WIDTH/2;
    	if(map.player.position.y + Player.PLAYER_HEIGHT > cam.position.y + GAME_HEIGHT/2 - CAM_BORDER)
    		cam.position.y = map.player.position.y + Player.PLAYER_HEIGHT + CAM_BORDER - GAME_HEIGHT/2;
    	if(map.player.position.y < cam.position.y - GAME_HEIGHT/2 + CAM_BORDER)
    		cam.position.y = Math.max(map.player.position.y - CAM_BORDER + GAME_HEIGHT/2, LOWER_CAM_BOUNDARY);
    }
    
}
