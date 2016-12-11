package com.ld.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
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
	static final boolean DEBUG_SHOW_HITBOXES = false;
	
    Map map;
    SpriteBatch batch, dialogBatch;
    ShapeRenderer r;
    OrthographicCamera cam;
    
    OrthogonalTiledMapRenderer tileMapRenderer;
    static final float GAME_WIDTH = 1280;
    static final float GAME_HEIGHT = 800;
    static final float[] CAM_BORDERS = {600f, 600f, 200f, 200f}; // left, right, up, down
//    static final float LOWER_CAM_BOUNDARY = GAME_HEIGHT/2;
    
    Texture targetImg;
    Texture playerImg;
    Texture sageImg;
    Texture swordImg;
    Texture checkpointImg;
    Texture usedCheckpointImg;
    TextureRegion imgRegion;
    TextureRegion playerStand, playerRun, playerPrejump, playerClimb, playerCyclone;
    
    Sound weaponSound;
  
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
        checkpointImg = new Texture(Gdx.files.internal("checkpoint.png"));

        playerImg = new Texture("samurai.png");
        sageImg = new Texture("sage.png");
        swordImg = new Texture("sword_arm.png");
        
        playerStand = new TextureRegion(playerImg, 8, 0,
        		Player.PLAYER_WIDTH, Player.PLAYER_HEIGHT);
        playerRun = new TextureRegion(playerImg, playerImg.getWidth() / 4 + 8, 0,
        		Player.PLAYER_WIDTH, Player.PLAYER_HEIGHT);
        playerPrejump = new TextureRegion(playerImg, 2 * playerImg.getWidth() / 4 + 8, 0,
        		Player.PLAYER_WIDTH, Player.PLAYER_HEIGHT);
        playerClimb = new TextureRegion(playerImg, 3 * playerImg.getWidth() / 4 + 8, 0,
        		Player.PLAYER_WIDTH, Player.PLAYER_HEIGHT);
        playerCyclone = new TextureRegion(playerImg, playerImg.getWidth() / 4 + 8, playerImg.getHeight()/4,
        		Player.PLAYER_WIDTH, Player.PLAYER_HEIGHT);
        
        dialogBatch = new SpriteBatch();
        font = new BitmapFont();
        fontRotation = new Matrix4();
        fontRotation.setToRotation(new Vector3(0, 0, 1), 10);
        dialogBatch.setTransformMatrix(fontRotation);
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
        /*for (Rectangle d: map.deathRects) {
            if (map.player.isAlive) batch.draw(targetImg, d.x, d.y, d.width, d.height);
        }*/
        for (Checkpoint cp: map.checkpoints) {
            if (cp==map.currCheckpoint) {
                batch.setColor(Color.GREEN);
                batch.draw(checkpointImg, cp.x, cp.y, cp.width, cp.height);
                batch.setColor(Color.WHITE);
            }
            else {
                batch.draw(checkpointImg, cp.x, cp.y, cp.width, cp.height);
            }
        }
        TextureRegion personTexture = determinePlayerTexture();
        int xScale = (map.player.getFacingLeft() ? 1 : -1);
        batch.draw(personTexture, map.player.getX(), map.player.getY(), width/2, height/2,
                    width, height, xScale, 1f, xScale * map.player.getRotation());
        
        if (map.player.playerSwordVisible) {
        	TextureRegion swordTexture = new TextureRegion(swordImg, 0, 0, swordImg.getWidth(), swordImg.getHeight());
        	batch.draw(swordTexture, map.player.getX() - 43, map.player.getY() + 8, 65, 20,
        			swordImg.getWidth(), swordImg.getHeight(), xScale, 1f, xScale * map.player.playerSwordRotation);
        }
        
        batch.draw(sageImg, 2 * 32, 1 * 32 - 2, sageImg.getWidth(), sageImg.getHeight());
        batch.end();
        
        if (Math.abs(map.player.getX() - 2*32) + Math.abs(map.player.getY() - 2*32 + 2) <= 150) {
        	dialogBatch.setProjectionMatrix(cam.combined);
        	dialogBatch.begin();
	        font.draw(dialogBatch, "Take my sword to my\nbrother across the forest", 100, 140);
	        dialogBatch.end();
        }
        
        if (DEBUG_SHOW_HITBOXES) {
	        r.setProjectionMatrix(cam.combined);
	        r.begin(ShapeType.Filled);
	        r.setColor(Color.RED);
	        for (Hurtbox box : map.player.getActiveHurtboxes()) {
	            r.arc(map.player.getX() + box.x, map.player.getY() + box.y, box.radius, 0, 360);
	        }
	        r.end();
        }
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
        else if (map.player.getPlayerFrame() == PlayerFrame.CYCLONE) {
        	return playerCyclone;
        }
        else {
            return playerStand;
        }
    }
    
    private void moveCamera(){
    	if(map.player.position.x + Player.PLAYER_WIDTH > cam.position.x + GAME_WIDTH/2 - CAM_BORDERS[1])
    		cam.position.x = Math.min(map.player.position.x + Player.PLAYER_WIDTH + CAM_BORDERS[1], map.pixelWidth) - GAME_WIDTH/2;
    	if(map.player.position.x < cam.position.x - GAME_WIDTH/2 + CAM_BORDERS[0])
    		cam.position.x = Math.max(map.player.position.x - CAM_BORDERS[0], 0) + GAME_WIDTH/2;
    	if(map.player.position.y + Player.PLAYER_HEIGHT > cam.position.y + GAME_HEIGHT/2 - CAM_BORDERS[2])
    		cam.position.y = Math.min(map.player.position.y + Player.PLAYER_HEIGHT + CAM_BORDERS[2], map.pixelHeight) - GAME_HEIGHT/2;
    	if(map.player.position.y < cam.position.y - GAME_HEIGHT/2 + CAM_BORDERS[3])
    		cam.position.y = Math.max(map.player.position.y - CAM_BORDERS[3], 0) + GAME_HEIGHT/2;
    }
    
}
