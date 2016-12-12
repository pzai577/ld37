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
    static final float[] CAM_BORDERS = {600f, 600f, 380f, 380f}; // left, right, up, down
//    static final float LOWER_CAM_BOUNDARY = GAME_HEIGHT/2;
    static final float SIGN_TEXT_WIDTH = 180;
    static final float SIGN_TEXT_VERTICAL_DISTANCE = 150;
    Texture targetImg;
    Texture playerImg;
    Texture particleImg;
    Texture sageImg;
    Texture swordImg;
    Texture checkpointImg;
    Texture signImg;
    TextureRegion imgRegion;
    TextureRegion playerSprites[][];
    TextureRegion particleSprites[][];
    
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

        playerImg = new Texture(Gdx.files.internal("samurai.png"));
        particleImg = new Texture(Gdx.files.internal("particles.png"));
        sageImg = new Texture(Gdx.files.internal("sage.png"));
        swordImg = new Texture(Gdx.files.internal("sword_arm.png"));

        playerSprites = new TextureRegion[4][4];
        for (int i = 0; i < 4; ++i) {
        	for (int j = 0; j < 4; ++j) {
        		playerSprites[i][j] = new TextureRegion(playerImg, 8 + j * playerImg.getWidth() / 4, i * playerImg.getHeight() / 4,
                		Player.PLAYER_WIDTH, Player.PLAYER_HEIGHT);
        	}
        }
        
        particleSprites = new TextureRegion[4][4];
        for (int i = 0; i < 4; ++i) {
        	for (int j = 0; j < 4; ++j) {
        		particleSprites[i][j] = new TextureRegion(particleImg, j * playerImg.getWidth() / 4, i * playerImg.getHeight() / 4,
                		56, 56);
        	}
        }
        
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
        drawTargets();
        drawCheckpoints();
        drawSigns();
        drawParticles();
        /*for (Rectangle d: map.deathRects) {
            if (map.player.isAlive) batch.draw(targetImg, d.x, d.y, d.width, d.height);
        }*/
                
        drawPlayer();
        batch.draw(sageImg, 2 * 32, 1 * 32 - 2, sageImg.getWidth(), sageImg.getHeight());
        
        batch.end();
        
        // Draw sage's dialog box (requires vector rotation)
        drawSageDialog();
        
        // Draw primitive shapes (requires a separate batch)
        r.setProjectionMatrix(cam.combined);
        r.begin(ShapeType.Filled);
        
        drawHitboxes();
        drawProjectiles();
        
        r.end();
    }
    
    private TextureRegion determinePlayerTexture(){
    	if (map.player.getPlayerFrame() == PlayerFrame.RUN) {
        	return playerSprites[0][1];
        }
    	if (map.player.getPlayerFrame() == PlayerFrame.RUN_NOARMS) {
        	return playerSprites[1][2];
        }
        else if (map.player.getPlayerFrame() == PlayerFrame.PREJUMP) {
        	return playerSprites[0][2];
        }
        else if (map.player.getPlayerFrame() == PlayerFrame.CLIMB) {
        	return playerSprites[0][3];
        }
        else if (map.player.getPlayerFrame() == PlayerFrame.TWIST) {
        	return playerSprites[1][0];
        }
        else if (map.player.getPlayerFrame() == PlayerFrame.CYCLONE) {
        	return playerSprites[1][1];
        }
        else {
            return playerSprites[0][0];
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
	        sageFont.draw(dialogBatch, Globals.SAGE_TEXT, 120, 190, 250, Align.center, true);
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
    	for(Projectile proj: this.map.projectiles) {
    		proj.render(r);
    	}
    }
    
    private void drawTargets() {
        for (Target t: map.targets) {
            if (t.exists) batch.draw(targetImg, t.x, t.y, t.width, t.height);
        }
    }
    
    private void drawCheckpoints() {
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
    
    private void drawSigns() {
        for (Sign s: map.signs) {
            batch.draw(signImg, s.x, s.y, s.width, s.height);
            if (s.active) {
                signFont.draw(batch, s.displayText, s.x+(s.width-SIGN_TEXT_WIDTH)/2, s.y+SIGN_TEXT_VERTICAL_DISTANCE, SIGN_TEXT_WIDTH, Align.center, true);
            }
        }
    }
    
    private void drawParticles() {
        for (Particle p : map.particles) {
        	p.render(batch, particleSprites);
        }
    }
}
