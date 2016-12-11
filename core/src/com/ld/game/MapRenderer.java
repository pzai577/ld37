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
    Map map;
    SpriteBatch batch, dialogBatch;
    ShapeRenderer r;
    OrthographicCamera cam;
    
    OrthogonalTiledMapRenderer tileMapRenderer;
    static final float GAME_WIDTH = 1280;
    static final float GAME_HEIGHT = 800;
    static final float CAM_BORDER = 150;
//    static final float LOWER_CAM_BOUNDARY = GAME_HEIGHT/2;
    
    Texture targetImg;
    Texture playerImg;
    Texture sageImg;
    Texture swordImg;
    TextureRegion imgRegion;
    TextureRegion playerStand, playerRun, playerPrejump, playerClimb;
    
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

        playerImg = new Texture("samurai.png");
        sageImg = new Texture("sage.png");
        swordImg = new Texture("sword_arm.png");
        
        playerStand = new TextureRegion(playerImg, 8, 0,
        		Player.PLAYER_WIDTH,Player.PLAYER_HEIGHT);
        playerRun = new TextureRegion(playerImg, playerImg.getWidth() / 4 + 8, 0,
        		Player.PLAYER_WIDTH, Player.PLAYER_HEIGHT);
        playerPrejump = new TextureRegion(playerImg, 2 * playerImg.getWidth() / 4 + 8, 0,
        		Player.PLAYER_WIDTH, Player.PLAYER_HEIGHT);
        playerClimb = new TextureRegion(playerImg, 3 * playerImg.getWidth() / 4 + 8, 0,
        		Player.PLAYER_WIDTH, Player.PLAYER_HEIGHT);
        
        weaponSound = Gdx.audio.newSound(Gdx.files.internal("swoosh.mp3"));
        
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
        TextureRegion personTexture = determinePlayerTexture();
        batch.draw(personTexture, map.player.getX(), map.player.getY(), width/2, height/2,
                    width, height, (map.player.getFacingLeft() ? 1 : -1), 1f, map.player.getRotation());
        
        if (map.player.playerSwordVisible) {
        	TextureRegion swordTexture = new TextureRegion(swordImg, 0, 0, swordImg.getWidth(), swordImg.getHeight());
        	batch.draw(swordTexture, map.player.getX() - 43, map.player.getY() + 8, 65, 20,
        			swordImg.getWidth(), swordImg.getHeight(), (map.player.getFacingLeft() ? 1 : -1), 1f, (map.player.getFacingLeft() ? 1 : -1) * map.player.playerSwordRotation);
        }
        
        batch.draw(sageImg, 2 * 32, 2 * 32 - 2, sageImg.getWidth(), sageImg.getHeight());
        batch.end();
        
        if (Math.abs(map.player.getX() - 2*32) + Math.abs(map.player.getY() - 2*32 + 2) <= 150) {
        	dialogBatch.setProjectionMatrix(cam.combined);
        	dialogBatch.begin();
	        font.draw(dialogBatch, "Take my sword to my\nbrother across the forest", 100, 140);
	        dialogBatch.end();
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
    		cam.position.x = Math.min(map.player.position.x + Player.PLAYER_WIDTH + CAM_BORDER, map.pixelWidth) - GAME_WIDTH/2;
    	if(map.player.position.x < cam.position.x - GAME_WIDTH/2 + CAM_BORDER)
    		cam.position.x = Math.max(map.player.position.x - CAM_BORDER, 0) + GAME_WIDTH/2;
    	if(map.player.position.y + Player.PLAYER_HEIGHT > cam.position.y + GAME_HEIGHT/2 - CAM_BORDER)
    		cam.position.y = Math.min(map.player.position.y + Player.PLAYER_HEIGHT + CAM_BORDER, map.pixelHeight) - GAME_HEIGHT/2;
    	if(map.player.position.y < cam.position.y - GAME_HEIGHT/2 + CAM_BORDER)
    		cam.position.y = Math.max(map.player.position.y - CAM_BORDER, 0) + GAME_HEIGHT/2;
    }
    
}
