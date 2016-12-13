package com.ld.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
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
    ShapeRenderer projRenderer;
    ShapeRenderer dialogueBoxRenderer;
    OrthographicCamera cam;
    
    OrthogonalTiledMapRenderer tileMapRenderer;
    static final float GAME_WIDTH = 1280;
    static final float GAME_HEIGHT = 800;
    static final float CAM_SCALE = 1;
    static final float[] CAM_BORDERS = {600f, 600f, 380f, 380f}; // left, right, up, down
//    static final float LOWER_CAM_BOUNDARY = GAME_HEIGHT/2;
    static final float SIGN_TEXT_WIDTH = 180;
    static final float SIGN_TEXT_VERTICAL_DISTANCE = 150;
    static final float DIALOGUE_BOX_WIDTH = 180;
    static final float DIALOGUE_BOX_HEIGHT = 180;
    static final float DIALOGUE_BORDER = 5;
    static final float DIALOGUE_BOX_VERTICAL_SPACING = 50;
    static final float DIALOGUE_TEXT_HORIZ_MARGIN = 10;
    static final float DIALOGUE_TEXT_VERT_MARGIN = 10;
    Texture targetImg;
    Texture playerImg;
    Texture particleImg;
    Texture startSageImg;
    Texture endSageImg;
    Texture swordImg;
    Texture checkpointImg;
    Texture signImg;
    TextureRegion imgRegion;
    TextureRegion playerSprites[][];
    TextureRegion particleSprites[][];
    
    Color textboxColor, borderColor;
    
//    Sound weaponSound;
  
    BitmapFont sageFont;
    Matrix4 sageFontRotation;
    BitmapFont signFont;
    BitmapFont dialogueFont;
    
    public MapRenderer (Map map, SpriteBatch batch) {
        this.map = map;
        this.batch = batch;
        this.projRenderer = new ShapeRenderer();
        this.dialogueBoxRenderer = new ShapeRenderer();
        this.cam = new OrthographicCamera(GAME_WIDTH, GAME_HEIGHT);
        cam.zoom = CAM_SCALE;
        this.cam.setToOrtho(false, GAME_WIDTH, GAME_HEIGHT);
        
        tileMapRenderer = new OrthogonalTiledMapRenderer(map.tileMap);

        targetImg = new Texture(Gdx.files.internal("target.png"));
//        checkpointImg = new Texture(Gdx.files.internal("checkpoint.png"));
        checkpointImg = new Texture(Gdx.files.internal("purplePlat.png"));
        signImg = new Texture(Gdx.files.internal("sign.png"));

        playerImg = new Texture(Gdx.files.internal("samurai.png"));
        particleImg = new Texture(Gdx.files.internal("particles.png"));
        startSageImg = new Texture(Gdx.files.internal("sage.png"));
        endSageImg = new Texture(Gdx.files.internal("end_sage.png"));
        swordImg = new Texture(Gdx.files.internal("sword_arm.png"));

        playerSprites = new TextureRegion[4][4];
        for (int i = 0; i < 4; ++i) {
        	for (int j = 0; j < 4; ++j) {
        		playerSprites[i][j] = new TextureRegion(playerImg, 8 + j * playerImg.getWidth() / 4, i * playerImg.getHeight() / 4,
                		40, Player.PLAYER_HEIGHT);
        	}
        }
        
        particleSprites = new TextureRegion[4][4];
        for (int i = 0; i < 4; ++i) {
        	for (int j = 0; j < 4; ++j) {
        		particleSprites[i][j] = new TextureRegion(particleImg, j * playerImg.getWidth() / 4, i * playerImg.getHeight() / 4,
                		56, 56);
        	}
        }
        
        textboxColor = new Color(Color.TAN);
        textboxColor.a = 0.5f;
        borderColor = new Color(Color.BROWN);
        borderColor.a = 0.5f;
        
        //dialogBatch = new SpriteBatch();
        //sageFont = new BitmapFont();
        //sageFontRotation = new Matrix4();
        //sageFontRotation.setToRotation(new Vector3(0, 0, 1), 10);
        //dialogBatch.setTransformMatrix(sageFontRotation);
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("PixelFJVerdana12pt.ttf"));
        FreeTypeFontParameter parameter = new FreeTypeFontParameter();
        parameter.size = 8;
        signFont = generator.generateFont(parameter);
        dialogueFont = generator.generateFont(parameter);
        generator.dispose();
    }
    
    public void render() {
        // Update camera stuff
    	moveCamera();
        cam.update();
        projRenderer.setProjectionMatrix(cam.combined);
        dialogueBoxRenderer.setProjectionMatrix(cam.combined);
        batch.setProjectionMatrix(cam.combined);
        tileMapRenderer.setView(cam);
        // Draw tilemap
        tileMapRenderer.render();

        // Draw map objects
        batch.begin();
        drawTargets();
        drawCheckpoints();
        drawSigns();
        drawParticles();
        /*for (Rectangle d: map.deathRects) {
            if (map.player.isAlive) batch.draw(targetImg, d.x, d.y, d.width, d.height);
        }*/
                
        drawPlayer();
        drawSages();     
        batch.end();
        
        // Draw dialogue (uses both a ShapeRenderer and the batch, so done separately)
        drawDialogue();
        
        // Draw sage's dialog box (requires vector rotation)
        // drawSageDialog();
        
        // Draw primitive shapes (requires a separate batch)
        Gdx.gl.glEnable(GL20.GL_BLEND);
        projRenderer.begin(ShapeType.Filled);
        drawHitboxes();
        drawProjectiles();
        projRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
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
    	int width = 40;
        int height = Player.PLAYER_HEIGHT;
        TextureRegion personTexture = determinePlayerTexture();
        int xScale = (map.player.getFacingLeft() ? 1 : -1);
        int playerClimbOffset = (map.player.getPlayerFrame() == PlayerFrame.CLIMB ? -4 : 0);
        batch.draw(personTexture, map.player.getX() + playerClimbOffset, map.player.getY(), width/2, height/2,
                    width, height, xScale, 1f, xScale * map.player.getRotation());
        
        if (map.player.playerSwordVisible) {
        	TextureRegion swordTexture = new TextureRegion(swordImg, 0, 0, swordImg.getWidth(), swordImg.getHeight());
        	batch.draw(swordTexture, map.player.getX() - 43 + (map.player.getFacingLeft() ? -5 : 0), map.player.getY() + 8, 67, 20,
        			swordImg.getWidth(), swordImg.getHeight(), xScale * (map.player.playerFlipSword ? -1 : 1), 1f, xScale * map.player.playerSwordRotation);
        }
    }
    
    private void drawSages() {
        batch.draw(startSageImg, map.sageStartPos.x, map.sageStartPos.y);
        batch.draw(endSageImg, map.sageEndPos.x, map.sageEndPos.y);
    }
    
    private void drawDialogue() {
        for (Dialogue d: map.dialogues) {
            if (d.active) {
                Rectangle speaker0Rect = convertSpeakerToRectangle(d.speaker0);
                Rectangle speaker1Rect = convertSpeakerToRectangle(d.speaker1);
                Rectangle activeRect;
                if (d.speakerList[d.currentSentence]==0) activeRect = speaker0Rect;
                else activeRect = speaker1Rect;
                float dialogueBoxX = activeRect.x+(activeRect.width-SIGN_TEXT_WIDTH)/2;
                float dialogueBoxY = activeRect.y+activeRect.height+DIALOGUE_BOX_VERTICAL_SPACING;
                // draw dialogue box background
                Gdx.gl.glEnable(GL20.GL_BLEND);
                dialogueBoxRenderer.begin(ShapeType.Filled);
                dialogueBoxRenderer.setColor(borderColor);
                dialogueBoxRenderer.rect(dialogueBoxX-DIALOGUE_BORDER, dialogueBoxY-DIALOGUE_BORDER, DIALOGUE_BOX_WIDTH+2*DIALOGUE_BORDER, DIALOGUE_BOX_HEIGHT+2*DIALOGUE_BORDER);
                dialogueBoxRenderer.setColor(textboxColor);
                dialogueBoxRenderer.rect(dialogueBoxX, dialogueBoxY, DIALOGUE_BOX_WIDTH, DIALOGUE_BOX_HEIGHT);
                dialogueBoxRenderer.end();
                Gdx.gl.glDisable(GL20.GL_BLEND);
                // draw dialogue text
                batch.begin();
                dialogueFont.draw(batch, d.text[d.currentSentence], 
                        dialogueBoxX+DIALOGUE_TEXT_HORIZ_MARGIN, dialogueBoxY+DIALOGUE_BOX_HEIGHT-DIALOGUE_TEXT_VERT_MARGIN, 
                        DIALOGUE_BOX_WIDTH-2*DIALOGUE_TEXT_HORIZ_MARGIN, Align.center, true);
                batch.end();
            }
        }
    }
    
    private Rectangle convertSpeakerToRectangle (String speaker) {
        if (speaker.equals("player")) {
            return map.player.position;
        }
        else if (speaker.equals("startSage")) {
            return new Rectangle(map.sageStartPos.x, map.sageStartPos.y, startSageImg.getWidth(), startSageImg.getHeight());
        }
        else if (speaker.equals("endSage")) {
            return new Rectangle(map.sageEndPos.x, map.sageEndPos.y, endSageImg.getWidth(), endSageImg.getHeight());
        }
        return null;
    }
    
    /*private void drawSageDialog() {
    	if (Math.abs(map.player.getX() - 2*32) + Math.abs(map.player.getY() - 2*32 + 2) <= 150) {
        	dialogBatch.setProjectionMatrix(cam.combined);
        	dialogBatch.begin();
	        sageFont.draw(dialogBatch, Globals.SAGE_TEXT, 120, 190, 250, Align.center, true);
	        dialogBatch.end();
        }
    }*/
    
    private void drawHitboxes() {
    	if (DEBUG_SHOW_HITBOXES) {    
	        projRenderer.setColor(Color.RED);
	        for (HurtboxCircle box : map.player.getActiveHurtboxes()) {
	            projRenderer.arc(map.player.getX() + box.x, map.player.getY() + box.y, box.radius, 0, 360);
	        }
        }
    }
    
    private void drawProjectiles() {
    	for(Projectile proj: this.map.projectiles) {
    		proj.render(projRenderer);
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
