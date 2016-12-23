package com.ld.game;

import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

public class Map {
    static final int NUM_LEGS = 4; //number of legs, not including the thing before the first dialogue
                                   //sorry, this results in some <=s instead of <s but hopefully it's not too annoying
    
    public TiledMap tileMap;
    public TiledMapTileLayer collisionLayer;

    public float pixelWidth, pixelHeight;
    
    public Player player;
    public Vector2 startPos;
    public Vector2 sageStartPos;
    public Vector2 sageEndPos;
    public Rectangle startZone;
    public Rectangle finishZone;
    public Array<Target> targets;
    public Array<Array<Rectangle>> deathRects; // [deathRects in leg 0 (presumably nothing), deathRects in leg 1 (Death Layer 1), deathRects in leg 2 (Death Layer 2), etc...]
    public Array<Projectile> projectiles;
    public Array<Checkpoint> checkpoints;
    public Array<Sign> signs;
    public Array<Particle> particles;
    public Checkpoint currCheckpoint;
    public int leg; // starts at 0 before you activate the starting dialogue, 1
                    // for sword trip, 2 for gun trip, etc.
    public int playerDeaths;
    Sounds sounds;

    public Array<Dialogue> dialogues;

    public Map(String levelFile) {
        targets = new Array<Target>();
        deathRects = new Array<Array<Rectangle>>();
        projectiles = new Array<Projectile>();
        checkpoints = new Array<Checkpoint>();
        signs = new Array<Sign>();
        particles = new Array<Particle>();
        dialogues = Globals.makeDialogue();
        leg = 0;
        playerDeaths = 0;
        sounds = new Sounds();

        tileMap = new TmxMapLoader().load(levelFile);
        collisionLayer = (TiledMapTileLayer) tileMap.getLayers().get("Collision Tile Layer 1");
        player = new Player(this, collisionLayer);

        pixelWidth = tileMap.getProperties().get("width", int.class)
                * tileMap.getProperties().get("tilewidth", int.class);
        pixelHeight = tileMap.getProperties().get("height", int.class)
                * tileMap.getProperties().get("tileheight", int.class);

        // this layer contains rectangles that kill you when they overlap you,
        // meant for static hazards
        loadDeathRects(deathRects);

        for (MapObject cp : getLayerObjects("Checkpoint Layer")) {
            MapProperties p = cp.getProperties();
            Checkpoint checkpoint = new Checkpoint(p.get("x", float.class), p.get("y", float.class));
            checkpoints.add(checkpoint);
        }
        // handleRectangleLayer("Checkpoint Layer", Checkpoint.class,
        // checkpoints);

        for (MapObject t : getLayerObjects("Targets Layer")) {
            MapProperties p = t.getProperties();
            Target target = new Target(p.get("x", float.class), p.get("y", float.class));
            targets.add(target);
            /*
             * Debugging code to print out all properties of an object
             * Iterator<String> keys = t.getProperties().getKeys(); while
             * (keys.hasNext()) { String property = keys.next();
             * System.out.println(property + ": "+p.get(property)); }
             */
        }

        for (MapObject l : getLayerObjects("Locations Layer")) {
            MapProperties p = l.getProperties();
            String name = l.getName();
            if (name.equals("start")) {
                startPos = new Vector2(p.get("x", float.class), p.get("y", float.class));
                player.position.x = startPos.x;
                player.position.y = startPos.y;
            } else if (name.equals("startZone")) {
                startZone = new Rectangle(p.get("x", float.class), p.get("y", float.class), p.get("width", float.class),
                        p.get("height", float.class));
            } else if (name.equals("finishZone")) {
                finishZone = new Rectangle(p.get("x", float.class), p.get("y", float.class),
                        p.get("width", float.class), p.get("height", float.class));
            } else if (name.equals("startSage")) {
                sageStartPos = new Vector2(p.get("x", float.class), p.get("y", float.class));
            } else if (name.equals("endSage")) {
                sageEndPos = new Vector2(p.get("x", float.class), p.get("y", float.class));
            }
        }

        for (MapObject s : getLayerObjects("Sign Layer")) {
            MapProperties p = s.getProperties();
            String signText = p.get("text", String.class);
            int signLeg;
            if (p.containsKey("leg")) signLeg = p.get("leg", int.class);
            else signLeg = 1; // unless if leg is specified, assume it's only on the first leg
            Sign sign = new Sign(p.get("x", float.class), p.get("y", float.class), p.get("width", float.class),
                    p.get("height", float.class), signText, signLeg);
            signs.add(sign);
        }
    }

    // loads the death rectangles per leg into deathRects
    private void loadDeathRects(Array<Array<Rectangle>> deathRects) {
        for (int i=0; i<=NUM_LEGS; i++) {
            Array<Rectangle> layerDeathRects = new Array<Rectangle>();
            for (MapObject d : getLayerObjects("Death Layer "+i)) {
                MapProperties p = d.getProperties();
                layerDeathRects.add(new Rectangle(p.get("x", float.class), p.get("y", float.class), p.get("width", float.class),
                        p.get("height", float.class)));
            }
            deathRects.add(layerDeathRects);
        }
    }
    
    private MapObjects getLayerObjects(String name) {
        MapLayer layer = tileMap.getLayers().get(name);
        if (layer != null) {
            return layer.getObjects();
        } else {
            return new MapObjects();
        }
    }

    /*
     * private void handleRectangleLayer(String name, Class<?> c, Array<?
     * extends Rectangle> array) { MapObjects objects = getLayerObjects(name);
     * for(MapObject object: objects) { MapProperties p =
     * object.getProperties(); Object rect; try { rect =
     * c.getConstructor(float.class, float.class).newInstance(p.get("x",
     * float.class), p.get("y", float.class));
     * System.out.println(rect.getClass() + " " + array); //array.add(rect); }
     * catch (Exception e) { // TODO Auto-generated catch block
     * e.printStackTrace(); } // array.add(c.cast(rect)); } }
     */

    public EnhancedCell getEnhancedCell(float x, float y) {
        int xCoord = (int) (x / collisionLayer.getTileWidth());
        int yCoord = (int) (y / collisionLayer.getTileHeight());
        Cell cell = collisionLayer.getCell(xCoord, yCoord);
        if (cell == null) {
            return null;
        }
        return new EnhancedCell(cell, xCoord, yCoord);
    }

    public void checkDeathCollision() {
        for (Rectangle deathRect : deathRects.get(leg)) { 
            if (player.position.overlaps(deathRect)) {
                killPlayer();
            }
        }
    }

    public void killPlayer() {
        playerDeaths++;
        player.horizVelocity = 0;
        player.vertVelocity = 0;

        if (currCheckpoint == null) {
            player.position.x = startPos.x;
            player.position.y = startPos.y;
        } else {
            player.position.x = currCheckpoint.x;
            player.position.y = currCheckpoint.y;
        }

        sounds.deathSound.play();

        refreshTargets();

        for (Projectile p : projectiles) {
            p.destroy();
        }
        
        player.grabShuriken();
    }

    public void startDialogue(int i) {
        dialogues.get(i).activate();
        player.inDialog = true;
    }

    // advances dialogue, returns true if dialogue is still happening, false
    // otherwise
    public boolean advanceDialogue() {
        Dialogue activeDialogue = null;
        for (Dialogue d : dialogues) {
            if (d.active)
                activeDialogue = d;
        }
        assert activeDialogue != null;
        activeDialogue.advance();
        sounds.dialogueSound.play();
        return activeDialogue.active;
    }

    public void checkTargetHits() {
        Array<Circle> hurtboxCircles = player.getHurtboxCircles();
        Array<HurtboxRectangle> hurtboxRects = player.getHurtboxRects();
        for (Target t : targets) {
            boolean removeTarget = false;
            for (Circle c : hurtboxCircles) {
                if (Intersector.overlaps(c, t) && t.exists)
                    removeTarget = true;
            }
            for (HurtboxRectangle r : hurtboxRects) {
                if (Intersector.overlaps(r, t) && t.exists) {
                    removeTarget = true;
                    handleProjectileInteraction(r);
                }
            }
            if (removeTarget) {
                removeTarget(t);
                player.hasDoubleJump = true;
                if (player.currentAnimationType == AnimationType.AIR_DAIR) {
                	player.vertVelocity = Math.min(player.vertVelocity, -5);
                	player.fastFalling = false;
                }
                sounds.targetBreakSound.play();
            }
        }
        //
        //
        //
        // Array<Circle> allHurtboxes = player.getHurtboxCircles();
        // for (Target t: targets) {
        // for (Circle c: allHurtboxes) {
        // if (Intersector.overlaps(c, t) && t.exists) {
        // removeTarget(t);
        // player.playerHasDoubleJump = true;
        // }
        // }
        // }
    }

    public void checkCheckpointHits() {
        Array<Circle> hurtboxCircles = player.getHurtboxCircles();
        Array<HurtboxRectangle> hurtboxRects = player.getHurtboxRects();
        for (Checkpoint cp : checkpoints) {
            boolean newCheckpoint = false;
            for (Circle c : hurtboxCircles) {
                if (Intersector.overlaps(c, cp) && currCheckpoint != cp)
                    newCheckpoint = true;
            }
            for (HurtboxRectangle r : hurtboxRects) {
                // System.out.println(r);
                if (Intersector.overlaps(r, cp) && currCheckpoint != cp) {
                    newCheckpoint = true;
                    handleProjectileInteraction(r);
                }
            }
            if (newCheckpoint) {
                currCheckpoint = cp;
                sounds.checkpointSound.play();
                // Sound checkpointSound =
                // Gdx.audio.newSound(Gdx.files.internal("checkpoint_hit.mp3"));
                // checkpointSound.play();
                // http://soundbible.com/1980-Swords-Collide.html
            }
        }

        //
        //
        //
        // Array<Circle> allHurtboxes = player.getHurtboxCircles();
        // for (Circle c: allHurtboxes) {
        // for (Checkpoint cp: checkpoints) {
        // if (Intersector.overlaps(c, cp) && currCheckpoint!=cp) {
        // currCheckpoint = cp;
        // //Sound checkpointSound =
        // Gdx.audio.newSound(Gdx.files.internal("checkpoint_hit.mp3"));
        // //checkpointSound.play();
        // //http://soundbible.com/1980-Swords-Collide.html
        // }
        // }
        // }
    }

    public void checkSignHits() {
        for (Sign s : signs) {
            if (Intersector.overlaps(s, player.position))
                s.active = true;
            else
                s.active = false;
        }
    }

    public void checkLegFinished() {
        boolean finished = false;
        if (leg == 0) {
            if (Intersector.overlaps(player.position, startZone) && player.state == PlayerState.GROUND) {
                // System.out.println("leg 0 finished!");
                leg++;
                startDialogue(0);

                player.setWeapon("sword");
                finished = true;
            }
        } else if (leg == 1) {
            if (Intersector.overlaps(player.position, finishZone) && player.state == PlayerState.GROUND) {
                // System.out.println("leg 1 finished!");
                leg++;
                startDialogue(1);
                refreshTargets();

                player.setWeapon("laser");
                finished = true;
            }
        } else if (leg == 2) {
            if (Intersector.overlaps(player.position, startZone) && player.state == PlayerState.GROUND) {
                leg++;
                startDialogue(2);
                finished = true;
            }
        }
        
        if (finished) {
            TiledMapTileLayer newCollisionLayer = (TiledMapTileLayer) tileMap.getLayers().get("Collision Tile Layer "+leg);
            player.setCollisionLayer(newCollisionLayer);
        }
    }

    private void removeProjectileGivenHurtbox(HurtboxRectangle r) {
        if (r.ownerProjectile != null) {// hurtboxOwner should be the projectile
                                        // that owns r
            Projectile laser = r.ownerProjectile;
            laser.destroy();
        }
    }
    
    private void handleProjectileInteraction(HurtboxRectangle r) {
        Projectile p = r.ownerProjectile;
        if (p != null) {// hurtboxOwner should be the projectile that owns r
            if(p instanceof LaserPulse){
                p.destroy();
            }
        }
    }

    public void refreshTargets() {
        for (Target t : targets) {
            t.exists = true;
        }
    }

    public void removeTarget(Target t) {
        t.exists = false;
        // probably want to increase score or play sound effects here as well
    }

    public void update() {
        for (Projectile proj : projectiles) {
            proj.update();
            if (getEnhancedCell(proj.head.x, proj.head.y) != null) {
                proj.handleWallCollision();
            }
        }
        for (int i = 0; i < particles.size; ++i) {
            particles.get(i).tick();
            if (particles.get(i).readyToDie()) {
                particles.removeIndex(i);
                --i;
            }
        }

        player.updateState();
        checkTargetHits();
        checkCheckpointHits();
        checkSignHits();
        checkDeathCollision();
        checkLegFinished();
    }

    public boolean isGameFinished() {
        if (leg == 3 && dialogues.get(2).currentSentence == 5)
            return true;
        else
            return false;
    }
}
