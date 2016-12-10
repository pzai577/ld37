package com.ld.game;

import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;

public class Map {
    public TiledMap tileMap;
    public TiledMapTileLayer collisionLayer;
    public Player player;
    
    public Map(String levelFile) {
        tileMap = new TmxMapLoader().load(levelFile);
        collisionLayer = (TiledMapTileLayer) tileMap.getLayers().get("Collision Tile Layer");
        System.out.println("layer name: " + collisionLayer.getName());

        player = new Player(collisionLayer);
    }

    public void update() {
        player.updateState();
    }
}
