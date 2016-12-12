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
    public Array<Rectangle> deathRects;
    public Array<Projectile> projectiles;
    public Array<Checkpoint> checkpoints;
    public Array<Sign> signs;
    public Array<Particle> particles;
    public Checkpoint currCheckpoint;
    public int leg; //starts at 0 before you start the starting dialog, is 1 for the sword trip, 2 for the gun trip, etc.
    
    private Array<Dialog> dialogs;
    
    public Map(String levelFile) {
        targets = new Array<Target>();
        deathRects = new Array<Rectangle>();
        projectiles = new Array<Projectile>();
        checkpoints = new Array<Checkpoint>();
        signs = new Array<Sign>();
        particles = new Array<Particle>();
        leg = 0;
        
        tileMap = new TmxMapLoader().load(levelFile);
        collisionLayer = (TiledMapTileLayer) tileMap.getLayers().get("Collision Tile Layer");
        player = new Player(this, collisionLayer);
        
        pixelWidth = tileMap.getProperties().get("width", int.class) * tileMap.getProperties().get("tilewidth", int.class);
        pixelHeight = tileMap.getProperties().get("height", int.class) * tileMap.getProperties().get("tileheight", int.class);
        
        //this layer contains rectangles that kill you when they overlap you, meant for static hazards
        
        for (MapObject d: getLayerObjects("Death Layer")) {
            MapProperties p = d.getProperties();
            deathRects.add(new Rectangle(p.get("x",float.class),p.get("y",float.class),p.get("width",float.class),p.get("height",float.class)));
        }
    
        for (MapObject cp: getLayerObjects("Checkpoint Layer")) {
            MapProperties p = cp.getProperties();
            Checkpoint checkpoint = new Checkpoint(p.get("x",float.class),p.get("y",float.class));
            checkpoints.add(checkpoint);
        }
//        handleRectangleLayer("Checkpoint Layer", Checkpoint.class, checkpoints);
    
        for (MapObject t: getLayerObjects("Targets Layer")) {
            MapProperties p = t.getProperties();
            Target target = new Target(p.get("x", float.class), p.get("y", float.class));
            targets.add(target);      
            /* Debugging code to print out all properties of an object
            Iterator<String> keys = t.getProperties().getKeys();
            while (keys.hasNext()) {
                String property = keys.next();
                System.out.println(property + ": "+p.get(property));
            }*/
        }
    
        for (MapObject l: getLayerObjects("Locations Layer")) {
            MapProperties p = l.getProperties();
            String name = l.getName();
            if (name.equals("start")) {
                startPos = new Vector2(p.get("x", float.class),p.get("y", float.class));
                player.position.x = startPos.x;
                player.position.y = startPos.y;
            }
            else if (name.equals("startZone")) {
                startZone = new Rectangle(p.get("x",float.class),p.get("y",float.class),p.get("width",float.class),p.get("height",float.class));
            }
            else if (name.equals("finishZone")) {
                finishZone = new Rectangle(p.get("x",float.class),p.get("y",float.class),p.get("width",float.class),p.get("height",float.class));
            }
            else if (name.equals("startSage")) {
                sageStartPos = new Vector2(p.get("x", float.class),p.get("y", float.class));
            }
            else if (name.equals("endSage")) {
                sageEndPos = new Vector2(p.get("x", float.class),p.get("y", float.class));
            }
        }
    
        for (MapObject s: getLayerObjects("Sign Layer")) {
            MapProperties p = s.getProperties();
            String signText = p.get("text",String.class);
            Sign sign = new Sign(p.get("x", float.class), p.get("y", float.class), p.get("width", float.class), p.get("height", float.class), signText);
            signs.add(sign);
        }
    }
    
    private MapObjects getLayerObjects(String name) {
    	MapLayer layer = tileMap.getLayers().get(name);
    	if (layer!=null) {
    		return layer.getObjects();
    	}
    	else{
    		return new MapObjects();
    	}
    }
    
    private void handleRectangleLayer(String name, Class<?> c, Array<? extends Rectangle> array) {
    	MapObjects objects = getLayerObjects(name);
    	for(MapObject object: objects) {
    		MapProperties p = object.getProperties();
    		Object rect;
    		try {
				rect = c.getConstructor(float.class, float.class).newInstance(p.get("x", float.class), p.get("y", float.class));
				System.out.println(rect.getClass() + " " + array);
//				array.add(rect);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
//    		array.add(c.cast(rect));
    	}
    }
    
    public EnhancedCell getEnhancedCell(float x, float y) {
		int xCoord = (int) (x/collisionLayer.getTileWidth());
		int yCoord = (int) (y/collisionLayer.getTileHeight());
		Cell cell = collisionLayer.getCell(xCoord, yCoord);
		if(cell == null) {
			return null;
		}
		return new EnhancedCell(cell, xCoord, yCoord);		
	}
    
    public void checkDeathCollision() {
        for (Rectangle deathRect: deathRects) {
            if (player.position.overlaps(deathRect)) {
                killPlayer();
            }
        }
    }
    
    public void killPlayer() {
        player.playerHorizVelocity = 0;
        player.playerVertVelocity = 0;
        
        if (currCheckpoint==null) {
            player.position.x = startPos.x;
            player.position.y = startPos.y;
        }
        else {
            player.position.x = currCheckpoint.x;
            player.position.y = currCheckpoint.y;
        }
        
        for (Target t: targets) {
            t.exists = true;
        }
        
        for (Projectile p: projectiles) {
            p.destroy();
        }
    }
    
    public boolean advanceDialog() {
    	return true;
    }

    public void checkTargetHits() {
    	Array<Circle> hurtboxCircles = player.getHurtboxCircles();
    	Array<HurtboxRectangle> hurtboxRects = player.getHurtboxRects();
    	for (Target t: targets) {
    		boolean removeTarget = false;
    		for(Circle c: hurtboxCircles){
    			if (Intersector.overlaps(c, t) && t.exists)
    				removeTarget = true;
    		}
    		for(HurtboxRectangle r: hurtboxRects){
    			if (Intersector.overlaps(r, t) && t.exists) {
    				removeTarget = true;
    				removeProjectileGivenHurtbox(r);
    			}
    		}
    		if(removeTarget){
    			removeTarget(t);
				player.playerHasDoubleJump = true;
    		}
    	}
//    	
//    	
//    	
//        Array<Circle> allHurtboxes = player.getHurtboxCircles();
//        for (Target t: targets) {
//            for (Circle c: allHurtboxes) {
//                if (Intersector.overlaps(c, t) && t.exists) {
//                    removeTarget(t);
//                    player.playerHasDoubleJump = true;
//                }
//            }
//        }
    }
    
    public void checkCheckpointHits() {
    	Array<Circle> hurtboxCircles = player.getHurtboxCircles();
    	Array<HurtboxRectangle> hurtboxRects = player.getHurtboxRects();
    	for (Checkpoint cp: checkpoints) {
    		boolean newCheckpoint = false;
    		for(Circle c: hurtboxCircles){
    			if (Intersector.overlaps(c, cp) && currCheckpoint!=cp)
    				newCheckpoint = true;
    		}
    		for(HurtboxRectangle r: hurtboxRects){
    			//System.out.println(r);
    			if (Intersector.overlaps(r, cp) && currCheckpoint!=cp) {
    				newCheckpoint = true;
    				removeProjectileGivenHurtbox(r);
    			}
    		}
    		if(newCheckpoint) {
    			currCheckpoint = cp;
        		//Sound checkpointSound = Gdx.audio.newSound(Gdx.files.internal("checkpoint_hit.mp3"));
        		//checkpointSound.play();
        		//http://soundbible.com/1980-Swords-Collide.html
    		}
    	}
    	
//    	
//    	
//    	
//        Array<Circle> allHurtboxes = player.getHurtboxCircles();
//        for (Circle c: allHurtboxes) {
//            for (Checkpoint cp: checkpoints) {
//                if (Intersector.overlaps(c, cp) && currCheckpoint!=cp) {
//                    currCheckpoint = cp;
//            		//Sound checkpointSound = Gdx.audio.newSound(Gdx.files.internal("checkpoint_hit.mp3"));
//            		//checkpointSound.play();
//            		//http://soundbible.com/1980-Swords-Collide.html
//                }
//            }
//        }
    }
    
    public void checkSignHits() {
        for (Sign s: signs) {
            if (Intersector.overlaps(s, player.position))
                s.active = true;
            else
                s.active = false;
        }
    }
    
    public void checkLegFinished() {
        if (leg==0) {
            if (Intersector.overlaps(player.position, startZone) && player.playerState==PlayerState.GROUND) {
                System.out.println("leg 0 finished!");
                leg++;
            }
        }
        else if (leg==1) {
            if (Intersector.overlaps(player.position, finishZone) && player.playerState==PlayerState.GROUND) {
                System.out.println("leg 1 finished!");
                leg++;
            }
        }
    }
    
    public void removeProjectileGivenHurtbox(HurtboxRectangle r) {
        if (r.ownerProjectile!=null) {// hurtboxOwner should be the projectile that owns r
            // I'm just assuming it's a laser pulse
            // This is bad coding but we can probably do some instanceof checks if we have multiple projectile types
            LaserPulse laser = (LaserPulse) r.ownerProjectile; 
            laser.destroy();
        }
    }
    
    public void removeTarget(Target t) {
        t.exists = false;
        //probably want to increase score or play sound effects here as well
    }
    
    public void update() {
    	for(Projectile proj : projectiles){
    		proj.update();
    		if(getEnhancedCell(proj.head.x, proj.head.y) != null) {
    			proj.destroy();
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
    
    public boolean isGameFinished(){
    	if(targets.size == 0){
    		return false;
    	}
    	else{
    		return false;
    	}
    }
}
