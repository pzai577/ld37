package com.ld.game;

import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.utils.Array;

public class Map {
    public TiledMap tileMap;
    public TiledMapTileLayer collisionLayer;
    
    public float pixelWidth, pixelHeight;
    
    public Player player;
    public Array<Target> targets;
    
    public Map(String levelFile) {
        tileMap = new TmxMapLoader().load(levelFile);
        collisionLayer = (TiledMapTileLayer) tileMap.getLayers().get("Collision Tile Layer");
        
        pixelWidth = tileMap.getProperties().get("width", int.class) * tileMap.getProperties().get("tilewidth", int.class);
        pixelHeight = tileMap.getProperties().get("height", int.class) * tileMap.getProperties().get("tileheight", int.class);
        
        MapObjects targetObjects = tileMap.getLayers().get("Targets").getObjects();
        targets = new Array<Target>();
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
        player = new Player(collisionLayer);
    }

    public void checkTargetHits() {
        Array<Circle> allHurtboxes = new Array<Circle>();
        //convert hurtboxes to circles for Intersector
        for (Hurtbox hb: player.getActiveHurtboxes()) {
            allHurtboxes.add(new Circle(player.getX()+hb.x, player.getY()+hb.y, hb.radius));
        }
        for (Target t: targets) {
            for (Circle c: allHurtboxes) {
                if (Intersector.overlaps(c, t.rect)) {
                    removeTarget(t);
                    player.playerHasDoubleJump = true;
                }
            }
        }
    }
    
    public void removeTarget(Target t) {
        targets.removeValue(t, true);
        //probably want to increase score or play sound effects here as well
    }
    
    public void update() {
        player.updateState();
        checkTargetHits();
    }
    
    public boolean isGameFinished(){
    	if(targets.size == 0){
    		return true;
    	}
    	else{
    		return false;
    	}
    }
}
