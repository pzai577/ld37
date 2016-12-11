package com.ld.game;

import java.util.Iterator;

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
    public Array<Checkpoint> checkpoints;
    public Checkpoint currCheckpoint;
    
    public Map(String levelFile) {
        targets = new Array<Target>();
        deathRects = new Array<Rectangle>();
        checkpoints = new Array<Checkpoint>();
        
        tileMap = new TmxMapLoader().load(levelFile);
        collisionLayer = (TiledMapTileLayer) tileMap.getLayers().get("Collision Tile Layer");
        player = new Player(collisionLayer);
        
        pixelWidth = tileMap.getProperties().get("width", int.class) * tileMap.getProperties().get("tilewidth", int.class);
        pixelHeight = tileMap.getProperties().get("height", int.class) * tileMap.getProperties().get("tileheight", int.class);
        
        //this layer contains rectangles that kill you when they overlap you, meant for static hazards
        MapLayer deathLayer = tileMap.getLayers().get("Death Layer");
        if (deathLayer!=null) {
            MapObjects deathObjects = deathLayer.getObjects();
            for (MapObject d: deathObjects) {
                MapProperties p = d.getProperties();
                deathRects.add(new Rectangle(p.get("x",float.class),p.get("y",float.class),p.get("width",float.class),p.get("height",float.class)));
            }
        }
        
        MapLayer cpLayer = tileMap.getLayers().get("Checkpoint Layer");
        if (cpLayer!=null) {
            MapObjects cpObjects = cpLayer.getObjects();
            for (MapObject cp: cpObjects) {
                MapProperties p = cp.getProperties();
                Checkpoint checkpoint = new Checkpoint(p.get("x",float.class),p.get("y",float.class));
                checkpoints.add(checkpoint);
            }
        }
        
        MapLayer targetLayer = tileMap.getLayers().get("Targets");
        if (targetLayer!=null) {
            MapObjects targetObjects = targetLayer.getObjects();
            for (MapObject t: targetObjects) {
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
        }
        
        MapLayer locsLayer = tileMap.getLayers().get("Locations Layer");
        if (locsLayer!=null) {
            MapObjects locObjects = locsLayer.getObjects();
            for (MapObject l: locObjects) {
                MapProperties p = l.getProperties();
                String name = l.getName();
                if (name.equals("start")) {
                    startPos = new Vector2(p.get("x", float.class),p.get("y", float.class));
                    player.position.x = startPos.x;
                    player.position.y = startPos.y;
                }
            }
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
                }
            }
        }
    }
    
    public void removeTarget(Target t) {
        t.exists = false;
        //probably want to increase score or play sound effects here as well
    }
    
    public void update() {
        player.updateState();
        checkTargetHits();
        checkCpHits();
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
