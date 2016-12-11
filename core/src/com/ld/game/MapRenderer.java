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
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Align;

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
    static final float SIGN_TEXT_WIDTH = 150;
    static final float SIGN_TEXT_VERTICAL_DISTANCE = 100;
    Texture targetImg;
    Texture playerImg;
    Texture sageImg;
    Texture swordImg;
    Texture checkpointImg;
    Texture signImg;
    TextureRegion imgRegion;
    TextureRegion playerStand, playerRun, playerPrejump, playerClimb, playerCyclone, playerTwist;
    
    Sound weaponSound;
  
    BitmapFont sageFont;
    Matrix4 sageFontRotation;
    BitmapFont signFont;
    
    public MapRenderer (Map map, SpriteBatch batch) {
        this.map = map;
        this.batch = batch;
        this.r = new ShapeRenderer();
        this.cam = new OrthographicCamera(GAME_WIDTH, GAME_HEIGHT);
        this.cam.setToOrtho(false, GAME_WIDTH, GAME_HEIGHT);
        
        tileMapRenderer = new OrthogonalTiledMapRenderer(map.tileMap);

        targetImg = new Texture(Gdx.files.internal("target.png"));
        checkpointImg = new Texture(Gdx.files.internal("checkpoint.png"));
        signImg = new Texture(Gdx.files.internal("sign.png"));

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
        playerTwist = new TextureRegion(playerImg, 8, playerImg.getHeight()/4,
        		Player.PLAYER_WIDTH, Player.PLAYER_HEIGHT);
        playerCyclone = new TextureRegion(playerImg, playerImg.getWidth() / 4 + 8, playerImg.getHeight()/4,
        		Player.PLAYER_WIDTH, Player.PLAYER_HEIGHT);
        
        dialogBatch = new SpriteBatch();
        sageFont = new BitmapFont();
        sageFontRotation = new Matrix4();
        sageFontRotation.setToRotation(new Vector3(0, 0, 1), 10);
        dialogBatch.setTransformMatrix(sageFontRotation);
        signFont = new BitmapFont();
    }
    
    public void render() {
    	moveCamera();
        cam.update();
        
        tileMapRenderer.setView(cam);
        tileMapRenderer.render();
        
        
        batch.setProjectionMatrix(cam.combined);
        batch.begin();
        renderTargets();
        renderCheckpoints();
        renderSigns();
        /*for (Rectangle d: map.deathRects) {
            if (map.player.isAlive) batch.draw(targetImg, d.x, d.y, d.width, d.height);
        }*/
                
        drawPlayer();
        batch.draw(sageImg, 2 * 32, 1 * 32 - 2, sageImg.getWidth(), sageImg.getHeight());
        
        batch.end();
        
        drawSageDialog();
        
        r.setProjectionMatrix(cam.combined);
        r.begin(ShapeType.Filled);
        
        drawHitboxes();
        drawProjectiles();
        
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
        else if (map.player.getPlayerFrame() == PlayerFrame.TWIST) {
        	return playerTwist;
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
    
    private void drawPlayer() {
    	int width = Player.PLAYER_WIDTH;
        int height = Player.PLAYER_HEIGHT;
        TextureRegion personTexture = determinePlayerTexture();
        int xScale = (map.player.getFacingLeft() ? 1 : -1);
        batch.draw(personTexture, map.player.getX(), map.player.getY(), width/2, height/2,
                    width, height, xScale, 1f, xScale * map.player.getRotation());
        
        if (map.player.playerSwordVisible) {
        	TextureRegion swordTexture = new TextureRegion(swordImg, 0, 0, swordImg.getWidth(), swordImg.getHeight());
        	batch.draw(swordTexture, map.player.getX() - 43 + (map.player.getFacingLeft() ? -5 : 0), map.player.getY() + 8, 67, 20,
        			swordImg.getWidth(), swordImg.getHeight(), xScale * (map.player.playerFlipSword ? -1 : 1), 1f, xScale * map.player.playerSwordRotation);
        }
    }
    
    private void drawSageDialog() {
    	if (Math.abs(map.player.getX() - 2*32) + Math.abs(map.player.getY() - 2*32 + 2) <= 150) {
        	dialogBatch.setProjectionMatrix(cam.combined);
        	dialogBatch.begin();
	        sageFont.draw(dialogBatch, "Take my sword to my\nbrother across the forest", 100, 140);
	        dialogBatch.end();
        }
    }
    
    private void drawHitboxes() {
    	if (DEBUG_SHOW_HITBOXES) {    
	        r.setColor(Color.RED);
	        for (Hurtbox box : map.player.getActiveHurtboxes()) {
	            r.arc(map.player.getX() + box.x, map.player.getY() + box.y, box.radius, 0, 360);
	        }
        }
    }
    
    private void drawProjectiles() {
    	for(Projectile proj: this.map.projectiles){
    		proj.update();
    		proj.render(r);
//    		for(int i=0; i<proj.hitboxes.size; i++){
//    			System.out.println(proj.colors.get(i)+" "+i);
//    			Rectangle rect = proj.hitboxes.get(i);
//    			r.setColor(proj.colors.get(i));
//    			r.rect(rect.x, rect.y, rect.width, rect.height);
//    		}
//    		
//    		
//    		r.setColor(proj.color);
//    		for(Rectangle rect: proj.hitboxes){
//    			r.rect(rect.x, rect.y, rect.width, rect.height);
//    		}
    	}
    }
    
    private void renderTargets() {
        for (Target t: map.targets) {
            if (t.exists) batch.draw(targetImg, t.x, t.y, t.width, t.height);
        }
    }
    
    private void renderCheckpoints() {
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
    }
    
    private void renderSigns() {
        for (Sign s: map.signs) {
            batch.draw(signImg, s.x, s.y, s.width, s.height);
            if (s.active) {
                signFont.draw(batch, s.displayText, s.x+(s.width-SIGN_TEXT_WIDTH)/2, s.y+SIGN_TEXT_VERTICAL_DISTANCE, SIGN_TEXT_WIDTH, Align.center, true);
            }
        }
    }
}
