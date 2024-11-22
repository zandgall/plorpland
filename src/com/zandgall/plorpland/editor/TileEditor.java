package com.zandgall.plorpland.editor;

import static org.lwjgl.glfw.GLFW.*;

import com.zandgall.plorpland.Camera;
import com.zandgall.plorpland.Main;
import com.zandgall.plorpland.graphics.G;
import com.zandgall.plorpland.graphics.Image;
import com.zandgall.plorpland.graphics.Shader;
import com.zandgall.plorpland.level.Tile;
import com.zandgall.plorpland.util.Hitbox;
import com.zandgall.plorpland.util.Hitrect;

public class TileEditor {	

	int tileX = 0, tileY = 0;

	public TileEditor() {

	}

	public void tick(Level lvl) {
		if(Main.keyEv[GLFW_KEY_LEFT])
			tileX--;
		if(Main.keyEv[GLFW_KEY_RIGHT])
			tileX++;
		if(Main.keyEv[GLFW_KEY_UP])
			tileY--;
		if(Main.keyEv[GLFW_KEY_DOWN])
			tileY++;
		if(Main.keys[GLFW_KEY_1])
			lvl.put(tileX, tileY, Tile.get(0));
		if(Main.keys[GLFW_KEY_2])
			lvl.put(tileX, tileY, Tile.get(1));
		if(Main.keys[GLFW_KEY_3])
			lvl.put(tileX, tileY, Tile.get(2));
		if(Main.keys[GLFW_KEY_4])
			lvl.put(tileX, tileY, Tile.get(3));
		if(Main.keys[GLFW_KEY_5])
			lvl.put(tileX, tileY, Tile.get(4));
		if(Main.keys[GLFW_KEY_6])
			lvl.put(tileX, tileY, Tile.get(5));
		Main.getCamera().target(tileX+0.5, tileY+0.5, LevelEditor.ZOOM);
		Main.getCamera().tick();
	}

	public void render(Level lvl) {
		lvl.renderSpecialImages();
		lvl.renderGraphics();
		lvl.renderEntities();
		lvl.renderTiles();
		
		Shader.Color.use().push().drawToWorld()
			.color(1, 1, 1).alpha(0.1)
			.setModel(tileX, tileY, 1.0, 1.0);
		G.drawSquare();
		Shader.Color.use().pop();
	}

	public void switchTo() {
		tileX = (int)Math.floor(Main.getCamera().getX());
		tileY = (int)Math.floor(Main.getCamera().getY());
	}
}
