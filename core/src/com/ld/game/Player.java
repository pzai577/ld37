package com.ld.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;

enum PlayerState {
    GROUND,
    GROUND_PREJUMP,
    GROUND_ANIM,
    AIR,
    AIR_ANIM,
    AIR_ANIM_RECOVER,
    WALL_LEFT,
    WALL_RIGHT;
}

enum PlayerFrame {
    // TODO: maybe get rid of this enum
    STAND,
    RUN,
    RUN_NOARMS,
    PREJUMP,
    CLIMB,
    CYCLONE,
    TWIST,
}

public class Player {
    // TODO: Maybe make all of the EnhancedCells class variables to avoid slowdown?
    // TODO: Use delta time to account for varying frame rates

    private static final double FALL_ACCELERATION = 0.35;
    
    // note: if PLAYER_GROUND_MOVESPEED>PLAYER_AIR_MAX_MOVESPEED, things look weird if you run off a platform
    private static final double PLAYER_GROUND_MAX_MOVESPEED = 5;
    private static final double PLAYER_AIR_INFLUENCE = 0.35;
    private static final double PLAYER_AIR_MAX_MOVESPEED = 5;
    
    private static final double PLAYER_MAX_SLOWFALL_SPEED = 8.3;
    private static final double PLAYER_FASTFALL_SPEED = 9.3;
    
    private static final double PLAYER_JUMP_SPEED = 9.5;
    private static final double PLAYER_DOUBLE_JUMP_SPEED = 8;
    private static final double PLAYER_SHORTHOP_SPEED = 6.5;
    private static final double PLAYER_PREJUMP_FRAMES = 5;

    private static final double PLAYER_WALL_INFLUENCE = 0.16;
    private static final double PLAYER_WALL_SCALE_SPEED = 5.5;
    private static final double WALL_FRICTION = 0.08;
    private static final double FLOOR_FRICTION = 0.12;
    private static final double AIR_FRICTION_SCALING = 0.99f;
    
    public static final int PLAYER_WIDTH = 32;
    public static final int PLAYER_HEIGHT = 56;
    
    private boolean paused;
    public boolean isAlive;
    public boolean inDialog;
    public Rectangle position;
    
    public PlayerState state = PlayerState.AIR; // also making this one public
    public double horizVelocity = 0.0; // making these two public so Map can access it
    public double vertVelocity = 0.0;
    private float rotation = 0.0f;
    
    private boolean facingLeft = true;
    private float inFloat = -1f; // direction, as a float: 1 if being told to move right, -1 if left, 0 if no left-right input
    public boolean hasDoubleJump = false;
    //private boolean playerRotating = false;
    //private boolean playerRotatingLeft = false;
    public boolean fastFalling = false;
    
    public boolean playerSwordVisible = false;
    public boolean playerFlipSword = false;
    public float playerSwordRotation = 0;
    public AnimationType currentAnimationType;
    
    private boolean currentAnimationIsFlipped;
    private float[][] currentAnimationFrames;
    private int currentDuration;
    
    private String weapon;
    private Sound weaponSound;
    private Sounds sounds;
    
    private PlayerFrame playerFrame;
    
    private int stateFrameDuration = 0;
    
    private Map map;
    private TiledMapTileLayer collisionLayer;
    // TODO: maybe change to Array to avoid excessive garbage collection? only applies for lots of hurtboxes
    private Array<HurtboxCircle> activeHurtboxes;
    private Array<HurtboxRectangle> activeHurtboxRects;
    
    public Player(Map map, TiledMapTileLayer collisionLayer) {
        paused = false;
        isAlive = true;
        inDialog = false;
        position = new Rectangle(200, 200, PLAYER_WIDTH, PLAYER_HEIGHT);
        this.map = map;
        this.collisionLayer = collisionLayer;
        this.activeHurtboxes = new Array<HurtboxCircle>();
        this.activeHurtboxRects = new Array<HurtboxRectangle>();
        this.playerFrame = PlayerFrame.STAND;
        this.sounds = map.sounds;
    }

//. update code    
    
    public void updateState() {
        
        if(inDialog) {
            updateInDialog();
            return;
        }
        if (paused) return;
        
        
    //  begin movement code        
        if (state == PlayerState.AIR || state == PlayerState.AIR_ANIM) {
            updatePlayerAir();
        }
        else if (state == PlayerState.GROUND || state == PlayerState.GROUND_ANIM
                || state == PlayerState.GROUND_PREJUMP) {
            updatePlayerGround();
        }
//        else if (state == PlayerState.WALL_LEFT || state == PlayerState.WALL_RIGHT){
//            updatePlayerWall();
//        }
         else if (state == PlayerState.WALL_LEFT) {
             updatePlayerWall(Keys.LEFT, Keys.RIGHT);
         }
         else if (state == PlayerState.WALL_RIGHT) {
             updatePlayerWall(Keys.RIGHT, Keys.LEFT);
         }
    //  end movement code
        
        
        if(Gdx.input.isKeyJustPressed(Keys.X)) {
            useWeapon();
        }
        
        // press r to refresh
        if(Gdx.input.isKeyJustPressed(Keys.R)) {
            map.killPlayer();
        }
        
        ++stateFrameDuration;
    }
    
    private void updateInDialog() {
        if(Gdx.input.isKeyJustPressed(Keys.Z)) {
            if(!map.advanceDialogue()){
                // dialog failed to advance, dialog is finished
                inDialog = false;
            }
        }
    }
    
    private void updatePlayerAir(){

        if (Gdx.input.isKeyJustPressed(Keys.DOWN) && vertVelocity > 0) {
            fastFalling = true;
        }
        
        if (Gdx.input.isKeyJustPressed(Keys.Z) && hasDoubleJump) {
            this.doubleJump();
        }
        
        boolean wasInAirAnim = true;
        if (state == PlayerState.AIR) {
            wasInAirAnim = false;
            
            this.setDirection();
            horizVelocity += inFloat * PLAYER_AIR_INFLUENCE;
            if(inFloat * horizVelocity > PLAYER_AIR_MAX_MOVESPEED) {
                horizVelocity = inFloat * PLAYER_AIR_MAX_MOVESPEED;
            }
            horizVelocity *= AIR_FRICTION_SCALING;    
        }
        
        //update y position of player
        if (fastFalling) {
            position.y -= PLAYER_FASTFALL_SPEED;
        }
        else {
            position.y -= vertVelocity;
            vertVelocity = Math.min(vertVelocity + FALL_ACCELERATION,
                                          PLAYER_MAX_SLOWFALL_SPEED);
        }
        
        //check whether you're intersecting something vertically
        EnhancedCell topCell = getCollidingTopCell();
        EnhancedCell bottomCell = getCollidingBottomCell();

        if (topCell != null) {
            if (vertVelocity < 0) {
                position.y = (topCell.y) * collisionLayer.getTileHeight() - PLAYER_HEIGHT - 1;
                vertVelocity = 0;
            }
        }
        
        if (bottomCell != null) {
            position.y = (bottomCell.y+1)*collisionLayer.getTileHeight();
            this.landOnGround();
        }
        
        //update x position of player
        position.x += horizVelocity;
        //check whether you're intersecting something horizontally
        EnhancedCell leftCell = getCollidingLeftCell();
        EnhancedCell rightCell = getCollidingRightCell();
        if (leftCell != null) {
            position.x = (leftCell.x+1)*collisionLayer.getTileWidth();
            horizVelocity = 0;
            if (state != PlayerState.GROUND) {
                state = PlayerState.WALL_LEFT;
                rotation = 0;
                //playerRotating = false;
                playerSwordVisible = false;
                playerFlipSword = false;
            }
        }
        else if (rightCell != null) {
            position.x = (rightCell.x)*collisionLayer.getTileWidth() - PLAYER_WIDTH - 1;
            horizVelocity = 0;
            if (state != PlayerState.GROUND) {
                state = PlayerState.WALL_RIGHT;
                rotation = 0;
                //playerRotating = false;
                playerSwordVisible = false;
                playerFlipSword = false;
            }
        }
        if (wasInAirAnim) {
            updateAnimationFramesIfInState(PlayerState.AIR_ANIM, PlayerState.AIR);
        }
    }
    
    private void updatePlayerGround(){
        
        this.moveHorizOnGround();

        EnhancedCell leftCell = getCollidingLeftCell();
        EnhancedCell rightCell = getCollidingRightCell();
        EnhancedCell bottomCell = getCollidingBottomCell();
        
        
        if (state == PlayerState.GROUND) {
            if (Gdx.input.isKeyPressed(Keys.LEFT)) {
                playerFrame = PlayerFrame.RUN;
            }
            else if (Gdx.input.isKeyPressed(Keys.RIGHT)) {
                playerFrame = PlayerFrame.RUN;
            }
            else {
                playerFrame = PlayerFrame.STAND;
            }
            if (Gdx.input.isKeyJustPressed(Keys.Z)) {
                this.beginGroundJump();
            }

            
            if (leftCell != null) {
                position.x = (leftCell.x+1) * collisionLayer.getTileWidth();
                if (Gdx.input.isKeyPressed(Keys.UP)) {
                    this.climbWall(PlayerState.WALL_LEFT);
                }
            }
            else if (rightCell != null) {
                position.x = (rightCell.x) * collisionLayer.getTileWidth() - PLAYER_WIDTH - 1;
                if (Gdx.input.isKeyPressed(Keys.UP)) {
                    this.climbWall(PlayerState.WALL_RIGHT);
                }
            }
            else if (bottomCell == null) {
                setState(PlayerState.AIR);
                fastFalling = false;
                hasDoubleJump = true;
                playerFrame = PlayerFrame.STAND;
                vertVelocity = 0;
            }
        }
        else{
            
            if (leftCell != null) {
                position.x = (leftCell.x+1) * collisionLayer.getTileWidth();
            }
            else if (rightCell != null) {
                position.x = (rightCell.x) * collisionLayer.getTileWidth() - PLAYER_WIDTH - 1;
            }
            else if (bottomCell == null) {
                position.x -= horizVelocity; // TODO: make notion of 'teeter' precise?
                horizVelocity = 0;
            }
            
            if (state == PlayerState.GROUND_PREJUMP) {
                if (stateFrameDuration == PLAYER_PREJUMP_FRAMES) {
                    this.endGroundJump();
                }
            }
            else{ // GROUND_ANIM
                updateAnimationFramesIfInState(PlayerState.GROUND_ANIM, PlayerState.GROUND);
            }
        }
    }
    
    private void moveHorizOnGround() {
        
        this.setDirection();
        
        horizVelocity = (1. - FLOOR_FRICTION) * horizVelocity;
        horizVelocity += inFloat * FLOOR_FRICTION * PLAYER_GROUND_MAX_MOVESPEED;
        
        if(inFloat == 0) {
            if (Math.abs(horizVelocity) < 1) {
                horizVelocity = 0;
            }
        }
        
        position.x += horizVelocity;
    }

    private void updatePlayerWall(int left, int right) {
        
        if(left == Keys.LEFT)
            this.faceLeft();
        else if(left == Keys.RIGHT)
            this.faceRight();
        
        horizVelocity = 0;
        playerFrame = PlayerFrame.CLIMB;

        if (Gdx.input.isKeyPressed(right)) {
            if(right == Keys.LEFT)
                this.faceLeft();
            else if(right == Keys.RIGHT)
                this.faceRight();
            
            horizVelocity = inFloat * 2 * PLAYER_AIR_INFLUENCE;
            state = PlayerState.AIR;
            playerFrame = PlayerFrame.STAND;
        }
        else if (Gdx.input.isKeyJustPressed(Keys.Z)) {
            if(right == Keys.LEFT)
                this.faceLeft();
            else if(right == Keys.RIGHT)
                this.faceRight();
            
            horizVelocity = inFloat * PLAYER_AIR_MAX_MOVESPEED;
            vertVelocity = -6;
            setState(PlayerState.AIR);
            playerFrame = PlayerFrame.STAND;
            fastFalling = false;
            hasDoubleJump = true;
            sounds.jumpSound.play();
        }
        else if (Gdx.input.isKeyPressed(Keys.UP)) {
            vertVelocity = (1. - PLAYER_WALL_INFLUENCE) * vertVelocity
                    + PLAYER_WALL_INFLUENCE * -PLAYER_WALL_SCALE_SPEED;
            position.y -= vertVelocity;
        }
        else {
            position.y -= vertVelocity;
            vertVelocity = (1. - WALL_FRICTION) * vertVelocity + WALL_FRICTION * 3;
        }

        position.x += horizVelocity;

        EnhancedCell bottomCell = getCollidingBottomCell();
        EnhancedCell topCell = getCollidingTopCell();
        EnhancedCell forwardCell;
        if(left == Keys.LEFT)
            forwardCell = getCollidingLeftCell();
        else if(left == Keys.RIGHT)
            forwardCell = getCollidingRightCell();
        else
            forwardCell = null;
        
        if (bottomCell != null) {
            position.y = (bottomCell.y+1) * collisionLayer.getTileHeight();
            state = PlayerState.GROUND;
            rotation = 0;
            //playerRotating = false;
            hasDoubleJump = true;
            playerSwordVisible = false;
            playerFlipSword = false;
        }
        else if (topCell != null) {
            if (vertVelocity < 0) {
                position.y = (topCell.y) * collisionLayer.getTileHeight() - PLAYER_HEIGHT - 1;
                vertVelocity = 0;
            }
        }
        else if (forwardCell==null) {
            // TODO: make the player snap to ground?
            setState(PlayerState.AIR);
            playerFrame = PlayerFrame.STAND;
        }

    }
    
    private void updatePlayerWallLeft(){
        horizVelocity = 0;
        facingLeft = true;
        playerFrame = PlayerFrame.CLIMB;
        if (Gdx.input.isKeyPressed(Keys.RIGHT)) {
            horizVelocity = 2 * PLAYER_AIR_INFLUENCE;
            state = PlayerState.AIR;
            playerFrame = PlayerFrame.STAND;
        }
        else if (Gdx.input.isKeyJustPressed(Keys.Z)) {
            horizVelocity = PLAYER_AIR_MAX_MOVESPEED;
            vertVelocity = -6;
            setState(PlayerState.AIR);
            playerFrame = PlayerFrame.STAND;
            facingLeft = false;
            fastFalling = false;
            hasDoubleJump = true;
            sounds.jumpSound.play();
        }
        else if (Gdx.input.isKeyPressed(Keys.UP)) {
            vertVelocity = (1. - PLAYER_WALL_INFLUENCE) * vertVelocity
                    + PLAYER_WALL_INFLUENCE * -PLAYER_WALL_SCALE_SPEED;
            position.y -= vertVelocity;
        }
        else {
            position.y -= vertVelocity;
            vertVelocity = (1. - WALL_FRICTION) * vertVelocity + WALL_FRICTION * 3;
        }
        
        position.x += horizVelocity;
        
        EnhancedCell leftCell = getCollidingLeftCell();
        EnhancedCell bottomCell = getCollidingBottomCell();
        EnhancedCell topCell = getCollidingTopCell();
        
        if (bottomCell != null) {
            position.y = (bottomCell.y+1) * collisionLayer.getTileHeight();
            state = PlayerState.GROUND;
            rotation = 0;
            //playerRotating = false;
            hasDoubleJump = true;
            playerSwordVisible = false;
            playerFlipSword = false;
        }
        else if (topCell != null) {
            if (vertVelocity < 0) {
                position.y = (topCell.y) * collisionLayer.getTileHeight() - PLAYER_HEIGHT - 1;
                vertVelocity = 0;
            }
        }
        else if (leftCell==null) {
            // TODO: make the player snap to ground?
            setState(PlayerState.AIR);
            playerFrame = PlayerFrame.STAND;
        }
    }
    
    private void updatePlayerWallRight(){
        horizVelocity = 0;
        facingLeft = false;
        playerFrame = PlayerFrame.CLIMB;
        if (Gdx.input.isKeyPressed(Keys.LEFT)) {
            horizVelocity = -2 * PLAYER_AIR_INFLUENCE;
            playerFrame = PlayerFrame.STAND;
            state = PlayerState.AIR;
        }
        else if (Gdx.input.isKeyJustPressed(Keys.Z)) {
            horizVelocity = -PLAYER_AIR_MAX_MOVESPEED;
            vertVelocity = -6;
            setState(PlayerState.AIR);
            facingLeft = true;
            playerFrame = PlayerFrame.STAND;
            fastFalling = false;
            hasDoubleJump = true;
            sounds.jumpSound.play();
        }
        else if (Gdx.input.isKeyPressed(Keys.UP)) {
            vertVelocity = (1. - PLAYER_WALL_INFLUENCE) * vertVelocity
                    + PLAYER_WALL_INFLUENCE * -PLAYER_WALL_SCALE_SPEED;
            position.y -= vertVelocity;
        }
        else {
            position.y -= vertVelocity;
            vertVelocity = (1. - WALL_FRICTION) * vertVelocity + WALL_FRICTION * 3;
        }
        
        position.x += horizVelocity;
        
        EnhancedCell rightCell = getCollidingRightCell();
        EnhancedCell bottomCell = getCollidingBottomCell();
        EnhancedCell topCell = getCollidingTopCell();
        
        if (bottomCell != null) {
            position.y = (bottomCell.y+1) * collisionLayer.getTileHeight();
            state = PlayerState.GROUND;
            rotation = 0;
            //playerRotating = false;
            hasDoubleJump = true;
            playerSwordVisible = false;
            playerFlipSword = false;
        }
        else if (topCell != null) {
            if (vertVelocity < 0) {
                position.y = (topCell.y) * collisionLayer.getTileHeight() - PLAYER_HEIGHT - 1;
                vertVelocity = 0;
            }
        }
        else if (rightCell==null) {
            setState(PlayerState.AIR);
            playerFrame = PlayerFrame.STAND;
        }
    }
    
    public void setState(PlayerState state) {
        this.state = state;
        stateFrameDuration = 0;
    }

//. end update code
    
//. movement actions    

    private void setDirection() {
        if (Gdx.input.isKeyPressed(Keys.LEFT)) {
            this.faceLeft();
        }
        else if (Gdx.input.isKeyPressed(Keys.RIGHT)) {
            this.faceRight();
        }
        else {
            inFloat = 0;
        }
    }
    
    private void faceLeft() {
        this.facingLeft = true;
        this.inFloat = -1f;
    }
    
    private void faceRight() {
        this.facingLeft = false;
        this.inFloat = 1f;
    }

    private void handleJumpInput() {

    }
    
    private void beginGroundJump() {
        setState(PlayerState.GROUND_PREJUMP);
        playerFrame = PlayerFrame.PREJUMP;
        fastFalling = false;
    }
    
    private void endGroundJump() {
        sounds.jumpSound.play();
        if (Gdx.input.isKeyPressed(Keys.Z)) {
            vertVelocity = -PLAYER_JUMP_SPEED;
        }
        else {
            vertVelocity = -PLAYER_SHORTHOP_SPEED;
        }
        setState(PlayerState.AIR);
        playerFrame = PlayerFrame.STAND;
    }
    
    private void doubleJump() {
        if (Gdx.input.isKeyPressed(Keys.LEFT)) {
            horizVelocity = -PLAYER_AIR_MAX_MOVESPEED;
            facingLeft = true;
        }
        else if (Gdx.input.isKeyPressed(Keys.RIGHT)) {
            horizVelocity = PLAYER_AIR_MAX_MOVESPEED;
            facingLeft = false;
        }
        hasDoubleJump = false;
        sounds.dblJumpSound.play();
        map.particles.add(new Particle(position.x, position.y,
                new Point[]{ new Point(0, 0), new Point(1, 0), new Point(2, 0), new Point(2, 0) }));
        //playerRotating = true;
        fastFalling = false;
        //playerRotatingLeft = (playerHorizVelocity <= 0);
        vertVelocity = -PLAYER_DOUBLE_JUMP_SPEED;
    }

    private void landOnGround() {
        state = PlayerState.GROUND;
        rotation = 0;
        hasDoubleJump = true;
        playerSwordVisible = false;
        playerFlipSword = false;
        sounds.landingSound.play();
    }

    private void climbWall(PlayerState wallSide) {
        vertVelocity = -PLAYER_WALL_SCALE_SPEED;
        setState(wallSide);
        playerFrame = PlayerFrame.CLIMB;
        playerSwordVisible = false;
        playerFlipSword = false;
    }

//. end movement actions
    
    /*
     * Some collision code adapted from https://www.youtube.com/watch?v=TLZbC9brH1c
     * 
     * These four methods return a colliding cell in the corresponding direction
     * An "EnhancedCell" is just a cell with extra information about the x and y coordinates.
     */
    public EnhancedCell getCollidingLeftCell() {
        for (float step = 1f; step < PLAYER_HEIGHT; step += collisionLayer.getTileHeight() / 2) {        
            EnhancedCell cell = map.getEnhancedCell(getX() - 1, getY() + step);
            if (cell != null) {
                return cell;
            }
        }
        return map.getEnhancedCell(getX() - 1, getY() + PLAYER_HEIGHT);
    }
    
    public EnhancedCell getCollidingRightCell() {
        for (float step = 1f; step < PLAYER_HEIGHT; step += collisionLayer.getTileHeight() / 2) {        
            EnhancedCell cell = map.getEnhancedCell(getX() + PLAYER_WIDTH + 1, getY() + step);
            if (cell != null) {
                return cell;
            }
        }
        return map.getEnhancedCell(getX() + PLAYER_WIDTH + 1, getY() + PLAYER_HEIGHT);
    }
    
    public EnhancedCell getCollidingTopCell() {
        for (float step = 0.1f; step < PLAYER_WIDTH; step += collisionLayer.getTileWidth() / 2) {        
            EnhancedCell cell = map.getEnhancedCell(getX() + step, getY() + PLAYER_HEIGHT);
            if (cell != null) {
                return cell;
            }
        }
        return map.getEnhancedCell(getX() + PLAYER_WIDTH, getY() + PLAYER_HEIGHT);
    }
    
    public EnhancedCell getCollidingBottomCell() {
        for (float step = 0.5f; step < PLAYER_WIDTH; step += collisionLayer.getTileWidth() / 2) {        
            EnhancedCell cell = map.getEnhancedCell(getX() + step, getY() - 1);
            if (cell != null) {
                return cell;
            }
        }
        return map.getEnhancedCell(getX() + PLAYER_WIDTH, getY() - 5);
    }
    
    public float getX() {
        return position.x;
    }
    
    public float getY() {
        return position.y;
    }
    
    public boolean getFacingLeft() {
        return facingLeft;
    }
    
    public float getRotation() {
        return rotation;
    }
    
    public PlayerFrame getPlayerFrame() {
        return playerFrame;
    }
    
    public void loadHurtboxData(AnimationType type) {
        currentAnimationType = type;
        currentAnimationFrames = HurtboxData.getAnimationFrames(type);
        currentDuration = HurtboxData.getDuration(type);
        currentAnimationIsFlipped = facingLeft;
        weaponSound.play();
    }
    
    public Array<HurtboxCircle> getActiveHurtboxes() {
        return activeHurtboxes;
    }
    
    public void updateAnimationFramesIfInState(PlayerState state, PlayerState endState) {
        if (this.state == state) {
            updateActiveHurtboxes();
            if (currentAnimationType == AnimationType.AIR_FAIR) {
                playerSwordRotation += 17;
            }
            else if (currentAnimationType == AnimationType.AIR_DAIR && stateFrameDuration < 20) {
                playerSwordRotation += 16;
                rotation += 16;
            }
            else if (currentAnimationType == AnimationType.AIR_UAIR && stateFrameDuration < 21) {
                playerSwordRotation -= 14;
                rotation -= 14;
                
                //paused = true;
            }
            for (float[] hurtboxData : currentAnimationFrames) {
                if (Math.abs(hurtboxData[0] - stateFrameDuration) < 1e-6) {
                    float adjustedXPosition = PLAYER_WIDTH / 2 + hurtboxData[1] * (currentAnimationIsFlipped ? -1 : 1);
                    activeHurtboxes.add(new HurtboxCircle(adjustedXPosition,
                                                    hurtboxData[2] + PLAYER_HEIGHT / 2,
                                                    hurtboxData[3],
                                                    (int)hurtboxData[4]));
                }
            }
            if (currentAnimationType == AnimationType.AIR_FAIR && stateFrameDuration == 11) {
                playerFrame = PlayerFrame.STAND;
                playerSwordVisible = false;
            }
            if (currentAnimationType == AnimationType.AIR_DAIR && stateFrameDuration == 20) {
                playerSwordVisible = false;
                rotation = 0;
            }
            if (currentAnimationType == AnimationType.AIR_UAIR && stateFrameDuration == 11) {
                playerFrame = PlayerFrame.CYCLONE;
            }
            else if (currentAnimationType == AnimationType.AIR_UAIR && stateFrameDuration == 15) {
                playerFlipSword = false;
                playerSwordVisible = false;
            }
            if (stateFrameDuration == currentDuration) {
                this.state = endState;
                if (currentAnimationType == AnimationType.AIR_DAIR ||
                    currentAnimationType == AnimationType.AIR_UAIR ||
                    this.state == PlayerState.GROUND_ANIM) {
                    playerFrame = PlayerFrame.STAND;
                    rotation = 0;
                }
                activeHurtboxes.clear();
            }
        }
        else {
            activeHurtboxes.clear();
        }
    }
    
    public void updateActiveHurtboxes() {
        for (int i = 0; i < activeHurtboxes.size; ++i) {
            --activeHurtboxes.get(i).duration;
            if (activeHurtboxes.get(i).duration == 0) {
                activeHurtboxes.removeIndex(i);
                --i;
            }
        }
    }
    
    public void setWeapon(String weapon) {
        this.weapon = weapon;
        if(weapon.equals("sword")) {
            this.weaponSound = sounds.swordSound;
        }
        else if(weapon.equals("laser")) {
            this.weaponSound = sounds.laserSound;
        }
    }
    
    private void useWeapon() {
        if(weapon.equals("sword")) {
            swingSword();
        }
        else if(weapon.equals("laser")) {
            shootLaser();
        }
    }
    
    private void swingSword() {
        
        playerSwordVisible = true;
        
        if (state == PlayerState.AIR || state == PlayerState.AIR_ANIM) {
            setState(PlayerState.AIR_ANIM);
           if (Gdx.input.isKeyPressed(Keys.UP)) {
                swordHelper(AnimationType.AIR_UAIR, PlayerFrame.TWIST, -140);
                this.rotation = 175;
                playerFlipSword = true;
            }
            else if (Gdx.input.isKeyPressed(Keys.DOWN)) {
                swordHelper(AnimationType.AIR_DAIR, PlayerFrame.CYCLONE, -75);
            }
            else {
                swordHelper(AnimationType.AIR_FAIR, PlayerFrame.RUN_NOARMS, -90);
            }
            
        }
        
        if (state == PlayerState.GROUND) {
            setState(PlayerState.GROUND_ANIM);
            swordHelper(AnimationType.AIR_FAIR, PlayerFrame.RUN_NOARMS, -90);
        }
    }
    
    private void swordHelper(AnimationType hurtboxData, PlayerFrame frame, float rotation){
        loadHurtboxData(hurtboxData);
        this.playerFrame = frame;
        this.playerSwordRotation = rotation;
    }

    private void shootLaser() {
        // TODO: make map.projectiles private?
        LaserPulse laser = new LaserPulse(this.map, this, !this.facingLeft);
        if (laser.hitbox.ownerProjectile == null) System.out.println("laser hitbox owner is null");
        map.projectiles.add(laser);
        this.activeHurtboxRects.add(laser.hitbox);
        weaponSound.play();
    }
    
    public void removeLaser(HurtboxRectangle rect) {
        this.activeHurtboxRects.removeValue(rect, true);
    }
    
    public Array<Circle> getHurtboxCircles() {
        Array<Circle> allHurtboxes = new Array<Circle>();
        //convert hurtboxes to circles for Intersector
        for (HurtboxCircle hb: getActiveHurtboxes()) {
            allHurtboxes.add(new Circle(getX()+hb.x, getY()+hb.y, hb.radius));
        }
        return allHurtboxes;
    }
    
    public Array<HurtboxRectangle> getHurtboxRects() {
        return activeHurtboxRects;
    }
    
    public void setCollisionLayer(TiledMapTileLayer collisionLayer) {
        this.collisionLayer = collisionLayer;
    }
    
}
