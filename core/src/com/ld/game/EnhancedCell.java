package com.ld.game;

import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;

public class EnhancedCell {
	public Cell cell;
	
	// x and y are the coordinates of cell on the tilemap
	public int x;
	public int y;
	
	public EnhancedCell(Cell cell, int x, int y)
	{
		this.cell = cell;
		this.x = x;
		this.y = y;
	}
}
