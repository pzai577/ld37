package com.ld.game;

import java.util.Iterator;

import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.utils.Array;

public class Map {
    public TiledMap tileMap;
    public TiledMapTileLayer collisionLayer;
    public Player player;
    public Array<Target> targets;
    
    public Map(String levelFile) {
        tileMap = new TmxMapLoader().load(levelFile);
        collisionLayer = (TiledMapTileLayer) tileMap.getLayers().get("Collision Tile Layer");
        
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

    public void update() {
        player.updateState();
    }
}
