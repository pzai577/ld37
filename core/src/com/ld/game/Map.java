package com.ld.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
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
    public Array<Target> targets;
    public Array<Rectangle> deathRects;
    public Array<Projectile> projectiles;
    public Array<Checkpoint> checkpoints;
    public Array<Sign> signs;
    public Array<Particle> particles;
    public Checkpoint currCheckpoint;
    
    public Map(String levelFile) {
        targets = new Array<Target>();
        deathRects = new Array<Rectangle>();
        projectiles = new Array<Projectile>();
        checkpoints = new Array<Checkpoint>();
        signs = new Array<Sign>();
        particles = new Array<Particle>();
        
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
    }

    public void checkTargetHits() {
        Array<Circle> allHurtboxes = player.getHurtboxCircles();
        for (Target t: targets) {
            for (Circle c: allHurtboxes) {
                if (Intersector.overlaps(c, t) && t.exists) {
                    removeTarget(t);
                    player.playerHasDoubleJump = true;
                }
            }
        }
    }
    
    public void checkCpHits() {
        Array<Circle> allHurtboxes = player.getHurtboxCircles();
        for (Circle c: allHurtboxes) {
            for (Checkpoint cp: checkpoints) {
                if (Intersector.overlaps(c, cp) && currCheckpoint!=cp) {
                    currCheckpoint = cp;
            		Sound checkpointSound = Gdx.audio.newSound(Gdx.files.internal("checkpoint_hit.mp3"));
            		checkpointSound.play();
            		//http://soundbible.com/1980-Swords-Collide.html
                }
            }
        }
    }
    
    public void checkSignHits() {
        for (Sign s: signs) {
            if (Intersector.overlaps(s, player.position))
                s.active = true;
            else
                s.active = false;
        }
    }
    
    public void removeTarget(Target t) {
        t.exists = false;
        //probably want to increase score or play sound effects here as well
    }
    
    public void update() {
    	for(Projectile proj : projectiles){
    		proj.update();
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
        checkCpHits();
        checkSignHits();
        checkDeathCollision();
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
